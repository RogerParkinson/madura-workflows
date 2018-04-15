/*******************************************************************************
 * Copyright (c)2014 Prometheus Consulting
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package nz.co.senanque.workflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.ws.Endpoint;

import nz.co.senanque.validationengine.ValidationEngine;
import nz.co.senanque.validationengine.ValidationSession;
import nz.co.senanque.workflow.instances.Audit;
import nz.co.senanque.workflow.instances.ProcessInstance;
import nz.co.senanque.workflowtest.instances.Order;
import nz.co.senanque.ws.serverimpl.MyServiceImpl;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This test will fail if you aren't online.
 * @author Roger Parkinson
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class WorkflowWithRules2Test {
	
	private static int WAIT_TIME = 20000;
	private static Endpoint s_endpoint;

	private static final Logger log = LoggerFactory
			.getLogger(WorkflowWithRules2Test.class);
	@Autowired
	ApplicationContext m_applicationContext;
	@Autowired
	private transient WorkflowClient m_workflowLauncher;
	@Autowired
	private transient Executor m_executor;
	@Autowired
	private transient WorkflowDAO m_workflowDAO;
    @Autowired @Qualifier("validationEngine1")
    private transient ValidationEngine m_validationEngine;
    @Autowired @Qualifier("validationEngine2")
    private transient ValidationEngine m_validationEngine2;

	// The following is needed to ensure the destroy methods of all the beans
	// including those in the bundles, are called correctly
    private static AbstractApplicationContext s_applicationContext;
    @PostConstruct
    public void init() {
    	s_applicationContext = (AbstractApplicationContext)m_applicationContext;
    }
    @BeforeClass
    public static void start() {
    	s_endpoint = Endpoint.publish("http://localhost:8080/WS/MyService",new MyServiceImpl());
    }
	@AfterClass
	public static void destroy() {
		if (s_applicationContext != null) {
			s_applicationContext.close();
		}
		s_endpoint.stop();
	}
    /**
     * This test should run through the workflow in the simplest path.
     * It sends a message to the temp converter service so we need to be on line
     * or it will fail. 
     * @throws Exception
     */
    @Test
	public void testRetry() throws Exception {
		Order order = new Order();
		order.setOrderName("OK");
		order.setFahrenheit("90");
		
        ValidationSession validationSession = m_validationEngine.createSession();
        validationSession.bind(order);
        validationSession.close();

		ProcessInstance processInstance = m_workflowLauncher.launch("testRetry", order, "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();
		Thread.sleep(WAIT_TIME);
		executeAllActiveProcessInstances();
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
//		log.debug("{}",printAuditTrail(processInstance.getAudits()));
		// This will fail if you are offline because the value is then 10
//		assertEquals(7,processInstance.getAudits().size());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
	}
	
	/**
	 * This test sends an invalid temperature to the web service so the response
	 * is a failure and it has to fall back on calculating it.
	 * We have to be on-line for the test to pass.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRetryFatal() throws Exception {
		Order order = new Order();
		order.setOrderName("OK");
		order.setFahrenheit("@"); // deliberate error
		order.setOrderName("fatal error");
		
        ValidationSession validationSession = m_validationEngine.createSession();
        validationSession.bind(order);
        validationSession.close();

		ProcessInstance processInstance = m_workflowLauncher.launch("testRetry", order, "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();
		Thread.sleep(WAIT_TIME);
		executeAllActiveProcessInstances();
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
//		log.debug("{}",printAuditTrail(processInstance.getAudits()));
		assertEquals(10,processInstance.getAudits().size());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
	}
	
//	@Test
//	public void testRetryRecoverable() throws Exception {
//		Order order = new Order();
//		order.setOrderName("OK");
//		order.setFahrenheit("@");
//		
//        ValidationSession validationSession = m_validationEngine.createSession();
//        validationSession.bind(order);
//        validationSession.close();
//
//		ProcessInstance processInstance = m_workflowLauncher.launch("testRetry", order, "hello");
//		assertTrue(processInstance.getId()>-1);
//		executeAllActiveProcessInstances();
//		Thread.sleep(WAIT_TIME);
//		executeAllActiveProcessInstances();
//		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
//		// Change to 10 if you force an abort.
//		// This will fail if you are offline because the value is then 7
//		log.debug("{}",printAuditTrail(processInstance.getAudits()));
//		assertEquals(10,processInstance.getAudits().size());
//		assertEquals(0,m_workflowDAO.clearDeferredEvents());
//	}
	
	private void executeAllActiveProcessInstances() {
		executeAllActiveProcessInstances(false);
	}
	private void executeAllActiveProcessInstances(boolean dump) {
		List<ProcessInstance> p = m_workflowDAO.getActiveProcesses();
		while (!p.isEmpty()) {
			for (ProcessInstance pi: p) {
				m_executor.execute(pi);
				m_workflowDAO.refreshProcessInstance(pi);
			}
			p = m_workflowDAO.getActiveProcesses();
		}		
	}
	private String printAuditTrail(List<Audit> audits) {
		StringBuilder sb = new StringBuilder("\n");
		for (Audit audit: audits) {
			sb.append("\t");
			sb.append(audit.getStatus());
			sb.append(" ");
			sb.append(audit.getComment());
			sb.append(" task Id: ");
			sb.append(audit.getTaskId());
			sb.append("\n");
		}
		return sb.toString();
	}

}
