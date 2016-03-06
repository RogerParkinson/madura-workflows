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
package nz.co.senanque.messaging.springintegration;

import javax.annotation.PostConstruct;
import javax.xml.transform.Result;

import nz.co.senanque.messaging.MessageSender;
import nz.co.senanque.workflow.WorkflowException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.xml.result.DomResultFactory;
import org.springframework.integration.xml.result.ResultFactory;
import org.springframework.integration.xml.transformer.ResultToDocumentTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.oxm.GenericMarshaller;
import org.w3c.dom.Document;


/**
 * @author Roger Parkinson
 *
 */
public class MessageSenderImpl<T> implements MessageSender<T> {

	private MessageChannel m_channel;
	private MessageChannel m_replyChannel;
	private MessageChannel m_errorChannel;
	private Integer m_messagePriority;
	@Autowired(required=false)
	private GenericMarshaller m_marshaller;
	private volatile ResultFactory m_resultFactory;
	private ResultToDocumentTransformer m_resultTransformer;

	public MessageSenderImpl() {
		m_resultFactory = new DomResultFactory();
	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.messaging.MessageSender#send(T)
	 */
	public boolean send(T graph, long correlationId) {
		
		Object payload = graph;
		
		if (m_marshaller != null) {
			Result result = m_resultFactory.createResult(graph);
			if (result == null) {
				throw new MessagingException(
						"Unable to marshal payload, ResultFactory returned null.");
			}
			try {
				m_marshaller.marshal(graph, result);
			} catch (Exception e) {
				throw new WorkflowException("Failed to marshal payload",e);
			}
			 Document doc = (Document)m_resultTransformer.transformResult(result);
			 payload = doc;
		}
		
		MessageBuilder<?> messageBuilder = MessageBuilder.withPayload(payload);
        if (getReplyChannel() != null) {
        	messageBuilder.setReplyChannel(getReplyChannel());
        }
        if (getErrorChannel() != null) {
        	messageBuilder.setErrorChannel(getErrorChannel());
        }
        if (getMessagePriority() != null) {
        	messageBuilder.setPriority(getMessagePriority());
        }
        messageBuilder.setCorrelationId(correlationId);
        Message<?> ret = messageBuilder.build();
		return getChannel().send(messageBuilder.build());
	}
	
	public MessageChannel getReplyChannel() {
		return m_replyChannel;
	}

	public void setReplyChannel(MessageChannel replyChannel) {
		m_replyChannel = replyChannel;
	}

	public MessageChannel getChannel() {
		return m_channel;
	}

	public void setChannel(MessageChannel channel) {
		m_channel = channel;
	}

	public MessageChannel getErrorChannel() {
		return m_errorChannel;
	}

	public void setErrorChannel(MessageChannel errorChannel) {
		m_errorChannel = errorChannel;
	}

	public Integer getMessagePriority() {
		return m_messagePriority;
	}

	public void setMessagePriority(Integer messagePriority) {
		m_messagePriority = messagePriority;
	}

	public GenericMarshaller getMarshaller() {
		return m_marshaller;
	}

	public void setMarshaller(GenericMarshaller marshaller) {
		m_marshaller = marshaller;
		m_resultTransformer = new ResultToDocumentTransformer();
	}
	@PostConstruct
	public void init() {
		m_resultTransformer = new ResultToDocumentTransformer();
	}

}
