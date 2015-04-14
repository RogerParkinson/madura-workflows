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
import nz.co.senanque.workflow.instances.TaskStatus;
import nz.co.senanque.workflowtest.instances.Order;
import nz.co.senanque.workflowtest.instances.OrderItem;

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
public class WorkflowManagerImplTest {

	private static final Logger log = LoggerFactory
			.getLogger(WorkflowManagerImplTest.class);
	@Autowired
	ApplicationContext m_applicationContext;
	@Autowired
	WorkflowClient m_workflowLauncher;
	@Autowired
	Executor m_executor;
	@Autowired
	WorkflowDAO m_workflowDAO;
	@Autowired
	ContextDAO m_contextDAO;

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
	public void testWAITandGO() throws Exception {
		ProcessInstance processInstance = m_workflowLauncher.launch("testWAITandGO", new Order(), "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();

		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(3,processInstance.getAudits().size());
		assertEquals(TaskStatus.WAIT,processInstance.getStatus());
		// The process is in a WAIT state, waiting for external interaction
		// Changing the state to GO is all we need to make the task proceed
		processInstance.setStatus(TaskStatus.GO);
		m_workflowDAO.mergeProcessInstance(processInstance);
		executeAllActiveProcessInstances();
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(7,processInstance.getAudits().size());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
	}
	@Test
	public void testABORTandCatch() throws Exception {
		ProcessInstance processInstance = m_workflowLauncher.launch("testABORTandCatch", new Order(), "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(7,processInstance.getAudits().size());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
	}
	@Test
	public void testABORTandNoCatch() throws Exception {
		ProcessInstance processInstance = m_workflowLauncher.launch("testABORTandNoCatch", new Order(), "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(3,processInstance.getAudits().size());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
	}
	@Test
	public void testABORTandCatch2() throws Exception {
		ProcessInstance processInstance = m_workflowLauncher.launch("testABORTandCatch2", new Order(), "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(7,processInstance.getAudits().size());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
	}
	@Test
	public void testABORTandContinue() throws Exception {
		ProcessInstance processInstance = m_workflowLauncher.launch("testABORTandContinue", new Order(), "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(9,processInstance.getAudits().size());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
	}
	@Test
	public void testABORTandRetry() throws Exception {
		ProcessInstance processInstance = m_workflowLauncher.launch("testABORTandRetry", new Order(), "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		processInstance.setStatus(TaskStatus.ABORTING);
		m_workflowDAO.mergeProcessInstance(processInstance);
		executeAllActiveProcessInstances();
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		processInstance.setStatus(TaskStatus.GO);
		m_workflowDAO.mergeProcessInstance(processInstance);
		executeAllActiveProcessInstances();
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(11,processInstance.getAudits().size());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
	}
	@Test
	public void testTIMEOUTandRetry() throws Exception {
		ProcessInstance processInstance = m_workflowLauncher.launch("testTIMEOUTandRetry", new Order(), "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();
		Thread.sleep(3000); // Should trigger a timeout
		while (m_executor.deferredEvents()>0);
		
		executeAllActiveProcessInstances(); // process the timeout and retry
		
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		processInstance.setStatus(TaskStatus.GO);
		m_workflowDAO.mergeProcessInstance(processInstance);
		executeAllActiveProcessInstances();

		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(10,processInstance.getAudits().size());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
	}
	@Test
	public void testTIMEOUTandRetry2() throws Exception {
		ProcessInstance processInstance = m_workflowLauncher.launch("testTIMEOUTandRetry2", new Order(), "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances(); // Waiting for MyMessage.
		log.debug("Should be waiting for MyMessage");

		Thread.sleep(2000);
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		processInstance.setStatus(TaskStatus.GO);
		m_workflowDAO.mergeProcessInstance(processInstance);
		executeAllActiveProcessInstances(); // Waiting for ReserveStockForm.
		log.debug("Should be waiting for ReserveStockForm");

		m_executor.deferredEvents(); // Should trigger timeout from 1000 timeout	
		m_executor.deferredEvents(); // Should trigger timeout from 1000 timeout	
		executeAllActiveProcessInstances(); // process the timeout
		log.debug("Should have handled timeout=1000");

		int i = m_executor.deferredEvents(); // is there still an unused deferred event here?
		assertEquals(0,i);					 // should be no.
		executeAllActiveProcessInstances(); // process the timeout if it was there.
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(9,processInstance.getAudits().size());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
	}
	@Test
	public void testIf() throws Exception {
		OrderItem context = new OrderItem();
		context.setApproved(false);
		ProcessInstance processInstance = m_workflowLauncher.launch("testIf", context, "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(6,processInstance.getAudits().size());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
	}
	@Test
	public void testIfNot() throws Exception {
		OrderItem context = new OrderItem();
		context.setApproved(false);
		ProcessInstance processInstance = m_workflowLauncher.launch("testIfNot", context, "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(6,processInstance.getAudits().size());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
	}
	@Test
	public void testIf2() throws Exception {
		OrderItem context = new OrderItem();
		context.setApproved(true);
		ProcessInstance processInstance = m_workflowLauncher.launch("testIf", context, "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(6,processInstance.getAudits().size());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
	}
	@Test
	public void testIfelse() throws Exception {
		OrderItem context = new OrderItem();
		context.setApproved(true);
		ProcessInstance processInstance = m_workflowLauncher.launch("testIfElse", context, "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(6,processInstance.getAudits().size());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
	}
	@Test
	public void testIfelse2() throws Exception {
		OrderItem context = new OrderItem();
		context.setApproved(true);
		ProcessInstance processInstance = m_workflowLauncher.launch("testIfElse", context, "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(6,processInstance.getAudits().size());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
	}
	@Test
	public void testSubprocesses() throws Exception {
		Order context = new Order();
		context.getOrderItems().add(new OrderItem());
		context.getOrderItems().add(new OrderItem());
		ProcessInstance processInstance = m_workflowLauncher.launch("testSubprocesses", context, "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();
		
		List<ProcessInstance> p = m_workflowDAO.getActiveProcesses();
		while (!p.isEmpty()) {
			for (ProcessInstance pi: p) {
				m_executor.execute(pi);
			}
			p = m_workflowDAO.getActiveProcesses();
		}
		while (m_executor.deferredEvents()>0);
		executeAllActiveProcessInstances();
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(4,processInstance.getAudits().size());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
	}

	@Test
	public void testSubprocesses2() throws Exception {
		Order context = new Order();
		context.getOrderItems().add(new OrderItem());
		context.getOrderItems().add(new OrderItem());
		ProcessInstance processInstance = m_workflowLauncher.launch("testSubprocesses2", context, "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();
		
		while (m_executor.deferredEvents()>0);
		executeAllActiveProcessInstances();
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(4,processInstance.getAudits().size());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
	}

	@Test
	public void testSubprocesses3() throws Exception {
		Order context = new Order();
		context.getOrderItems().add(new OrderItem());
		context.getOrderItems().add(new OrderItem());
		ProcessInstance processInstance = m_workflowLauncher.launch("testSubprocesses3", context, "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();
		while (m_executor.deferredEvents()>0);
		executeAllActiveProcessInstances();
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(3,processInstance.getAudits().size());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
	}

	@Test
	public void testSubprocesses4() throws Exception {
		Order context = new Order();
		ProcessInstance processInstance = m_workflowLauncher.launch("MainProcess", context, "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();
		while (m_executor.deferredEvents()>0);
		executeAllActiveProcessInstances();
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(4,processInstance.getAudits().size());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
	}

	@Test
	public void testTimeout() throws Exception {
		Order context = new Order();
		ProcessInstance processInstance = m_workflowLauncher.launch("testTimeout", context, "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();
		while (m_executor.deferredEvents()>0);
		
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(3,processInstance.getAudits().size());
		assertTrue(TaskStatus.WAIT.equals(processInstance.getStatus())||TaskStatus.TIMEOUT.equals(processInstance.getStatus()));
		// The process is in a WAIT state, waiting for external interaction
		// Changing the state to GO is all we need to make the task proceed
		processInstance.setStatus(TaskStatus.GO);
		processInstance = m_workflowDAO.mergeProcessInstance(processInstance);
		executeAllActiveProcessInstances();
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(7,processInstance.getAudits().size());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
	}

	@Test
	public void testTimeout2() throws Exception {
		Order context = new Order();
		ProcessInstance processInstance = m_workflowLauncher.launch("testTimeout", context, "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();
		while (m_executor.deferredEvents()>0);
		
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(3,processInstance.getAudits().size());
		assertTrue(TaskStatus.WAIT.equals(processInstance.getStatus())||TaskStatus.TIMEOUT.equals(processInstance.getStatus()));
		// wait for a timeout
		Thread.sleep(200);
		while (m_executor.deferredEvents()>0);
		executeAllActiveProcessInstances(false);
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(9,processInstance.getAudits().size());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
	}
	
	@Test
	public void testComplex() throws Exception {
		Order context = new Order();
		context.setDecisionField(true);
		ProcessInstance processInstance = m_workflowLauncher.launch("testComplex", context, "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();
		while (m_executor.deferredEvents()>0);

		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		processInstance.setStatus(TaskStatus.GO);
		processInstance = m_workflowDAO.mergeProcessInstance(processInstance);
		executeAllActiveProcessInstances();
		
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(8,processInstance.getAudits().size());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
	}
	
	@Test
	public void testComplex1() throws Exception {
		Order context = new Order();
		context.setDecisionField(true);
		ProcessInstance processInstance = m_workflowLauncher.launch("testComplex", context, "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();
		Thread.sleep(100);

		while (m_executor.deferredEvents()>0);
		executeAllActiveProcessInstances();
		
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		processInstance.setStatus(TaskStatus.ABORTING);
		processInstance = m_workflowDAO.mergeProcessInstance(processInstance);
		executeAllActiveProcessInstances();
		
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(14,processInstance.getAudits().size());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
	}
	
	@Test
	public void testTwoTimers() throws Exception {
		Order context = new Order();
		ProcessInstance processInstance = m_workflowLauncher.launch("testTwoTimers", context, "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		processInstance.setStatus(TaskStatus.GO);
		processInstance = m_workflowDAO.mergeProcessInstance(processInstance);
		executeAllActiveProcessInstances();

		Thread.sleep(15);
		while (m_executor.deferredEvents()>0);
		executeAllActiveProcessInstances();
		
		Thread.sleep(150);
		while (m_executor.deferredEvents()>0);
		executeAllActiveProcessInstances();
		
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(9,processInstance.getAudits().size());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
	}
	
	@Test
	public void testDoWhile() throws Exception {
		Order context = new Order();
		context.setDecisionField(true);
		ProcessInstance processInstance = m_workflowLauncher.launch("testDoWhile", context, "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();
		while (m_executor.deferredEvents()>0);
		executeAllActiveProcessInstances();

		// Do the form once
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		ProcessInstance subProcess = processInstance.getChildProcesses().get(0);
		assertEquals(TaskStatus.WAIT,subProcess.getStatus());
		subProcess.setStatus(TaskStatus.GO);
		subProcess = m_workflowDAO.mergeProcessInstance(subProcess);
		executeAllActiveProcessInstances();
		while (m_executor.deferredEvents()>0);
		executeAllActiveProcessInstances();

		// Do the form again, this time change the flag so we exit the loop
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		subProcess = processInstance.getChildProcesses().get(1);
		subProcess = m_workflowDAO.refreshProcessInstance(subProcess);
		assertEquals(TaskStatus.WAIT,subProcess.getStatus());
		subProcess.setStatus(TaskStatus.GO);
		subProcess = m_workflowDAO.mergeProcessInstance(subProcess);
		Order order = (Order)m_contextDAO.getContext(processInstance.getObjectInstance());
		order.setDecisionField(false);
		m_contextDAO.mergeContext(order);
		executeAllActiveProcessInstances();
		while (m_executor.deferredEvents()>0);
		executeAllActiveProcessInstances();

		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(TaskStatus.DONE,processInstance.getStatus());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
	}
	
	@Test
	public void testDoUntil() throws Exception {
		Order context = new Order();
		context.setDecisionField(false);
		ProcessInstance processInstance = m_workflowLauncher.launch("testDoUntil", context, "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();
		while (m_executor.deferredEvents()>0);
		executeAllActiveProcessInstances();

		// Do the form once
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		ProcessInstance subProcess = processInstance.getChildProcesses().get(0);
		assertEquals(TaskStatus.WAIT,subProcess.getStatus());
		subProcess.setStatus(TaskStatus.GO);
		subProcess = m_workflowDAO.mergeProcessInstance(subProcess);
		executeAllActiveProcessInstances();
		while (m_executor.deferredEvents()>0);
		executeAllActiveProcessInstances();

		// Do the form again, this time change the flag so we exit the loop
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		subProcess = processInstance.getChildProcesses().get(1);
		subProcess = m_workflowDAO.refreshProcessInstance(subProcess);
		assertEquals(TaskStatus.WAIT,subProcess.getStatus());
		subProcess.setStatus(TaskStatus.GO);
		subProcess = m_workflowDAO.mergeProcessInstance(subProcess);
		Order order = (Order)m_contextDAO.getContext(processInstance.getObjectInstance());
		order.setDecisionField(true);
		m_contextDAO.mergeContext(order);
		executeAllActiveProcessInstances();
		while (m_executor.deferredEvents()>0);
		executeAllActiveProcessInstances();

		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		assertEquals(TaskStatus.DONE,processInstance.getStatus());
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
	public ContextDAO getContextDAO() {
		return m_contextDAO;
	}
	public void setContextDAO(ContextDAO contextDAO) {
		m_contextDAO = contextDAO;
	}

}
