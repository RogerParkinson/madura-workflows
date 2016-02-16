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
import nz.co.senanque.validationengine.ValidationEngine;
import nz.co.senanque.validationengine.ValidationObject;
import nz.co.senanque.validationengine.ValidationSession;
import nz.co.senanque.workflow.WorkflowManager;
import nz.co.senanque.workflow.instances.ProcessInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Roger Parkinson
 *
 */
public class WorkflowFormImpl implements WorkflowForm {

	private static final Logger log = LoggerFactory
			.getLogger(WorkflowFormImpl.class);

	private Object m_context;
	private ProcessInstance m_processInstance;
    @Autowired private WorkflowManager m_workflowManager;
    @Autowired private ValidationEngine m_validationEngine;
    private ValidationSession m_validationSession;
	private ProcessDefinition m_processDefinition;

	private boolean m_readOnly;
	
	@PostConstruct
	public void init() {
	}

	public WorkflowFormImpl() {
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
	private ValidationSession getValidationSession() {
		if (m_validationSession == null) {
			m_validationSession = m_validationEngine.createSession();
		}
		return m_validationSession;
	}
	@Override
	public void bind() {
		ValidationObject o = (ValidationObject)getContext();
		getValidationSession().bind(o);
	}
	@Override
	public void close() {
		ValidationSession session = getValidationSession();
		session.unbindAll();
		session.close();
		m_validationSession = null;
	}
	public void finalize() {
		close();
	}

	public WorkflowManager getWorkflowManager() {
		return m_workflowManager;
	}

	public void setWorkflowManager(WorkflowManager workflowManager) {
		m_workflowManager = workflowManager;
	}

	public void setProcessDefinition(ProcessDefinition processDefinition) {
		m_processDefinition = processDefinition;
	}

	public ValidationEngine getValidationEngine() {
		return m_validationEngine;
	}

	public void setValidationEngine(ValidationEngine validationEngine) {
		m_validationEngine = validationEngine;
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
