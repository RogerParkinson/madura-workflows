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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nz.co.senanque.workflow.instances.ProcessInstance;
import nz.co.senanque.workflowtest.instances.Order;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Roger Parkinson
 * 
 * Verifies the behaviour of multiple data sources with JPA
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class DatabaseTest {

	private static final Logger log = LoggerFactory
			.getLogger(DatabaseTest.class);

	@PersistenceContext(unitName="pu-workflow")
	private EntityManager m_workflow;
	
	@PersistenceContext(unitName="pu-local")
	private EntityManager m_local;
	
	
	@Test @Ignore
	public void testConnection() throws Exception {
		Order order = new Order();
		order.setOrderName("OK");
		order.setFahrenheit("90");
		m_local.persist(order);
		
		ProcessInstance pi = new ProcessInstance();
		pi.setProcessDefinitionName("Whatever");
		m_workflow.persist(pi);
		
	}
}
