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
package nz.co.senanque.workflowui.conf;

import java.io.Serializable;

import nz.co.senanque.madura.bundle.BundleManager;
import nz.co.senanque.permissionmanager.PermissionManager;
import nz.co.senanque.process.instances.ProcessDefinition;
import nz.co.senanque.workflow.InitialBundleSelector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.spring.annotation.UIScope;

/**
 * @author Roger Parkinson
 *
 */
@UIScope
@org.springframework.stereotype.Component
public class InitialBundleSelectorImpl implements InitialBundleSelector, Serializable {
	
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory
			.getLogger(InitialBundleSelectorImpl.class);
	@Autowired BundleManager m_bundleManager;
	@Autowired QueueProcessManager m_queueProcessmanager;
	@Autowired PermissionManager m_permissionManager;

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.BundleSelector#selectInitialBundle(java.lang.Object)
	 */
	@Override
	public String selectInitialBundle(Object o) {
		if (o instanceof String) {
			String processName = (String)o;
			for (ProcessDefinition processDefinition :m_queueProcessmanager.getVisibleProcesses(m_permissionManager)) {
				if (processDefinition.getName().equals(processName)) {
					m_bundleManager.setBundle(processDefinition.getVersion());
					return processDefinition.getVersion();
				}
			}
		}
		m_bundleManager.setBundle("order-workflow");
		log.debug("Selected bundle order-workflow");
		return "order-workflow";
	}

	public BundleManager getBundleManager() {
		return m_bundleManager;
	}

	public void setBundleManager(BundleManager bundleManager) {
		m_bundleManager = bundleManager;
	}

	public QueueProcessManager getQueueProcessmanager() {
		return m_queueProcessmanager;
	}

	public void setQueueProcessmanager(QueueProcessManager queueProcessmanager) {
		m_queueProcessmanager = queueProcessmanager;
	}

	public PermissionManager getPermissionManager() {
		return m_permissionManager;
	}

	public void setPermissionManager(PermissionManager permissionManager) {
		m_permissionManager = permissionManager;
	}

}
