package nz.co.senanque.workflowui;

import java.lang.reflect.Method;

import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;

import nz.co.senanque.forms.FormEnvironment;
import nz.co.senanque.forms.WorkflowForm;
import nz.co.senanque.locking.LockFactory;
import nz.co.senanque.locking.simple.SimpleLockFactory;
import nz.co.senanque.madura.bundle.BundleExport;
import nz.co.senanque.madura.bundle.spring.BundledInterfaceRegistrar;
import nz.co.senanque.vaadin.Hints;
import nz.co.senanque.vaadin.permissionmanager.PermissionManager;
import nz.co.senanque.vaadin.permissionmanager.PermissionManagerImpl;
import nz.co.senanque.workflow.WorkflowClient;
import nz.co.senanque.workflow.WorkflowDAO;
import nz.co.senanque.workflow.WorkflowJPA;
import nz.co.senanque.workflow.instances.ProcessInstance;
import nz.co.senanque.workflowui.ProcessInstances.ProcessInstanceEvent;
import nz.co.senanque.workflowui.conf.QueueProcessManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.context.ContextLoaderListener;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.EnableVaadin;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.spring.server.SpringVaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("mytheme")
@Title("Madura Workflow")
@Widgetset("com.vaadin.DefaultWidgetSet")
@SpringUI
public class MyUI extends UI implements MessageSourceAware {

	private static final long serialVersionUID = 1L;
//	private static Logger m_logger = LoggerFactory.getLogger(MyUI.class);
	
	@Autowired private PermissionManager m_permissionManager;
	@Autowired private AboutWindow m_aboutWindow;
	@Autowired private transient LaunchWizard m_launchWizard;
	@Autowired private transient FormWizard m_formWizard;
	@Autowired transient WorkflowClient m_workflowClient;
	@Autowired private transient ProcessInstances m_processInstances;
	@Autowired transient QueueProcessManager m_queueProcessManager;

	private VerticalLayout mainLayout;
	private HorizontalLayout bodyLayout;
	private Panel panel_2;
	private VerticalLayout ApplicationlBodyLayout;
	private HorizontalLayout headingButtonslLayout;
	private Label loggedInAs;
	private MenuBar menuBar;
	private HorizontalLayout headingLayout;
	private Label titleLabel;
	private Embedded embedded_1;
	private MessageSource m_messageSource;
	private transient MenuItem launch;

    @WebServlet(name = "MyUIServlet", urlPatterns = "/*", asyncSupported = true)
    public static class MyUIServlet extends SpringVaadinServlet {

		private static final long serialVersionUID = 1L;
    }

    @WebListener
    public static class MyContextLoaderListener extends ContextLoaderListener {
    	// This causes the applicationContext.xml context file to be loaded
    	// per session.
    }

    @Configuration
    @EnableVaadin
    @Import(BundledInterfaceRegistrar.class)
    @ComponentScan(basePackages = {
    		"nz.co.senanque.vaadin",			// madura-vaadin
    		"nz.co.senanque.validationengine",	// madura-objects
    		"nz.co.senanque.workflowui.conf"})
    @PropertySource("classpath:config.properties")
    public static class MyConfiguration {
    	
    	@Autowired MessageSource messageSource;
    	
    	public MyConfiguration() {
    	}

