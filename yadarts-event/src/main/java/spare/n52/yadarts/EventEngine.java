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
package spare.n52.yadarts;

import java.io.IOException;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spare.n52.yadarts.event.EventListener;
import spare.n52.yadarts.event.EventProducer;

/**
 * The core engine of the framework.
 * It manages the available implementations of {@link EventListener}
 * and starts/stops the {@link EventProducer} instance
 */
public class EventEngine {
	
	private static final Logger logger = LoggerFactory.getLogger(EventEngine.class);
	
	private static EventEngine instance;
	private EventProducer producer;

	private EventEngine() throws InitializationException {
		this.producer = initializeProducer();
		initializeListeners();
		try {
			this.producer.start();
		} catch (IOException e) {
			throw new InitializationException(e);
		}
	}
	
	public static synchronized EventEngine instance() throws InitializationException {
		if (instance == null) {
			instance = new EventEngine();
		}
		return instance;
	}
	
	/**
	 * free all initialized resources
	 */
	public void shutdown() {
		if (this.producer != null) {
			try {
				this.producer.stop();
			} catch (IOException e) {
				logger.warn(e.getMessage(), e);
			}
		}
	}

	private void initializeListeners() {
		ServiceLoader<EventListener> listeners = ServiceLoader.load(EventListener.class);
		
		for (EventListener l : listeners) {
			this.producer.registerEventListener(l);
		}
	}

	private EventProducer initializeProducer() throws InitializationException {
		ServiceLoader<EventProducer> producers = ServiceLoader.load(EventProducer.class);
		
		for (EventProducer eventProducer : producers) {
			/*
			 * use the first to find
			 */
			return eventProducer;
		}

		throw new InitializationException("Could not find an implementation of EventProducer");
	}

}
