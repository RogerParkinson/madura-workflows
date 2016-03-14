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

import java.util.List;
import java.util.concurrent.locks.Lock;

import nz.co.senanque.locking.LockAction;
import nz.co.senanque.locking.LockFactory;
import nz.co.senanque.locking.LockTemplate;
import nz.co.senanque.workflow.instances.DeferredEvent;
import nz.co.senanque.workflow.instances.ProcessInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class is holds the target methods for the scheduler to fire.
 * 
 * @author Roger Parkinson
 * 
 */
public class ExecutorImpl implements Executor {

	private static final Logger log = LoggerFactory.getLogger(ExecutorImpl.class);
	@Autowired
	private WorkflowManager m_workflowManager;
	@Autowired
	private WorkflowDAO m_workflowDAO;
	@Autowired
	private LockFactory m_lockFactory;
	@Autowired
	private BundleSelector m_bundleSelector;
	
	boolean m_shuttingDown = false;
	boolean m_freeze = false;
	
	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#freeze()
	 */
	@Override
	public void freeze() {
		m_freeze = true;
	}
	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#resume()
	 */
	@Override
	public void resume() {
		m_freeze = false;
	}
	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#isFrozen()
	 */
	@Override
	public boolean isFrozen() {
		return m_freeze;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#execute(nz.co.senanque.workflow.instances.ProcessInstance)
	 */
	@Override
	public boolean execute(final ProcessInstance processInstance) {
		getBundleSelector().selectBundle(processInstance);
		List<Lock> locks = ContextUtils.getLocks(processInstance,getLockFactory(),"nz.co.senanque.workflow.Executor.execute(ProcessInstance)");
		LockTemplate lockTemplate = new LockTemplate(locks, new LockAction() {
			
			public void doAction() {
				log.debug("executing {}",processInstance.getId());
				getWorkflowManager().execute(processInstance.getId());
			}});
		return lockTemplate.doAction();
	}
	
	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#activeProcesses()
	 */
	@Override
	public void activeProcesses() {
		try {
			log.debug("active processes {} {}",m_shuttingDown,m_freeze);
			if (!m_shuttingDown && !m_freeze) {
				List<ProcessInstance> activeProcesses;
				try {
					activeProcesses = getWorkflowDAO().getActiveProcesses();
				} catch (RuntimeException e) {
					return;
				}
				while (!activeProcesses.isEmpty()) {
					execute(activeProcesses.get(0));
					activeProcesses = getWorkflowDAO().getActiveProcesses();
				}
				log.debug("active processes: exit");
			}
		} catch (Exception e) {
			log.debug("activeProcesses", e);
		}
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#deferredEvents()
	 */
	@Override
	public int deferredEvents() {
		int ret = 0;
		if (!m_shuttingDown && !m_freeze) {
			List<DeferredEvent> deferredEvents;
			try {
				deferredEvents = getWorkflowDAO().getDeferredEvents();
			} catch (RuntimeException e) {
				return 0;
			}
			for (DeferredEvent deferredEvent: deferredEvents) {
				final DeferredEvent finalDeferredEvent = deferredEvent;
				ProcessInstance processInstance = finalDeferredEvent.getProcessInstance();
				getBundleSelector().selectBundle(processInstance);
				List<Lock> locks = ContextUtils.getLocks(processInstance,getLockFactory(),"nz.co.senanque.workflow.Executor.deferredEvents()");
				LockTemplate lockTemplate = new LockTemplate(locks, new LockAction() {
	
					public void doAction() {
						getWorkflowManager().executeDeferredEvent(finalDeferredEvent.getId());
					}});
				if (lockTemplate.doAction()) {
					ret++;
				}
			}
		}
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#clearDeferredEvents()
	 */
	@Override
	public void clearDeferredEvents() {
		if (!m_shuttingDown && !m_freeze) {
			try {
				getWorkflowDAO().clearDeferredEvents();
			} catch (Exception e) {
				// ignore exceptions
			}
		}
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#getLockFactory()
	 */
	@Override
	public LockFactory getLockFactory() {
		return m_lockFactory;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#setLockFactory(nz.co.senanque.locking.LockFactory)
	 */
	@Override
	public void setLockFactory(LockFactory lockFactory) {
		m_lockFactory = lockFactory;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#getWorkflowDAO()
	 */
	@Override
	public WorkflowDAO getWorkflowDAO() {
		return m_workflowDAO;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#setWorkflowDAO(nz.co.senanque.workflow.WorkflowDAO)
	 */
	@Override
	public void setWorkflowDAO(WorkflowDAO workflowDAO) {
		m_workflowDAO = workflowDAO;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#getWorkflowManager()
	 */
	@Override
	public WorkflowManager getWorkflowManager() {
		return m_workflowManager;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#setWorkflowManager(nz.co.senanque.workflow.WorkflowManager)
	 */
	@Override
	public void setWorkflowManager(WorkflowManager workflowManager) {
		m_workflowManager = workflowManager;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#shutdown()
	 */
	@Override
	public void shutdown() {
		m_shuttingDown = true;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#getBundleSelector()
	 */
	@Override
	public BundleSelector getBundleSelector() {
		return m_bundleSelector;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#setBundleSelector(nz.co.senanque.workflow.BundleSelector)
	 */
	@Override
	public void setBundleSelector(BundleSelector bundleSelector) {
		m_bundleSelector = bundleSelector;
	}

}
