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
import nz.co.senanque.workflowtest.instances.OrderItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

/**
 * @author Roger Parkinson
 *
 */
public class OrderEndpoint {
	
	private static final Logger log = LoggerFactory
			.getLogger(OrderEndpoint.class);

	public Message<Order> issueResponseFor(Message<Order> order) {
		if (order.getPayload().getOrderName().equals("fatal error")) {
			throw new RuntimeException("fatal error");
		}
		if (order.getPayload().getOrderName().equals("recoverable error")) {
			throw new RuntimeException("recoverable error");
		}
		Order ret = new ObjectFactory().createOrder();
		OrderItem orderItem = new ObjectFactory().createOrderItem();
		orderItem.setItemName("#1");
		ret.setRejected(false);
		ret.setOrderName("whatever");
		ret.getOrderItems().add(orderItem);
		log.debug("processed order: correlationId {}",order.getHeaders().get(IntegrationMessageHeaderAccessor.CORRELATION_ID,Long.class));
		MessageBuilder<Order> messageBuilder = MessageBuilder.withPayload(ret);
		messageBuilder.copyHeaders(order.getHeaders());
		return messageBuilder.build();
	}

}
