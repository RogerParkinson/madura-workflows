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
import java.util.List;
import java.util.concurrent.locks.Lock;

import javax.annotation.PostConstruct;

import nz.co.senanque.locking.LockAction;
import nz.co.senanque.locking.LockFactory;
import nz.co.senanque.locking.LockTemplate;
import nz.co.senanque.validationengine.ValidationEngine;
import nz.co.senanque.validationengine.ValidationSessionHolder;
import nz.co.senanque.validationengine.ValidationSessionHolderImpl;
import nz.co.senanque.workflow.BundleSelector;
import nz.co.senanque.workflow.ContextUtils;
import nz.co.senanque.workflow.WorkflowDAO;
import nz.co.senanque.workflow.WorkflowException;
import nz.co.senanque.workflow.WorkflowManager;
import nz.co.senanque.workflow.instances.ProcessInstance;
import nz.co.senanque.workflow.instances.TaskStatus;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.filter.ContentFilter;
import org.jdom.input.DOMBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

/**
 * An incoming message or a message response arrives here. We don't care which it is.
 * The message is assumed to have a correlationId we can use as a processInstanceId to find
 * the process. We lock that process and then proceed to unpack the message.
 * The message unpacker is contained in this class, though an alternate can be injected if desired.
 * The default unpacker assumes a simple org.w3c.dom.Document which has one layer of elements under
 * the root. Each of those element has a name which maps to a context object field and a value
 * to be set in that field. Names which fail to map are ignored.
 * The elements can contain an attribute named xpath and, if present, is used as the name when the
 * call to org.apache.commons.beanutils.PropertyUtils.setProperty(object, name, value).
 * If the root element has an attribute named 'error' then this message is treated as an error and
 * not unpacked. The content of the attribute is used as the error message and the process instance is aborted.
 * 
 * @author Roger Parkinson
 * 
 */
public class GenericEndpoint implements MessageMapper {

	private static final Logger log = LoggerFactory
			.getLogger(GenericEndpoint.class);

	@Autowired
	WorkflowDAO m_workflowDAO;
	@Autowired
	private LockFactory m_lockFactory;
	@Autowired
	private WorkflowManager m_workflowManager;
	@Autowired
	private BundleSelector m_bundleSelector;
	private MessageMapper m_messageMapper;

	public void issueResponseFor(final Message<?> message) {
		MessageHeaders messageHeaders = message.getHeaders();
		Long correlationId = (Long)message.getHeaders().getCorrelationId();
		log.debug("ProcessInstance: correlationId {}", correlationId);
		if (correlationId == null) {
			log.error("correlation Id is null");
			throw new WorkflowException("correlation Id is null");
		}
		final ProcessInstance processInstance = getWorkflowDAO().findProcessInstance(correlationId);
		if (processInstance == null) {
			throw new WorkflowException("Failed to find processInstance for "+correlationId);
		}
		getBundleSelector().selectBundle(processInstance);
		List<Lock> locks = ContextUtils.getLocks(processInstance,getLockFactory(),"nz.co.senanque.messaging.GenericEndpoint.issueResponseFor");
		LockTemplate lockTemplate = new LockTemplate(locks, new LockAction() {
			
			public void doAction() {
				
				if (processInstance.getStatus() != TaskStatus.WAIT) {
					throw new WorkflowException("Process is not in a wait state");
				}
				getWorkflowManager().processMessage(processInstance, message, getMessageMapper());
				log.debug("completed lock message processing");
			}});
		if (!lockTemplate.doAction()) {
			throw new WorkflowRetryableException("Failed to get a lock"); // this will be retried later, not a hard error
		}
		log.debug("completed incomming message processing");
	}

	public void unpackMessage(Message<?> message, Object context) {
		Object payload = message.getPayload();
		if (payload instanceof org.w3c.dom.Document) {
			Document document = new DOMBuilder().build((org.w3c.dom.Document)payload);
			if (log.isDebugEnabled()) {
				log.debug("document\n{}",getStringFromDoc((org.w3c.dom.Document)payload));
			}
			Element root = document.getRootElement();
			String errorValue = root.getAttributeValue("error");
			if (errorValue != null) {
				throw new WorkflowException(errorValue);
			}
			unpackRoot(root,context);
		}
		else {
			throw new WorkflowException("Expected payload to be org.w3c.dom.Document, instead found a "+payload.getClass().getName());
		}
	}
	
	private String getStringFromDoc(org.w3c.dom.Document doc)    {
	    DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
	    LSSerializer lsSerializer = domImplementation.createLSSerializer();
	    return lsSerializer.writeToString(doc);   
	}
	private void unpackRoot(Element element, Object context) {
		
		ValidationSessionHolder validationSessonHolder = new ValidationSessionHolderImpl(getValidationEngine());
		validationSessonHolder.bind(context);
		try {
			@SuppressWarnings("unchecked")
			Iterator<Text> itr = (Iterator<Text>) element
					.getDescendants(new ContentFilter(ContentFilter.TEXT
							| ContentFilter.CDATA));
			while (itr.hasNext()) {
				Text text = itr.next();
	
				String name = getName(text);
				if (name.equals("id") || name.equals("version")) {
					continue;
				}
				try {
					Class<?> targetType = PropertyUtils.getPropertyType(context, name);
					Object value = ConvertUtils.convert(text.getValue(), targetType);
					PropertyUtils.setProperty(context, name, value);
					log.debug("name {} value {}", name, text.getValue());
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
		finally {
			validationSessonHolder.close();
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

	@PostConstruct
	public void init() {
		if (getMessageMapper() == null) {
			setMessageMapper(this);
		}
	}

	public WorkflowDAO getWorkflowDAO() {
		return m_workflowDAO;
	}

	public void setWorkflowDAO(WorkflowDAO workflowDAO) {
		m_workflowDAO = workflowDAO;
	}

	public LockFactory getLockFactory() {
		return m_lockFactory;
	}

	public void setLockFactory(LockFactory lockFactory) {
		m_lockFactory = lockFactory;
	}

	public WorkflowManager getWorkflowManager() {
		return m_workflowManager;
	}

	public void setWorkflowManager(WorkflowManager workflowManager) {
		m_workflowManager = workflowManager;
	}

	public MessageMapper getMessageMapper() {
		return m_messageMapper;
	}

	public void setMessageMapper(MessageMapper messageMapper) {
		m_messageMapper = messageMapper;
	}

	public ValidationEngine getValidationEngine() {
		return getWorkflowManager().getValidationEngine();
	}

	public BundleSelector getBundleSelector() {
		return m_bundleSelector;
	}

	public void setBundleSelector(BundleSelector bundleSelector) {
		m_bundleSelector = bundleSelector;
	}

}
