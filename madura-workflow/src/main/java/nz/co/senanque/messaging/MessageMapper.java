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
package nz.co.senanque.messaging;

import org.springframework.messaging.Message;

/**
 * Implementations of this interface should unpack the message into the context object.
 * There is an assumption that the message type and the context type will be the ones expected.
 * If the incoming message holds an error the mapper ought to throw an exception which will
 * be caught and logged by the caller.
 * 
 * @author Roger Parkinson
 *
 */
public interface MessageMapper {

	void unpackMessage(Message<?> message, Object context);

}
