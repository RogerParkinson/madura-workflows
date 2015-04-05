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

import nz.co.senanque.workflow.instances.ProcessInstance;
import nz.co.senanque.workflow.orderinstances.Order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Roger Parkinson
 *
 */
public class ContextTesterImpl implements ContextTester {
	
	@Autowired private ContextDAO m_contextDAO;
	@Autowired private WorkflowDAO m_workflowDAO;
	
	/* (non-Javadoc)
	 * @see nz.co.senanque.workflowtest.ContextTester#createOrder()
	 */
	@Transactional
	public String createOrder() {
		Order order = new Order();
		ClassLoader cl = order.getClass().getClassLoader();
		order.setOrderName("Hello");
		order = (Order)getContextDAO().mergeContext(order);
		getContextDAO().persistContext(order);
		return getContextDAO().createContextDescriptor(order);
	}
	
	@Transactional
	public Object getOrder(String contextDescriptor) {
		Order order = (Order)getContextDAO().getContext(contextDescriptor);
		return null;
	}

	@Transactional
	public ProcessInstance getProcessInstance(long id) {
		return getWorkflowDAO().findProcessInstance(id);
	}

	public ContextDAO getContextDAO() {
		return m_contextDAO;
	}

	public void setContextDAO(ContextDAO contextDAO) {
		m_contextDAO = contextDAO;
	}

	public WorkflowDAO getWorkflowDAO() {
		return m_workflowDAO;
	}

	public void setWorkflowDAO(WorkflowDAO workflowDAO) {
		m_workflowDAO = workflowDAO;
	}

}
