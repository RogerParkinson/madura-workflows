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
package nz.co.senanque.workflow;

import nz.co.senanque.messaging.ErrorEndpoint;
import nz.co.senanque.validationengine.ValidationEngine;
import nz.co.senanque.validationengine.ValidationSession;
import nz.co.senanque.workflow.instances.Audit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This test will fail if you aren't online.
 * @author Roger Parkinson
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class LoadContext2Test {
	
	private static final Logger log = LoggerFactory.getLogger(LoadContext2Test.class);
	@Autowired
	ApplicationContext m_applicationContext;
    @Autowired
    private transient ValidationEngine m_validationEngine;
    @Autowired ErrorEndpoint m_errorEndpoint;

    @Test
	public void testLoad() throws Exception {
		Audit audit = new Audit();
		
        ValidationSession validationSession = m_validationEngine.createSession();
        validationSession.bind(audit);
        validationSession.unbind(audit);
		
		validationSession.close();
	}
	
}
