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

import java.io.OutputStream;

import javax.annotation.PostConstruct;

import nz.co.senanque.vaadinsupport.viewmanager.ViewManager;
import nz.co.senanque.workflow.WorkflowDAO;
import nz.co.senanque.workflow.instances.Attachment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Form;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * @author Roger Parkinson
 *
 */
public class AttachmentPopup extends Window implements MessageSourceAware {
	
	private static final long serialVersionUID = 1L;
	private Layout main;
	private Panel panel;
	private CheckBox checkbox;
	private TextField comment;
	private String m_windowWidth = "600px";
	private String m_windowHeight = "200px";
	@Autowired WorkflowDAO m_workflowDAO;
	@Autowired private ViewManager m_viewManager;
	private transient MessageSourceAccessor m_messageSourceAccessor;
	private AttachmentReceiver receiver = new AttachmentReceiver();

	@Override
	public void setMessageSource(MessageSource messageSource) {
		m_messageSourceAccessor = new MessageSourceAccessor(messageSource);
	}
    public void close() {
    	if (getParent() != null) {
    		getParent().removeWindow(this);
    	}
    }
	public class AttachmentEvent extends Event {

		private static final long serialVersionUID = 1L;

		public AttachmentEvent(Component component) {
			super(component);
		}
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
        
        setCaption(m_messageSourceAccessor.getMessage("attachment", "Attachment"));
        this.addListener(new CloseListener(){

			@Override
			public void windowClose(CloseEvent e) {
				fireEvent(new AttachmentEvent(panel));
			}});
	}
	public void load(final long pid) {
		panel.removeAllComponents();
		Form form = new Form();
		final Upload upload = new Upload("", receiver);
		checkbox = new CheckBox(m_messageSourceAccessor.getMessage("upload.protected", "Protected"));
		comment = new TextField(m_messageSourceAccessor.getMessage("upload.comment", "Comment"));
		form.addField("comment", comment);
		form.addField("protected", checkbox);
		panel.addComponent(form);
		panel.addComponent(upload);

        upload.addListener(new Upload.FinishedListener() {

			private static final long serialVersionUID = 1L;

			public void uploadFinished(FinishedEvent event) {
				Attachment attachment = receiver.getWrapper().getCurrentAttachment();
				attachment.setProcessInstanceId(pid);
				attachment.setComment((String)comment.getValue());
				attachment.setProtectedDocument((boolean)checkbox.getValue());
				m_workflowDAO.addAttachment(attachment);
				close();
            }
        });
        
    	if (getParent() == null) {
        	m_viewManager.getMainWindow().addWindow(this);
        	this.center();
        }
	}
	

	public class AttachmentReceiver implements Receiver {

		private static final long serialVersionUID = 1L;
		private AttachmentWrapper m_wrapper;

        public OutputStream receiveUpload(String filename, String MIMEType) {
        	m_wrapper = new AttachmentWrapper(filename, MIMEType);
            return m_wrapper.getStream();
        }

		public AttachmentWrapper getWrapper() {
			return m_wrapper;
		}
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
	public ViewManager getViewManager() {
		return m_viewManager;
	}
	public void setViewManager(ViewManager viewManager) {
		m_viewManager = viewManager;
	}

}
