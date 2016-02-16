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

import java.io.Serializable;
import java.lang.reflect.Method;

import javax.annotation.PostConstruct;

import nz.co.senanque.forms.WorkflowForm;
import nz.co.senanque.vaadin.MaduraSessionManager;
import nz.co.senanque.vaadin.permissionmanager.PermissionManager;
import nz.co.senanque.workflow.WorkflowClient;
import nz.co.senanque.workflow.instances.ProcessInstance;
import nz.co.senanque.workflowui.ProcessInstances.ProcessInstanceEvent;
import nz.co.senanque.workflowui.bundles.BundleListenerImpl;
import nz.co.senanque.workflowui.bundles.QueueProcessManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * @author Roger Parkinson
 *
 */
//@UIScope
//@org.springframework.stereotype.Component
public class MainLayout extends CustomComponent implements Serializable, MessageSourceAware {

	private static final Logger log = LoggerFactory
			.getLogger(MainLayout.class);
	
	@Autowired private AboutWindow m_aboutWindow;

	@AutoGenerated
	private VerticalLayout mainLayout;
	@AutoGenerated
	private HorizontalLayout bodyLayout;
	@AutoGenerated
	private Panel panel_2;
	@AutoGenerated
	private VerticalLayout ApplicationlBodyLayout;
	@AutoGenerated
	private Panel panel_1;
	@AutoGenerated
	private VerticalLayout ApplicationIconContainer;
	@AutoGenerated
	private HorizontalLayout headingButtonslLayout;
	@AutoGenerated
	private Label loggedInAs;
	@AutoGenerated
	private MenuBar menuBar_1;
	@AutoGenerated
	private HorizontalLayout headingLayout;
	@AutoGenerated
	private Label titleLabel;
	@AutoGenerated
	private Embedded embedded_1;

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	private static final long serialVersionUID = 1L;
	@Autowired transient PermissionManager m_permissionManager;
	@Autowired transient BundleListenerImpl m_bundleListener;
	@Autowired transient QueueProcessManager m_queueProcessManager;
	@Autowired private MaduraSessionManager m_maduraSessionManager;

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */
	private transient MessageSourceAccessor m_messageSourceAccessor;
	private transient MenuItem launch;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	public MainLayout() {
		buildMainLayout();
		setCompositionRoot(mainLayout);
		titleLabel.addStyleName("v-textfield-align-right");
	}
	@PostConstruct
	public void init() throws Exception {
		final MainLayout me = this;
		String loggedInAsString = m_messageSourceAccessor.getMessage("logged.in.as",new String[]{m_permissionManager.getCurrentUser()});
		loggedInAs.addStyleName("v-textfield-align-right");
		loggedInAs.setValue(loggedInAsString);
//		m_processInstances.setup();
//		ApplicationlBodyLayout.addComponent(m_processInstances);
//		Method processInstanceClickMethod = me.getClass().getMethod("processInstanceClick", new Class<?>[]{ProcessInstanceEvent.class});
//		m_processInstances.addListener(ProcessInstanceEvent.class, me, processInstanceClickMethod);
//		Method formWizardClickMethod = me.getClass().getMethod("formWizardClick", new Class<?>[]{ClickEvent.class});
//		m_formWizard.addListener(ClickEvent.class, me, formWizardClickMethod);
//		m_launchWizard.addListener(ClickEvent.class, me, formWizardClickMethod);
//		setupMenu();
//		if (m_launchWizard.setup() <= 0) {
//			launch.setEnabled(false);
//		}
	}
	
//	public void processInstanceClick(ProcessInstanceEvent processInstanceEvent) {
//		ProcessInstance processInstance = processInstanceEvent.getProcessInstance();
//		WorkflowForm form = m_workflowClient.getCurrentForm(processInstance);
//		if (!m_queueProcessManager.getWriteableQueues(m_permissionManager).contains(processInstance.getQueueName())) {
//			form.setReadOnly(true);
//		}
//		m_formWizard.load(form);
//	}
	
//	public void formWizardClick(ClickEvent event) {
//		Button button = (com.vaadin.ui.Button)(event.getSource());
//		String eventData = (String)button.getData();
//		if (eventData != null) {
//			if (eventData.startsWith(WorkflowForm.LAUNCH)) {
//				String processId = eventData.substring(WorkflowForm.LAUNCH.length());
//				m_workflowClient.finishLaunch(Long.parseLong(processId));
//			}
//		}
//		if (!"cancel".equals(eventData)) {
//			m_processInstances.refresh();
//		}
//	}
	
//	private void setupMenu() {
//
//		
//		final MenuBar.MenuItem file = menuBar_1.addItem(m_messageSourceAccessor.getMessage("file","File"), null);
//		launch = file.addItem(m_messageSourceAccessor.getMessage("Launch...","Launch..."), new Command() {
//
//			private static final long serialVersionUID = 1L;
//			public void menuSelected(MenuItem selectedItem) {
//				m_launchWizard.load();				
//			}});
//		file.addItem(m_messageSourceAccessor.getMessage("logout","Logout"), new Command() {
//
//			private static final long serialVersionUID = 1L;
//			public void menuSelected(MenuItem selectedItem) {
//				m_maduraSessionManager.logout(UI.getCurrent());
//			}});
//		final MenuBar.MenuItem help = menuBar_1.addItem(m_messageSourceAccessor.getMessage("help","Help"), null);
//		help.addItem(m_messageSourceAccessor.getMessage("demo.script","Demo Script (PDF)"), new Command() {
//
//			private static final long serialVersionUID = 1L;
//			public void menuSelected(MenuItem selectedItem) {
//                if (about.getParent() != null) {
//                    // window is already showing
//                    Notification.show("Window is already open");//should not show...
//                } else {
//                	UI.getCurrent().getPage().setLocation(m_messageSourceAccessor.getMessage("demo.pdf"));
//                }
//			}});
//		help.addItem(m_messageSourceAccessor.getMessage("about","About"), new Command() {
//
//			private static final long serialVersionUID = 1L;
//			public void menuSelected(MenuItem selectedItem) {
//				m_aboutWindow.load();
//                }
//			});
//	}
	
	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("800px");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(false);
		
