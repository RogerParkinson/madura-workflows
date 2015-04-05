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

import nz.co.senanque.workflow.ContextDAO;

/**
 * This holds the master list of all processes (including subprocesses), as well as the list of main processes.
 * It also holds various administration methods and counters to manage the process definitions.
 * It is an abstract class because it is extended by concrete classes that implement the persisitence mechanism.
 * 
 * @author Roger Parkinson
 *
 */
public class ContextMock implements ContextDAO {

	@Override
	public Object getContext(String contextDescriptor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createContextDescriptor(Object o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object mergeContext(Object context) {
		// TODO Auto-generated method stub
		return null;
	}

	public void persistContext(Object context) {
	}
}
