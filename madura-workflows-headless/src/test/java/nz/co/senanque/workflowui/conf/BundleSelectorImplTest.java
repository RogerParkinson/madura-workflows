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
/*******************************************************************************
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package nz.co.senanque.workflowui.conf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import javax.annotation.PostConstruct;

import nz.co.senanque.madura.bundle.BundleManager;
import nz.co.senanque.madura.bundle.BundleManagerImpl;
import nz.co.senanque.process.instances.ProcessDefinition;
import nz.co.senanque.vaadin.permissionmanager.PermissionManager;
import nz.co.senanque.workflow.InitialBundleSelector;
import nz.co.senanque.workflow.WorkflowClient;
import nz.co.senanque.workflow.WorkflowDAO;
import nz.co.senanque.workflowui.conf.QueueProcessManager;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class BundleSelectorImplTest {

	@Autowired BundleManagerImpl m_bundleManager;
	@Autowired ApplicationContext applicationContext;
	@Autowired WorkflowClient workflowClient;
	@Autowired WorkflowDAO workflowDAO;
	@Autowired QueueProcessManager queueProcessManager;
	@Autowired InitialBundleSelector bundleSelector;
	@Autowired PermissionManager permissionManager;

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
	public void testSelectBundle() {
		m_bundleManager.scan();
		String bundle = bundleSelector.selectInitialBundle("Process1");
		assertTrue(bundle.startsWith("order-workflow"));
		Set<ProcessDefinition> processes = queueProcessManager.getVisibleProcesses(permissionManager);
		assertEquals(6,processes.size());
		Set<String> queues = queueProcessManager.getVisibleQueues(permissionManager);
		assertEquals(2,queues.size());
	}

}
