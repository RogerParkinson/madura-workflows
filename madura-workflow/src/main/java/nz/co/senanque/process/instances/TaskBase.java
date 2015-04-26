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

import java.util.Map;

import nz.co.senanque.schemaparser.FieldDescriptor;
import nz.co.senanque.validationengine.ValidationEngine;
import nz.co.senanque.validationengine.ValidationSessionHolder;
import nz.co.senanque.validationengine.ValidationSessionHolderImpl;
import nz.co.senanque.workflow.AbortException;
import nz.co.senanque.workflow.WorkflowManager;
import nz.co.senanque.workflow.instances.DeferredEvent;
import nz.co.senanque.workflow.instances.ProcessInstance;

/**
 * 
 * @author Roger Parkinson
 * 
 */
public abstract class TaskBase {

	private final ProcessDefinition m_ownerProcess;
	private Map<String, String> m_arguments;
	private long m_id;
	private boolean m_negate;
	private String m_position;

	public TaskBase(ProcessDefinition ownerProcess) {
		m_ownerProcess = ownerProcess;
		m_id = ownerProcess.getNextTaskId();
	}
	
	public String getProcessDefinitionName() {
		return m_ownerProcess.getName();
	}

	public ProcessDefinition getOwnerProcess() {
		return m_ownerProcess;
	}

	public Map<String, String> getArguments() {
		return m_arguments;
	}

	public void setArguments(Map<String, String> arguments) {
		m_arguments = arguments;
	}

	public long getTaskId() {
		return m_id;
	}

	public String toString() {
		return Indentation.getIndent()+getClass().getSimpleName()+getPosition()+" process="+getOwnerProcess().getName()+" taskId="+this.getTaskId();
	}
	abstract public boolean execute(ProcessInstance processInstance);

	public Boolean getHandler() {
		return false;
	}

	public TaskBase loadTask(ProcessInstance processInstance) {
		ProcessDefinition processDefinition = getOwnerProcess();
		processDefinition.loadTask(processInstance, getTaskId());
		return this;
	}

	private WorkflowManager getWorkflowManager() {
		return getOwnerProcess().getWorkflowManager();
	}
	protected ValidationEngine getValidationEngine() {
		return getWorkflowManager().getValidationEngine();
	}
	
	protected Object getContext(ProcessInstance processInstance) {
		return getWorkflowManager().getContext(processInstance.getObjectInstance());
	}
	
	protected void mergeContext(Object context) {
		getWorkflowManager().mergeContext(context);
	}

	protected TaskBase findInterruptedTask(ProcessInstance processInstance) {
		return getWorkflowManager().findInterruptedTask(
				processInstance);
	}
	protected TaskBase getTask(DeferredEvent deferredEvent) {
		return getWorkflowManager().getTask(deferredEvent);
	}
	protected Boolean getConditionalField(ProcessInstance processInstance,
			FieldDescriptor condition) {

		Object context = getContext(processInstance);
		ValidationSessionHolder validationSessonHolder = null;
		try {
			validationSessonHolder = new ValidationSessionHolderImpl(getValidationEngine());
			validationSessonHolder.bind(context);
			return (Boolean)getWorkflowManager().getField(processInstance, condition);
		} catch (Exception e) {
			throw new AbortException(e);
		} finally {
			validationSessonHolder.close();
		}			
	}

	public TaskBase getNextTask(ProcessInstance processInstance) {
		return getOwnerProcess().getTask(m_id+1L);
	}
	public TaskBase getPreviousTask(ProcessInstance processInstance) {
		return getOwnerProcess().getTask(m_id-1L);
	}

	public void setNegate(boolean negate) {
		m_negate = negate;
	}

	public boolean isNegate() {
		return m_negate;
	}

	public void setPosition(String position) {
		int i = position.indexOf('!');
		int j = position.indexOf("jar:");
		if (i > -1 && j > -1 && i > j) {
			m_position = position.substring(0,j-1)+position.substring(i+1);
			return;
		}
		m_position = position;
	}

	public String getPosition() {
		return m_position;
	}

//	public void loadNextTask(ProcessInstance processInstance) {
//		ProcessDefinition processDefinition = getOwnerProcess();
//		TaskBase nextTask = getNextTask(processInstance);
//		if (nextTask == null) {
//			throw new WorkflowException("No next task available "+this.toString());
//		}
//		processDefinition.loadTask(processInstance, getTaskId() + nextTask.getTaskId());
//	}
}
