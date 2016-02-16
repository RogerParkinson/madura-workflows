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
package nz.co.senanque.forms;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This gives us a form based on a form name. The form is always a bean, usually a prototype.
 * 
 * @author Roger Parkinson
 *
 */
@org.springframework.stereotype.Component("formFactory")
public class FormFactoryImpl implements BeanFactoryAware, FormFactory {
	
	private static final Logger log = LoggerFactory
			.getLogger(FormFactoryImpl.class);
	
	@Autowired(required=false) private FormEnvironment m_environment;
	private Map<String,Map<String,String>> m_formsMap = new HashMap<String,Map<String,String>>();
	private BeanFactory m_beanFactory;
	private String m_environmentName="";
	
	/* (non-Javadoc)
	 * @see nz.co.senanque.forms.FormFactory#getForm(java.lang.String)
	 */
	@Override
	public WorkflowForm getForm(String formName) {
		if (m_formsMap != null && !m_formsMap.isEmpty()) {
			Map<String,String> environmentEntry = m_formsMap.get(m_environmentName);
			if (environmentEntry == null) {
				environmentEntry = m_formsMap.values().iterator().next();
			}
			String beanName = environmentEntry.get(formName);
			if (beanName == null) {
				beanName = m_environmentName+StringUtils.capitalize(formName);
			}
			return m_beanFactory.getBean(beanName, WorkflowForm.class);
		}
		String beanName = m_environmentName+StringUtils.capitalize(formName);
		return m_beanFactory.getBean(beanName, WorkflowForm.class);
	}

	public FormEnvironment getEnvironment() {
		return m_environment;
	}

	public void setEnvironment(FormEnvironment environment) {
		m_environment = environment;
	}

	public Map<String, Map<String, String>> getFormsMap() {
		return m_formsMap;
	}

	public void setFormsMap(Map<String, Map<String, String>> formsMap) {
		m_formsMap = formsMap;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		m_beanFactory = beanFactory;
	}
	@PostConstruct
	public void init() {
		if (m_environment != null) {
			m_environmentName = m_environment.getName();
		}
	}

}
