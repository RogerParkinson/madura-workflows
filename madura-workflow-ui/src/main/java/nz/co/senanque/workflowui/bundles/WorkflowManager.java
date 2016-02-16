/**
 * 
 */
package nz.co.senanque.workflowui.bundles;

import nz.co.senanque.madura.bundle.BundleInterface;


/**
 * Dummy interface that just extends the one we want and adds the bundle annotation.
 * This allows us to avoid having core workflow depend on bundles.
 * 
 * @author Roger Parkinson
 *
 */
@BundleInterface("workflowManager")
public interface WorkflowManager extends nz.co.senanque.workflow.WorkflowManager {

}
