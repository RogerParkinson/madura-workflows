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
package nz.co.senanque.workflow;

import static org.junit.Assert.assertEquals;
import nz.co.senanque.validationengine.metadata.ClassMetadata;
import nz.co.senanque.validationengine.metadata.EngineMetadata;
import nz.co.senanque.validationengine.metadata.PropertyMetadata;
import nz.co.senanque.workflow.instances.ProcessInstance;

import org.junit.Test;
import org.junit.runner.RunWith;
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
public class MetadataTest {
	
	@Autowired
	ApplicationContext m_applicationContext;
    @Autowired
    private transient EngineMetadata m_metadata;

    @Test
	public void testMetadata() throws Exception {
    	ClassMetadata m = m_metadata.getClassMetadata(ProcessInstance.class);
    	PropertyMetadata p = m.getField("status");
    	assertEquals("status",p.getLabelName());
	}
	
}
