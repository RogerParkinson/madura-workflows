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

import java.util.Collection;

import nz.co.senanque.forms.WorkflowForm;
import nz.co.senanque.locking.LockFactory;
import nz.co.senanque.workflow.instances.Audit;
import nz.co.senanque.workflow.instances.ProcessInstance;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Roger Parkinson
 *
 */
public class WorkflowClient {
	
	@Autowired 
	private WorkflowManager m_workflowManager;
	@Autowired
	private BundleSelector m_bundleSelector;
	@Autowired
	private InitialBundleSelector m_initialBundleSelector;
	@Autowired
	private LockFactory m_lockFactory;

	public ProcessInstance launch(String processName, Object context, String comment) {
		String bundleName = getInitialBundleSelector().selectInitialBundle(context);
		return m_workflowManager.launch(processName, context, comment, bundleName);
	}

	public long launch(WorkflowForm launchForm) {
		return launch(launchForm, "");
	}
	
	public long launch(WorkflowForm launchForm, String comment) {
		String bundleName = getInitialBundleSelector().selectInitialBundle(launchForm.getProcessName());
		return m_workflowManager.launch(launchForm, comment, bundleName);
	}
	
	public WorkflowManager getWorkflowManager() {
		return m_workflowManager;
	}

	public void setWorkflowManager(WorkflowManager workflowManager) {
		m_workflowManager = workflowManager;
	}

	public BundleSelector getBundleSelector() {
		return m_bundleSelector;
	}

	public void setBundleSelector(BundleSelector bundleSelector) {
		m_bundleSelector = bundleSelector;
	}

	public WorkflowForm getLaunchForm(String processName) {
		getInitialBundleSelector().selectInitialBundle(processName);
		return m_workflowManager.getLaunchForm(processName);
	}

	public void launch(String processName, Object o) {
		launch(processName,o,null);
	}
	public ProcessInstance lockProcessInstance(final ProcessInstance processInstance, final boolean techSupport, final String userName) {
		return m_workflowManager.lockProcessInstance(processInstance, techSupport, userName);
	}
	public void finishLaunch(long processId) {
		m_workflowManager.finishLaunch(processId);
	}

	public WorkflowForm getCurrentForm(ProcessInstance processInstance) {
		getBundleSelector().selectBundle(processInstance);
		return m_workflowManager.getCurrentForm(processInstance);
	}
	public long save(WorkflowForm workflowForm) {
		return m_workflowManager.save(workflowForm);
	}

	public Collection<Audit> getAudits(ProcessInstance processInstance) {
		return m_workflowManager.getAudits(processInstance);
	}

	public LockFactory getLockFactory() {
		return m_lockFactory;
	}

	public void setLockFactory(LockFactory lockFactory) {
		m_lockFactory = lockFactory;
	}

	public InitialBundleSelector getInitialBundleSelector() {
		return m_initialBundleSelector;
	}

	public void setInitialBundleSelector(InitialBundleSelector initialBundleSelector) {
		m_initialBundleSelector = initialBundleSelector;
	}

}
