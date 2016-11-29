/**
 * 
 */
package nz.co.senanque.workflowimpl;

import nz.co.senanque.madura.bundle.AbstractBundleManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author Roger Parkinson
 *
 */
@Component
public class BundleScheduler {
	
	@Autowired AbstractBundleManager m_bundleManager;

	@Scheduled(fixedDelayString="${nz.co.senanque.workflowimpl.BundleScheduler.scan:10000}")
	public void activeProcesses() throws Exception {
		m_bundleManager.scan();
	}
}
