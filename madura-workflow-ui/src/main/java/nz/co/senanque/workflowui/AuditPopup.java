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

import nz.co.senanque.vaadin.MaduraForm;
import nz.co.senanque.vaadin.MaduraSessionManager;
import nz.co.senanque.validationengine.ValidationObject;
import nz.co.senanque.workflow.instances.Audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;

import com.vaadin.data.util.BeanItem;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Used to display an Audit record on a read-only form.
 * 
 * @author Roger Parkinson
 *
 */
@UIScope
@org.springframework.stereotype.Component
public class AuditPopup extends Window implements MessageSourceAware {

	private static final long serialVersionUID = 1L;
	private Layout main;
	private Layout panel;
	private String m_windowWidth = "800px";
	private String m_windowHeight = "400px";
	private transient MessageSourceAccessor m_messageSourceAccessor;
    private MaduraForm m_auditForm;
	@Autowired private MaduraSessionManager m_maduraSessionManager;

	@Override
	public void setMessageSource(MessageSource messageSource) {
		m_messageSourceAccessor = new MessageSourceAccessor(messageSource);
	}
    public void close() {
    	getMaduraSessionManager().getValidationSession().unbind((ValidationObject) m_auditForm.getData());
    	UI.getCurrent().removeWindow(this);
    }
    public void load(Audit audit) {
    	panel.removeAllComponents();
		getMaduraSessionManager().getValidationSession().bind(audit);
    	BeanItem<Audit> beanItem = new BeanItem<Audit>(audit);

    	m_auditForm = new MaduraForm(getMaduraSessionManager());
    	String[] fieldList = new String[]{"created","lockedBy","status","comment"};
    	m_auditForm.setFieldList(fieldList);
    	m_auditForm.setItemDataSource(beanItem);
    	TextArea comment = (TextArea)m_auditForm.getField("comment");
    	comment.setWidth("700px");
    	panel.addComponent(m_auditForm);
		panel.addComponent(getInitialLayout());
    	panel.requestRepaint();
    	if (getParent() == null) {
    		UI.getCurrent().addWindow(this);
        	this.center();
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
        
        setCaption(m_messageSourceAccessor.getMessage("audit", "Audit"));
	}

	private Component getInitialLayout() {
		VerticalLayout ret = new VerticalLayout();
        // Buttons
        Button close = new Button(m_messageSourceAccessor.getMessage("close", "Close"));
        HorizontalLayout actions = new HorizontalLayout();
        actions.setMargin(true);
        actions.setSpacing(true);
        actions.addComponent(close);
        close.addClickListener(new ClickListener(){

			@Override
			public void buttonClick(ClickEvent event) {
				close();
			}});
        ret.addComponent(actions);
        return ret;
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
	public MaduraSessionManager getMaduraSessionManager() {
		return m_maduraSessionManager;
	}
	public void setMaduraSessionManager(MaduraSessionManager maduraSessionManager) {
		m_maduraSessionManager = maduraSessionManager;
	}
}
