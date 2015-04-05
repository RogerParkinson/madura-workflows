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

import nz.co.senanque.workflow.ProcessInstanceUtils;
import nz.co.senanque.workflow.instances.ProcessInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskForm extends TaskBase {

	private static final Logger log = LoggerFactory
			.getLogger(TaskForm.class);
	private final String m_formName;
	private final String m_queue;
	public TaskForm(ProcessDefinition ownerProcess,String formName, String queue) {
		super(ownerProcess);
		m_formName = formName;
		m_queue = queue;
	}

	public String toString() {
		return super.toString()+" formName=" + m_formName + " args=" +getArguments();
	}

	public String getFormName() {
		return m_formName;
	}
	/* We cannot actually execute a TaskForm without external help.
	 * All we can do is return false
	 */
	public boolean execute(ProcessInstance processInstance) {
		log.debug("{}",this);
		ProcessInstanceUtils.setQueue(processInstance, getOwnerProcess().getWorkflowManager().getQueue(m_queue));
		return false;
	}

	public String getQueue() {
		return m_queue;
	}

}
