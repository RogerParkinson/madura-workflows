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

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import nz.co.senanque.forms.FormFactory;
import nz.co.senanque.forms.WorkflowForm;
import nz.co.senanque.locking.LockAction;
import nz.co.senanque.locking.LockFactory;
import nz.co.senanque.locking.LockTemplate;
import nz.co.senanque.messaging.MessageMapper;
import nz.co.senanque.parser.InputStreamParserSource;
import nz.co.senanque.parser.ParserSource;
import nz.co.senanque.process.instances.ProcessDefinition;
import nz.co.senanque.process.instances.TaskBase;
import nz.co.senanque.process.instances.TaskForm;
import nz.co.senanque.process.instances.TaskIf;
import nz.co.senanque.process.instances.TaskTry;
import nz.co.senanque.process.parser.ParsePackage;
import nz.co.senanque.process.parser.ProcessTextProvider;
import nz.co.senanque.schemaparser.FieldDescriptor;
import nz.co.senanque.schemaparser.SchemaParser;
import nz.co.senanque.validationengine.ValidationEngine;
import nz.co.senanque.workflow.instances.Audit;
import nz.co.senanque.workflow.instances.DeferredEvent;
import nz.co.senanque.workflow.instances.EventType;
import nz.co.senanque.workflow.instances.ProcessInstance;
import nz.co.senanque.workflow.instances.TaskStatus;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.monitor.IntegrationMBeanExporter;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * This implements the non-database functionality of the workflow manager.
 * Injected beans handle the database activity
 * 
 * @author Roger Parkinson
 *
 */
public class WorkflowManagerImpl extends WorkflowManagerAbstract {

	private static final Logger log = LoggerFactory
			.getLogger(WorkflowManagerImpl.class);

	@Autowired(required=false)
    private transient IntegrationMBeanExporter m_integrationMBeanExporter;
	
