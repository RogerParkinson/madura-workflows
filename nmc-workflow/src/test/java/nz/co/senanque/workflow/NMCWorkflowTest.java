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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.annotation.PostConstruct;

import nz.co.senanque.forms.WorkflowForm;
import nz.co.senanque.process.instances.TaskEnd;
import nz.co.senanque.process.instances.TaskForm;
import nz.co.senanque.validationengine.ValidationEngine;
import nz.co.senanque.validationengine.ValidationSession;
import nz.co.senanque.workflow.instances.Audit;
import nz.co.senanque.workflow.instances.ProcessInstance;
import nz.co.senanque.workflow.instances.TaskStatus;
import nz.co.senanque.workflow.nmcinstances.NMC;

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
public class NMCWorkflowTest {
	
	private static int WAIT_TIME = 20000;

	private static final Logger log = LoggerFactory
			.getLogger(NMCWorkflowTest.class);
	@Autowired
	ApplicationContext m_applicationContext;
	@Autowired
	private transient WorkflowClient m_workflowLauncher;
	@Autowired
	private transient Executor m_executor;
	@Autowired
	private transient WorkflowDAO m_workflowDAO;
    @Autowired 
    private transient ValidationEngine m_validationEngine;
	@Autowired transient WorkflowClient m_workflowClient;


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
    /**
     * This proves that the test runs through the simple case (there were funds and everything was okay)
     * as well as verifying that the rules associated with the NMC fired correctly.
     * @throws Exception
     */
    @Test
	public void testWorkflow() throws Exception {
		NMC nmc = new NMC();
		nmc.setClaimId("111");
        ValidationSession validationSession = m_validationEngine.createSession();

		ProcessInstance processInstance = m_workflowLauncher.launch("NMCProcess", nmc, "hello");
		assertTrue(processInstance.getId()>-1);
		executeAllActiveProcessInstances();
//		Thread.sleep(WAIT_TIME);
		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		TaskForm taskForm = (TaskForm)m_workflowLauncher.getWorkflowManager().getCurrentTask(processInstance);
		// We get here with formName=ReviewClaim
		assertEquals("ReviewClaim",taskForm.getFormName());
		// Now move on from the form
//		taskForm.execute(processInstance);
        WorkflowForm currentForm = m_workflowClient.getCurrentForm(processInstance);
        assertNotNull(currentForm);
        assertNotNull(currentForm.getContext());
        assertNotNull(currentForm.getProcessInstance());
        
        processInstance.setStatus(TaskStatus.GO);
        NMC context = (NMC)currentForm.getContext();
        context.setSameAmount(true);
        context.setSameClaim(true);
        assertTrue(!context.isLoop());
        m_workflowClient.save(currentForm);
        executeAllActiveProcessInstances();

		processInstance = m_workflowDAO.refreshProcessInstance(processInstance);
		// We will get an error here f the task has not ended.
		TaskEnd taskEnd = (TaskEnd)m_workflowLauncher.getWorkflowManager().getCurrentTask(processInstance);
		assertEquals(14,processInstance.getAudits().size());
		assertEquals(0,m_workflowDAO.clearDeferredEvents());
		
		validationSession.close();
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
