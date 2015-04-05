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

import nz.co.senanque.schemaparser.FieldDescriptor;
import nz.co.senanque.workflow.ProcessInstanceUtils;
import nz.co.senanque.workflow.instances.ProcessInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Roger Parkinson
 *
 */
abstract class TaskDo extends TaskBase {

	private static final Logger log = LoggerFactory
			.getLogger(TaskDo.class);
	private final FieldDescriptor m_fd;
	private final ProcessDefinition m_subProcess;

	public TaskDo(ProcessDefinition subProcess, FieldDescriptor fd, ProcessDefinition processDefinition) {
		super(processDefinition);
		m_subProcess = subProcess;
		m_fd = fd;
	}

	public FieldDescriptor getFd() {
		return m_fd;
	}

	public ProcessDefinition getSubProcess() {
		return m_subProcess;
	}

	public String toString() {
		return super.toString()+" subProcess=" + m_subProcess;
	}

	protected void launchOneCyclicSubprocess(ProcessInstance processInstance) {
		ProcessInstance subProcess = new ProcessInstance();
		getSubProcess().startProcess(subProcess);
		processInstance.getChildProcesses().add(subProcess);
		subProcess.setObjectInstance(processInstance.getObjectInstance());
		subProcess.setParentProcess(processInstance);
		log.debug("subprocess launched: {}",subProcess.getProcessDefinitionName());
		ProcessInstanceUtils.clearQueue(processInstance);
		processInstance.setWaitCount(1);
		log.debug("subprocess launched for: {} {}",processInstance.getProcessDefinitionName(),processInstance.getId());			
	}

}
