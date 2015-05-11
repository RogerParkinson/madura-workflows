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

import java.util.Collection;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import nz.co.senanque.forms.WorkflowForm;
import nz.co.senanque.messaging.MessageMapper;
import nz.co.senanque.messaging.MessageSender;
import nz.co.senanque.messaging.MessageSenderImpl;
import nz.co.senanque.process.instances.ComputeType;
import nz.co.senanque.process.instances.ProcessDefinition;
import nz.co.senanque.process.instances.TaskBase;
import nz.co.senanque.schemaparser.FieldDescriptor;
import nz.co.senanque.validationengine.ValidationEngine;
import nz.co.senanque.workflow.instances.Audit;
import nz.co.senanque.workflow.instances.ProcessInstance;
import nz.co.senanque.workflow.instances.TaskStatus;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;

/**
 * Used as a mock for the workflow manager. It is used in tests but it is also used by the plugin parser which
 * only needs to validate rather than actually run a process.
 * 
 * @author Roger Parkinson
 *
 */
public class WorkflowManagerMock extends WorkflowManagerAbstract {

	private static final Logger log = LoggerFactory
			.getLogger(WorkflowManagerMock.class);
	
	public WorkflowManagerMock() {
		
	}

	public WorkflowManagerMock(String messageNames, String computeNames) {
		if (messageNames != null) {
			StringTokenizer st = new StringTokenizer(messageNames, ",");
			MessageSender<String> ms = new MessageSender<String>(){

				@Override
				public boolean send(String graph, long correlationId) {
					return false;
				}};
			while (st.hasMoreTokens()) {
				getMessages().put(st.nextToken(), ms);
			}
		}
		if (computeNames != null) {
			ComputeType<String> ct = new ComputeType<String>(){

				@Override
				public void execute(ProcessInstance processInstance,
						String context, Map<String, String> map) {
				}};
			StringTokenizer st = new StringTokenizer(computeNames, ",");
			while (st.hasMoreTokens()) {
				this.getComputeTypes().put(st.nextToken(), ct);
			}
		}
	}

	@PostConstruct
	public void init() {
		findBeans();
	}
	@PreDestroy
	public void shutdown() {
	}

	@Override
	public Object getField(ProcessInstance processInstance, FieldDescriptor fd) {
		throw new NotImplementedException();
	}

	@Override
	public ProcessInstance launch(String processName, Object o, String comment,
			String bundleName) {
		ProcessDefinition processDefinition = getProcessDefinition(processName);
		if (processDefinition == null) {
			throw new WorkflowException("Failed to find process definition named "+processName);
		}
		ProcessInstance processInstance = new ProcessInstance();
		processInstance.setComment(comment);
		processInstance.setBundleName(bundleName);
		return processInstance;
	}

	@Override
	public void execute(long id) {
		
	}

	@Override
	public void executeDeferredEvent(long deferredEventId) {
	}

	@Override
	public void processMessage(ProcessInstance processInstance,
			Message<?> message, MessageMapper messageMapper) {
	}

	@Override
	public Object getContext(String objectInstance) {
		return null;
	}

	@Override
	public void mergeContext(Object context) {
	}

	@Override
	public String createContextDescriptor(Object o) {
		return null;
	}

	@Override
	protected void tickleParentProcess(ProcessInstance processInstance,
			TaskStatus status) {
	}

	@Override
	protected TaskBase endOfProcessDetected(ProcessInstance processInstance,
			Audit currentAudit) {
		return null;
	}

	@Override
	public long save(WorkflowForm workflowForm) {
		return 0;
	}
	@Override
	public WorkflowForm getLaunchForm(String processName) {
		return null;
	}
	@Override
	public WorkflowForm getCurrentForm(ProcessInstance processInstance) {
		return null;
	}
	@Override
	public long launch(WorkflowForm launchForm, String comment,
			String bundleName) {
		return 0;
	}
	@Override
	public ValidationEngine getValidationEngine() {
		return null;
	}
	@Override
	public Collection<Audit> getAudits(ProcessInstance processInstance) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public ProcessInstance refresh(ProcessInstance processInstance) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public ProcessInstance lockProcessInstance(ProcessInstance processInstance,
			boolean techSupport, String userName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void finishLaunch(long processId) {
		// TODO Auto-generated method stub
		
	}
}
