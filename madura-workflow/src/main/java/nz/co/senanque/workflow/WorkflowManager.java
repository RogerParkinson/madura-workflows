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
import java.util.List;
import java.util.Set;

import nz.co.senanque.forms.WorkflowForm;
import nz.co.senanque.messaging.MessageMapper;
import nz.co.senanque.messaging.MessageSender;
import nz.co.senanque.process.instances.ComputeType;
import nz.co.senanque.process.instances.ProcessDefinition;
import nz.co.senanque.process.instances.QueueDefinition;
import nz.co.senanque.process.instances.TaskBase;
import nz.co.senanque.process.instances.TimeoutProvider;
import nz.co.senanque.schemaparser.FieldDescriptor;
import nz.co.senanque.validationengine.ValidationEngine;
import nz.co.senanque.workflow.instances.Audit;
import nz.co.senanque.workflow.instances.DeferredEvent;
import nz.co.senanque.workflow.instances.ProcessInstance;

import org.springframework.integration.Message;

public interface WorkflowManager {

	public Set<ProcessDefinition> getAllProcesses();
	public Set<ProcessDefinition> getMainProcesses();
	public MessageSender<?> getMessage(String messageName);
	public ProcessDefinition getProcessDefinition(String name);
	public TaskBase getTask(ProcessDefinition pd, long taskId);
	public TaskBase getCurrentTask(ProcessInstance processInstance);
	public TaskBase getTask(String processDefinitionName, Long taskid);
	public Object getField(ProcessInstance processInstance,
			FieldDescriptor fd);
	public List<Audit> findHandlerTasks(
			ProcessInstance processInstance);
	public TaskBase getTask(DeferredEvent deferredEvent);
	public TaskBase findInterruptedTask(ProcessInstance processInstance);
	public void addMainProcess(ProcessDefinition processDefinition);
	public void addSubProcess(ProcessDefinition processDefinition);
	public ProcessInstance launch(String processName, Object o, String comment, String bundleName);
	public void execute(ProcessInstance processInstance);
	public void execute(long id);
	public Set<QueueDefinition> getQueues();
	public QueueDefinition getQueue(String queueName);
	public void executeDeferredEvent(long id);
	public ComputeType<?> getComputeType(String computeName);
	public TimeoutProvider getTimeoutProvider(String timeoutProviderName);
	public void processMessage(ProcessInstance processInstance,
			Message<?> message, MessageMapper messageMapper);
	public Object getContext(String objectInstance);
	public void mergeContext(Object context);
	public String createContextDescriptor(Object o);
	public void shutdown();
	public long save(WorkflowForm workflowForm);
	public WorkflowForm getLaunchForm(String processName);
	public WorkflowForm getCurrentForm(ProcessInstance processInstance);
	public long launch(WorkflowForm launchForm, String comment, String bundleName);
	public ValidationEngine getValidationEngine();
	public Collection<Audit> getAudits(ProcessInstance processInstance);
	public ProcessInstance refresh(ProcessInstance processInstance);
	public ProcessInstance lockProcessInstance(final ProcessInstance processInstance, final boolean techSupport, final String userName);
}