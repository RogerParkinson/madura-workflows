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

import nz.co.senanque.workflow.WorkflowDAO;
import nz.co.senanque.workflow.instances.Attachment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;

import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * @author Roger Parkinson
 *
 */
@UIScope
@org.springframework.stereotype.Component
public class AttachmentPopup extends Window implements MessageSourceAware {
	
	private static final long serialVersionUID = 1L;
	private Layout main;
	private Layout panel;
	private CheckBox checkbox;
	private TextField comment;
	private String m_windowWidth = "600px";
	private String m_windowHeight = "300px";
	@Autowired WorkflowDAO m_workflowDAO;
	private transient MessageSourceAccessor m_messageSourceAccessor;
	private AttachmentReceiver receiver = new AttachmentReceiver();

	@Override
	public void setMessageSource(MessageSource messageSource) {
		m_messageSourceAccessor = new MessageSourceAccessor(messageSource);
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
        main.setWidth(getWindowWidth());
        main.setHeight(getWindowHeight());
        
        panel = new VerticalLayout();
        main.addComponent(panel);
        
        setCaption(m_messageSourceAccessor.getMessage("attachment", "Attachment"));
        this.addCloseListener(new CloseListener(){

			private static final long serialVersionUID = 1L;

			@Override
			public void windowClose(CloseEvent e) {
				fireEvent(new AttachmentEvent(panel));
			}});
	}
	public void load(final long pid) {
		panel.removeAllComponents();
		final Upload upload = new Upload(null, receiver);
		upload.setImmediate(true);
		upload.setButtonCaption(m_messageSourceAccessor.getMessage("upload.file", "Upload File"));
		checkbox = new CheckBox(m_messageSourceAccessor.getMessage("upload.protected", "Protected"));
		comment = new TextField(m_messageSourceAccessor.getMessage("upload.comment", "Comment"));
		panel.addComponent(comment);
		panel.addComponent(checkbox);
		panel.addComponent(upload);

        upload.addFinishedListener(new Upload.FinishedListener() {

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
    		UI.getCurrent().addWindow(this);
        	this.center();
        }
	}
    public void close() {
    	UI.getCurrent().removeWindow(this);
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
}
