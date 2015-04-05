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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

import nz.co.senanque.locking.LockFactory;
import nz.co.senanque.locking.LockFactory.LockType;
import nz.co.senanque.workflow.instances.ProcessInstance;


/**
 * @author Roger Parkinson
 *
 */
public class ContextUtils {
	
	public static Class<?> getContextClass(String contextDescriptor) {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			String className = contextDescriptor.substring(0, contextDescriptor.indexOf('@'));
			return Class.forName(className, false, classLoader);
		} catch (ClassNotFoundException e) {
			throw new WorkflowException(e.getMessage());
		}
	}
	public static Long getContextId(String objectInstance) {
		return new Long(objectInstance.substring(objectInstance.indexOf('@')+1));
	}

	public static String createContextDescriptor(Object o, Object id) {
		return o.getClass().getName()+"@"+((Long)id).longValue();
	}
	
	public static List<Lock> getLocks(ProcessInstance processInstance, LockFactory lockFactory, String comment) {
		String contextDescription = ContextUtils.createContextDescriptor(processInstance,processInstance.getId());
		Lock processInstanceLock = lockFactory.getWrappedLock(contextDescription, LockType.WRITE, comment);
		Lock contextLock = lockFactory.getWrappedLock(processInstance.getObjectInstance(), LockType.WRITE, comment);
		List<Lock> ret = new ArrayList<Lock>();
		ret.add(processInstanceLock);
		ret.add(contextLock);
		return ret;
	}

}
