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

import nz.co.senanque.workflow.ContinueException;
import nz.co.senanque.workflow.WorkflowException;
import nz.co.senanque.workflow.instances.ProcessInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Find the last try task and set it up to run the task following it
 * @author Roger Parkinson
 *
 */
public class TaskContinue extends TaskBase {

	private static final Logger log = LoggerFactory
			.getLogger(TaskCompute.class);
	public TaskContinue(ProcessDefinition ownerProcess) {
		super(ownerProcess);
	}

	public boolean execute(ProcessInstance processInstance) {
		log.debug("{}",this);
		TaskBase interrupted = findInterruptedTask(processInstance);
		if (interrupted == null) {
			throw new WorkflowException("Trying to continue from a task when there is none");
		}
		TaskBase task = interrupted.getNextTask(processInstance);
		task.loadTask(processInstance);
		log.debug("continuing task {}",task);
		throw new ContinueException();
	}
}
