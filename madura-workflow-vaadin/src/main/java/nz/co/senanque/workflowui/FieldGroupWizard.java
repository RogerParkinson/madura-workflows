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

import java.util.Map;

import javax.annotation.PostConstruct;

import nz.co.senanque.forms.WorkflowForm;
import nz.co.senanque.permissionmanager.PermissionManager;
import nz.co.senanque.process.instances.ProcessDefinition;
import nz.co.senanque.vaadin.MaduraFieldGroup;
import nz.co.senanque.vaadin.MaduraSessionManager;
import nz.co.senanque.workflow.WorkflowClient;
import nz.co.senanque.workflow.instances.ProcessInstance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;

import com.vaadin.data.util.BeanItem;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


/**
 * @author Roger Parkinson
 *
 */
@UIScope
@org.springframework.stereotype.Component
public class FieldGroupWizard extends Window implements MessageSourceAware {
	
//	private static final Logger log = LoggerFactory
//			.getLogger(FieldGroupWizard.class);
	
	private static final long serialVersionUID = 1L;
	private Layout main;
	private VerticalLayout auditPanel;
	private VerticalLayout formPanel;
	private VerticalLayout processPanel;
	private TabSheet tabSheet;
    private String m_windowWidth = "800px";
    private String m_windowHeight = "600px";
    private MaduraFieldGroup m_maduraFieldGroup;
    
	@Autowired private MaduraSessionManager m_maduraSessionManager;
	@Autowired private transient Audits m_audits;
	@Autowired private transient WorkflowClient m_workflowClient;
	@Autowired private transient AttachmentsPopup m_attachmentsPopup;
	private transient MessageSourceAccessor m_messageSourceAccessor;
		
	public FieldGroupWizard() {
	}

	@PostConstruct
	public void init() {
        main = new VerticalLayout();
        setContent(main);
        setModal(true);
        this.setWidth(getWindowWidth());
        this.setHeight(getWindowHeight());
        
        tabSheet = new TabSheet();
        
        formPanel = new VerticalLayout();
        tabSheet.addTab(formPanel,m_messageSourceAccessor.getMessage("formwizard.form"));
        processPanel = new VerticalLayout();
        tabSheet.addTab(processPanel,m_messageSourceAccessor.getMessage("formwizard.process"));
        auditPanel = new VerticalLayout();
        tabSheet.addTab(auditPanel,m_messageSourceAccessor.getMessage("formwizard.audit"));
        main.addComponent(tabSheet);
	}

