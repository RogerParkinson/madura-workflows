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

/**
 * @author Roger Parkinson
 *
 */
public class QueueDefinition implements Comparable<QueueDefinition> {
	
	private final String m_name;
	private String m_permission;
	private String m_readPermission;
	private String m_version;

	public QueueDefinition(String name) {
		m_name = name;
	}

	public String getName() {
		return m_name;
	}

	public void setPermission(String permission) {
		m_permission = permission;		
	}

	public void setReadPermission(String readPermission) {
		m_readPermission = readPermission;		
	}

	public String getPermission() {
		return m_permission;
	}

	public String getReadPermission() {
		return m_readPermission;
	}
	public int compareTo(QueueDefinition pd) {
		return this.getName().compareTo(pd.getName());
	}

	public void setVersion(String version) {
		m_version = version;
	}

	public String getVersion() {
		return m_version;
	}
	
	public String getFullName() {
		return m_name+":"+m_version;
	}

}
