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
package nz.co.senanque.process.instances;

import nz.co.senanque.messaging.MessageSender;
import nz.co.senanque.workflow.ProcessInstanceUtils;
import nz.co.senanque.workflow.WorkflowException;
import nz.co.senanque.workflow.instances.ProcessInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TaskMessage extends TaskBase {

	private static final Logger log = LoggerFactory
			.getLogger(TaskMessage.class);
    public TaskMessage(ProcessDefinition ownerProcess) {
		super(ownerProcess);
	}

	private String m_messageName;
	private MessageSender<Object> m_messageSender;

	public String getMessageName() {
		return m_messageName;
	}

	public void setMessageName(String messageName) {
		m_messageName = messageName;
	}

	public String toString() {
		return super.toString()+" messageName="+ m_messageName + " args=" +getArguments();
	}

	public boolean execute(ProcessInstance processInstance) {
		log.debug("{}",this);
		Object context = getContext(processInstance);
		ProcessInstanceUtils.clearQueue(processInstance);
		log.debug(getOwnerProcess().getName()+" Waiting for message "+this.toString());
		if (!getMessageSender().send(context, processInstance.getId())) {
			throw new WorkflowException("Failed to send message "+getMessageName());
		}
		return false;
	}

	public MessageSender<Object> getMessageSender() {
		return m_messageSender;
	}

	public void setMessageSender(MessageSender<Object> messageSender) {
		m_messageSender = messageSender;
	}
}
