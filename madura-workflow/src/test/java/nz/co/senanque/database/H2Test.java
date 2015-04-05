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

import javax.annotation.PostConstruct;

import nz.co.senanque.workflow.instances.ProcessInstance;
import nz.co.senanque.workflowtest.instances.Order;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Roger Parkinson
 * 
 * Verifies the behaviour of multiple data sources with JPA using Atomikos JTA
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class H2Test {

	private static final Logger log = LoggerFactory
			.getLogger(H2Test.class);

	@Autowired
	ApplicationContext m_applicationContext;
	@Autowired
	private Interface1 m_interface1;
	
	// The following is needed to ensure the destroy methods of all the beans
	// including those in the bundles, are called correctly
    private static AbstractApplicationContext s_applicationContext;
    @PostConstruct
    public void init() {
    	s_applicationContext = (AbstractApplicationContext)m_applicationContext;
    }
	@AfterClass
	public static void destroy() {
		s_applicationContext.close();
	}

	@Test
	public void testConnection() throws Exception {
		
		Order order = new Order();
		order.setOrderName("OK");
		order.setFahrenheit("90");
		ProcessInstance pi = new ProcessInstance();
		pi.setProcessDefinitionName("Whatever");
		getInterface1().saveObjects(pi, order);
	}

	public Interface1 getInterface1() {
		return m_interface1;
	}

	public void setInterface1(Interface1 interface1) {
		m_interface1 = interface1;
	}

}
