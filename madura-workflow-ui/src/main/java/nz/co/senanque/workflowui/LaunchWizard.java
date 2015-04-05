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

import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import nz.co.senanque.forms.WorkflowForm;
import nz.co.senanque.process.instances.ProcessDefinition;
import nz.co.senanque.vaadinsupport.permissionmanager.PermissionManager;
import nz.co.senanque.vaadinsupport.viewmanager.ViewManager;
import nz.co.senanque.workflow.WorkflowClient;
import nz.co.senanque.workflow.instances.ProcessInstance;
import nz.co.senanque.workflowui.bundles.QueueProcessManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.util.StringUtils;

import com.vaadin.data.Container;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * This allows a user to launch a process. It fetches the launch form from the relevant bundle and presents it to the user.
 * Then it saves the details for workflow to process.
 * 
 * @author Roger Parkinson
 *
 */
public class LaunchWizard extends Window implements MessageSourceAware {
	
	private static final long serialVersionUID = 1L;
	@Autowired PermissionManager m_permissionManager;
	private Layout main;
	private Panel panel;
    private String m_windowWidth = "800px";
    private String m_windowHeight = "400px";

    ListSelect select = new ListSelect();
	@Autowired transient QueueProcessManager m_queueProcessManager;
    @Autowired private ViewManager m_viewManager;
    @Autowired transient WorkflowClient m_workflowClient;
    @Autowired transient AttachmentsPopup m_attachmentsPopup;
	private transient MessageSourceAccessor m_messageSourceAccessor;
	
	public class FormEvent extends Event {

		private static final long serialVersionUID = 1L;
		private final ProcessInstance m_processInstance;

		public FormEvent(Component component, ProcessInstance processInstance) {
			super(component);
			m_processInstance = processInstance;
		}

		public ProcessInstance getProcessInstance() {
			return m_processInstance;
		}
	}
	
	public class ProcessDefinitionHolder {
		private final ProcessDefinition m_processDefinition;
		public ProcessDefinitionHolder(ProcessDefinition processDefinition) {
			m_processDefinition = processDefinition;
		}
		public ProcessDefinition getProcessDefinition() {
			return m_processDefinition;
		}
		public String toString() {
			return m_processDefinition.getFullName();
		}
	}
	public LaunchWizard() {
	}

	public int setup() {
		SortedSet<ProcessDefinitionHolder> options = getVisibleProcesses();
        final Container c = new IndexedContainer();
        if (options != null) {
            for (final Iterator<?> i = options.iterator(); i.hasNext();) {
                c.addItem(i.next());
            }
        }
        select.setContainerDataSource(c);
        select.setRows(Math.min(10, options.size()+2));
        return options.size();
    }

	@PostConstruct
	public void init() {
        main = new VerticalLayout();
        setContent(main);
        setModal(true);
        main.setStyleName(Panel.STYLE_LIGHT);
        main.setWidth(getWindowWidth());
        main.setHeight(getWindowHeight());
        
        panel = new Panel();
        main.setMargin(true);
        main.addComponent(panel);
        
        panel.addComponent(getInitialLayout());
        setCaption(m_messageSourceAccessor.getMessage("launch.wizard", "Launch Wizard"));
	}
	
	private Component getInitialLayout() {
		VerticalLayout ret = new VerticalLayout();
        // Buttons
        Button cancel = new Button(m_messageSourceAccessor.getMessage("Cancel", "Cancel"));
        HorizontalLayout actions = new HorizontalLayout();
        actions.setMargin(true);
        actions.setSpacing(true);
        actions.addComponent(cancel);
        cancel.addListener(new ClickListener(){

			@Override
			public void buttonClick(ClickEvent event) {
				close();
			}});
        select.setImmediate(true);
        select.addListener(new ValueChangeListener(){

			@Override
			public void valueChange(ValueChangeEvent event) {
				ProcessDefinitionHolder pdh = (ProcessDefinitionHolder)select.getValue();
				if (pdh != null) {
					select.unselect(pdh);
					final ProcessDefinition processDefinition = pdh.getProcessDefinition();
					final WorkflowForm form = m_workflowClient.getLaunchForm(processDefinition.getName());
					form.getProcessInstance().setBundleName(processDefinition.getVersion());
					form.bind();
					((VerticalLayout)form).addListener(new Listener(){

						@Override
						public void componentEvent(Event event) {
							try {
								String s = (String)(((Button)event.getComponent()).getData());
								if ("cancel".equals(s)) {
									close();
								} else if (s.startsWith("OK:")) {
									panel.removeAllComponents();
									panel.addComponent(getFinalLayout(processDefinition.getName(),Long.parseLong(s.substring(3))));
									panel.requestRepaint();
								}
							} catch (Exception e) {
								// ignore null pointer exceptions etc
							}
						}});
					panel.removeAllComponents();
					panel.addComponent((VerticalLayout)form);
					panel.requestRepaint();
				}
			}});
        ret.addComponent(select);
        ret.addComponent(actions);
        return ret;
	}
	
