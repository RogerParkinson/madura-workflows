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

import nz.co.senanque.madura.bundle.BundleManager;
import nz.co.senanque.workflow.BundleSelector;
import nz.co.senanque.workflow.instances.ProcessInstance;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Roger Parkinson
 *
 */
@org.springframework.stereotype.Component
public class BundleSelectorImpl implements BundleSelector {

//	private static final Logger log = LoggerFactory
//			.getLogger(BundleSelectorImpl.class);
	@Autowired BundleManager m_bundleManager;

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.BundleSelector#selectBundle(nz.co.senanque.workflow.instances.ProcessInstance)
	 */
	@Override
	public void selectBundle(ProcessInstance pi) {
		m_bundleManager.setBundle(pi.getBundleName());
	}

	public BundleManager getBundleManager() {
		return m_bundleManager;
	}

	public void setBundleManager(BundleManager bundleManager) {
		m_bundleManager = bundleManager;
	}

}
