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
package nz.co.senanque.database;

import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.annotation.PostConstruct;

import nz.co.senanque.madura.bundle.BundleManager;
import nz.co.senanque.madura.bundle.StringWrapper;
import nz.co.senanque.workflow.ContextDAO;
import nz.co.senanque.workflow.ContextTester;
import nz.co.senanque.workflow.WorkflowDAO;
import nz.co.senanque.workflow.instances.ProcessInstance;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Roger Parkinson
 * 
 * This tests that the database connections work when operated through the bundle.
 * The test depends on the tbundle which actually does the database stuff.
 * The database operations are actually a 2phase transaction with one connection defined in this
 * main part and the other connection in the bundle.
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Transactional
public class SimpleWorkflowTest {

	private static final Logger log = LoggerFactory
			.getLogger(SimpleWorkflowTest.class);

	@Autowired BundleManager m_bundleManager;
	@Autowired ApplicationContext applicationContext;
	@Autowired ContextDAO contextDAO;
	@Autowired WorkflowDAO workflowDAO;
	@Autowired ContextTester contextTester;
	@Autowired DatabaseLoadDAO m_databaseLoadDAO;
	
	// The following is needed to ensure the destroy methods of all the beans
	// including those in the bundles, are called correctly
	private static BundleManager s_bundleManager;
    private static AbstractApplicationContext s_applicationContext;
    @PostConstruct
    public void init() {
    	s_bundleManager = m_bundleManager;
    	s_applicationContext = (AbstractApplicationContext)applicationContext;
    }
	@AfterClass
	public static void destroy() {
		s_bundleManager.shutdown();
		s_applicationContext.close();
	}
	
	@Test
	public void testConnection() throws Exception {
		
		m_bundleManager.setBundle("simple-workflow");
        StringWrapper n = (StringWrapper)this.applicationContext.getBean("bundleName");
        assertTrue(n.toString().startsWith("simple-workflow"));
        
//        m_databaseLoadDAO.clear();
        m_databaseLoadDAO.load();
        List<Long> result = m_databaseLoadDAO.query();
        ProcessInstance pi = workflowDAO.findProcessInstance(result.get(0));
        pi.toString();
        
        String contextDescriptor = contextTester.createOrder();
        Object o = contextTester.getOrder(contextDescriptor);
        pi = contextTester.getProcessInstance(result.get(0));
        pi.toString();
	}

}
