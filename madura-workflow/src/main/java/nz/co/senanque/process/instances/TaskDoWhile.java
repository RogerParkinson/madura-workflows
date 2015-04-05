/*******************************************************************************
 * Copyright (c)13/04/2014 Prometheus Consulting
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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

import nz.co.senanque.schemaparser.FieldDescriptor;
import nz.co.senanque.workflow.instances.ProcessInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Roger Parkinson
 *
 */
public class TaskDoWhile extends TaskDo {

	private static final Logger log = LoggerFactory
			.getLogger(TaskDoWhile.class);
	/**
	 * @param subProcess
	 * @param fd
	 * @param processDefinition
	 */
	public TaskDoWhile(ProcessDefinition subProcess, FieldDescriptor fd,
			ProcessDefinition processDefinition) {
		super(subProcess, fd, processDefinition);
	}

	public boolean execute(ProcessInstance processInstance) {
		log.debug("{}",this);
		Boolean bool = (Boolean)getField(processInstance, getFd());
		if (bool) {
			launchOneCyclicSubprocess(processInstance);
			processInstance.setCyclic(true);
			return false;
		} else {
			processInstance.setCyclic(false);
			return true;
		}
	}
	
}
