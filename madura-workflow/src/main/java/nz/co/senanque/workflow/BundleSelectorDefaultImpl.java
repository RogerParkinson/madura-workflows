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

import nz.co.senanque.workflow.instances.ProcessInstance;

/**
 * This is a dummy bundle selector that actually does nothing at all.
 * A real implementation would use MaduraBundle to select a current bundle but we want to avoid
 * making Workflow dependent on MaduraBundle in case another mechanism is preferred.
 * So applications get to pick their own mechanism and override this dummy class.
 * 
 * @author Roger Parkinson
 *
 */
public class BundleSelectorDefaultImpl implements BundleSelector {

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.BundleSelector#selectBundle(nz.co.senanque.workflow.instances.ProcessInstance)
	 */
	@Override
	public void selectBundle(ProcessInstance pi) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see nz.co.senanque.workflow.BundleSelector#selectInitialBundle(nz.co.senanque.workflow.instances.ProcessInstance, java.lang.Object)
	 */
	@Override
	public String  selectInitialBundle(Object o) {
		// TODO Auto-generated method stub
		return null;

	}

}
