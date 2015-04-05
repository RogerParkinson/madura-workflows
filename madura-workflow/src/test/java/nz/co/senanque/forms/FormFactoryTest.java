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
package nz.co.senanque.forms;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This shows the FormFactory working with the full structure.
 * Each bean is qualified by an environment name which is hard coded in the xml file
 * though it might be defined in a property there so it can be adjusted from the outside.
 * This allows different beans to be delivered by different environments.
 * 
 * @author Roger Parkinson
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class FormFactoryTest {
	
	@Autowired private FormFactory m_formFactory;

	@Test
	public void testGetForm() {
		WorkflowForm form1 = m_formFactory.getForm("launchForm");
		WorkflowForm form2 = m_formFactory.getForm("launchForm");
		assertFalse(System.identityHashCode(form2)==System.identityHashCode(form1));
		WorkflowForm form3 = m_formFactory.getForm("secondForm");
		assertNotNull(form3);
	}

}
