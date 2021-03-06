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

import nz.co.senanque.workflow.instances.ProcessInstance;
import nz.co.senanque.workflowtest.instances.Order;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Roger Parkinson
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class WorkflowRetryTest {

	private static final Logger log = LoggerFactory
			.getLogger(WorkflowRetryTest.class);
	@Autowired
	ApplicationContext m_applicationContext;
	@Autowired
	private transient WorkflowClient m_workflowLauncher;
	@Autowired
	private transient Executor m_executor;
	@Autowired
	private transient WorkflowDAO m_workflowDAO;

	// The following is needed to ensure the destroy methods of all the beans
	// including those in the bundles, are called correctly
    private static AbstractApplicationContext s_applicationContext;
    @PostConstruct
    public void init() {
    	s_applicationContext = (AbstractApplicationContext)m_applicationContext;
    }
	@AfterClass
	public static void destroy() {
		s_applicationContext.close();
	}
    @Test
	public void testRetry() throws Exception {
		Order order = new Order();
		order.setOrderName("OK");
		order.setFahrenheit("90");
		
		ProcessInstance processInstance = m_workflowLauncher.launch("testRetry", order, "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();
		Thread.sleep(10000);
		executeAllActiveProcessInstances();
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(7,processInstance.getAudits().size());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
	}
	
	@Test
	public void testRetryFatal() throws Exception {
		Order order = new Order();
		order.setOrderName("OK");
		order.setFahrenheit("@"); // deliberate error
		order.setOrderName("fatal error");
		ProcessInstance processInstance = m_workflowLauncher.launch("testRetry", order, "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();
		Thread.sleep(10000);
		executeAllActiveProcessInstances();
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(10,processInstance.getAudits().size());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
	}
	
	@Test
	public void testRetryRecoverable() throws Exception {
		Order order = new Order();
		order.setOrderName("OK");
		order.setFahrenheit("90");
		ProcessInstance processInstance = m_workflowLauncher.launch("testRetry", order, "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();
		Thread.sleep(10000);
		executeAllActiveProcessInstances();
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		// Change to 10 if you force an abort.
		assertEquals(7,processInstance.getAudits().size());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
	}
	
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
}
