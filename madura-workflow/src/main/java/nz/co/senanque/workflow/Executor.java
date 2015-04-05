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

import nz.co.senanque.locking.LockFactory;
import nz.co.senanque.workflow.instances.ProcessInstance;

public interface Executor {

	//	@ManagedOperation
	public abstract void freeze();

	//	@ManagedOperation
	public abstract void resume();

	//	@ManagedOperation
	public abstract boolean isFrozen();

	/**
	 * lock the process instance and execute it.
	 * Normally called privately but exposed as public for testing.
	 *  
	 * @param processInstance
	 */
	public abstract boolean execute(final ProcessInstance processInstance);

	/**
	 * This method is scheduled to run regularly.
	 * It requests all the active processes, picks the first one and executes it.
	 */
	public abstract void activeProcesses();

	/**
	 * This method is scheduled to run regularly.
	 * It requests all the deferred events that are due and flags them.
	 * The process instance is locked beforehand.
	 * It returns count if it did anything.
	 */
	public abstract int deferredEvents();

	/**
	 * This method is scheduled to run regularly.
	 * It requests all the deferred events that are DONE and removes them.
	 * So basically just a bit of housekeeping.
	 */
	public abstract void clearDeferredEvents();

	public abstract LockFactory getLockFactory();

	public abstract void setLockFactory(LockFactory lockFactory);

	public abstract WorkflowDAO getWorkflowDAO();

	public abstract void setWorkflowDAO(WorkflowDAO workflowDAO);

	public abstract WorkflowManager getWorkflowManager();

	public abstract void setWorkflowManager(WorkflowManager workflowManager);

	public abstract void shutdown();

	public abstract BundleSelector getBundleSelector();

	public abstract void setBundleSelector(BundleSelector bundleSelector);

}