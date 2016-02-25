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
package nz.co.senanque.workflowui;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nz.co.senanque.vaadin.format.FormattingTable;
import nz.co.senanque.vaadin.permissionmanager.PermissionManager;
import nz.co.senanque.workflow.instances.ProcessInstance;
import nz.co.senanque.workflowui.conf.QueueProcessManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.JPAContainerItem;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

/**
 * @author Roger Parkinson
 *
 */
@UIScope
@org.springframework.stereotype.Component
public class ProcessInstances extends VerticalLayout implements MessageSourceAware {
	
    Logger logger = LoggerFactory.getLogger(ProcessInstances.class);
	private static final long serialVersionUID = 1L;
	@Autowired PermissionManager m_permissionManager;
	@Autowired QueueProcessManager m_queueProcessManager;
	@PersistenceContext(unitName="em-workflow")
	private EntityManager m_entityManager;
	private transient MessageSourceAccessor m_messageSourceAccessor;
	private ProcessTable m_processTable;
	
	public class ProcessTable extends FormattingTable {

		private static final long serialVersionUID = 1L;
		public ProcessTable(JPAContainer<ProcessInstance> container) {
			super(m_messageSourceAccessor.getMessage("Processes","Processes"), container);
			setVisibleColumns(new Object[]{"id","queueName", "processDefinitionName","status","comment","reference","lastUpdated"});
			setColumnHeaders(new String[]{
					m_messageSourceAccessor.getMessage("id","Id"),
					m_messageSourceAccessor.getMessage("queueName","Queue"),
					m_messageSourceAccessor.getMessage("processDefinitionName","Process"),
					m_messageSourceAccessor.getMessage("status","Status"),
					m_messageSourceAccessor.getMessage("comment","Comment"),
					m_messageSourceAccessor.getMessage("reference","Reference"),
					m_messageSourceAccessor.getMessage("lastUpdated","Last Updated")});
			setColumnCollapsingAllowed(true);
	    }

		protected String formatPropertyValue(Object rowId, Object colId,
	            Property<?> property) {
	        if (property == null) {
	            return "";
	        }
	        if ("childProcesses".equals(colId)) {
	        	return "";
	        }
	        if ("audits".equals(colId)) {
	        	return "";
	        }
	        return property.toString();
	    }
	}
	
	public class ProcessInstanceEvent extends Event {

		private static final long serialVersionUID = 1L;
		private final ProcessInstance m_processInstance;

		public ProcessInstanceEvent(Component component, ProcessInstance processInstance) {
			super(component);
			m_processInstance = processInstance;
		}

		public ProcessInstance getProcessInstance() {
			return m_processInstance;
		}
	}
	
	public ProcessInstances() {
	}

	@PostConstruct
	public void init() {
		setWidth("100.0%");
	}

	public void setup() {

		if (m_processTable != null) {
			// Sometimes we can get here more than once
			return;
		}
		logger.debug("setting up ProcessInstances for {}",getPermissionManager().getCurrentUser());
		final Filter filter = getQueueProcessManager().getQueueFilter(getPermissionManager());
		this.setMargin(false);
	
		m_processTable = getProcessTable(filter);
		addComponent(m_processTable);
		Button refresh = new Button(m_messageSourceAccessor.getMessage("refresh"));
		refresh.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				removeComponent(m_processTable);
				m_processTable = getProcessTable(filter);
				addComponent(m_processTable,0);
			}});
		addComponent(refresh);
	}
	
	public void refresh() {
		final Filter filter = getQueueProcessManager().getQueueFilter(getPermissionManager());
		removeComponent(m_processTable);
		m_processTable = getProcessTable(filter);
		addComponent(m_processTable,0);
	}
	
	private ProcessTable getProcessTable(Filter filter) {
		ProcessTable processTable = new ProcessTable(getContainer(filter));
		processTable.addItemClickListener(new ItemClickEvent.ItemClickListener() {

			private static final long serialVersionUID = 1L;
            @SuppressWarnings("unchecked")
			@Override
            public void itemClick(ItemClickEvent event) {
            	ProcessInstance processInstance = ((JPAContainerItem<ProcessInstance>)event.getItem()).getEntity();
            	fireEvent(new ProcessInstanceEvent(m_processTable,processInstance));	
            }
        });
		processTable.setSizeFull();
		return processTable;
	}
	
	private JPAContainer<ProcessInstance> getContainer(Filter filter) {
		JPAContainer<ProcessInstance> container = JPAContainerFactory.makeReadOnly(ProcessInstance.class, m_entityManager);
		if (filter != null) {
			container.addContainerFilter(filter);
		}
		container.sort(new String[]{"lastUpdated"}, new boolean[]{false});
		return container;
	}

	public PermissionManager getPermissionManager() {
		return m_permissionManager;
	}

	public void setPermissionManager(PermissionManager permissionManager) {
		m_permissionManager = permissionManager;
	}

	public QueueProcessManager getQueueProcessManager() {
		return m_queueProcessManager;
	}

	public void setQueueProcessManager(QueueProcessManager queueProcessManager) {
		m_queueProcessManager = queueProcessManager;
	}

	@Override
	public void setMessageSource(MessageSource messageSource) {
		m_messageSourceAccessor = new MessageSourceAccessor(messageSource);
	}

}
