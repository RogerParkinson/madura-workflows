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

import nz.co.senanque.workflow.instances.Audit;
import nz.co.senanque.workflow.instances.ProcessInstance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.JPAContainerItem;
import com.vaadin.data.Property;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * @author Roger Parkinson
 *
 */
public class Audits extends VerticalLayout implements MessageSourceAware {
	
	private static final long serialVersionUID = 1L;
	private transient MessageSourceAccessor m_messageSourceAccessor;
	@PersistenceContext(unitName="em-workflow")
	private EntityManager m_entityManager;
	private JPAContainer<Audit> m_container;
	private AuditTable m_auditTable;
	@Autowired private AuditPopup m_auditPopup; 
	
	public class AuditEvent extends Event {

		private static final long serialVersionUID = 1L;
		private final Audit m_audit;

		public AuditEvent(Component component, Audit audit) {
			super(component);
			m_audit = audit;
		}

		public Audit getAudit() {
			return m_audit;
		}
	}
	public class AuditTable extends Table {

		private static final long serialVersionUID = 1L;
		public AuditTable() {
			super(m_messageSourceAccessor.getMessage("Audit","Audit"), m_container);
			setVisibleColumns(new String[]{"id","created","lockedBy","comment"});
			setColumnHeaders(new String[]{
					m_messageSourceAccessor.getMessage("id","Id"),
					m_messageSourceAccessor.getMessage("created","Created"),
					m_messageSourceAccessor.getMessage("lockedBy","Locked By"),
					m_messageSourceAccessor.getMessage("comment","Comment")});
	    }

		protected String formatPropertyValue(Object rowId, Object colId,
	            Property property) {
	        if (property == null) {
	            return "";
	        }
	        return property.toString();
	    }
	}
	

	public Audits() {
	}

	@PostConstruct
	public void init() {
	}

	public void setup(ProcessInstance processInstance) {
		m_container = JPAContainerFactory.makeReadOnly(Audit.class, m_entityManager);
//		Filter by AUDITS_PROCESSINSTANCE_ID
//		Collection<Object> filterablePropertyIds = m_container.getFilterablePropertyIds();
		m_container.addContainerFilter(new Compare.Equal("parentId",processInstance.getId()));
		m_container.sort(new String[]{"created"}, new boolean[]{true});
		m_auditTable = new AuditTable();
		m_auditTable.addListener(new ItemClickEvent.ItemClickListener() {

			private static final long serialVersionUID = 1L;
            @SuppressWarnings("unchecked")
			@Override
            public void itemClick(ItemClickEvent event) {
            	Audit audit = ((JPAContainerItem<Audit>)event.getItem()).getEntity();
            	getAuditPopup().load(audit);	
            }
        });

		removeAllComponents();
		addComponent(m_auditTable);		
		m_auditTable.setSizeFull();
	}

	@Override
	public void setMessageSource(MessageSource messageSource) {
		m_messageSourceAccessor = new MessageSourceAccessor(messageSource);
	}

	public AuditPopup getAuditPopup() {
		return m_auditPopup;
	}

	public void setAuditPopup(AuditPopup auditPopup) {
		m_auditPopup = auditPopup;
	}

}
