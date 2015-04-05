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

import nz.co.senanque.forms.WorkflowForm;
import nz.co.senanque.process.instances.ProcessDefinition;
import nz.co.senanque.vaadinsupport.MaduraForm;
import nz.co.senanque.vaadinsupport.application.MaduraSessionManager;
import nz.co.senanque.vaadinsupport.permissionmanager.PermissionManager;
import nz.co.senanque.vaadinsupport.viewmanager.ViewManager;
import nz.co.senanque.workflow.WorkflowClient;
import nz.co.senanque.workflow.instances.ProcessInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;

import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * @author Roger Parkinson
 *
 */
public class FormWizard extends Window {
	
	private static final Logger log = LoggerFactory
			.getLogger(FormWizard.class);
	
	private static final long serialVersionUID = 1L;
	private Layout main;
	private Panel auditPanel;
	private Panel formPanel;
	private Panel processPanel;
	private TabSheet tabSheet;
    private String m_windowWidth = "800px";
    private String m_windowHeight = "600px";
    private MaduraForm m_processForm;

    @Autowired private ViewManager m_viewManager;
	@Autowired private MaduraSessionManager m_maduraSessionManager;
	@Autowired private transient Audits m_audits;
	@Autowired private transient WorkflowClient m_workflowClient;
	@Autowired private transient AttachmentsPopup m_attachmentsPopup;
	
	public FormWizard() {
	}

	@PostConstruct
	public void init() {
        main = new VerticalLayout();
        setContent(main);
        setModal(true);
        main.setStyleName(Panel.STYLE_LIGHT);
        main.setWidth(getWindowWidth());
        main.setHeight(getWindowHeight());
        
        tabSheet = new TabSheet();
        
        formPanel = new Panel();
        formPanel.setCaption("Form");
        tabSheet.addTab(formPanel);
        processPanel = new Panel();
        processPanel.setCaption("Process");
        tabSheet.addTab(processPanel);
        auditPanel = new Panel();
        auditPanel.setCaption("Audit");
        tabSheet.addTab(auditPanel);
        main.setMargin(true);
        main.addComponent(tabSheet);
	}

	public void load(final WorkflowForm form) {
    	
//     	log.debug("Loading form {}",form.getClass().getSimpleName());

    	ProcessDefinition processDefinition = form.getProcessDefinition();
    	String task = processDefinition.getTask(form.getProcessInstance().getTaskId()).toString();

    	ProcessDefinition ownerProcessDefinition = processDefinition;
    	while (processDefinition != null) {
    		ownerProcessDefinition = processDefinition;
    		processDefinition = processDefinition.getOwnerProcess();
    	}
    	MessageSourceAccessor messageSourceAccessor = new MessageSourceAccessor(getMaduraSessionManager().getMessageSource());
    	setCaption(messageSourceAccessor.getMessage("form.wizard.caption", 
    			new Object[]{new Long(form.getProcessInstance().getId()),
    			ownerProcessDefinition.getName(),
    			form.getProcessInstance().getReference()}));
    	setDescription(ownerProcessDefinition.getDescription());
    	formPanel.removeAllComponents();
    	formPanel.addComponent((VerticalLayout)form);
    	ProcessInstance processInstance = form.getProcessInstance();
    	if (!form.isReadOnly()) {
	    	PermissionManager pm = m_maduraSessionManager.getPermissionManager();
	    	processInstance = getWorkflowClient().lockProcessInstance(form.getProcessInstance(), 
	    			pm.hasPermission(FixedPermissions.TECHSUPPORT), pm.getCurrentUser());
	     	if (processInstance == null) {
				String message = messageSourceAccessor.getMessage("failed.to.get.lock");
				m_viewManager.getMainWindow().showNotification(message);
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

    	formPanel.requestRepaint();

    	BeanItem<ProcessInstance> beanItem = new BeanItem<ProcessInstance>(form.getProcessInstance());

    	m_processForm = new MaduraForm(getMaduraSessionManager());
    	String[] fieldList = new String[]{"queueName","bundleName","status","comment","reference", "lastUpdated", "lockedBy"};
    	m_processForm.setFieldList(fieldList);
    	m_processForm.setReadOnly(form.isReadOnly());
    	m_processForm.setItemDataSource(beanItem);
    	TextField taskField = new TextField(messageSourceAccessor.getMessage("task"));
    	m_processForm.addField("Task", taskField);
    	taskField.setValue(task);
    	taskField.setReadOnly(true);

     	processPanel.removeAllComponents();
    	processPanel.addComponent(m_processForm);
    	Button attachments = new Button(messageSourceAccessor.getMessage("attachments", "Attachments"));
		attachments.addListener(new ClickListener(){

			@Override
			public void buttonClick(ClickEvent event) {
				m_attachmentsPopup.load(form.getProcessInstance().getId());				
			}});
		processPanel.addComponent(attachments);
    	processPanel.requestRepaint();
    	
    	auditPanel.removeAllComponents();
    	m_audits.setup(form.getProcessInstance());
    	auditPanel.addComponent(m_audits);

    	if (getParent() == null) {
        	m_viewManager.getMainWindow().addWindow(this);
        	this.center();
        }
    }

    public void close() {
    	m_processForm.destroy();
    	getMaduraSessionManager().getValidationSession().unbindAll();
    	getMaduraSessionManager().close();
    	if (getParent() != null) {
    		getParent().removeWindow(this);
    	}
    }

	public ViewManager getViewManager() {
		return m_viewManager;
	}

	public void setViewManager(ViewManager viewManager) {
		m_viewManager = viewManager;
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

}
