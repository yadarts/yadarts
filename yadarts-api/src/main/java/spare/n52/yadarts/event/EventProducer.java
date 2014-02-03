/**
 * Copyright 2014 the staff of 52°North Initiative for Geospatial Open
 * Source Software GmbH in their free time
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spare.n52.yadarts.event;

import java.io.IOException;

import spare.n52.yadarts.InitializationException;
import spare.n52.yadarts.entity.InteractionEvent;

/**
 * Interface for a component that produces events
 * in terms of {@link InteractionEvent} instances.
 */
public interface EventProducer {

	/**
	 * An implementation shall store the provided instance
	 * internally and provide it with updates through
	 * the {@link EventListener#receiveEvent(InteractionEvent)}
	 * method.
	 * 
	 * @param el the new listeners
	 */
	public void registerEventListener(EventListener el);

	/**
	 * An implemenation shall remove the provided instance
	 * from its internal memory and no longer provide updates
	 * to it.
	 * 
	 * @param el the listener to remove
	 */
	public void removeEventListener(EventListener el);

	/**
	 * An implementation shall start creating events
	 * after calling this method
	 * @throws IOException 
	 * @throws InitializationException 
	 */
	public void start() throws IOException;
	
	/**
	 * An implementation shall stop creating events
	 * after calling this method
	 * @throws IOException 
	 */
	public void stop() throws IOException;
	
}
