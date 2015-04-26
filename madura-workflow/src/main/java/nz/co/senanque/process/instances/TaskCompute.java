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

import nz.co.senanque.validationengine.ValidationSessionHolder;
import nz.co.senanque.validationengine.ValidationSessionHolderImpl;
import nz.co.senanque.workflow.AbortException;
import nz.co.senanque.workflow.ProcessInstanceUtils;
import nz.co.senanque.workflow.instances.ProcessInstance;
import nz.co.senanque.workflow.instances.TaskStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskCompute extends TaskBase {

	private static final Logger log = LoggerFactory
			.getLogger(TaskCompute.class);
    public TaskCompute(ProcessDefinition ownerProcess) {
		super(ownerProcess);
	}
	@SuppressWarnings("rawtypes")
	private ComputeType m_computeType;

	public String toString() {
		return super.toString()+" args=" +getArguments();
	}

	@SuppressWarnings("rawtypes")
	public ComputeType getComputeType() {
		return m_computeType;
	}
	@SuppressWarnings("unchecked")
	public boolean execute(ProcessInstance processInstance) {
		log.debug("{}",this);
		Object context = getContext(processInstance);
		ValidationSessionHolder validationSessonHolder = null;
		try {
			validationSessonHolder = new ValidationSessionHolderImpl(getValidationEngine());
			validationSessonHolder.bind(context);
			m_computeType.execute(processInstance, context, getArguments());
		} catch (Exception e) {
			throw new AbortException(e);
		} finally {
			validationSessonHolder.close();
		}			
		ProcessInstanceUtils.clearQueue(processInstance, TaskStatus.DONE);
		mergeContext(context);
		return true;
	}

	public void setComputeType(@SuppressWarnings("rawtypes") ComputeType computeType) {
		m_computeType = computeType;
	}

}
