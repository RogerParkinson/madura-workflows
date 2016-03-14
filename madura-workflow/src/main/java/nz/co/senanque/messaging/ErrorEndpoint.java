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
package nz.co.senanque.messaging;

import java.util.List;
import java.util.concurrent.locks.Lock;

import nz.co.senanque.locking.LockAction;
import nz.co.senanque.locking.LockFactory;
import nz.co.senanque.locking.LockTemplate;
import nz.co.senanque.workflow.BundleSelector;
import nz.co.senanque.workflow.ContextUtils;
import nz.co.senanque.workflow.WorkflowDAO;
import nz.co.senanque.workflow.WorkflowException;
import nz.co.senanque.workflow.WorkflowManager;
import nz.co.senanque.workflow.instances.ProcessInstance;
import nz.co.senanque.workflow.instances.TaskStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;

/**
 * We land here if there is some kind of error so the goal here is to pull the error from the
 * message and tell the relevant process to abort. Of course the process may choose to trap and
 * handle the abort, so this is not final. But if it does that it is not our concern here.
 * 
 * @author Roger Parkinson
 * 
 */
public class ErrorEndpoint implements MessageMapper {

	private static final Logger log = LoggerFactory
			.getLogger(ErrorEndpoint.class);

	@Autowired
	private WorkflowManager m_workflowManager;
	@Autowired
	WorkflowDAO m_workflowDAO;
	@Autowired
	private LockFactory m_lockFactory;
	@Autowired
	private BundleSelector m_bundleSelector;

	public void processErrorMessage(final Message<MessagingException> message) {
		MessageHeaders messageHeaders = message.getHeaders();
		final MessagingException messagingException = (MessagingException)message.getPayload();
		Long correlationId = message.getHeaders().get(IntegrationMessageHeaderAccessor.CORRELATION_ID,Long.class);
		if (correlationId == null) {
			correlationId = messagingException.getFailedMessage().getHeaders().get(IntegrationMessageHeaderAccessor.CORRELATION_ID,Long.class);
		}
		log.debug("ProcessInstance: correlationId {}", correlationId);
		if (correlationId == null) {
			log.error("correlation Id is null");
			throw new WorkflowException("correlation Id is null");
		}
		final ProcessInstance processInstance = getWorkflowDAO().findProcessInstance(correlationId);
		if (processInstance == null) {
			throw new WorkflowException("Failed to find processInstance for "+correlationId);
		}
		getBundleSelector().selectBundle(processInstance);
		List<Lock> locks = ContextUtils.getLocks(processInstance,getLockFactory(),"nz.co.senanque.messaging.ErrorEndpoint.processErrorMessage");
		LockTemplate lockTemplate = new LockTemplate(locks, new LockAction() {
		
			public void doAction() {
				
				if (processInstance.getStatus() != TaskStatus.WAIT) {
					throw new WorkflowException("Process is not in a wait state");
				}
				getWorkflowManager().processMessage(processInstance, message, getMessageMapper());
			}

		});
		if (!lockTemplate.doAction()) {
			throw new WorkflowRetryableException("Failed to get a lock"); // This will be retried 
		}
	}

	private MessageMapper getMessageMapper() {
		return this;
	}
	
	public void unpackMessage(Message<?> message, Object context) {
		// this is always an error so extract the error message and throw an exception.
		final MessagingException messagingException = (MessagingException)message.getPayload();
		throw new WorkflowException(messagingException.getCause().getMessage());
	}

	public LockFactory getLockFactory() {
		return m_lockFactory;
	}

	public void setLockFactory(LockFactory lockFactory) {
		m_lockFactory = lockFactory;
	}

	public WorkflowDAO getWorkflowDAO() {
		return m_workflowDAO;
	}

	public void setWorkflowDAO(WorkflowDAO workflowDAO) {
		m_workflowDAO = workflowDAO;
	}

	public WorkflowManager getWorkflowManager() {
		return m_workflowManager;
	}

	public void setWorkflowManager(WorkflowManager workflowManager) {
		m_workflowManager = workflowManager;
	}

	public BundleSelector getBundleSelector() {
		return m_bundleSelector;
	}

	public void setBundleSelector(BundleSelector bundleSelector) {
		m_bundleSelector = bundleSelector;
	}
	
}
