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

import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Roger Parkinson
 *
 */
public class MessageEndpoint {

	private static final Logger log = LoggerFactory.getLogger(MessageEndpoint.class);
	private Unmarshaller m_unmarshaller;

//	public Source issueResponseFor(DOMSource request) {
//		
//		try {
//			Object graph = getUnmarshaller().unmarshal(request);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//		return new DomSourceFactory().createSource(
//				"<echoResponse xmlns=\"http://www.springframework.org/spring-ws/samples/echo\">" +
//				request.getNode().getTextContent() + "</echoResponse>");
//	}
	
	public Unmarshaller getUnmarshaller() {
		return m_unmarshaller;
	}

	public void setUnmarshaller(Unmarshaller unmarshaller) {
		m_unmarshaller = unmarshaller;
	}

}
