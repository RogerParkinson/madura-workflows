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
package nz.co.senanque.database;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nz.co.senanque.workflow.ContextDAO;
import nz.co.senanque.workflow.ContextUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements the context handling used by workflow manager
 * The context is the database that holds process contexts as opposed to the workflow
 * management database.
 * 
 * @author Roger Parkinson
 *
 */
public class ContextJPAMock implements ContextDAO {

	private static final Logger log = LoggerFactory
			.getLogger(ContextJPAMock.class);

	@PersistenceContext(unitName="pu-workflow")
	private EntityManager m_entityManager;

	@Transactional(value="pu-workflow")
	public Object getContext(String contextDescriptor) {
		return  m_entityManager.find(ContextUtils.getContextClass(contextDescriptor),ContextUtils.getContextId(contextDescriptor));
	}
	public String createContextDescriptor(Object o) {
		Object id = m_entityManager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(o);
		return ContextUtils.createContextDescriptor(o, id);
	}
	@Transactional(value="pu-workflow")
	public Object mergeContext(Object context) {
		Object ret =  m_entityManager.merge(context);
		m_entityManager.flush();
		return ret;
	}
	@Transactional(value="pu-workflow")
	public void persistContext(Object context) {
		m_entityManager.persist(context);
		m_entityManager.flush();
	}
}
