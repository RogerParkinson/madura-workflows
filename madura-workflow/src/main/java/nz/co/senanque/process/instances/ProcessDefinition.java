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
package nz.co.senanque.process.instances;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nz.co.senanque.workflow.ProcessInstanceUtils;
import nz.co.senanque.workflow.WorkflowManager;
import nz.co.senanque.workflow.instances.ProcessInstance;
import nz.co.senanque.workflow.instances.TaskStatus;


public class ProcessDefinition implements Comparable<ProcessDefinition> {

    private final String m_name;
    private int m_generatedSubprocesses = 0;
    protected List<TaskBase> m_tasks = new ArrayList<TaskBase>();
	private final ProcessDefinition m_ownerProcess;
	private final String m_className;
	private final WorkflowManager m_workflowManager;
	private long m_nexTaskId = 0;
	private final String m_launchForm;
	private final String m_packageName;
	private final String m_queueName;
	private String m_version="";
	private final String m_description;
	
	public ProcessDefinition(ProcessDefinition ownerProcess) {
		m_ownerProcess = ownerProcess;
		m_name = generateSubprocessDefinition();
		m_className = ownerProcess.getClassName();
		m_workflowManager = ownerProcess.getWorkflowManager();
		m_workflowManager.addSubProcess(this);
		m_launchForm = null;
		m_packageName = ownerProcess.getPackageName();
		m_queueName = null;
		m_description = "";
	}
	public ProcessDefinition(String name, String className,WorkflowManager workflowManager, String launchForm, String queueName, String packageName, String description) {
		m_name = name;
		m_ownerProcess = null;
		m_className = className;
		workflowManager.addMainProcess(this);
		m_workflowManager = workflowManager;
		m_launchForm = launchForm;
		m_packageName = packageName;
		m_queueName = queueName;
		m_description = description;
	}
	public String getName() {
		return m_name;
	}
	public List<TaskBase> getTasks() {
		return Collections.unmodifiableList(m_tasks);
	}
	public void addTask(TaskBase task) {
		this.m_tasks.add(task);
	}
	public String generateSubprocessDefinition()
	{
		return m_ownerProcess.m_name+"_"+(++(m_ownerProcess.m_generatedSubprocesses));
	}
	public String getClassName() {
		return m_className;
	}
	public String getFullClassName() {
		return m_packageName+m_className;
	}
	public String getFullName() {
		return m_name+": "+m_description;
	}
	public ProcessDefinition getOwnerProcess() {
		return m_ownerProcess;
	}
	public String toString() {
		String ret = Indentation.getIndent()+"ProcessDefinition [m_name=" + m_name
				+ ", m_generatedSubprocesses=" + m_generatedSubprocesses
				+ ", m_className=" + m_packageName+m_className 
				+ ", m_tasks=\n" +Indentation.increment()+ m_tasks +Indentation.decrement()+"]\n";
		return ret;
	}
	public int compareTo(ProcessDefinition pd) {
		return this.getName().compareTo(pd.getName());
	}
	public WorkflowManager getWorkflowManager() {
		return m_workflowManager;
	}
	public long getNextTaskId() {
		return m_nexTaskId++;
	}
	public void startProcess(ProcessInstance pi) {
		pi.setProcessDefinitionName(getName());
		pi.setTaskId(0L);
		ProcessInstanceUtils.clearQueue(pi, TaskStatus.PENDING);
	}
	public void loadTask(ProcessInstance processInstance, long taskId) {
		startProcess(processInstance);
		processInstance.setTaskId(taskId);
	}
	public TaskBase getTask(long taskId) {
		for (TaskBase task: getTasks()) {
			if (task.getTaskId() == taskId) {
				return task;
			}
		}
		return null;
	}
	public String getLaunchForm() {
		if (m_launchForm != null) {
			return m_launchForm;
		}
		if (m_ownerProcess != null) {
			return m_ownerProcess.getLaunchForm();
		}
		return null;
	}
	public String getPackageName() {
		return m_packageName;
	}
	public String getQueueName() {
		return m_queueName;
	}
	public String getVersion() {
		return m_version;
	}
	public void setVersion(String version) {
		m_version = version;
	}
	public String getDescription() {
		return m_description;
	}
}