	private Component getFinalLayout(String processName, final long processId) {
		VerticalLayout ret = new VerticalLayout();
		Button okay = new Button(m_messageSourceAccessor.getMessage("OK", "Okay"));
        HorizontalLayout actions = new HorizontalLayout();
        Label label = new Label(m_messageSourceAccessor.getMessage("launched.processid", new Object[]{processName,processId}));
        
        ret.addComponent(label);
        actions.setMargin(true);
        actions.setSpacing(true);
        actions.addComponent(okay);
        okay.addListener(new ClickListener(){

			@Override
			public void buttonClick(ClickEvent event) {
				close();
				fireEvent(event);
			}});
        ret.addComponent(actions);
		Button attachments = new Button(m_messageSourceAccessor.getMessage("attachments", "Attachments"));
		actions.addComponent(attachments);
		attachments.addListener(new ClickListener(){

			@Override
			public void buttonClick(ClickEvent event) {
				m_attachmentsPopup.load(processId);				
			}});
        
		return ret;
	}
	
	private class ProcessDefinitionHolderComparator implements Comparator<ProcessDefinitionHolder> {

		@Override
		public int compare(ProcessDefinitionHolder o1,
				ProcessDefinitionHolder o2) {
			return o1.getProcessDefinition().getName().compareTo(o2.getProcessDefinition().getName());
		}
	}
	
	private SortedSet<ProcessDefinitionHolder> getVisibleProcesses() {
		SortedSet<ProcessDefinitionHolder> ret = new TreeSet<>(new ProcessDefinitionHolderComparator());
		for (ProcessDefinition processDefinition: m_queueProcessManager.getVisibleProcesses(m_permissionManager)) {
			if (StringUtils.hasText(processDefinition.getLaunchForm())) {
				ret.add(new ProcessDefinitionHolder(processDefinition));
			}
		}
		return ret;
	}

    public void load() {
		panel.removeAllComponents();
		Object selected = select.getValue();
		if (selected != null) {
			select.unselect(selected);
		}
		panel.addComponent(getInitialLayout());
        if (getParent() == null) {
        	m_viewManager.getMainWindow().addWindow(this);
        	this.center();
        }
    }
    public void close() {
    	if (getParent() != null) {
    		getParent().removeWindow(this);
    	}
    }

    public PermissionManager getPermissionManager() {
		return m_permissionManager;
	}

	public void setPermissionManager(PermissionManager permissionManager) {
		m_permissionManager = permissionManager;
	}

	public ViewManager getViewManager() {
		return m_viewManager;
	}

	public void setViewManager(ViewManager viewManager) {
		m_viewManager = viewManager;
	}

	@Override
	public void setMessageSource(MessageSource messageSource) {
		m_messageSourceAccessor = new MessageSourceAccessor(messageSource);
	}

	public QueueProcessManager getQueueProcessManager() {
		return m_queueProcessManager;
	}

	public void setQueueProcessManager(QueueProcessManager queueProcessManager) {
		m_queueProcessManager = queueProcessManager;
	}

	public WorkflowClient getWorkflowClient() {
		return m_workflowClient;
	}

	public void setWorkflowClient(WorkflowClient workflowClient) {
		m_workflowClient = workflowClient;
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

	public AttachmentsPopup getAttachmentsPopup() {
		return m_attachmentsPopup;
	}

	public void setAttachmentsPopup(AttachmentsPopup attachmentsPopup) {
		m_attachmentsPopup = attachmentsPopup;
	}

}
