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
package nz.co.senanque.workflow;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.persistence.OptimisticLockException;

import nz.co.senanque.forms.WorkflowForm;
import nz.co.senanque.process.instances.ProcessDefinition;
import nz.co.senanque.vaadin.MaduraFieldGroup;
import nz.co.senanque.vaadin.MaduraSessionManager;
import nz.co.senanque.validationengine.ValidationObject;
import nz.co.senanque.validationengine.ValidationSession;
import nz.co.senanque.workflow.instances.ProcessInstance;
import nz.co.senanque.workflow.instances.TaskStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.util.StringUtils;

import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;

/**
 * @author Roger Parkinson
 *
 */
public class GenericVaadinForm extends VerticalLayout implements WorkflowForm, ClickListener, MessageSourceAware {

	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory
			.getLogger(GenericVaadinForm.class);

	private transient Object m_context;
	private transient ProcessInstance m_processInstance;
	@Autowired private transient MaduraSessionManager m_maduraSessionManager;
	protected transient MaduraFieldGroup fieldGroup;
	protected transient Layout panel;
    protected transient Button okay;
    protected transient Button cancel;
    protected transient Button park;
    protected transient Label readOnlyMessage;
    @Autowired private transient WorkflowManager m_workflowManager;

	private transient ProcessDefinition m_processDefinition;
	private transient MessageSourceAccessor m_messageSourceAccessor;
	private String m_referenceName="reference";
	private Field<?> m_referenceField;
	private List<String> m_fieldList;
	private boolean m_readOnlyForm;
	private boolean m_launcher;
	
	public class CloseEvent extends Event {

		private static final long serialVersionUID = 1L;

		public CloseEvent(Button button) {
			super(button);
		}
	}
	@PostConstruct
	public void init() {
        panel = new VerticalLayout();
        panel.setSizeFull();
        addComponent(panel);
	}

	public GenericVaadinForm() {
	}
	
	protected HorizontalLayout createButtons() {
		log.debug("isReadOnlyForm():{}",isReadOnlyForm());
        okay = fieldGroup.createSubmitButton("OK", this);
        okay.setReadOnly(isReadOnlyForm());
        okay.setEnabled(!isReadOnlyForm());
        cancel = fieldGroup.createButton("cancel",this);
        HorizontalLayout actions = new HorizontalLayout();
        actions.setMargin(true);
        actions.setSpacing(true);
        actions.addComponent(okay);
        cancel.addClickListener(this);
        actions.addComponent(cancel);
		park = fieldGroup.createSubmitButton("park",this);
		park.setReadOnly(this.isReadOnlyForm());
		park.setEnabled(!isReadOnlyForm());
        actions.addComponent(park);
        park.addClickListener(this);
        park.setVisible(false);
        return actions;
	}

	public Object getContext() {
		return m_context;
	}
	public void setContext(Object context) {
		m_context = context;
	}
	public ProcessInstance getProcessInstance() {
		return m_processInstance;
	}
	public void setProcessInstance(ProcessInstance processInstance) {
		m_processInstance = processInstance;
	}
	public String getProcessName() {
		return m_processDefinition.getName();
	}
	public boolean isLauncher() {
		return m_launcher;
	}
	@Override
	public void bind() {
		panel.removeAllComponents();
		ValidationObject o = (ValidationObject)getContext();
		BeanItem<ValidationObject> beanItem = new BeanItem<>(o);
		m_maduraSessionManager.getValidationSession().bind(o);
		fieldGroup = m_maduraSessionManager.createMaduraFieldGroup();
		Layout actions = createButtons();
		log.debug("park:{}",(park.isEnabled()?"enabled":"disabled"));
    	Map<String,Field<?>> fields = fieldGroup.buildAndBind(m_fieldList,beanItem);
    	for (Field<?> f:fields.values()) {
    		if (isReadOnlyForm()) {
    			f.setReadOnly(true);
    			f.setEnabled(false);
    		}
    		panel.addComponent(f);
    	}
    	m_referenceField = fields.get(m_referenceName);
		m_launcher = (m_processInstance==null || m_processInstance.getId()== 0);
        park.setVisible(!isLauncher());
        panel.addComponent(actions);
        if (isReadOnlyForm()) {
			park.setReadOnly(true);
			park.setEnabled(false);
	        okay.setReadOnly(true);
	        okay.setEnabled(false);
        }
		log.debug("park:{}",(park.isEnabled()?"enabled":"disabled"));
	}
	@Override
	public void close() {
		ValidationSession session = m_maduraSessionManager.getValidationSession();
		m_maduraSessionManager.close();
		log.debug("{}",m_maduraSessionManager.getValidationEngine().getStats(session));
	}

