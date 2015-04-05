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
import javax.xml.transform.Result;

import nz.co.senanque.workflow.ContextDAO;
import nz.co.senanque.workflow.WorkflowException;
import nz.co.senanque.workflow.instances.ProcessInstance;
import nz.co.senanque.workflowtest.instances.Order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.MessagingException;
import org.springframework.integration.xml.result.DomResultFactory;
import org.springframework.oxm.GenericMarshaller;
import org.springframework.transaction.annotation.Transactional;

public class Class1_3 implements Interface1 {

	@PersistenceContext(unitName="em-workflow")
	private EntityManager m_entityManager;
	
	@Autowired ContextDAO m_contextDAO;
	
	@Autowired GenericMarshaller m_marshaller;
	DomResultFactory m_resultFactory;
	
	public Class1_3() {
		m_resultFactory = new DomResultFactory();
	}
	@Transactional
	public void saveObjects(ProcessInstance pi, Order o) {
		m_entityManager.persist(pi);
		m_entityManager.flush();
		Object oo = getContextDAO().mergeContext(o);
		String contextDescriptor = getContextDAO().createContextDescriptor(oo);
		Order o1 = (Order) getContextDAO().getContext(contextDescriptor);
		
		Result result = m_resultFactory.createResult(o1);
		if (result == null) {
			throw new MessagingException(
					"Unable to marshal payload, ResultFactory returned null.");
		}
		try {
			m_marshaller.marshal(o1, result);
		} catch (Exception e) {
			throw new WorkflowException("Failed to marshal payload",e);
		}
	}

	public GenericMarshaller getMarshaller() {
		return m_marshaller;
	}

	public void setMarshaller(GenericMarshaller marshaller) {
		m_marshaller = marshaller;
	}
	public ContextDAO getContextDAO() {
		return m_contextDAO;
	}
	public void setContextDAO(ContextDAO contextDAO) {
		m_contextDAO = contextDAO;
	}
}
