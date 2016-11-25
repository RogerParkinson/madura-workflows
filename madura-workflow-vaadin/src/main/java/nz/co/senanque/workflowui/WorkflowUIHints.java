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

import nz.co.senanque.vaadin.HintsImpl;
import nz.co.senanque.vaadin.MaduraPropertyWrapper;

import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

/**
 * @author Roger Parkinson
 *
 */
public class WorkflowUIHints extends HintsImpl {

	private static final long serialVersionUID = 1L;
	public AbstractField<?> getDateField(MaduraPropertyWrapper property) {
		if (property.getName().equals("lastUpdated")) {
			TextField tf = new TextField();
			tf.setReadOnly(true);
			return tf;
		}
		if (property.getName().equals("created")) {
			TextField tf = new TextField();
			tf.setReadOnly(true);
			return tf;
		}
		return super.getDateField(property);
	}
	public AbstractField<?> getTextField(MaduraPropertyWrapper property) {
    	AbstractTextField ret = null;
        if (property.isSecret())
        {
            ret = new PasswordField();
        }
        else
        {
        	if ("comment".equals(property.getName())) {
        		TextArea textArea = new TextArea();
        		textArea.setRows(5);
        		textArea.setWordwrap(true);
        		textArea.setWidth("400px");
        		ret = textArea;
        	} else {
        		ret = new TextField();
        	}
        }
        ret.setMaxLength(property.getMaxLength());
    	if (property.getValue() == null) {
    		property.setValue("");
    	}
        return ret;
    }
}
