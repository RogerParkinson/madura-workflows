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

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import nz.co.senanque.workflowtest.instances.Order;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.filter.ContentFilter;
import org.jdom.input.DOMBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;

/**
 * @author Roger Parkinson
 * 
 */
public class OrderReplyEndpoint {

	private static final Logger log = LoggerFactory
			.getLogger(OrderEndpoint.class);

	public void issueResponseFor(Message<org.w3c.dom.Document> orderResponse) {
		log.debug("processed orderResponse: correlationId {}", orderResponse
				.getHeaders().getCorrelationId());
		org.w3c.dom.Document doc = orderResponse.getPayload();
		Document document = new DOMBuilder().build(doc);
		Element root = document.getRootElement();
		
		Order context = new Order();
		@SuppressWarnings("unchecked")
		Iterator<Text> itr = (Iterator<Text>) root
				.getDescendants(new ContentFilter(ContentFilter.TEXT
						| ContentFilter.CDATA));
		while (itr.hasNext()) {
			Text text = itr.next();
			log.debug("name {} value {}", text.getParentElement().getName(),
					text.getValue());

			String name = getName(text);
			try {
				Class<?> targetType = PropertyUtils.getPropertyType(context, name);
				Object value = ConvertUtils.convert(text.getValue(), targetType);
				PropertyUtils.setProperty(context, name, value);
			} catch (IllegalAccessException e) {
				// Ignore these and move on
				log.debug("{} {}",name,e.getMessage());
			} catch (InvocationTargetException e) {
				// Ignore these and move on
				log.debug("{} {}",name,e.getMessage());
			} catch (NoSuchMethodException e) {
				// Ignore these and move on
				log.debug("{} {}",name,e.getMessage());
			}
		}
	}
	
	private String getName(Text text) {
		Element parent = text.getParentElement();
		String xpath = parent.getAttributeValue("xpath");
		if (xpath != null) {
			return xpath;
		}
		return parent.getName();
	}

}