	@SuppressWarnings("serial")
	public void load(final WorkflowForm form) {
    	
    	ProcessDefinition processDefinition = form.getProcessDefinition();
    	String task = processDefinition.getTask(form.getProcessInstance().getTaskId()).toString();

    	ProcessDefinition ownerProcessDefinition = processDefinition;
    	while (processDefinition != null) {
    		ownerProcessDefinition = processDefinition;
    		processDefinition = processDefinition.getOwnerProcess();
    	}
    	setCaption(m_messageSourceAccessor.getMessage("form.wizard.caption", 
    			new Object[]{new Long(form.getProcessInstance().getId()),
    			ownerProcessDefinition.getName(),
    			form.getProcessInstance().getReference(),
    			ownerProcessDefinition.getDescription()}));
    	formPanel.removeAllComponents();
    	formPanel.addComponent((VerticalLayout)form);
    	ProcessInstance processInstance = form.getProcessInstance();
    	if (!form.isReadOnly()) {
	    	PermissionManager pm = m_maduraSessionManager.getPermissionManager();
	    	processInstance = getWorkflowClient().lockProcessInstance(form.getProcessInstance(), 
	    			pm.hasPermission(FixedPermissions.TECHSUPPORT), pm.getCurrentUser());
	     	if (processInstance == null) {
	     		com.vaadin.ui.Notification.show(m_messageSourceAccessor.getMessage("failed.to.get.lock"),
						m_messageSourceAccessor.getMessage("message.noop"),
						com.vaadin.ui.Notification.Type.HUMANIZED_MESSAGE);
	    		return;
	    	}
    	}
     	form.setProcessInstance(processInstance);
     	// This is binding the process instance associated with the form to the workflow validation session.
//     	log.debug("Binding {} to Validation engine {}",processInstance.getClass().getSimpleName(),getMaduraSessionManager().getValidationEngine().getIdentifier());
     	getMaduraSessionManager().getValidationSession().bind(form.getProcessInstance());
		((VerticalLayout)form).addListener(new Listener(){

			@Override
			public void componentEvent(Event event) {
				close();
				fireEvent(event);
			}});

    	formPanel.markAsDirty();

    	BeanItem<ProcessInstance> beanItem = new BeanItem<ProcessInstance>(form.getProcessInstance());
    	
    	m_maduraFieldGroup = m_maduraSessionManager.createMaduraFieldGroup();
		Button attachments = m_maduraFieldGroup.createButton("attachments", new ClickListener(){

			@Override
			public void buttonClick(ClickEvent event) {
				m_attachmentsPopup.load(form.getProcessInstance().getId());				
			}});
    	Map<String,Field<?>> fields = m_maduraFieldGroup.buildAndBind(
    			new String[]{"queueName","bundleName","status","reference","lastUpdated","lockedBy","comment"},
    			beanItem);

    	TextArea comment = (TextArea)fields.get("comment");
    	comment.setRows(2);
    	comment.setWordwrap(true);
    	comment.setWidth("700px");
    	TextArea taskField = new TextArea();
    	taskField.setRows(2);
    	taskField.setWordwrap(true);
    	taskField.setWidth("700px");
    	taskField.setValue(task);
    	taskField.setReadOnly(true);

     	processPanel.removeAllComponents();
     	processPanel.setMargin(true);
     	processPanel.setSpacing(true);
     	HorizontalLayout processPanelHorizontal = new HorizontalLayout();
     	processPanelHorizontal.setSpacing(true);
    	processPanel.addComponent(processPanelHorizontal);
    	VerticalLayout processPanelColumn1 = new VerticalLayout();
    	VerticalLayout processPanelColumn2 = new VerticalLayout();
    	VerticalLayout processPanelColumn3 = new VerticalLayout();
    	processPanelHorizontal.addComponent(processPanelColumn1);
    	processPanelHorizontal.addComponent(processPanelColumn2);
    	processPanelHorizontal.addComponent(processPanelColumn3);
    	processPanelColumn1.addComponent(fields.get("queueName"));
    	processPanelColumn1.addComponent(fields.get("bundleName"));
    	processPanelColumn2.addComponent(fields.get("reference"));
    	processPanelColumn2.addComponent(fields.get("lastUpdated"));
    	processPanelColumn3.addComponent(fields.get("lockedBy"));
    	processPanelColumn3.addComponent(fields.get("status"));
    	
    	processPanel.addComponent(comment);
    	processPanel.addComponent(taskField);
    	m_maduraSessionManager.updateOtherFields(null);
    	
    	m_maduraFieldGroup.setReadOnly(form.isReadOnly());
    	
		processPanel.addComponent(attachments);
    	processPanel.markAsDirty();
    	
    	auditPanel.removeAllComponents();
    	m_audits.setup(form.getProcessInstance());
    	auditPanel.addComponent(m_audits);

    	if (getParent() == null) {
    		UI.getCurrent().addWindow(this);
        	this.center();
        }
    }

    public void close() {
    	m_maduraFieldGroup.destroy();
    	getMaduraSessionManager().getValidationSession().unbindAll();
    	getMaduraSessionManager().close();
    	super.close();
    }

	public String getWindowWidth() {
		return m_windowWidth;
	}

	public void setWindowWidth(String windowWidth) {
		m_windowWidth = windowWidth;
	}

	public MaduraSessionManager getMaduraSessionManager() {
		return m_maduraSessionManager;
	}

	public void setMaduraSessionManager(MaduraSessionManager maduraSessionManager) {
		m_maduraSessionManager = maduraSessionManager;
	}

	public String getWindowHeight() {
		return m_windowHeight;
	}

	public void setWindowHeight(String windowHeight) {
		m_windowHeight = windowHeight;
	}

	public WorkflowClient getWorkflowClient() {
		return m_workflowClient;
	}

	public void setWorkflowClient(WorkflowClient workflowClient) {
		m_workflowClient = workflowClient;
	}

	public AttachmentsPopup getAttachmentsPopup() {
		return m_attachmentsPopup;
	}

	public void setAttachmentsPopup(AttachmentsPopup attachmentsPopup) {
		m_attachmentsPopup = attachmentsPopup;
	}
	@Override
	public void setMessageSource(MessageSource messageSource) {
		m_messageSourceAccessor = new MessageSourceAccessor(messageSource);
	}

}
