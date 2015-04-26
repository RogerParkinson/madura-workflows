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

import java.util.List;

import nz.co.senanque.workflow.ProcessInstanceUtils;
import nz.co.senanque.workflow.instances.ProcessInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Roger Parkinson
 *
 */
public class TaskFork extends TaskBase {

	private static final Logger log = LoggerFactory
			.getLogger(TaskFork.class);
	private final List<ProcessDefinition> m_subProcess;

	public TaskFork(List<ProcessDefinition> subProcess, ProcessDefinition processDefinition) {
		super(processDefinition);
		m_subProcess = subProcess;
	}

	public List<ProcessDefinition> getSubProcess() {
		return m_subProcess;
	}

	public boolean execute(ProcessInstance processInstance) {
		log.debug("{}",this);
		for (ProcessDefinition subProcess:m_subProcess)
		{
			ProcessInstance subprocessInstance = new ProcessInstance();
			subprocessInstance.setBundleName(processInstance.getBundleName());
			subProcess.startProcess(subprocessInstance);
			processInstance.getChildProcesses().add(subprocessInstance);
			subprocessInstance.setObjectInstance(processInstance.getObjectInstance());
			subprocessInstance.setParentProcess(processInstance);
			log.debug("subprocess launched: {}",subprocessInstance.getProcessDefinitionName());
		}
		ProcessInstanceUtils.clearQueue(processInstance);
		processInstance.setWaitCount(m_subProcess.size());
		log.debug("{} subprocesses launched for: {} {}",processInstance.getWaitCount(),processInstance.getProcessDefinitionName(),processInstance.getId());

		return false;
	}

}
