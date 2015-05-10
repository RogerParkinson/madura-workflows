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
package nz.co.senanque.process.instances;

import java.util.Date;

import nz.co.senanque.workflow.RetryException;
import nz.co.senanque.workflow.WorkflowException;
import nz.co.senanque.workflow.instances.DeferredEvent;
import nz.co.senanque.workflow.instances.EventType;
import nz.co.senanque.workflow.instances.ProcessInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Find the last try task and set it up to run again from the top of the handler.
 * @author Roger Parkinson
 *
 */
public class TaskRetry extends TaskBase {

	private static final Logger log = LoggerFactory
			.getLogger(TaskRetry.class);
	public TaskRetry(ProcessDefinition ownerProcess) {
		super(ownerProcess);
	}

	public boolean execute(ProcessInstance processInstance) {
		log.debug("{}",this);
		DeferredEvent oldDeferredEvent = processInstance.getDeferredEvent();
		if (oldDeferredEvent != null) {
			// If this is a retry of a block with a timeout we need to restore the
			// deferred event for the timeout.
			TaskBase tb = getTask(processInstance.getDeferredEvent());
			if (tb instanceof TaskTry) {
				TaskTry taskTry = (TaskTry) tb;
				DeferredEvent deferredEvent = new DeferredEvent();
				deferredEvent.setProcessDefinitionName(taskTry.getProcessDefinitionName());
				deferredEvent.setTaskId(taskTry.getTaskId());
				deferredEvent.setProcessInstance(processInstance);
				long now = new Date().getTime();
				deferredEvent.setCreated(now);
				deferredEvent.setEventType(EventType.DEFERRED);
				deferredEvent.setFire(now+taskTry.getTimeoutValue());
				deferredEvent.setComment(trimComment(taskTry.toString()));
				processInstance.getDeferredEvents().add(deferredEvent);
				processInstance.setDeferredEvent(deferredEvent);
				log.debug("recreating new deferred event for timeout: fire: {}",deferredEvent.getFire());
			}
		}
		TaskBase interrupted = findInterruptedTask(processInstance);
		if (interrupted == null) {
			throw new WorkflowException("Trying to retry a task when there is none");
		}
		TaskBase task = interrupted.loadTask(processInstance);
		log.debug("retrying task {}",task);
		throw new RetryException();
	}


}
