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
package nz.co.senanque.process.parser;

import static org.junit.Assert.assertEquals;

import javax.annotation.Resource;

import nz.co.senanque.parser.InputStreamParserSource;
import nz.co.senanque.parser.ParserSource;
import nz.co.senanque.process.instances.ProcessDefinition;
import nz.co.senanque.schemaparser.SchemaParserImpl;
import nz.co.senanque.workflow.WorkflowManager;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Roger Parkinson
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ParsePackageTest1 {

	private static final Logger log = LoggerFactory
			.getLogger(ParsePackageTest1.class);
	@Autowired
	ApplicationContext m_applicationContext;
	@Autowired
	WorkflowManager m_workflowManager;
	@Resource(name="schema")
	org.springframework.core.io.Resource m_schema;
	@Resource(name="processes")
	org.springframework.core.io.Resource m_processes;

	/**
	 * Test method for
	 * {@link nz.co.senanque.process.parser.ParsePackage#parse(nz.co.senanque.parser.TextProvider)}
	 * .
	 */
	@Test
	public void testParse() throws Exception {

		SAXBuilder builder = new SAXBuilder();
		Document doc = null;
		doc = builder.build(m_schema.getInputStream());
		SchemaParserImpl schemaParser = new SchemaParserImpl();
		schemaParser.parse(doc, "nz.co.senanque.processparser");
		WorkflowManager workflowManager = m_workflowManager;
		
		ParserSource parserSource = new InputStreamParserSource(m_processes);
		
		ProcessTextProvider textProvider = new ProcessTextProvider(parserSource,schemaParser, workflowManager);
		ParsePackage parsePackage = new ParsePackage();
		parsePackage.setDebug(true);
		parsePackage.parse(textProvider);
		assertEquals(8, workflowManager.getMainProcesses().size());
		assertEquals(26, workflowManager.getAllProcesses().size());
		log.debug("All processes:");
		for (ProcessDefinition pd :workflowManager.getAllProcesses()) {
			log.debug(pd.getName());
		}
		log.debug("Main processes:");
		for (ProcessDefinition pd :workflowManager.getMainProcesses()) {
			log.debug(pd.getName());
		}
		if (log.isDebugEnabled()) {
			log.debug(textProvider.toString());
		}
	}

}
