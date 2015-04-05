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

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import nz.co.senanque.messaging.MessageMapper;
import nz.co.senanque.messaging.MessageSender;
import nz.co.senanque.process.instances.ComputeType;
import nz.co.senanque.process.instances.ProcessDefinition;
import nz.co.senanque.process.instances.QueueDefinition;
import nz.co.senanque.process.instances.TaskBase;
import nz.co.senanque.process.instances.TaskEnd;
import nz.co.senanque.process.instances.TaskIf;
import nz.co.senanque.process.instances.TaskTry;
import nz.co.senanque.process.instances.TimeoutProvider;
import nz.co.senanque.schemaparser.FieldDescriptor;
import nz.co.senanque.workflow.instances.Audit;
import nz.co.senanque.workflow.instances.DeferredEvent;
import nz.co.senanque.workflow.instances.ProcessInstance;
import nz.co.senanque.workflow.instances.TaskStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.Resource;
import org.springframework.integration.Message;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * This implements the non-database functionality of the workflow manager.
 * Injected beans handle the database activity
 * @author Roger Parkinson
 *
 */
public abstract class WorkflowManagerAbstract implements WorkflowManager, BeanFactoryAware {

	private static final Logger log = LoggerFactory
			.getLogger(WorkflowManagerAbstract.class);
    private final Set<ProcessDefinition> m_allProcesses = new TreeSet<ProcessDefinition>();
    private final Set<ProcessDefinition> m_mainProcesses = new TreeSet<ProcessDefinition>();
	private final Set<QueueDefinition> m_queues = new TreeSet<QueueDefinition>();
    @SuppressWarnings("rawtypes")
	private Map<String,MessageSender> m_allMessages = new HashMap<String,MessageSender>();
    @SuppressWarnings("rawtypes")
	private Map<String, ComputeType> m_computeTypes = new HashMap<String,ComputeType>();
	private Resource m_schema;
	private Resource m_processes;
	