	private long save() {
		long processId=0;
		try {
			processId = getWorkflowManager().save(this);
		} catch (OptimisticLockException e) {
			String message = m_messageSourceAccessor.getMessage(
					"lock.out", "Locked out");
			Notification.show(message);
			return processId;
			}
		return processId;
	}
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getComponent().equals(okay)) {
			if (StringUtils.hasText(m_referenceName) && m_processInstance.getId() == 0) {
				try {
					String reference = m_referenceField.getValue().toString();
					m_processInstance.setReference(reference);
				} catch (Exception e) {
					// ignore errors
				}
			}
			m_processInstance.setStatus((m_launcher?TaskStatus.WAIT:TaskStatus.GO));
			m_processInstance.setQueueName(null);
			m_processInstance.setLockedBy(null);
			long processId = save();
			if (processId == 0) {
				return;
			}
			okay.setData(WorkflowForm.OK+processId);
		}
		if (event.getComponent().equals(cancel)) {
			long processId = m_processInstance.getId();
			if (processId != 0 && !isReadOnly()) {
				m_processInstance = getWorkflowManager().refresh(m_processInstance);
				m_processInstance.setStatus(TaskStatus.WAIT);
				m_processInstance.setLockedBy(null);
				save();
			}
		}
		if (event.getComponent().equals(park)) {
			long processId = m_processInstance.getId();
			if (processId == 0) {
				return;
			} else {
				save();
			}
		}
		close();
		fireEvent(event);		
	}
	
	public MaduraSessionManager getMaduraSessionManager() {
		return m_maduraSessionManager;
	}

	public void setMaduraSessionManager(MaduraSessionManager maduraSessionManager) {
		m_maduraSessionManager = maduraSessionManager;
	}

	public WorkflowManager getWorkflowManager() {
		return m_workflowManager;
	}

	public void setWorkflowManager(WorkflowManager workflowManager) {
		m_workflowManager = workflowManager;
	}

	public void setProcessDefinition(ProcessDefinition processDefinition) {
		m_processDefinition = processDefinition;
	}

	public ProcessDefinition getProcessDefinition() {
		return m_processDefinition;
	}
	public void setReadOnly(boolean b) {
		super.setReadOnly(b);
    	if (isReadOnly()) {
    		setReadOnlyForm(true);
        	if (readOnlyMessage == null) {
        		readOnlyMessage = new Label(m_messageSourceAccessor.getMessage("process.not.writable"));
        		addComponent(readOnlyMessage);
        	}
    	}
	}
	@Override
	public void setMessageSource(MessageSource messageSource) {
		m_messageSourceAccessor = new MessageSourceAccessor(messageSource);
	}

	public String getReferenceName() {
		return m_referenceName;
	}

	public void setReferenceName(String referenceName) {
		m_referenceName = referenceName;
	}

	public List<String> getFieldList() {
		return m_fieldList;
	}

	public void setFieldList(List<String> fieldList) {
		m_fieldList = fieldList;
	}

	public boolean isReadOnlyForm() {
		return m_readOnlyForm;
	}

	public void setReadOnlyForm(boolean readOnlyForm) {
		m_readOnlyForm = readOnlyForm;
	}

}
