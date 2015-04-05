/*******************************************************************************
 * Copyright (c)29/04/2014 Prometheus Consulting
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a utility class that logs the stack as well as a field in the target object.
 * We use reflection to ensure we don't need to have the relevant class on the classpath
 * at compile time. It is only used for debugging.
 * 
 * @author Roger Parkinson
 *
 */
public class DisplayContext {
	
	private static final Logger log = LoggerFactory
			.getLogger(DisplayContext.class);
	
	public static void display(Object context) {
		if (log.isDebugEnabled()) {
			
			StackTraceElement[] ste = Thread.currentThread().getStackTrace();

	        try {
				Method m = context.getClass().getMethod("getCelsius");
				Object value = m.invoke(context);
				int i = 0;
				log.debug("--------------celsius {}",value);
				for (i=2; i<ste.length;i++) {
					String s = ste[i].getClassName();
					if (s.startsWith("com.vaadin")) {
						break;
					}
					if (s.startsWith("nz.co")) {
						log.debug("{}:{} {}",ste[i].getFileName(),ste[i].getLineNumber(),ste[i].getMethodName());
					}
				}
			} catch (Exception e) {
				log.debug("{}",e.getMessage());
			} finally {
				log.debug("--------------");
			}
		}
	}
//	private static boolean classOfInterest(StackTraceElement ste) {
//		String packageName = ste.getClass().getPackage().getName();
//		if (packageName.startsWith("nz.co")) {
//			// the class is one of ours
//			if (ste.getFileName().equals("Order.java")) {
//				return false; // not interested in Order
//			}
//			return true;
//		}
//		return false;
//	}
//	private static void dumpStack(StackTraceElement[] ste, int element) {
//		for (int i=element; i<ste.length;i++) {
//			String s = ste.getClass().getPackage().getName();
//			if (s.startsWith("com.vaadin")) {
//				break;
//			}
//			if (s.startsWith("nz.co")) {
//				log.debug("{}:{}",ste[i].getFileName(),ste[i].getLineNumber());
//			}
//		}
//	}
}
