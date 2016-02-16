package nz.co.senanque.workflowui;

import javax.annotation.PostConstruct;

import nz.co.senanque.vaadin.AboutInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * The About window pops up on request, presents it data and closes on request.
 * 
 * @author Roger Parkinson
 *
 */
@UIScope
@Component("AboutWindow")
public class AboutWindow extends Window implements MessageSourceAware {

	private static final long serialVersionUID = 1L;
	@Autowired @Qualifier("aboutInfo") private AboutInfo m_aboutInfo;
	private MessageSource m_messageSource;
	
	@PostConstruct
	public void init() {
		
		final Window me = this;
		MessageSourceAccessor messageSourceAccessor= new MessageSourceAccessor(m_messageSource);
        Layout main = new VerticalLayout();
        setContent(main);
        main.setWidth("400px");
		Embedded embedded = new Embedded();
		embedded.setImmediate(false);
		embedded.setWidth("-1px");
		embedded.setHeight("-1px");
		embedded.setSource(new com.vaadin.server.ThemeResource("images/logo.jpg"));
		embedded.setType(1);
		embedded.setMimeType("image/gif");
		main.addComponent(embedded);

        main.addComponent(new Label(messageSourceAccessor.getMessage("about.text")));
        Label aboutInfoLabel = new Label(m_aboutInfo.toString(),ContentMode.HTML);
        main.addComponent(aboutInfoLabel);
        Button OK = new Button(messageSourceAccessor.getMessage("OK"));
        OK.setClickShortcut( KeyCode.ENTER ) ;
        OK.addStyleName( ValoTheme.BUTTON_PRIMARY ) ;
		main.addComponent(OK);
		OK.addClickListener(new ClickListener(){

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				me.close();
			}});
	}

	public void load() {
        if (getParent() == null) {
        	UI.getCurrent().addWindow(this);
        	this.center();
        }
	}

	public void setMessageSource(MessageSource messageSource) {
		m_messageSource = messageSource;
	}

}
