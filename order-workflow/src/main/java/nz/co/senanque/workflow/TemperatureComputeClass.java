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

import java.util.Map;

import nz.co.senanque.process.instances.ComputeType;
import nz.co.senanque.workflow.instances.ProcessInstance;
import nz.co.senanque.workflow.orderinstances.Order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple demo of a compute task that converts fahrenheit to celsius.
 * @author Roger Parkinson
 *
 */
public class TemperatureComputeClass implements ComputeType<Order> {

	private static final Logger log = LoggerFactory
			.getLogger(TemperatureComputeClass.class);
	
	public TemperatureComputeClass() {
	}
	public void execute(ProcessInstance processInstance, Order context, Map<String, String> map) {
		float fahrenheit = context.getFahrenheit();
		float celsius = (fahrenheit - 32F)*5F/9F;
		celsius = Math.round(celsius * 100F)/100F;
		context.setCelsius(celsius);
		log.debug("Computed temperature {}",celsius);
	}

}
