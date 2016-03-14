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
package nz.co.senanque.workflow;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import nz.co.senanque.process.instances.TaskBase;
import nz.co.senanque.workflow.instances.Attachment;
import nz.co.senanque.workflow.instances.Audit;
import nz.co.senanque.workflow.instances.DeferredEvent;
import nz.co.senanque.workflow.instances.EventType;
import nz.co.senanque.workflow.instances.ProcessInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Roger Parkinson
 *
 */
public class WorkflowJPA implements WorkflowDAO {

	private static final Logger log = LoggerFactory
			.getLogger(WorkflowJPA.class);

	@PersistenceContext(unitName="em-workflow")
	private EntityManager m_entityManager;

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowDAO#findProcessInstance(java.lang.Long)
	 */
	@Transactional
	public ProcessInstance findProcessInstance(Long id) {
		log.debug("findProcessInstance {}",id);
		ProcessInstance ret = null;
		try {
			ret = m_entityManager.find(ProcessInstance.class, id, LockModeType.PESSIMISTIC_WRITE);
			if (ret == null) {
				throw new WorkflowException("Could not find process instance id="+id);
			}
			ret.getAudits().size();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return ret;
	}

	@Transactional
	public void addAttachment(Attachment attachment) {
		m_entityManager.persist(attachment);
		m_entityManager.flush();
	}
	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowDAO#refreshProcessInstance(nz.co.senanque.workflow.instances.ProcessInstance)
	 */
	@Transactional
	public ProcessInstance refreshProcessInstance(
			ProcessInstance processInstance) {
		log.debug("refreshProcessInstance");
		ProcessInstance ret =  m_entityManager.find(ProcessInstance.class, processInstance.getId());
		m_entityManager.refresh(ret);
		log.debug("\n{}",displayProcess(ret, 0));
		return ret;
	}
	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowDAO#mergeProcessInstance(nz.co.senanque.workflow.instances.ProcessInstance)
	 */
	@Transactional
	public ProcessInstance mergeProcessInstance(ProcessInstance processInstance) {
		log.debug("mergeProcessInstance");
		ProcessInstance ret =  m_entityManager.merge(processInstance);
		m_entityManager.flush();
		if (log.isDebugEnabled()) {
			log.debug("updating processInstance {} to {}",processInstance.getId(),processInstance.getStatus());
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowDAO#getActiveProcesses()
	 */
	@Transactional
	public List<ProcessInstance> getActiveProcesses() {
		if (log.isDebugEnabled()) {
			getAllProcesses();
		}
		Query query = m_entityManager.createNamedQuery("ActiveProcesses");
		@SuppressWarnings("unchecked")
		List<ProcessInstance> result = query.getResultList();
		if (log.isDebugEnabled()) {
			for (ProcessInstance processInstance:result) {
				log.debug("processInstanceId={} processName={} taskId={} status={}",processInstance.getId(),processInstance.getProcessDefinitionName(),processInstance.getTaskId(), processInstance.getStatus());
			}
			log.debug("found {} active processes",result.size());
		}
		return result;
	}

	@Transactional
	public List<ProcessInstance> getAllProcesses() {
		log.debug("getAllProcesses");
		Query query = m_entityManager.createQuery("SELECT c FROM nz.co.senanque.workflow.instances.ProcessInstance c");
		@SuppressWarnings("unchecked")
		List<ProcessInstance> result = query.getResultList();
		if (log.isDebugEnabled()) {
			log.debug("----------all processInstances ---------------");
			for (ProcessInstance processInstance:result) {
				log.debug("processInstanceId={} processName={} taskId={} status={}",processInstance.getId(),processInstance.getProcessDefinitionName(),processInstance.getTaskId(), processInstance.getStatus());
			}
			log.debug("found {} processes",result.size());
			log.debug("----------------------------------------------");
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowDAO#getDeferredEvents()
	 */
	@Transactional
	public List<DeferredEvent> getDeferredEvents() {
		if (log.isDebugEnabled()) {
			Query query = m_entityManager.createNamedQuery("DeferredEventsDebug");
			@SuppressWarnings("unchecked")
			List<DeferredEvent> result = query.getResultList();
			log.debug("#found {} deferred events",result.size());
			for (DeferredEvent deferredEvent:result) {
				log.debug("#deferred event: {} fire: {} now: {} created: {}",deferredEvent.getEventType(), deferredEvent.getFire(),new Date().getTime(),deferredEvent.getCreated());
			}
		}
		long now = new Date().getTime();
		Query query = m_entityManager.createNamedQuery("DeferredEvents").setParameter("rightNow", now);
		@SuppressWarnings("unchecked")
		List<DeferredEvent> result = query.getResultList();
		log.debug("found {} deferred events",result.size());
		return result;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowDAO#removeDeferredEvent(nz.co.senanque.workflow.instances.ProcessInstance, nz.co.senanque.process.instances.TaskBase)
	 */
	@Transactional
	public DeferredEvent removeDeferredEvent(ProcessInstance processInstance,
			TaskBase task) {
		List<DeferredEvent> deferredEvents = processInstance.getDeferredEvents();
		for (DeferredEvent deferredEvent: deferredEvents) {
			if (deferredEvent.getEventType() != EventType.DONE && deferredEvent.getTaskId().longValue() == task.getTaskId() && task.getProcessDefinitionName().equals(deferredEvent.getProcessDefinitionName())) {
				log.debug("killed deferred event processId {} taskId {} status {} {}", deferredEvent.getProcessInstance().getId(), deferredEvent.getId(), deferredEvent.getEventType() ,deferredEvent.getComment() );
				deferredEvent.setEventType(EventType.DONE);
				return deferredEvent;
			}
		}
		log.debug("failed to find deferred event processId {} {} taskId {}", processInstance.getId(), task.getProcessDefinitionName(), task.getTaskId());
		return null;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowDAO#clearDeferredEvents()
	 */
	@Transactional
	public int clearDeferredEvents() {
		Query query = m_entityManager.createNamedQuery("clearDeferredEvents");
		@SuppressWarnings("unchecked")
		List<DeferredEvent> result = query.getResultList();
		int ret = result.size();
		log.debug("#found {} deferred events",result.size());
		for (DeferredEvent deferredEvent:result) {
			log.debug("#deferred pid: {} processName: {} eventType: {} fire: {} now: {} ",deferredEvent.getProcessInstance().getId(), deferredEvent.getProcessDefinitionName(), deferredEvent.getEventType(), deferredEvent.getFire(),new Date().getTime());
			m_entityManager.remove(deferredEvent);
		}
		m_entityManager.flush();
		return ret;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowDAO#flush()
	 */
	@Transactional
	public void flush() {
		log.debug("flush");
		m_entityManager.flush();
		log.debug("flush complete");
	}
	@Transactional
	public DeferredEvent findDeferredEvent(long deferredEventId) {
		log.debug("findDeferredEvent");
		return m_entityManager.find(DeferredEvent.class, deferredEventId);
	}

	private String displayProcess(ProcessInstance processInstance, int indent) {
		StringBuilder ret = new StringBuilder();
		StringBuilder sb = new StringBuilder();
		for (int i=0;i<indent;i++) {
			sb.append('\t');
		}
		ret.append(sb);
		ret.append(" processId: ");
		ret.append(processInstance.getId());
		ret.append(" processName: ");
		ret.append(processInstance.getProcessDefinitionName());
		ret.append(" status: ");
		ret.append(processInstance.getStatus());
		ret.append(" waitCount: ");
		ret.append(processInstance.getWaitCount());
		ret.append("\n");
		for (Audit audit: processInstance.getAudits()) {
			ret.append(sb);
			ret.append("AUDIT: ");
			ret.append(audit.getStatus());
			ret.append(" ");
			ret.append(audit.getComment());
			ret.append(" handler:");
			ret.append(audit.isHandler());
			ret.append(" interrupted:");
			ret.append(audit.isInterrupted());
			ret.append(" taskId:");
			ret.append(audit.getTaskId());
			ret.append(" ");
			ret.append(audit.getComment());
			ret.append("\n");
		}
		for (DeferredEvent deferredEvent: processInstance.getDeferredEvents()) {
			ret.append(sb);
			ret.append("EVENT: ");
			ret.append(deferredEvent.getId());
			ret.append(" ");
			ret.append(deferredEvent.getEventType());
			ret.append(" ");
			ret.append(deferredEvent.getComment());
			ret.append(" process:");
			ret.append(deferredEvent.getProcessDefinitionName());
			ret.append(" processInstanceId:");
			ret.append(deferredEvent.getProcessInstance().getId());
			ret.append(" taskId:");
			ret.append(deferredEvent.getTaskId());
			ret.append("\n");
		}
		for (ProcessInstance child: processInstance.getChildProcesses()) {
			ret.append(displayProcess(child, indent+1));
		}
		return ret.toString();
	}


}
