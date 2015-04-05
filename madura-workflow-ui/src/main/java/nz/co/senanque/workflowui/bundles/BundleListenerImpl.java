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
package nz.co.senanque.workflowui.bundles;

import java.io.Serializable;

import nz.co.senanque.madura.bundlemap.BundleVersion;
import nz.co.senanque.workflow.WorkflowManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Workflow implementation of a bundle listener
 * 
 * @author Roger Parkinson
 * @version $Revision:$
 */
public class BundleListenerImpl extends nz.co.senanque.perspectivemanager.BundleListenerImpl implements Serializable
{
	private static final long serialVersionUID = 921936781648787811L;

	private Logger m_logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired QueueProcessManager m_queueProcessManager;
	
    /* (non-Javadoc)
     * @see nz.co.senanque.madura.bundle.BundleListener#add(java.lang.String, nz.co.senanque.madura.bundle.BundleRoot)
     */
    public void add(BundleVersion bv)
    {
    	super.add(bv);
        // Find the queues defined in this workflow bundle and add them to our global list.
        // This allows us to present all the queues to the user.
        try {
        	WorkflowManager workflowManager = bv.getRoot().getApplicationContext().getBean("workflowManager",WorkflowManager.class);
//        	m_queueProcessManager.extractQueues(workflowManager.getQueues(),bundleName);        	
//        	m_queueProcessManager.extractProcesses(workflowManager.getMainProcesses(),bundleName);
        	m_queueProcessManager.extract(bv.getName(),workflowManager.getQueues(), workflowManager.getMainProcesses());
		} catch (BeansException e) {
			// ignore bundles with missing bean, assume they do something else.
			m_logger.debug(e.getMessage());
		}
    }
    public void remove(BundleVersion bv)
    {
    	super.remove(bv);
    	m_queueProcessManager.removeBundle(bv.getName());
    }

	public QueueProcessManager getQueueProcessManager() {
		return m_queueProcessManager;
	}

	public void setQueueProcessManager(QueueProcessManager queueProcessManager) {
		m_queueProcessManager = queueProcessManager;
	}

}
