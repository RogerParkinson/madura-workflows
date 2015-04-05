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
import nz.co.senanque.workflow.instances.ProcessInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskIf extends TaskBase {

	private static final Logger log = LoggerFactory
			.getLogger(TaskIf.class);
	public TaskIf(ProcessDefinition ownerProcess) {
		super(ownerProcess);
	}

	private FieldDescriptor m_condition;
	private ProcessDefinition m_trueHandler;
	private ProcessDefinition m_elseHandler;

	public String toString() {
		return super.toString()+" condition=" + m_condition;
	}

	public void setCondition(FieldDescriptor fd) {
		m_condition = fd;		
	}

	public FieldDescriptor getCondition() {
		return m_condition;
	}

	public void setTrueHandler(ProcessDefinition trueProcess) {
		m_trueHandler = trueProcess;		
	}

	public void setElseHandler(ProcessDefinition elseProcess) {
		m_elseHandler = elseProcess;		
	}

	public ProcessDefinition getTrueHandler() {
		return m_trueHandler;
	}

	public ProcessDefinition getElseHandler() {
		return m_elseHandler;
	}

	public boolean execute(ProcessInstance processInstance) {
		log.debug("{}",this);
		return true;
	}
	public Boolean getHandler() {
		return true;
	}

	public TaskBase getConditionalTask(ProcessInstance processInstance) {
		Boolean bool = (Boolean)getField(processInstance, getCondition());
		log.debug("condition={}",bool);

		TaskBase ret=null;
		if (bool) {
			ret = m_trueHandler.m_tasks.get(0);
		} else if (!m_elseHandler.m_tasks.isEmpty()) {
			ret = m_elseHandler.m_tasks.get(0);
		}
		if (ret == null)
		{
			// if there is no else handler move to the next task in the current process.
			ret = super.getNextTask(processInstance);
		}
		return ret;
	}


}
