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
package nz.co.senanque.forms;

import javax.annotation.PostConstruct;

import nz.co.senanque.process.instances.ProcessDefinition;
import nz.co.senanque.validationengine.ValidationObject;
import nz.co.senanque.workflow.instances.ProcessInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Roger Parkinson
 *
 */
public class WorkflowFormMock implements WorkflowForm {

	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory
			.getLogger(WorkflowFormMock.class);

	private Object m_context;
	private ProcessInstance m_processInstance;
	private ProcessDefinition m_processDefinition;

	private boolean m_readOnly;
	
	@PostConstruct
	public void init() {
	}

	public WorkflowFormMock() {
	}
	
	public Object getContext() {
		return m_context;
	}
	public void setContext(Object context) {
		m_context = context;
	}
	public ProcessInstance getProcessInstance() {
		return m_processInstance;
	}
	public void setProcessInstance(ProcessInstance processInstance) {
		m_processInstance = processInstance;
	}
	public String getProcessName() {
		return m_processDefinition.getName();
	}
	public boolean isLauncher() {
		return (m_processInstance==null);
	}
	@Override
	public void bind() {
		ValidationObject o = (ValidationObject)getContext();
	}
	@Override
	public void close() {
	}
	public void finalize() {
		close();
	}

	public void setProcessDefinition(ProcessDefinition processDefinition) {
		m_processDefinition = processDefinition;
	}

	@Override
	public ProcessDefinition getProcessDefinition() {
		return m_processDefinition;
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		m_readOnly = readOnly;
	}

	public boolean isReadOnly() {
		return m_readOnly;
	}
}