    	// needed for @PropertySource
    	@Bean
    	public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
    		return new PropertySourcesPlaceholderConfigurer();
    	}
    	@Bean(name="hints")
    	@Scope(value="vaadin-ui", proxyMode = ScopedProxyMode.TARGET_CLASS)
    	@BundleExport
    	public Hints getHints() {
    		return new WorkflowUIHints();
    	}
    	@Bean(name="permissionManager")
    	@Scope(value="vaadin-ui", proxyMode = ScopedProxyMode.TARGET_CLASS)
    	@BundleExport
    	public PermissionManager getPermissionManager() {
    		PermissionManagerImpl ret = new PermissionManagerImpl();
    		return ret;
    	}
    	@Bean(name="workflowClient")
    	@UIScope
    	public WorkflowClient getWorkflowClient() {
    		return new WorkflowClient();
    	}
    	@Bean(name="formEnvironment")
    	@BundleExport
    	public FormEnvironment getFormEnvironment() {
    		return new FormEnvironment();
    	}
    	@Bean(name="workflowDAO")
    	@BundleExport
    	public WorkflowDAO getWorkflowDAO() {
    		return new WorkflowJPA();
    	}
    	@Bean(name="lockFactory")
    	@BundleExport
    	public LockFactory getLockFactory() {
    		return new SimpleLockFactory();
    	}
    	
    }
    @Override
    protected void init(VaadinRequest vaadinRequest) { // called at session start
	
    	final MessageSourceAccessor messageSourceAccessor = new MessageSourceAccessor(m_messageSource);
    	buildMainLayout(messageSourceAccessor);
    	setContent(mainLayout);
    	
		final MenuBar.MenuItem file = createFileMenu(messageSourceAccessor);
		final MenuBar.MenuItem help = createHelpMenu(messageSourceAccessor);
		launch = file.addItem(messageSourceAccessor.getMessage("Launch...","Launch..."), new Command() {

			private static final long serialVersionUID = 1L;
			public void menuSelected(MenuItem selectedItem) {
				m_launchWizard.load();				
			}});
		file.addItem(messageSourceAccessor.getMessage("logout","Logout"), new Command(){

			private static final long serialVersionUID = -1L;

			public void menuSelected(MenuItem selectedItem) {
				m_permissionManager.close(getUI());
			}
			});
		Method formWizardClickMethod;
		try {
			Method processInstanceClickMethod = this.getClass().getMethod("processInstanceClick", new Class<?>[]{ProcessInstanceEvent.class});
			m_processInstances.addListener(ProcessInstanceEvent.class, this, processInstanceClickMethod);
			formWizardClickMethod = this.getClass().getMethod("formWizardClick", new Class<?>[]{ClickEvent.class});
			m_formWizard.addListener(ClickEvent.class, this, formWizardClickMethod);
			m_launchWizard.addListener(ClickEvent.class, this, formWizardClickMethod);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (m_launchWizard.setup() <= 0) {
			launch.setEnabled(false);
		}
		m_processInstances.setup();
		ApplicationlBodyLayout.addComponent(m_processInstances);
    }
	public void processInstanceClick(ProcessInstanceEvent processInstanceEvent) {
		ProcessInstance processInstance = processInstanceEvent.getProcessInstance();
		WorkflowForm form = m_workflowClient.getCurrentForm(processInstance);
		if (!m_queueProcessManager.getWriteableQueues(m_permissionManager).contains(processInstance.getQueueName())) {
			form.setReadOnly(true);
		}
		m_formWizard.load(form);
	}
	public void formWizardClick(ClickEvent event) {
		Button button = (com.vaadin.ui.Button)(event.getSource());
		String eventData = (button.getData() != null && button.getData() instanceof String)?(String)button.getData():null;
		if (eventData != null) {
			if (eventData.startsWith(WorkflowForm.LAUNCH)) {
				String processId = eventData.substring(WorkflowForm.LAUNCH.length());
				m_workflowClient.finishLaunch(Long.parseLong(processId));
			}
		}
		if (!"cancel".equals(eventData)) {
			m_processInstances.refresh();
		}
	}

    private MenuBar.MenuItem createHelpMenu(final MessageSourceAccessor messageSourceAccessor) {
    	MenuBar.MenuItem help = menuBar.addItem(messageSourceAccessor.getMessage("help","Help"), null);
		help.addItem(messageSourceAccessor.getMessage("demo.script","GitHub"), new Command() {

			private static final long serialVersionUID = 1L;
			public void menuSelected(MenuItem selectedItem) {
				Page.getCurrent().open(messageSourceAccessor.getMessage("demo.url"), null);
			}});

		help.addItem(messageSourceAccessor.getMessage("about","About"), new Command(){

			private static final long serialVersionUID = -1L;

			public void menuSelected(MenuItem selectedItem) {
				m_aboutWindow.load();
			}});
		return help;
    }
    private MenuBar.MenuItem createFileMenu(final MessageSourceAccessor messageSourceAccessor) {
    	MenuBar.MenuItem file = menuBar.addItem(messageSourceAccessor.getMessage("file","File"), null);
		return file;
    }
	private VerticalLayout buildMainLayout(MessageSourceAccessor messageSourceAccessor) {
		// top-level component properties
		setWidth("950px");
		setHeight("-1px");
		
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("900px");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(false);
		
		// headingLayout
		headingLayout = buildHeadingLayout(messageSourceAccessor);
		mainLayout.addComponent(headingLayout);
		
		// headingButtonslLayout
		headingButtonslLayout = buildHeadingButtonslLayout(messageSourceAccessor);
		mainLayout.addComponent(headingButtonslLayout);
		
		// bodyLayout
		bodyLayout = buildBodyLayout(messageSourceAccessor);
		mainLayout.addComponent(bodyLayout);
		
		return mainLayout;
	}

	private HorizontalLayout buildHeadingLayout(MessageSourceAccessor messageSourceAccessor) {
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
		titleLabel.addStyleName("v-textfield-align-right");
		titleLabel.setImmediate(false);
//		titleLabel.setWidth("470px");
		titleLabel.setHeight("-1px");
		titleLabel.setValue(messageSourceAccessor.getMessage("title"));
		headingLayout.addComponent(titleLabel);
		headingLayout.setComponentAlignment(titleLabel, Alignment.MIDDLE_RIGHT);
		
		return headingLayout;
	}

	private HorizontalLayout buildHeadingButtonslLayout(MessageSourceAccessor messageSourceAccessor) {
		// common part: create layout
		headingButtonslLayout = new HorizontalLayout();
		headingButtonslLayout.setStyleName("heading-buttons");
		headingButtonslLayout.setImmediate(false);
		headingButtonslLayout.setWidth("100.0%");
		headingButtonslLayout.setHeight("26px");
		headingButtonslLayout.setMargin(false);
		
		// menuBar_1
		menuBar = new MenuBar();
		menuBar.setImmediate(false);
		menuBar.setWidth("-1px");
		menuBar.setHeight("-1px");
		headingButtonslLayout.addComponent(menuBar);
		
		// loggedInAs
		loggedInAs = new Label();
		loggedInAs.setStyleName("heading-button");
		loggedInAs.addStyleName("v-textfield-align-right");
		loggedInAs.setImmediate(false);
		loggedInAs.setWidth("-1px");
		loggedInAs.setHeight("-1px");
		String loggedInAsString = messageSourceAccessor.getMessage("logged.in.as",new String[]{m_permissionManager.getCurrentUser()});
		loggedInAs.setValue(loggedInAsString);
		headingButtonslLayout.addComponent(loggedInAs);
		headingButtonslLayout.setComponentAlignment(loggedInAs, Alignment.MIDDLE_RIGHT);
		
		return headingButtonslLayout;
	}

	private HorizontalLayout buildBodyLayout(MessageSourceAccessor messageSourceAccessor) {
		// common part: create layout
		bodyLayout = new HorizontalLayout();
		bodyLayout.setImmediate(false);
		bodyLayout.setWidth("100.0%");
		bodyLayout.setHeight("-1px");
		bodyLayout.setMargin(false);
		
		// panel_2
		panel_2 = buildPanel_2(messageSourceAccessor);
		bodyLayout.addComponent(panel_2);
		bodyLayout.setComponentAlignment(panel_2, Alignment.TOP_RIGHT);
		
		return bodyLayout;
	}

	private Panel buildPanel_2(MessageSourceAccessor messageSourceAccessor) {
		// common part: create layout
		panel_2 = new Panel();
		panel_2.setImmediate(false);
//		panel_2.setWidth("100.0%");
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
	@Override
	public void setMessageSource(MessageSource messageSource) {
		m_messageSource = messageSource;
	}

}
