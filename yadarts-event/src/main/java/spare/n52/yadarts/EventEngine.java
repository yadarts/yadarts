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
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spare.n52.yadarts.entity.InteractionEvent;
import spare.n52.yadarts.event.EventListener;
import spare.n52.yadarts.event.EventProducer;

/**
 * The core engine of the framework.
 * It manages the available implementations of {@link EventListener}
 * and starts/stops the {@link EventProducer} instance
 */
public class EventEngine implements EventListener {
	
	private static final Logger logger = LoggerFactory.getLogger(EventEngine.class);
	
	private static EventEngine instance;

	private static List<EventListener> serviceLoadedlisteners = new ArrayList<>();
	private EventProducer producer;

	private List<EventListener> listeners = new ArrayList<>();

	private boolean running;
	
	static {
		ServiceLoader<EventListener> listeners = ServiceLoader.load(EventListener.class);
		
		for (EventListener l : listeners) {
			serviceLoadedlisteners.add(l);
		}
	}

	private EventEngine() throws InitializationException {
		this.listeners.addAll(serviceLoadedlisteners);
	}
	
	public static synchronized EventEngine instance() throws InitializationException {
		if (instance == null) {
			instance = new EventEngine();
		}
		return instance;
	}
	
	/**
	 * start the {@link EventProducer} (= the USB connection in 
	 * production mode).
	 * Everything can be stopped again using {@link #shutdown()}
	 * 
	 * @throws InitializationException
	 * @throws AlreadyRunningException
	 */
	public synchronized void start() throws InitializationException, AlreadyRunningException {
		if (this.running) {
			throw new AlreadyRunningException("EventEngine already running!");
		}
		
		this.producer = initializeProducer();
		this.producer.registerEventListener(this);
		
		try {
			this.producer.start();
		} catch (IOException e) {
			throw new InitializationException(e);
		}
		
		this.running = true;
	}
	
	/**
	 * free all initialized resources. call if you would like
	 * to stop the {@link EventProducer} instance from generacting events
	 */
	public synchronized void shutdown() {
		this.running = false;
		
		if (this.producer != null) {
			try {
				this.producer.removeEventListener(this);
				this.producer.stop();
			} catch (IOException e) {
				logger.warn(e.getMessage(), e);
			}
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
	
	/**
	 * Use this method to manually add an {@link EventListener} instance.
	 * 
	 * @param el the listener
	 */
	public synchronized void registerListener(EventListener el) {
		this.listeners.add(el);
	}

	/**
	 * Remove a listener, added with {@link #registerListener(EventListener)}
	 * 
	 * @param el the listener to remove
	 */
	public synchronized void removeListener(EventListener el) {
		this.listeners.remove(el);
	}
	
	@Override
	public void receiveEvent(InteractionEvent event) {
		synchronized (this) {
			for (EventListener el : this.listeners) {
				try {
					el.receiveEvent(event);
				}
				catch (RuntimeException e) {
					logger.warn(e.getMessage(), e);
				}
			}
		}
	}

}
