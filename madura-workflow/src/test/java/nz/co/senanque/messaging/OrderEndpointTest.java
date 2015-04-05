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
package nz.co.senanque.messaging;

import nz.co.senanque.workflowtest.instances.ObjectFactory;
import nz.co.senanque.workflowtest.instances.Order;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Roger Parkinson
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class OrderEndpointTest {

	private static final Logger log = LoggerFactory
			.getLogger(OrderEndpointTest.class);
	@Autowired
	ApplicationContext m_applicationContext;
	@Autowired
	MessageSenderMock<Order> m_messageSender;

	@Test
	public void testSendOrder() throws Exception {
		Order order = new ObjectFactory().createOrder();
		order.setOrderName("first");
		m_messageSender.send(order, 100L);
//		Thread.sleep(60000);
	}
}
