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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.senanque.vaadinsupport.application.Preauthenticator;
import nz.co.senanque.vaadinsupport.application.SpringApplicationLoader;
import nz.co.senanque.vaadinsupport.viewmanager.LoginLayout;
import nz.co.senanque.vaadinsupport.viewmanager.ViewManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;

@SuppressWarnings("serial")
public class WorkflowUI extends Application implements HttpServletRequestListener, MessageSourceAware {

	protected transient Logger log = LoggerFactory.getLogger(this.getClass());
	private transient MessageSourceAccessor m_messageSourceAccessor;
	@Autowired(required=false) private Preauthenticator m_preauthenticator;
    private boolean m_preauthenticated;
    @Autowired private ViewManager m_viewManager;
    private HttpServletRequest m_currentRequest;

    public WorkflowUI() {
        this.log.info(this.getClass().getSimpleName() + " constructor invoked");
    }

    @Override
    public void init() {
        this.log.info("initializing workflow application...");
        if (m_messageSourceAccessor != null)
        {
        	throw new IllegalStateException("context already loaded");
        }
        if (m_currentRequest == null)
        {
        	throw new IllegalStateException("no current request");
        }
        SpringApplicationLoader.loadContext(this, m_currentRequest);
        getViewManager().setApplication(this);
		setMainWindow(getViewManager().getMainWindow());
		if (isPreauthenticated())
		{
			getViewManager().loadApplication();
		}
		else
		{
			getViewManager().switchScreen(LoginLayout.class.getName());
		}
    }

	public Preauthenticator getPreauthenticator() {
		return m_preauthenticator;
	}

	public void setMessageSource(MessageSource messageSource) {
		m_messageSourceAccessor = new MessageSourceAccessor(messageSource);
	}

	public boolean isPreauthenticated() {
		return m_preauthenticated;
	}

	public void setPreauthenticated(boolean preauthenticated) {
		m_preauthenticated = preauthenticated;
	}
    public String getVersion()
    {
    	return m_messageSourceAccessor.getMessage("version", null,"NOVERSION");
    }

	public void onRequestStart(HttpServletRequest request,
			HttpServletResponse response) {
		m_currentRequest = request;
        Preauthenticator preauthenticator = getPreauthenticator();
        if (preauthenticator != null && !isPreauthenticated())
        {
            String userName = preauthenticator.preauthenticate(request);
            if (userName != null)
            {
                setPreauthenticated(true);
            }
        }
	}

	public void onRequestEnd(HttpServletRequest request,
			HttpServletResponse response) {	
		m_currentRequest = null;
	}

	public ViewManager getViewManager() {
		return m_viewManager;
	}

	public void setViewManager(ViewManager viewManager) {
		m_viewManager = viewManager;
	}

	public void setPreauthenticator(Preauthenticator preauthenticator) {
		m_preauthenticator = preauthenticator;
	}

	public void setMessageSourceAccessor(MessageSourceAccessor messageSourceAccessor) {
		m_messageSourceAccessor = messageSourceAccessor;
	}

}

