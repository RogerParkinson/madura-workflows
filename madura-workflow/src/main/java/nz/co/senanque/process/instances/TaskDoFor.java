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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nz.co.senanque.schemaparser.FieldDescriptor;
import nz.co.senanque.workflow.ProcessInstanceUtils;
import nz.co.senanque.workflow.WorkflowManager;
import nz.co.senanque.workflow.instances.ProcessInstance;

/**
 * @author Roger Parkinson
 *
 */
public class TaskDoFor extends TaskDo {

	private static final Logger log = LoggerFactory
			.getLogger(TaskDoFor.class);
	/**
	 * @param subProcess
	 * @param fd
	 * @param processDefinition
	 */
	public TaskDoFor(ProcessDefinition subProcess, FieldDescriptor fd,
			ProcessDefinition processDefinition) {
		super(subProcess, fd, processDefinition);
	}

	public boolean execute(ProcessInstance processInstance) {
		log.debug("{}",this);
		WorkflowManager workflowManager = getOwnerProcess().getWorkflowManager();
		List<?> list = (List<?>)workflowManager.getField(processInstance, getFd());
		if (!list.isEmpty())
		{
			for (Object o:list)
			{
				ProcessInstance subProcess = new ProcessInstance();
				getSubProcess().startProcess(subProcess);
				processInstance.getChildProcesses().add(subProcess);
				subProcess.setObjectInstance(workflowManager.createContextDescriptor(o));
				subProcess.setParentProcess(processInstance);
				log.debug("subprocess launched: {}",subProcess.getProcessDefinitionName());
			}
			ProcessInstanceUtils.clearQueue(processInstance);
			processInstance.setWaitCount(list.size());
			log.debug("{} subprocesses launched for: {} {}",processInstance.getWaitCount(),processInstance.getProcessDefinitionName(),processInstance.getId());

		}
		return false;
	}
}
