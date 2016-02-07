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

import java.util.List;
import java.util.UUID;

import nz.co.senanque.process.instances.TaskBase;
import nz.co.senanque.workflow.instances.Attachment;
import nz.co.senanque.workflow.instances.DeferredEvent;
import nz.co.senanque.workflow.instances.ProcessInstance;

/**
 * @author Roger Parkinson
 *
 */
public interface WorkflowDAO {

	public ProcessInstance findProcessInstance(Long id);
	public ProcessInstance refreshProcessInstance(ProcessInstance processInstance);
	public ProcessInstance mergeProcessInstance(ProcessInstance processInstance);
	public List<ProcessInstance> getActiveProcesses();
	public List<DeferredEvent> getDeferredEvents();
	public DeferredEvent removeDeferredEvent(ProcessInstance processInstance, TaskBase task);
	public int clearDeferredEvents();
	public void flush();
	public DeferredEvent findDeferredEvent(long id);
	public List<ProcessInstance> getAllProcesses();
	public void addAttachment(Attachment attachment);
}