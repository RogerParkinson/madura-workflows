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

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import nz.co.senanque.process.instances.ProcessDefinition;
import nz.co.senanque.process.instances.QueueDefinition;
import nz.co.senanque.vaadinsupport.permissionmanager.PermissionManager;
import nz.co.senanque.workflow.instances.TaskStatus;
import nz.co.senanque.workflowui.FixedPermissions;

import org.springframework.util.StringUtils;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.Or;

/**
 * Stores the queues and processes that the bundled workflows declare.
 * This allows the UI to easily query all the queues. The queues are updated when bundles
 * are registered.
 * 
 * @author Roger Parkinson
 *
 */
public class QueueProcessManager {

	private Set<QueueDefinition> m_queues = new TreeSet<>(new Comparator<QueueDefinition>(){

		@Override
		public int compare(QueueDefinition o1, QueueDefinition o2) {
			return o1.getFullName().compareTo(o2.getFullName());
		}});
	private Set<ProcessDefinition> m_processes = new TreeSet<>();

    private void extractQueues(Set<QueueDefinition> queues, String bundleName) {
    	for (QueueDefinition queue: queues) {
    		queue.setVersion(bundleName);
    		m_queues.add(queue);
    	}
    }

	/**
	 * Get the list of queues visible to this user ie to the user with the permissions
	 * defined in the given permissions manager. ADMIN permission means all are visible.
	 * @param permissionManager
	 * @return list of queue names.
	 */
	public Set<String> getVisibleQueues(PermissionManager permissionManager) {
		Set<String> ret = new HashSet<String>();
		for (QueueDefinition queueDefinition: m_queues) {
			if (permissionManager.hasPermission(FixedPermissions.ADMIN)) {
				ret.add(queueDefinition.getName());
				continue;
			}
			if (!ret.contains(queueDefinition.getName())) {
				if (permissionManager.hasPermission(queueDefinition.getPermission()) ||
						permissionManager.hasPermission(queueDefinition.getReadPermission())) {
					ret.add(queueDefinition.getName());
				}
			}
		}
		return ret;
	}
	/**
	 * Get the list of queues writeable for this user ie to the user with the permissions
	 * defined in the given permissions manager. ADMIN permission means all are writeable.
	 * @param permissionManager
	 * @return list of queue names
	 */
	public Set<String> getWriteableQueues(PermissionManager permissionManager) {
		Set<String> ret = new HashSet<String>();
		for (QueueDefinition queueDefinition: m_queues) {
			if (permissionManager.hasPermission(FixedPermissions.ADMIN)||permissionManager.hasPermission(FixedPermissions.TECHSUPPORT)) {
				ret.add(queueDefinition.getName());
				continue;
			}
			if (!ret.contains(queueDefinition.getName())) {
				if (permissionManager.hasPermission(queueDefinition.getPermission())) {
					ret.add(queueDefinition.getName());
				}
			}
		}
		return ret;
		
	}
	/**
	 * If we have the TECHSUPPORT or ADMIN permission then return null. Those users can see everything so no filter required.
	 * For the rest we only display queues they have permission to see and only processes in WAIT status.
	 * @param permissionManager
	 * @return filter
	 */
	public Filter getQueueFilter(PermissionManager permissionManager) {
		if (permissionManager.hasPermission(FixedPermissions.TECHSUPPORT) || permissionManager.hasPermission(FixedPermissions.ADMIN)) {
			return null;
		}
		Set<String> visibleQueues = getVisibleQueues(permissionManager);
		Filter filters[] = new Filter[visibleQueues.size()];
		int i=0;
		for (String queueName: visibleQueues) {
			filters[i++] = new Compare.Equal("queueName", queueName);
		}
		Filter statusFilter[] = new Filter[2];
		statusFilter[0] = new Or(filters);
		statusFilter[1] = new Compare.Equal("status", TaskStatus.WAIT);
		
		Filter userFilter[] = new Filter[2];
		userFilter[0] = new Compare.Equal("lockedBy",permissionManager.getCurrentUser());
		userFilter[1] = new Compare.Equal("status", TaskStatus.BUSY);

		return new Or(new And(statusFilter),new And(userFilter));
	}

	/**
	 * Get the list of queues visible to this user ie to the user with the permissions
	 * defined in the given permissions manager,
	 * @param permissionManager
	 * @return
	 */
	public Set<ProcessDefinition> getVisibleProcesses(PermissionManager permissionManager) {
		Set<ProcessDefinition> ret = new HashSet<>();
		Set<String> queues = getWriteableQueues(permissionManager);
		String lastProcessName = "";
		for (ProcessDefinition processDefinition: m_processes) {
			String processName = processDefinition.getName();
			if (!processName.equals(lastProcessName)) {
				String queueName = processDefinition.getQueueName();
				if (StringUtils.hasText(queueName)) {
					if (queues.contains(queueName)) {
						ret.add(processDefinition);
					}
				} else {
					ret.add(processDefinition);					
				}
				lastProcessName = processName;
			}
		}
		return ret;
	}

	private void extractProcesses(Set<ProcessDefinition> mainProcesses, String bundleName) {
		for (ProcessDefinition pd: mainProcesses) {
			pd.setVersion(bundleName);
			m_processes.add(pd);
		}
	}
	public void removeBundle(String bundleName) {
		synchronized (m_queues) {
			Set<QueueDefinition> deleteQueueDefinitions = new HashSet<>();
			for (QueueDefinition queueDefinition: m_queues) {
				if (bundleName.equals(queueDefinition.getVersion())) {
					deleteQueueDefinitions.add(queueDefinition);
				}
			}
			Set<ProcessDefinition> deleteProcessDefinitions = new HashSet<>();
			for (ProcessDefinition processDefinition: m_processes) {
				if (bundleName.equals(processDefinition.getVersion())) {
					deleteProcessDefinitions.add(processDefinition);
				}
			}
			for (QueueDefinition queueDefinition: deleteQueueDefinitions) {
				m_queues.remove(queueDefinition);
			}
			for (ProcessDefinition processDefinition: deleteProcessDefinitions) {
				m_processes.remove(processDefinition);
			}
		}
	}

	public void extract(String bundleName, Set<QueueDefinition> queues,
			Set<ProcessDefinition> processes) {
		synchronized (m_queues) {
			extractQueues(queues,bundleName);        	
			extractProcesses(processes,bundleName);
		}
	}

}
