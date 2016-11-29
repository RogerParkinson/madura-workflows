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
package nz.co.senanque.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.annotation.PostConstruct;

import nz.co.senanque.forms.WorkflowForm;
import nz.co.senanque.madura.bundle.BundleManager;
import nz.co.senanque.workflow.Executor;
import nz.co.senanque.workflow.WorkflowClient;
import nz.co.senanque.workflow.WorkflowDAO;
import nz.co.senanque.workflow.WorkflowManager;
import nz.co.senanque.workflow.instances.Audit;
import nz.co.senanque.workflow.instances.ProcessInstance;
import nz.co.senanque.workflow.instances.TaskStatus;

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
import org.springframework.transaction.annotation.Transactional;

/**
 * This test runs two workflow processes: Process1 and Process2.
 * The processes are all inside the workflow1 bundle and this is located automatically
 * using the bundle selector (nz.co.senanque.database.Bundle2Selector).
 * It illustrates that the jacket program, ie the test, does not need to know much at all
 * about the actual processes. All the information about the flow, compute tasks, forms
 * and context objects is held in the bundle.
 * 
 * @author Roger Parkinson
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
//@Transactional
public class OrderWorkflowTest {
	
	private static final Logger log = LoggerFactory
			.getLogger(OrderWorkflowTest.class);

	@Autowired BundleManager m_bundleManager;
	@Autowired ApplicationContext applicationContext;
	@Autowired WorkflowClient workflowClient;
	@Autowired WorkflowDAO workflowDAO;
	@Autowired WorkflowManager workflowManager;
	@Autowired DatabaseLoadDAO m_databaseLoadDAO;
	@Autowired Executor m_executor;

	private TaskStatus m_lastStatus;
	
	// The following is needed to ensure the destroy methods of all the beans
	// including those in the bundles, are called correctly
	private static BundleManager s_bundleManager;
    private static AbstractApplicationContext s_applicationContext;
    @PostConstruct
    public void init() {
    	s_bundleManager = m_bundleManager;
    	s_applicationContext = (AbstractApplicationContext)applicationContext;
    }
	@AfterClass
	public static void destroy() {
		s_bundleManager.shutdown();
		s_applicationContext.close();
	}

	@Test
	public void testProcess1() throws Exception {
		
        m_databaseLoadDAO.clear();
        WorkflowForm launchForm = workflowClient.getLaunchForm("Process1");
        assertNotNull(launchForm.getContext());
        long pid = workflowClient.launch(launchForm);
        // At this point the process instance should be launched and waiting to execute its first task
        m_executor.activeProcesses();
        // Now the process instance should be Waiting in a queue on a form
        ProcessInstance processInstance = workflowDAO.findProcessInstance(pid);
        assertEquals("Q1",processInstance.getQueueName());
        
        WorkflowForm currentForm = workflowClient.getCurrentForm(processInstance);
        assertNotNull(currentForm);
        assertNotNull(currentForm.getContext());
        assertNotNull(currentForm.getProcessInstance());
        processInstance.setStatus(TaskStatus.GO);
        workflowClient.save(currentForm);
        
        m_executor.activeProcesses();
        processInstance = workflowDAO.findProcessInstance(pid);
        assertEquals(TaskStatus.DONE,processInstance.getStatus());
        assertEquals(8,processInstance.getAudits().size());
	}
	@Test
	public void testProcess2() throws Exception {
		
        WorkflowForm launchForm = workflowClient.getLaunchForm("Process2");
        assertNotNull(launchForm.getContext());

        long pid = workflowClient.launch(launchForm);
        ProcessInstance processInstance = null;

        // At this point the process instance should be launched and waiting to execute its first task
        waitForStatus(pid, new TaskStatus[]{TaskStatus.GO,TaskStatus.DONE});
        if (m_lastStatus.equals(TaskStatus.DONE)) {
        	// This happens if the message failed and that aborted the process
            processInstance = workflowDAO.findProcessInstance(pid);
            assertEquals(8,processInstance.getAudits().size());
            for (Audit audit : processInstance.getAudits()) {
            	log.info("audit: {}",audit.toString());
            }
        } else {
	        // Successful message. Now the process instance should be Waiting in a queue on a form
	        m_executor.activeProcesses();
	        processInstance = workflowDAO.findProcessInstance(pid);
	
	        assertEquals("Q1",processInstance.getQueueName());
	        WorkflowForm currentForm = workflowClient.getCurrentForm(processInstance);
	        assertNotNull(currentForm);
	        assertNotNull(currentForm.getContext());
	        assertNotNull(currentForm.getProcessInstance());
	        assertFalse(currentForm.isLauncher());
	        processInstance.setStatus(TaskStatus.GO);
	        workflowClient.save(currentForm);
	        
	        m_executor.activeProcesses();
	        waitForStatus(pid, new TaskStatus[]{TaskStatus.DONE});
	        processInstance = workflowDAO.findProcessInstance(pid);
	        assertEquals(TaskStatus.DONE,m_lastStatus);
	        assertEquals(9,processInstance.getAudits().size());
        }
	}
	private void waitForStatus(long pid, TaskStatus[] taskStatus) throws Exception {
        do {
        	m_executor.activeProcesses();
        	Thread.sleep(5000);
        } while (!checkStatus(pid,taskStatus));
	}
	private boolean checkStatus(long pid, TaskStatus[] taskStatuses) throws Exception {
    	ProcessInstance processInstance = workflowDAO.findProcessInstance(pid);
    	processInstance = workflowDAO.refreshProcessInstance(processInstance);
    	log.debug("checking {} status: {}",processInstance.getId(),processInstance.getStatus());
    	for (TaskStatus ts: taskStatuses) {
    		if (processInstance.getStatus().equals(ts)) {
    			m_lastStatus = ts;
    			return true;
    		}
    	}
    	return false;
	}

}
