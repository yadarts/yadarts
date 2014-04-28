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
package spare.n52.yadarts.usb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spare.n52.yadarts.entity.InteractionEvent;
import spare.n52.yadarts.event.EventListener;
import spare.n52.yadarts.event.EventProducer;
import spare.n52.yadarts.usb.handler.ConfirmationEvent;
import spare.n52.yadarts.usb.handler.EmprexEventHandler;
import spare.n52.yadarts.usb.handler.EventHandler;

public class USBEventProducer implements EventProducer, EmprexRawDataListener {

	private static final Logger logger = LoggerFactory.getLogger(USBEventProducer.class);

	protected static final long MIN_TIME_DELTA = 250;
	
	private List<EventListener> listeners = new ArrayList<>();
	private SimplifiedEmprexUSBDriver driver;
	private EventHandler handler;
	private ExecutorService executor = Executors.newSingleThreadExecutor();

	protected InteractionEvent pending;
	private InteractionEvent previous;
	private long previousEventTime;
	
	public USBEventProducer() {
		initHandlers();
	}

	private void initHandlers() {
		this.handler = new EmprexEventHandler();
	}

	private void initConnection() throws IOException {
		this.driver = new SimplifiedEmprexUSBDriver(this);
		if (this.driver.isReady()) {
			this.driver.start();
		}
		else {
			throw new IOException("Could not init the dongle driver.");
		}
	}

	@Override
	public void registerEventListener(EventListener el) {
		this.listeners.add(el);
	}

	@Override
	public void removeEventListener(EventListener el) {
		this.listeners.remove(el);
	}
	
	@Override
	public void receiveData(byte[] dataBuffer, final int byteCount) {
		final int[] rawData = convertToIntArray(dataBuffer, byteCount);
		executor.submit(new Runnable() {
			
			@Override
			public void run() {
				InteractionEvent event = USBEventProducer.this.handler.createEvent(rawData);
				
				if (event == null) {
					return;
				}
				
				synchronized (USBEventProducer.this) {
					InteractionEvent outgoing;
					/*
					 * we receive the actual event and then a "release"
					 * event in order. fire the event after the
					 * release was received.
					 */
					
					if (event instanceof ConfirmationEvent) {
						if (pending != null) {
							outgoing = pending;
							pending = null;
							previous = outgoing;
							previousEventTime = outgoing.getTimestamp();	
						}
						else {
							return;
						}
					} 
					else {
						logger.info("new hit event: "+event+"; prev: "+previous+"; delta: "+(event.getTimestamp() - previousEventTime));
						if (event.hasSameContent(previous) 
								&& (event.getTimestamp() - previousEventTime) < MIN_TIME_DELTA) {
							/*
							 * probably a ghost event
							 */
							return;
						}
						
						pending = event;
						return;
					}
					
					for (EventListener el : USBEventProducer.this.listeners) {
						try {
							el.receiveEvent(outgoing);
						}
						catch (RuntimeException e) {
							logger.warn(e.getMessage(), e);
						}
					}
				}
			}

		});
		
	}

	protected int[] convertToIntArray(byte[] dataBuffer, int byteCount) {
		int[] result = new int[Math.min(byteCount, dataBuffer.length)];
		
		for (int i = 0; i < result.length; i++) {
			result[i] = dataBuffer[i] & 0xFF;
		}

		return result;
	}

	@Override
	public void start() throws IOException {
		initConnection();
	}

	@Override
	public void stop() throws IOException {
		this.driver.shutdown();
	}
	
}
