/**
 * 
 */
package nz.co.senanque.workflowimpl;

import nz.co.senanque.locking.LockFactory;
import nz.co.senanque.workflow.BundleSelector;
import nz.co.senanque.workflow.Executor;
import nz.co.senanque.workflow.WorkflowDAO;
import nz.co.senanque.workflow.WorkflowManager;
import nz.co.senanque.workflow.instances.ProcessInstance;

import org.springframework.stereotype.Component;

/**
 * This is needed as a dummy class to satisfy the schedule bean instantiated during testing.
 * It does nothing, but it stops the schedule bean complaining there is no executor.
 * 
 * @author Roger Parkinson
 *
 */
@Component
public class ExecutorTest implements Executor {

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#freeze()
	 */
	@Override
	public void freeze() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#resume()
	 */
	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#isFrozen()
	 */
	@Override
	public boolean isFrozen() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#execute(nz.co.senanque.workflow.instances.ProcessInstance)
	 */
	@Override
	public boolean execute(ProcessInstance processInstance) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#activeProcesses()
	 */
	@Override
	public void activeProcesses() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#deferredEvents()
	 */
	@Override
	public int deferredEvents() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#clearDeferredEvents()
	 */
	@Override
	public void clearDeferredEvents() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#getLockFactory()
	 */
	@Override
	public LockFactory getLockFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#setLockFactory(nz.co.senanque.locking.LockFactory)
	 */
	@Override
	public void setLockFactory(LockFactory lockFactory) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#getWorkflowDAO()
	 */
	@Override
	public WorkflowDAO getWorkflowDAO() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#setWorkflowDAO(nz.co.senanque.workflow.WorkflowDAO)
	 */
	@Override
	public void setWorkflowDAO(WorkflowDAO workflowDAO) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#getWorkflowManager()
	 */
	@Override
	public WorkflowManager getWorkflowManager() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#setWorkflowManager(nz.co.senanque.workflow.WorkflowManager)
	 */
	@Override
	public void setWorkflowManager(WorkflowManager workflowManager) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#shutdown()
	 */
	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#getBundleSelector()
	 */
	@Override
	public BundleSelector getBundleSelector() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.Executor#setBundleSelector(nz.co.senanque.workflow.BundleSelector)
	 */
	@Override
	public void setBundleSelector(BundleSelector bundleSelector) {
		// TODO Auto-generated method stub

	}

}
