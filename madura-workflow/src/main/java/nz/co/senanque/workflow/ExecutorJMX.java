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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * @author Roger Parkinson
 *
 */
@ManagedResource(objectName = "nz.co.senanque.workflow:name=ExecutorJMX")
public class ExecutorJMX  implements BeanFactoryAware{

	private static final Logger log = LoggerFactory.getLogger(ExecutorJMX.class);
	
	private Executor m_executor;

	boolean m_freeze = false;

	private DefaultListableBeanFactory m_beanFactory;
	
	@ManagedOperation
	public void freeze() {
		getExecutor().freeze();
	}
	@ManagedOperation
	public void resume() {
		getExecutor().resume();
	}
	@ManagedOperation
	public boolean isFrozen() {
		return getExecutor().isFrozen();
	}
	@Override
	public void setBeanFactory(BeanFactory arg0) throws BeansException {
		m_beanFactory = (DefaultListableBeanFactory)arg0;
	}
	private Executor getExecutor() {
		if (m_executor == null) {
			try {
				m_executor = (Executor)m_beanFactory.getBean(nz.co.senanque.workflow.Executor.class);
			} catch (BeansException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return m_executor;
	}
}
