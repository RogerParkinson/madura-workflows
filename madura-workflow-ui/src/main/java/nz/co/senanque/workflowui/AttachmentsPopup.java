/*******************************************************************************
 * Copyright (c)15/04/2014 Prometheus Consulting
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nz.co.senanque.vaadin.format.FormattingTable;
import nz.co.senanque.vaadin.permissionmanager.PermissionManager;
import nz.co.senanque.workflow.WorkflowDAO;
import nz.co.senanque.workflow.instances.Attachment;
import nz.co.senanque.workflowui.AttachmentPopup.AttachmentEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.JPAContainerItem;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.server.DownloadStream;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * @author Roger Parkinson
 *
 */
@UIScope
@org.springframework.stereotype.Component
public class AttachmentsPopup extends Window implements MessageSourceAware {
	
	private static final long serialVersionUID = 1L;
	private String[] columns = new String[]{"fileName", "comment", "created"};
	private Layout main;
	private Layout panel;
	private String m_windowWidth = "800px";
	private String m_windowHeight = "600px";
	@PersistenceContext(unitName="em-workflow")
	private EntityManager m_entityManager;
	@Autowired PermissionManager m_permissionManager;
	@Autowired WorkflowDAO m_workflowDAO;
	private transient MessageSource m_messageSource;
	private transient MessageSourceAccessor m_messageSourceAccessor;
	private AttachmentsTable m_attachmentsTable;
    @Autowired transient AttachmentPopup m_attachmentPopup;
    private long m_currentPid;

	@Override
	public void setMessageSource(MessageSource messageSource) {
		m_messageSource = messageSource;
		m_messageSourceAccessor = new MessageSourceAccessor(m_messageSource);
	}
    public void close() {
    	UI.getCurrent().removeWindow(this);
    }
	
	public class AttachmentsTable extends FormattingTable {

		private static final long serialVersionUID = 1L;
		public AttachmentsTable(JPAContainer<Attachment> container) {
			setContainerDataSource(container,columns,columns,m_messageSource);
	    }
	}

	@PostConstruct
	public void init() {
        main = new VerticalLayout();
        setContent(main);
        setModal(true);
//        main.setStyleName(Panel.STYLE_LIGHT);
        main.setWidth(getWindowWidth());
        main.setHeight(getWindowHeight());
        
        panel = new VerticalLayout();
//        main.setMargin(true);
        main.addComponent(panel);
        
        setCaption(m_messageSourceAccessor.getMessage("attachments", "Attachments"));
	}
	public void load(final long pid) {
		m_currentPid = pid;
		panel.removeAllComponents();
		HorizontalLayout hl = new HorizontalLayout();
		hl.setMargin(true);
		hl.setSpacing(true);
		Button done = new Button(m_messageSourceAccessor.getMessage("done", "Close"));
		done.addClickListener(new ClickListener(){

			@Override
			public void buttonClick(ClickEvent event) {
				close();				
			}});

		Button newAttachment = new Button(m_messageSourceAccessor.getMessage("new", "New"));
		newAttachment.addClickListener(new ClickListener(){

			@Override
			public void buttonClick(ClickEvent event) {
				m_attachmentPopup.load(m_currentPid);;				
			}});

		hl.addComponent(newAttachment);
		hl.addComponent(done);
		panel.addComponent(hl);
		refresh();
		m_attachmentPopup.addListener(AttachmentEvent.class, this, "refresh");

    	if (getParent() == null) {
    		UI.getCurrent().addWindow(this);
        	this.center();
        }
	}

	public void refresh() {
		try {
			panel.removeComponent(m_attachmentsTable);
		} catch (NullPointerException e) {
			// ignore NPEs
		}
		m_attachmentsTable = getAttachmentsTable(getFilter(getPermissionManager(),m_currentPid));
		panel.addComponent(m_attachmentsTable);
	}

	private AttachmentsTable getAttachmentsTable(Filter filter) {
		AttachmentsTable attachmentsTable = new AttachmentsTable(getContainer(filter));
		attachmentsTable.addItemClickListener(new ItemClickEvent.ItemClickListener() {

			private static final long serialVersionUID = 1L;
            @SuppressWarnings("unchecked")
			@Override
            public void itemClick(ItemClickEvent event) {
            	JPAContainerItem<Attachment> item = (JPAContainerItem<Attachment>)event.getItem();
            	Attachment attachment = item.getEntity();
            	makeStream(attachment);
            }
        });
		attachmentsTable.setSizeFull();
		return attachmentsTable;
	}
	
	private void makeStream(final Attachment attachment) {
        final FileResource stream = new FileResource(new File("")) {
			private static final long serialVersionUID = 1L;

			@Override
            public DownloadStream getStream() {
                ByteArrayInputStream in = new ByteArrayInputStream(
                        attachment.getBody());
                DownloadStream ds = new DownloadStream(in,
                		attachment.getMIMEType(), attachment.getFileName());
                // Need a file download POPUP
                ds.setParameter("Content-Disposition",
                        "attachment; filename="+attachment.getFileName());
                return ds;
            }
        };
        stream.setCacheTime(0);
        Page.getCurrent().open(stream, "_blank", true);
//        panel.requestRepaint();
	}
	
	private JPAContainer<Attachment> getContainer(Filter filter) {
		JPAContainer<Attachment> container = JPAContainerFactory.makeReadOnly(Attachment.class, m_entityManager);
		if (filter != null) {
			container.addContainerFilter(filter);
		}
		container.sort(new String[]{"created"}, new boolean[]{false});
		return container;
	}
	
	private Filter getFilter(PermissionManager permissionManager, long pid) {
		if (permissionManager.hasPermission(FixedPermissions.TECHSUPPORT) || permissionManager.hasPermission(FixedPermissions.ADMIN)) {
			return new Compare.Equal("processInstanceId", new Long(pid));
		}
		return new And(new Compare.Equal("hidden", false), new Compare.Equal("processInstanceId", new Long(pid)));
	}
	public PermissionManager getPermissionManager() {
		return m_permissionManager;
	}
	public void setPermissionManager(PermissionManager permissionManager) {
		m_permissionManager = permissionManager;
	}
	public WorkflowDAO getWorkflowDAO() {
		return m_workflowDAO;
	}
	public void setWorkflowDAO(WorkflowDAO workflowDAO) {
		m_workflowDAO = workflowDAO;
	}
	public String getWindowWidth() {
		return m_windowWidth;
	}
	public void setWindowWidth(String windowWidth) {
		m_windowWidth = windowWidth;
	}
	public String getWindowHeight() {
		return m_windowHeight;
	}
	public void setWindowHeight(String windowHeight) {
		m_windowHeight = windowHeight;
	}
	public AttachmentPopup getAttachmentPopup() {
		return m_attachmentPopup;
	}
	public void setAttachmentPopup(AttachmentPopup attachmentPopup) {
		m_attachmentPopup = attachmentPopup;
	}


}