	@Autowired(required=false)
	private Executor m_executor;
	@Autowired
	private WorkflowDAO m_workflowDAO;
	@Autowired
	private ContextDAO m_contextDAO;
	@Autowired
	private LockFactory m_lockFactory;
    private transient ValidationEngine m_validationEngine;
	@Autowired 
	private FormFactory m_formFactory;

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#createContextDescriptor(java.lang.Object)
	 */
	@Override
	public String createContextDescriptor(Object o) {
		return getContextDAO().createContextDescriptor(o);
	}
	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#execute(long)
	 */
	@Transactional
	public void execute(long id) {
		execute(getWorkflowDAO().findProcessInstance(id));
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#execute(nz.co.senanque.workflow.instances.DeferredEvent)
	 */
	@Transactional
	public void executeDeferredEvent(long deferredEventId) {
		DeferredEvent deferredEvent = getWorkflowDAO().findDeferredEvent(deferredEventId);
		log.debug("fired deferred event {} for {} {}",deferredEvent.getEventType(),deferredEvent.getProcessInstance().getId(),deferredEvent.getComment());
		ProcessInstance processInstance = deferredEvent.getProcessInstance();
		switch (deferredEvent.getEventType()) {
		case DEFERRED: 
			ProcessInstanceUtils.clearQueue(processInstance, TaskStatus.TIMEOUT);
			int lastAuditIndex = processInstance.getAudits().size()-1;
			Audit lastAudit = processInstance.getAudits().get(lastAuditIndex);
			lastAudit.setInterrupted(true);
			lastAudit.setStatus(TaskStatus.TIMEOUT);
			Date now = new Date();
			lastAudit.setComment(trimComment(lastAudit.getComment()+" Timed out at "+now));
			// this sets the task to the TryTask that generated the timeout.
			processInstance.setProcessDefinitionName(deferredEvent.getProcessDefinitionName());
			processInstance.setTaskId(deferredEvent.getTaskId());
			break;
		case FORCE_ABORT: 
			ProcessInstanceUtils.clearQueue(processInstance, TaskStatus.ABORTING);
			processInstance.setComment("Sibling aborted");
			break;
		case SUBPROCESS_END:
			if (processInstance.getWaitCount() == 0) {
				break;
			}
			processInstance.setWaitCount(processInstance.getWaitCount()-1);
			// If this is the last process then kick off the parent, but
			// if any of the siblings aborted then abort the parent.
			if (processInstance.getWaitCount() == 0) {
				if (processInstance.isCyclic()) {
					// but if this is a cyclic there won't be any siblings
					// and we want to re-execute the parent. Use the Retry logic.
					TaskBase previous = getCurrentTask(processInstance).getPreviousTask(processInstance);
					if (previous == null) {
						throw new WorkflowException("Trying to retry a task when there is none");
					}
					previous.loadTask(processInstance);
					processInstance.setStatus(TaskStatus.GO);
				} else {
					processInstance.setStatus(TaskStatus.GO);
					for (ProcessInstance sibling: processInstance.getChildProcesses()) {
						if (sibling.getStatus() == TaskStatus.ABORTED) {
							processInstance.setStatus(TaskStatus.ABORTING); 
							processInstance.setComment("Child aborted");
						}
					}				
				}
			}
			break;
		default:
			log.error("Unexpected event type {} (ignoring)",deferredEvent.getEventType());
			return;
		}
		deferredEvent.setEventType(EventType.DONE);
	}
	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#getContext(java.lang.String)
	 */
	@Transactional
	public Object getContext(String objectInstance) {
		return getContextDAO().getContext(objectInstance);
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#getField(nz.co.senanque.workflow.instances.ProcessInstance, nz.co.senanque.schemaparser.FieldDescriptor)
	 */
	@Transactional
	public Object getField(ProcessInstance processInstance, FieldDescriptor fd) {
		Object o = getContextDAO().getContext(processInstance.getObjectInstance());
		String prefix="get";
		if (fd.getType().endsWith("Boolean")) {
			prefix="is";
		}
		String name=prefix+StringUtils.capitalize(fd.getName());
		try {
			Method getter = o.getClass().getMethod(name);
			return getter.invoke(o);
		} catch (Exception e) {
			throw new WorkflowException("Problem finding field: "+fd.getName());
		}
	}
	@Transactional
	public ProcessInstance launch(String processName, Object o, String comment, String bundleName) {
		ProcessInstance processInstance = new ProcessInstance();
		return launch(processName, o,comment,bundleName, processInstance);
	}

	@Transactional
	public long launch(WorkflowForm launchForm, String comment, String bundleName) {
		ProcessInstance processInstanceRet = launch(launchForm.getProcessName(), launchForm.getContext(),comment,bundleName, launchForm.getProcessInstance());
		return processInstanceRet.getId();
	}
	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#launch(java.lang.String, java.lang.Object, java.lang.String, java.lang.String)
	 */
	@Transactional
	public ProcessInstance launch(String processName, Object context, String comment,
			String bundleName, ProcessInstance processInstance) {
		ProcessDefinition processDefinition = getProcessDefinition(processName);
		if (processDefinition == null) {
			throw new WorkflowException("Failed to find process definition named "+processName);
		}
		Object mergedContext = getContextDAO().mergeContext(context);
		processInstance.setComment(comment==null?" ":comment);
		processInstance.setBundleName(bundleName);
		processInstance.setObjectInstance(getContextDAO().createContextDescriptor(mergedContext));
		if (!ContextUtils.getContextClass(processInstance.getObjectInstance()).equals(mergedContext.getClass())) {
			throw new WorkflowException("Context object does not match process context in "+processName);
		}
		processDefinition.startProcess(processInstance);

		ProcessInstance processInstanceRet = getWorkflowDAO().mergeProcessInstance(processInstance);
		getWorkflowDAO().flush();
		return processInstanceRet;
	}

	@Transactional
	public long save(WorkflowForm workflowForm) {
		Object context = getContextDAO().mergeContext(workflowForm.getContext());
		ProcessInstance processInstance = workflowForm.getProcessInstance();
		processInstance.setLastUpdated(new Timestamp(System.currentTimeMillis()));
		processInstance.setObjectInstance(createContextDescriptor(context));
		ProcessInstance pi = null;
		if (processInstance.getId() == 0) {
			processInstance.setTaskId(0L);
			TaskBase task = getTask(workflowForm.getProcessDefinition(),0L);
			pi = getWorkflowDAO().mergeProcessInstance(processInstance);
			Audit audit = createAudit(pi, task);
			audit.setStatus(TaskStatus.DONE);
		}
		else {
			TaskBase task = getCurrentTask(processInstance);
			pi = getWorkflowDAO().mergeProcessInstance(processInstance);
			createAudit(pi, task);
		}
		return pi.getId();
	}
	@Transactional
	public ProcessInstance refresh(ProcessInstance processInstance) {
		return getWorkflowDAO().refreshProcessInstance(processInstance);
	}

	
	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#mergeContext(java.lang.Object)
	 */
	@Transactional
	public void mergeContext(Object context) {
		getContextDAO().mergeContext(context);
	}

	@Transactional
	public Collection<Audit> getAudits(ProcessInstance processInstance) {
		ProcessInstance pi = getWorkflowDAO().mergeProcessInstance(processInstance);
		return pi.getAudits();
	}
	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#processMessage(nz.co.senanque.workflow.instances.ProcessInstance, org.springframework.integration.Message, nz.co.senanque.messaging.MessageMapper)
	 */
	@Transactional
	public void processMessage(ProcessInstance processInstance,
			Message<?> message, MessageMapper messageMapper) {
		Object context = getContextDAO().getContext(processInstance.getObjectInstance());
		processInstance.setStatus(TaskStatus.GO);
		try {
			messageMapper.unpackMessage(message, context);
		} catch (Exception e) {
			processInstance.setStatus(TaskStatus.ABORTING);
			processInstance.setComment(e.getMessage());
		}
		getContextDAO().mergeContext(context);
		getWorkflowDAO().mergeProcessInstance(processInstance);
		getWorkflowDAO().flush();
		if (log.isDebugEnabled()) {
			getWorkflowDAO().getActiveProcesses();
		}
		log.debug("set status to processInstance {} to {} {}",processInstance.getId(),processInstance.getStatus(),context);
	}

	@Transactional
	public void execute(ProcessInstance processInstance) {
		super.execute(processInstance);
		getWorkflowDAO().flush();
	}
	
	/**
	 * This manages the transition of the process instance from Waiting to Busy with appropriate locking
	 * and unlocking. Once it is busy it is ours but we have to check that it was not changed since we last saw it.
	 * The situation we are protecting against is that although the process instance looked to be in wait state
	 * in the table we might have sat on that for a while and someone else may have updated it meanwhile.
	 * In that case we reject the request, unless we are TECHSUPPORT. Those guys can do anything.
	 * @param processInstance
	 * @param techSupport (boolean that indicates if we have TECHSUPPORT privs)
	 * @param userName
	 * @return updated processInstance or null if we failed to get the lock
	 */
	public ProcessInstance lockProcessInstance(final ProcessInstance processInstance, final boolean techSupport, final String userName) {
		List<Lock> locks = ContextUtils.getLocks(processInstance,getLockFactory(),"nz.co.senanque.workflow.WorkflowClient.lock(ProcessInstance)");
		LockTemplate lockTemplate = new LockTemplate(locks, new LockAction() {
			
			public void doAction() {
				
				String taskId = ProcessInstanceUtils.getTaskId(processInstance);
				ProcessInstance pi = getWorkflowDAO().refreshProcessInstance(processInstance);
//				if (log.isDebugEnabled()) {
//					log.debug("taskId {} ProcessInstanceUtils.getTaskId(pi) {} {}",taskId,ProcessInstanceUtils.getTaskId(pi),(!taskId.equals(ProcessInstanceUtils.getTaskId(pi))));
//					log.debug("pi.getStatus() {} techSupport {} {}",pi.getStatus(),techSupport,((pi.getStatus() != TaskStatus.WAIT) && !techSupport));
//					log.debug("pi.getStatus() {} userName {} pi.getLockedBy() {} {}",pi.getStatus(),userName,pi.getLockedBy(),(pi.getStatus() == TaskStatus.BUSY) && !userName.equals(pi.getLockedBy()) && !techSupport);
//				}
				if (!techSupport) {
					if (!(taskId.equals(ProcessInstanceUtils.getTaskId(pi)) && 
							((pi.getStatus() == TaskStatus.WAIT) || 
									((pi.getStatus() == TaskStatus.BUSY) && userName.equals(pi.getLockedBy()))))) {
//						// In this case we did not actually fail to get the lock but
//						// the process is not in the state
//						// it was in when we saw it in the table because another
//						// user (probably) has updated it.
//						// Therefore it is dangerous to proceed (unless we are tech support)
						throw new RuntimeException("ProcessInstance is already busy");
					}
				}
				pi.setStatus(TaskStatus.BUSY);
				pi.setLockedBy(userName);
				TaskBase task = getCurrentTask(pi);
				Audit audit = createAudit(pi, task);
				getWorkflowDAO().mergeProcessInstance(pi);
			}
		});
		boolean weAreOkay = true;
		try {
			weAreOkay = lockTemplate.doAction();
		} catch (Exception e) {
			weAreOkay = false;
		}
		if (!weAreOkay) {
			return null;
		}
		return getWorkflowDAO().refreshProcessInstance(processInstance);
	}
	protected void tickleParentProcess(ProcessInstance processInstance, TaskStatus status) {
		ProcessInstance parent = processInstance.getParentProcess();
		if (parent != null)
		{
			// we have a parent waiting for subprocesses to exit

			// If this child process was aborted
			// go find all the siblings that are still alive and abort them
			if (status == TaskStatus.ABORTED) {
				for (ProcessInstance sibling: parent.getChildProcesses()) {
					if (sibling.getStatus() != TaskStatus.ABORTED && 
							sibling.getStatus() != TaskStatus.ABORTING && 
							sibling.getStatus() != TaskStatus.DONE) {
						
						DeferredEvent deferredEvent = new DeferredEvent();
						deferredEvent.setEventType(EventType.FORCE_ABORT);
						sibling.setStatus(TaskStatus.ABORTING);
						deferredEvent.setProcessInstance(sibling);
						deferredEvent.setComment("aborting sibling");
						sibling.getDeferredEvents().add(deferredEvent);
					}
				}
			}
			if (parent.getWaitCount() > 0) {
				// this lets the parent know this subprocess is finished.
				log.debug("created an event for SUBPROCESS_END");
				DeferredEvent deferredEvent = new DeferredEvent();
				deferredEvent.setEventType(EventType.SUBPROCESS_END);
				deferredEvent.setProcessInstance(parent);
				parent.getDeferredEvents().add(deferredEvent);
				deferredEvent.setComment("process instance: "+processInstance.getId());
			}
			getWorkflowDAO().mergeProcessInstance(parent);
		}		
	}


	/**
	 * If this is just the end of the handler then return the next task after the handler
	 * If it is the end of the whole process then return null.
	 * @param processInstance
	 * @param currentAudit
	 * @return TaskBase
	 */
	protected TaskBase endOfProcessDetected(ProcessInstance processInstance, Audit currentAudit) {
		TaskBase ret = null;
		TaskBase currentTask = getCurrentTask(processInstance);
		ProcessInstanceUtils.clearQueue(processInstance, TaskStatus.DONE);
		currentAudit.setStatus(TaskStatus.DONE);
		// End of process can mean just the end of a handler process.
		{
			List<Audit> audits = findHandlerTasks(processInstance);
			for (Audit audit : audits) {
				TaskBase taskBase = getTask(audit);
				audit.setHandler(false);
				if (taskBase instanceof TaskTry) {
					TaskTry taskTry = (TaskTry)taskBase;
					if (taskTry.getTimeoutValue() > -1) {
						// we ended a handler that had a timeout.
						// That means we need to cancel the timeout.
						getWorkflowDAO().removeDeferredEvent(processInstance,taskTry);
					}
					TaskBase nextTask = taskTry.getNextTask(processInstance);
					nextTask.loadTask(processInstance);
				}
				if (taskBase instanceof TaskIf) {
					TaskIf taskIf = (TaskIf)taskBase;
					TaskBase nextTask = taskIf.getNextTask(processInstance);
					nextTask.loadTask(processInstance);
				}
				ret = getCurrentTask(processInstance);
				break;
			}

		}
		// If this is a subprocess then tickle the parent.
		tickleParentProcess(processInstance,TaskStatus.DONE);
		if (ret == currentTask) {
			processInstance.setStatus(TaskStatus.DONE);
		}
		getWorkflowDAO().mergeProcessInstance(processInstance);
		getWorkflowDAO().flush();
		getWorkflowDAO().refreshProcessInstance(processInstance);
		return ret;
	}
	
	/**
	 * Scan resources for workflow files and message definitions
	 */
	@PostConstruct
	public void init() {
		findBeans();
		SAXBuilder builder = new SAXBuilder();
		Document doc = null;
		try {
			doc = builder.build(getSchema().getInputStream());
			SchemaParser schemaParser = new SchemaParser();
			schemaParser.parse(doc);
			ParserSource parserSource = new InputStreamParserSource(getProcesses());
			
			ProcessTextProvider textProvider = new ProcessTextProvider(parserSource,schemaParser,this);
			ParsePackage parsePackage = new ParsePackage();
			parsePackage.parse(textProvider);
		} catch (Exception e) {
			throw new WorkflowException(e);
		}
	}
	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#shutdown()
	 */
	@PreDestroy
	public void shutdown() {
		if (getExecutor() != null) {
			getExecutor().shutdown();
		}
//		if (getIntegrationMBeanExporter() != null) {
//			log.info("Stopping Spring Integration...");
//			getIntegrationMBeanExporter().stopActiveComponents(true, 10000);
//			log.info("Stopping Spring Integration...finished");
//		}
	}
	
	public void finishLaunch(long processId) {
		ProcessInstance processInstance = getWorkflowDAO().findProcessInstance(processId);
		processInstance.setStatus(TaskStatus.GO);
		getWorkflowDAO().mergeProcessInstance(processInstance);
		getWorkflowDAO().flush();
	}
	
	public WorkflowForm getLaunchForm(String processName) {
        ProcessDefinition processDefinition = getProcessDefinition(processName);
        String launchForm = processDefinition.getLaunchForm();
        Object context=null;
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			context = Class.forName(processDefinition.getFullClassName(),false,classLoader).newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		WorkflowForm workflowForm = m_formFactory.getForm(launchForm);
		workflowForm.setProcessDefinition(processDefinition);
		workflowForm.setContext(context);
		ProcessInstance processInstance = new ProcessInstance();
		processInstance.setProcessDefinitionName(processDefinition.getName());
		processInstance.setComment(" ");
		workflowForm.setProcessInstance(processInstance);
		return workflowForm;
	}

	public WorkflowForm getCurrentForm(ProcessInstance processInstance) {
        ProcessDefinition processDefinition = getProcessDefinition(processInstance.getProcessDefinitionName());
		String formName = processDefinition.getLaunchForm();
		boolean readOnly = true;
		TaskBase task = getCurrentTask(processInstance);
		if ((task instanceof TaskForm)) {
			formName =  ((TaskForm)task).getFormName();
			readOnly = false;
		}
		WorkflowForm workflowForm = m_formFactory.getForm(formName);
		Object context = getContext(processInstance.getObjectInstance());
		workflowForm.setProcessDefinition(processDefinition);
		workflowForm.setProcessInstance(processInstance);
		workflowForm.setContext(context);
		workflowForm.setReadOnly(readOnly);
		workflowForm.bind();
		return workflowForm;
	}
	public ContextDAO getContextDAO() {
		return m_contextDAO;
	}

	public void setContextDAO(ContextDAO contextDAO) {
		m_contextDAO = contextDAO;
	}

	public WorkflowDAO getWorkflowDAO() {
		return m_workflowDAO;
	}

	public void setWorkflowDAO(WorkflowDAO workflowDAO) {
		m_workflowDAO = workflowDAO;
	}

	public Executor getExecutor() {
		return m_executor;
	}

	public void setExecutor(Executor executor) {
		m_executor = executor;
	}

	public LockFactory getLockFactory() {
		return m_lockFactory;
	}

	public void setLockFactory(LockFactory lockFactory) {
		m_lockFactory = lockFactory;
	}

	public IntegrationMBeanExporter getIntegrationMBeanExporter() {
		return m_integrationMBeanExporter;
	}

	public void setIntegrationMBeanExporter(
			IntegrationMBeanExporter integrationMBeanExporter) {
		m_integrationMBeanExporter = integrationMBeanExporter;
	}

	public FormFactory getFormFactory() {
		return m_formFactory;
	}
	public void setFormFactory(FormFactory formFactory) {
		m_formFactory = formFactory;
	}
	public ValidationEngine getValidationEngine() {
		return m_validationEngine;
	}
	public void setValidationEngine(ValidationEngine validationEngine) {
		m_validationEngine = validationEngine;
	}

}
