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
package nz.co.senanque.workflow.simple;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nz.co.senanque.workflow.ContextDAO;
import nz.co.senanque.workflow.ContextUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements the context handling used by workflow manager
 * The context is the database that holds process contexts as opposed to the workflow
 * management database.
 * 
 * @author Roger Parkinson
 *
 */
@Component("contextDAO")
public class ContextJPA implements ContextDAO {

	private static final Logger log = LoggerFactory
			.getLogger(ContextJPA.class);

	@PersistenceContext(unitName="pu-simple")
	private EntityManager m_entityManager;

	@Transactional
	public Object getContext(String contextDescriptor) {
		log.debug("getContext {}",contextDescriptor);
		Object ret =  m_entityManager.find(ContextUtils.getContextClass(contextDescriptor),ContextUtils.getContextId(contextDescriptor));
		log.debug("fetched context {}",ret);
		m_entityManager.refresh(ret);
		return ret;
	}
	public String createContextDescriptor(Object o) {
		Object id = m_entityManager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(o);
		return ContextUtils.createContextDescriptor(o, id);
	}
	@Transactional
	public Object mergeContext(Object context) {
		log.debug("{}",context);
		Object ret =  m_entityManager.merge(context);
		m_entityManager.flush();
		return ret;
	}
	@Transactional
	public void persistContext(Object context) {
		log.debug("{}",context);
		m_entityManager.persist(context);
		m_entityManager.flush();
	}
}