		// top-level component properties
		setWidth("900px");
		setHeight("-1px");
		
		// headingLayout
		headingLayout = buildHeadingLayout();
		mainLayout.addComponent(headingLayout);
		
		// headingButtonslLayout
		headingButtonslLayout = buildHeadingButtonslLayout();
		mainLayout.addComponent(headingButtonslLayout);
		
		// bodyLayout
		bodyLayout = buildBodyLayout();
		mainLayout.addComponent(bodyLayout);
		
		return mainLayout;
	}

	@AutoGenerated
	private HorizontalLayout buildHeadingLayout() {
		// common part: create layout
		headingLayout = new HorizontalLayout();
		headingLayout.setStyleName("heading");
		headingLayout.setImmediate(false);
		headingLayout.setWidth("100.0%");
		headingLayout.setHeight("40px");
		headingLayout.setMargin(false);
		
		// embedded_1
		embedded_1 = new Embedded();
		embedded_1.setImmediate(false);
		embedded_1.setWidth("-1px");
		embedded_1.setHeight("-1px");
		embedded_1.setSource(new com.vaadin.server.ThemeResource("images/logo.png"));
		embedded_1.setType(1);
		embedded_1.setMimeType("image/gif");
		headingLayout.addComponent(embedded_1);
		
		// titleLabel
		titleLabel = new Label();
		titleLabel.setStyleName("heading-words");
		titleLabel.setImmediate(false);
		titleLabel.setWidth("470px");
		titleLabel.setHeight("-1px");
		titleLabel.setValue("title");
		headingLayout.addComponent(titleLabel);
		headingLayout.setComponentAlignment(titleLabel, new Alignment(34));
		
		return headingLayout;
	}

	@AutoGenerated
	private HorizontalLayout buildHeadingButtonslLayout() {
		// common part: create layout
		headingButtonslLayout = new HorizontalLayout();
		headingButtonslLayout.setStyleName("heading-buttons");
		headingButtonslLayout.setImmediate(false);
		headingButtonslLayout.setWidth("100.0%");
		headingButtonslLayout.setHeight("26px");
		headingButtonslLayout.setMargin(false);
		
		// menuBar_1
		menuBar_1 = new MenuBar();
		menuBar_1.setImmediate(false);
		menuBar_1.setWidth("-1px");
		menuBar_1.setHeight("-1px");
		headingButtonslLayout.addComponent(menuBar_1);
		
		// loggedInAs
		loggedInAs = new Label();
		loggedInAs.setStyleName("heading-button");
		loggedInAs.setImmediate(false);
		loggedInAs.setWidth("-1px");
		loggedInAs.setHeight("-1px");
		loggedInAs.setValue("logged.in.as");
		headingButtonslLayout.addComponent(loggedInAs);
		headingButtonslLayout.setComponentAlignment(loggedInAs, new Alignment(
				34));
		
		return headingButtonslLayout;
	}

	@AutoGenerated
	private HorizontalLayout buildBodyLayout() {
		// common part: create layout
		bodyLayout = new HorizontalLayout();
		bodyLayout.setImmediate(false);
		bodyLayout.setWidth("100.0%");
		bodyLayout.setHeight("-1px");
		bodyLayout.setMargin(false);
		
		// panel_1
//		panel_1 = buildPanel_1();
//		bodyLayout.addComponent(panel_1);
		
		// panel_2
		panel_2 = buildPanel_2();
		bodyLayout.addComponent(panel_2);
		bodyLayout.setComponentAlignment(panel_2, Alignment.TOP_LEFT);
		
		return bodyLayout;
	}

	@AutoGenerated
	private Panel buildPanel_1() {
		// common part: create layout
		panel_1 = new Panel();
		panel_1.setImmediate(false);
		panel_1.setWidth("100px");
		panel_1.setHeight("-1px");
		
		// ApplicationIconContainer
		ApplicationIconContainer = new VerticalLayout();
		ApplicationIconContainer.setImmediate(false);
		ApplicationIconContainer.setWidth("100.0%");
		ApplicationIconContainer.setHeight("100.0%");
		ApplicationIconContainer.setMargin(false);
		panel_1.setContent(ApplicationIconContainer);
		
		return panel_1;
	}

	@AutoGenerated
	private Panel buildPanel_2() {
		// common part: create layout
		panel_2 = new Panel();
		panel_2.setImmediate(false);
//		panel_2.setWidth("700px");
		panel_2.setHeight("-1px");
		
		// ApplicationlBodyLayout
		ApplicationlBodyLayout = new VerticalLayout();
		ApplicationlBodyLayout.setImmediate(false);
		ApplicationlBodyLayout.setWidth("100.0%");
		ApplicationlBodyLayout.setHeight("100.0%");
		ApplicationlBodyLayout.setMargin(false);
		panel_2.setContent(ApplicationlBodyLayout);
		
		return panel_2;
	}

	public void setMessageSource(MessageSource messageSource) {
		m_messageSourceAccessor = new MessageSourceAccessor(messageSource);
	}
	public BundleListenerImpl getBundleListener() {
		return m_bundleListener;
	}
	public void setBundleListener(BundleListenerImpl bundleListener) {
		m_bundleListener = bundleListener;
	}
	public QueueProcessManager getQueueProcessManager() {
		return m_queueProcessManager;
	}
	public void setQueueProcessManager(QueueProcessManager queueProcessManager) {
		m_queueProcessManager = queueProcessManager;
	}
	public PermissionManager getPermissionManager() {
		return m_permissionManager;
	}

	public void setPermissionManager(PermissionManager permissionManager) {
		m_permissionManager = permissionManager;
	}

//	public ProcessInstances getProcessInstances() {
//		return m_processInstances;
//	}
//	public void setProcessInstances(ProcessInstances processInstances) {
//		m_processInstances = processInstances;
//	}
//	public WorkflowClient getWorkflowClient() {
//		return m_workflowClient;
//	}
//	public void setWorkflowClient(WorkflowClient workflowClient) {
//		m_workflowClient = workflowClient;
//	}
//	public LaunchWizard getLaunchWizard() {
//		return m_launchWizard;
//	}
//	public void setLaunchWizard(LaunchWizard launchWizard) {
//		m_launchWizard = launchWizard;
//	}
//	public FormWizard getFormWizard() {
//		return m_formWizard;
//	}
//	public void setFormWizard(FormWizard formWizard) {
//		m_formWizard = formWizard;
//	}
}
