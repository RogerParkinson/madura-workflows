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

import org.springframework.messaging.MessageChannel;



/**
 * @author Roger Parkinson
 *
 */
public class MessageSenderMock<T>  implements MessageSender<T> {

	private MessageChannel m_channel;
	public MessageChannel getChannel() {
		return m_channel;
	}
	public void setChannel(MessageChannel channel) {
		m_channel = channel;
	}
	public MessageChannel getReplyChannel() {
		return m_replyChannel;
	}
	public void setReplyChannel(MessageChannel replyChannel) {
		m_replyChannel = replyChannel;
	}
	private MessageChannel m_replyChannel;
	public boolean send(T graph, long correlationId) {
		
		return true;
	}
	
}
