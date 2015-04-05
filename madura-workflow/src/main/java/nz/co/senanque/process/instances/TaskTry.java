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

import java.util.Date;

import nz.co.senanque.parser.ParserException;
import nz.co.senanque.process.parser.ProcessTextProvider;
import nz.co.senanque.workflow.instances.DeferredEvent;
import nz.co.senanque.workflow.instances.EventType;
import nz.co.senanque.workflow.instances.ProcessInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Roger Parkinson
 *
 */
public class TaskTry extends TaskBase {

	private static final Logger log = LoggerFactory
			.getLogger(TaskTry.class);
	private ProcessDefinition m_errorHandler=null;
	private ProcessDefinition m_abortHandler=null;
	private ProcessDefinition m_timeoutHandler=null;
	private ProcessDefinition m_mainHandler;
	private TimeoutProvider m_timeoutProvider;

	public TaskTry(ProcessDefinition ownerProcess) {
		super(ownerProcess);
	}

	public int getTimeoutValue() {
		return (m_timeoutProvider==null)?-1:m_timeoutProvider.getTimeoutValue();
	}

	public void addTimeoutHandler(TimeoutProvider timeoutProvider, ProcessDefinition timeoutHandler,ProcessTextProvider textProvider) {
		if (m_timeoutHandler != null) {
			throw new ParserException("Duplicate timeout handler defined",textProvider);
		}
		m_timeoutHandler = timeoutHandler;
		m_timeoutProvider = timeoutProvider;
	}

	public void addErrorHandler(ProcessDefinition errorHandler,ProcessTextProvider textProvider) {
		if (m_errorHandler != null) {
			throw new ParserException("Duplicate error handler defined",textProvider);
		}
		m_errorHandler = errorHandler;		
	}
	public void addAbortHandler(ProcessDefinition abortHandler,ProcessTextProvider textProvider) {
		if (m_abortHandler != null) {
			throw new ParserException("Duplicate abort handler defined",textProvider);
		}
		m_abortHandler = abortHandler;		
	}

	public String toString() {
		return super.toString()+" TimeoutValue:"+getTimeoutValue();
	}

	public void addMainHandler(ProcessDefinition mainHandler) {
		m_mainHandler = mainHandler;
	}

	public TaskBase getFirstTask() {
		return m_mainHandler.m_tasks.get(0);
	}

	public boolean execute(ProcessInstance processInstance) {
		log.debug("{}",this);
		// Most of the work is actually done in the executor. It loads the relevant handler and lets it execute.
		if (getTimeoutValue() > -1) {
			DeferredEvent deferredEvent = new DeferredEvent();
			deferredEvent.setProcessDefinitionName(getOwnerProcess().getName());
			deferredEvent.setTaskId(getTaskId());
			deferredEvent.setProcessInstance(processInstance);
			long now = new Date().getTime();
			deferredEvent.setCreated(now);
			deferredEvent.setEventType(EventType.DEFERRED);
			deferredEvent.setComment(this.toString());
			deferredEvent.setFire(now+getTimeoutValue());
			processInstance.getDeferredEvents().add(deferredEvent);
			processInstance.setDeferredEvent(deferredEvent);
		}
		return true;
	}

	public ProcessDefinition getErrorHandler() {
		return m_errorHandler;
	}

	public ProcessDefinition getTimeoutHandler() {
		return m_timeoutHandler;
	}

	public ProcessDefinition getMainHandler() {
		return m_mainHandler;
	}

	public ProcessDefinition getAbortHandler() {
		return m_abortHandler;
	}
	public Boolean getHandler() {
		return true;
	}

}
