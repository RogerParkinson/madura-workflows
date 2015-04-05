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
package nz.co.senanque.process.instances;

import java.util.Map;

import nz.co.senanque.validationengine.ValidationEngine;
import nz.co.senanque.validationengine.ValidationObject;
import nz.co.senanque.validationengine.ValidationSession;
import nz.co.senanque.workflow.instances.ProcessInstance;

/**
 * @author Roger Parkinson
 * @param <T>
 *
 */
public class ComputeTypeWrapper<T> implements ComputeType<T> {

    private final ValidationEngine m_validationEngine;
	private final ComputeType<T> m_computeType;

    public ComputeTypeWrapper(ValidationEngine validationEngine, ComputeType<T> computeType) {
    	m_validationEngine = validationEngine;
    	m_computeType = computeType;
	}
	public void execute(ProcessInstance processInstance, T context, Map<String,String> map) {
		ValidationSession validationSession =  m_validationEngine.createSession();
        validationSession.bind((ValidationObject) context);
        m_computeType.execute(processInstance, context, map);
        validationSession.unbind((ValidationObject) context);
	}
}