	private DefaultListableBeanFactory m_beanFactory;
	private Map<String, TimeoutProvider> m_timeouts;

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#getAllProcesses()
	 */
	@Override
	public Set<ProcessDefinition> getAllProcesses() {
		return m_allProcesses;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#getMainProcesses()
	 */
	@Override
	public Set<ProcessDefinition> getMainProcesses() {
		return m_mainProcesses;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#getMessage(java.lang.String)
	 */
	@Override
	public MessageSender<?> getMessage(String messageName) {
		return m_allMessages.get(messageName);
	}
	
	protected Map<String,MessageSender> getMessages() {
		return m_allMessages;
	}

	protected Map<String,ComputeType> getComputeTypes() {
		return m_computeTypes;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#getProcessDefinition(java.lang.String)
	 */
	@Override
	public ProcessDefinition getProcessDefinition(String name) {
		for (ProcessDefinition pd: m_allProcesses) {
			if (pd.getName().equals(name)) {
				return pd;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#getTask(nz.co.senanque.process.instances.ProcessDefinition, long)
	 */
	@Override
	public TaskBase getTask(ProcessDefinition pd, long taskId) {
		for (TaskBase task: pd.getTasks()) {
			if (task.getTaskId() == taskId) {
				return task;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#getCurrentTask(nz.co.senanque.workflow.instances.ProcessInstance)
	 */
	@Override
	public TaskBase getCurrentTask(ProcessInstance processInstance) {
		if (processInstance == null) {
			throw new NullPointerException("processInstance was null");
		}
		return getTask(processInstance.getProcessDefinitionName(),processInstance.getTaskId());
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#getTask(java.lang.String, java.lang.Long)
	 */
	@Override
	public TaskBase getTask(String processDefinitionName, Long taskid) {
		ProcessDefinition pd = getProcessDefinition(processDefinitionName);
		if (pd == null) {
			throw new WorkflowException("Could not find active process for "+processDefinitionName);
		}
		TaskBase task = getTask(pd,taskid);
		if (task == null) {
			throw new WorkflowException("Could not find task for "+processDefinitionName+":"+taskid);
		}
		return task;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#getField(nz.co.senanque.workflow.instances.ProcessInstance, nz.co.senanque.schemaparser.FieldDescriptor)
	 */
	@Override
	public abstract Object getField(ProcessInstance processInstance, FieldDescriptor fd);

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#findHandlerTasks(nz.co.senanque.workflow.instances.ProcessInstance)
	 */
	@Transactional
	public List<Audit> findHandlerTasks(ProcessInstance processInstance) {
		Stack<Audit> ret = new Stack<Audit>();
		for (Audit audit : processInstance.getAudits()) {
			if (audit.isHandler() && audit.getStatus() != null) {
				ret.push(audit);
			}
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#getTask(nz.co.senanque.workflow.instances.DeferredEvent)
	 */
	@Override
	public TaskBase getTask(DeferredEvent deferredEvent) {
		return getTask(deferredEvent.getProcessDefinitionName(),deferredEvent.getTaskId());
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#findInterruptedTask(nz.co.senanque.workflow.instances.ProcessInstance)
	 */
	@Transactional
	public TaskBase findInterruptedTask(ProcessInstance processInstance) {
		TaskBase ret = null;
		for (Audit audit : processInstance.getAudits()) {
			if (audit.isInterrupted()) {
				ret = getTask(
						audit.getProcessDefinitionName(), audit.getTaskId());
				audit.setInterrupted(false);
				break;
			}
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#addMainProcess(nz.co.senanque.process.instances.ProcessDefinition)
	 */
	@Override
	public void addMainProcess(ProcessDefinition processDefinition) {
		m_mainProcesses.add(processDefinition);
		addSubProcess(processDefinition);
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#addSubProcess(nz.co.senanque.process.instances.ProcessDefinition)
	 */
	@Override
	public void addSubProcess(ProcessDefinition processDefinition) {
		m_allProcesses.add(processDefinition);
	}
	
	protected String trimComment(String comment) {
		String ret = comment.trim();
		if (ret.length() > 50) {
			return ret.substring(0, 50);
		}
		return ret;
	}

	protected Audit createAudit(ProcessInstance processInstance, TaskBase task) {
		Audit audit = new Audit();
		audit.setCreated(new Timestamp(System.currentTimeMillis()));
		audit.setTaskId(task.getTaskId());
		audit.setProcessDefinitionName(task.getOwnerProcess().getName());
		audit.setComment(trimComment(task.toString()));
		audit.setHandler(task.getHandler());
		audit.setInterrupted(false);
		audit.setLockedBy(processInstance.getLockedBy());
		audit.setStatus(processInstance.getStatus());
		audit.setParentId(processInstance.getId());
		processInstance.getAudits().add(audit);
		return audit;
	}
	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#execute(nz.co.senanque.workflow.instances.ProcessInstance)
	 */
	@Transactional
	public void execute(ProcessInstance processInstance) {
		TaskBase task = getCurrentTask(processInstance);
		if (processInstance.getStatus() == TaskStatus.DONE) {
			return;
		}
		while (true) {
			log.debug("processInstanceId={} processName={} taskId={} status={}",processInstance.getId(),task.getOwnerProcess().getName(),task.getTaskId(), processInstance.getStatus());
			boolean b = true;
			Audit audit = createAudit(processInstance, task);
			try {
				if (processInstance.getStatus()==TaskStatus.ABORTING)
				{
					// Someone (external) set our status to ABORTING.
					// Don't attempt to execute the current instruction just throw an abort error.
					throw new AbortException(processInstance.getComment());
				}
				if (processInstance.getStatus()==TaskStatus.TIMEOUT)
				{
					// Just throw an exception and the exception handler looks after it.
					throw new TimeoutException();
				}
				if (processInstance.getStatus()==TaskStatus.GO)
				{
					if (task instanceof TaskEnd) {
						throw new WorkflowException("Trying to restart an end task, should never happen "+task.toString());
					}
					log.debug("moving on from "+task.toString());
					// simple search for the task after this one.
					task = task.getNextTask(processInstance);
					if (task == null) {
						// There is no task, this should never happen
						throw new WorkflowException("Failed to find a next task");
					}
					// Not the end of the process, so ensure the next task is pending.
					audit.setTaskId(task.getTaskId());
					audit.setProcessDefinitionName(task.getOwnerProcess().getName());
					audit.setComment(trimComment(task.toString()));
					audit.setHandler(task.getHandler());
					task.loadTask(processInstance);
				}
				audit.setStatus(TaskStatus.PENDING);
				ProcessInstanceUtils.clearQueue(processInstance, TaskStatus.PENDING);
				b = task.execute(processInstance);
			} catch (ContinueException e) {
				task = getCurrentTask(processInstance);
				continue;
			} catch (RetryException e) {
				task = getCurrentTask(processInstance);
				continue;
			} catch (ErrorException e) {
				audit.setStatus(TaskStatus.ERROR);
				if (!handleError(processInstance)) {
					// no appropriate handler, fall back to abort handler
					audit.setStatus(TaskStatus.ERROR);
					audit.setInterrupted(true);
					if (!handleAbort(processInstance)) {
						// Still no handler so we are screwed
						// Setting this status in the process 
						// instance will stop it
						processInstance.setStatus(TaskStatus.ABORTED);
					}
				} else {
					task = getCurrentTask(processInstance);
					continue;
				}
			} catch (AbortException e) {
				audit.setInterrupted(true);
				audit.setStatus(TaskStatus.ABORTED);
				audit.setComment(e.getMessage());
				if (!handleAbort(processInstance)) {
					// No handler found so just force an abort
					// Setting this status in the process instance will stop it
					processInstance.setStatus(TaskStatus.ABORTED);
					tickleParentProcess(processInstance,TaskStatus.ABORTED); // this should abort the sibling processes as well as the parent.
					break;
				} else {
					task = getCurrentTask(processInstance);
					continue;
				}
			} catch (TimeoutException e) {
				processInstance.getAudits().remove(audit);
				if (!handleTimeout(processInstance, audit)) {
					// No handler found so just force an abort
					// Setting this status in the process instance will stop it
					processInstance.setStatus(TaskStatus.ABORTED);
					tickleParentProcess(processInstance,processInstance.getStatus());
					break;
				} else {
					task = getCurrentTask(processInstance);
					continue;
				}
			}
			if (!b) {
				// task was not completed, exit the loop
				processInstance.setStatus(TaskStatus.WAIT);
				audit.setStatus(TaskStatus.WAIT);
				break;
			} else {
				if (task instanceof TaskEnd) {
					endOfProcessDetected(processInstance,audit);
					break;
				} else if (task instanceof TaskTry) {
					task = ((TaskTry)task).getFirstTask();
				} else if (task instanceof TaskIf) {
					task = ((TaskIf)task).getConditionalTask(processInstance);
				} else {
					task = getCurrentTask(processInstance).getNextTask(processInstance);
				}
				if (task == null) {
					throw new WorkflowException("Trying to step to a task when there is none");
				}
				// Not the end of the process, so ensure the next task is pending.
				task.loadTask(processInstance);
			}
		}
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#execute(long)
	 */
	@Override
	public abstract void execute(long id);

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#getQueues()
	 */
	@Override
	public Set<QueueDefinition> getQueues() {
		return m_queues;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#getQueue(java.lang.String)
	 */
	@Override
	public QueueDefinition getQueue(String queueName) {
		if (!StringUtils.hasText(queueName)) {
			return null;
		}
		for (QueueDefinition qd: m_queues) {
			if (qd.getName().equals(queueName)) {
				return qd;
			}
		}
		log.warn("failed to find queue: {}", queueName);
		return null;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#execute(nz.co.senanque.workflow.instances.DeferredEvent)
	 */
	@Transactional
	public abstract void executeDeferredEvent(long deferredEventId);

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#getComputeType(java.lang.String)
	 */
	@Override
	public ComputeType<?> getComputeType(String computeName) {
		return m_computeTypes.get(computeName);
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#getTimeoutProvider(java.lang.String)
	 */
	@Override
	public TimeoutProvider getTimeoutProvider(String timeoutProviderName) {
		return m_timeouts.get(timeoutProviderName);
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#getValidationEngine()
	 */
//	@Override
//	public ValidationEngine getValidationEngine() {
//		return null;
//	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#processMessage(nz.co.senanque.workflow.instances.ProcessInstance, org.springframework.integration.Message, nz.co.senanque.messaging.MessageMapper)
	 */
	@Override
	public abstract void processMessage(ProcessInstance processInstance,
			Message<?> message, MessageMapper messageMapper);

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#getContext(java.lang.String)
	 */
	@Override
	public abstract Object getContext(String objectInstance);
	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#mergeContext(java.lang.Object)
	 */
	@Override
	public abstract void mergeContext(Object context);

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.WorkflowManager#createContextDescriptor(java.lang.Object)
	 */
	@Override
	public abstract String createContextDescriptor(Object o);

	protected void findBeans() {
		m_computeTypes = m_beanFactory.getBeansOfType(ComputeType.class);
		m_allMessages = m_beanFactory.getBeansOfType(MessageSender.class);
		m_timeouts = m_beanFactory.getBeansOfType(TimeoutProvider.class);
	}

	protected boolean handleError(ProcessInstance processInstance) {
		List<Audit> audits = findHandlerTasks(processInstance);
		for (Audit audit : audits) {
			TaskBase taskBase = getTask(audit);
			if (taskBase instanceof TaskTry) {
				TaskTry taskTry = (TaskTry) taskBase;
				tidyAuditTrail(processInstance,taskTry.getTaskId());
				ProcessDefinition pd = taskTry.getErrorHandler();
				if (pd == null) {
					continue;
				}
				pd.startProcess(processInstance);
				return true;
			}
		}
		return false;
	}

	protected boolean handleTimeout(ProcessInstance processInstance, Audit audit) {
		dumpAuditTrail(processInstance);
		TaskBase tb = getCurrentTask(processInstance);
		if (tb instanceof TaskTry) {
			TaskTry taskTry = (TaskTry) tb;
			ProcessDefinition pd = taskTry.getTimeoutHandler();
			pd.startProcess(processInstance);
			return true;
		}
		return false;
	}
	private Audit tidyAuditTrail(ProcessInstance processInstance, long taskId) {
		boolean found = false;
		Audit ret = null;
		for (Audit audit: processInstance.getAudits()) {
			if (audit.getProcessDefinitionName() != null && processInstance.getProcessDefinitionName().equals(audit.getProcessDefinitionName()) && processInstance.getTaskId() == taskId ) {
				found = true;
				ret = audit;
			} else if (found) {
				audit.setHandler(false);
			}
		}
		return ret;
	}
	private void dumpAuditTrail(ProcessInstance processInstance) {
		if (!log.isDebugEnabled()) {
			return;
		}
		for (Audit audit: processInstance.getAudits()) {
			log.debug("{} {} {} {} {} {} {}",
					audit.getId(),audit.getProcessDefinitionName(),
					audit.getTaskId(),audit.getStatus(),audit.getComment(),audit.isHandler(),audit.isInterrupted());
		}
	}

	protected abstract void tickleParentProcess(ProcessInstance processInstance, TaskStatus status);
	protected abstract TaskBase endOfProcessDetected(ProcessInstance processInstance, Audit currentAudit);

	protected TaskBase getTask(Audit audit) {
		return getTask(audit.getProcessDefinitionName(), audit.getTaskId());
		
	}
	protected boolean handleAbort(ProcessInstance processInstance) {
		List<Audit> audits = findHandlerTasks(processInstance);
		for (Audit audit : audits) {
			TaskBase taskBase = getTask(audit);
			if (taskBase instanceof TaskTry) {
				TaskTry taskTry = (TaskTry) taskBase;
				tidyAuditTrail(processInstance,taskTry.getTaskId());
				ProcessDefinition pd = taskTry.getAbortHandler();
				if (pd == null) {
					continue;
				}
				pd.startProcess(processInstance);
				return true;
			}
		}
		return false;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		m_beanFactory = (DefaultListableBeanFactory)beanFactory;
	}

	public Resource getSchema() {
		return m_schema;
	}

	public void setSchema(Resource schema) {
		m_schema = schema;
	}

	public Resource getProcesses() {
		return m_processes;
	}

	public void setProcesses(Resource processes) {
		m_processes = processes;
	}

}
