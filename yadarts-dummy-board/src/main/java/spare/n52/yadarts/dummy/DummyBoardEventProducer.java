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
package spare.n52.yadarts.dummy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spare.n52.yadarts.entity.InteractionEvent;
import spare.n52.yadarts.entity.impl.ButtonEvent;
import spare.n52.yadarts.entity.impl.HitEvent;
import spare.n52.yadarts.event.EventListener;
import spare.n52.yadarts.event.EventProducer;

/**
 * Simple dummy producer throwing a fictitious 2 player game.
 * Player 1 finishes 301 in a perfect round.
 * 
 * The game repeats forever.
 */
public class DummyBoardEventProducer implements EventProducer {

	private static final Logger logger = LoggerFactory.getLogger(DummyBoardEventProducer.class);
	
	private List<EventListener> listeners = new ArrayList<>();

	private boolean running = true;

	protected static List<InteractionEvent> eventQueue;

	static {
		eventQueue = new ArrayList<>();
		eventQueue.add(HitEvent.tripleHit(20));
		eventQueue.add(HitEvent.tripleHit(20));
		eventQueue.add(HitEvent.tripleHit(20));
		eventQueue.add(ButtonEvent.nextPlayer());
		eventQueue.add(HitEvent.singleHitInner(1));
		eventQueue.add(HitEvent.singleHitInner(20));
		eventQueue.add(HitEvent.singleHitInner(5));
		eventQueue.add(ButtonEvent.nextPlayer());
		eventQueue.add(HitEvent.tripleHit(20));
		eventQueue.add(HitEvent.tripleHit(17));
		eventQueue.add(HitEvent.doubleHit(5));
		eventQueue.add(ButtonEvent.nextPlayer());
		eventQueue.add(HitEvent.singleHitInner(1));
		eventQueue.add(ButtonEvent.bounceOut());
		eventQueue.add(ButtonEvent.dartMissed());
		eventQueue.add(ButtonEvent.nextPlayer());
	}
	
	public DummyBoardEventProducer() {
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
	public void start() throws IOException {
		startThread();
	}

	private void startThread() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (running) {
					for (InteractionEvent ie : eventQueue) {
						sendEvent(ie);
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							logger.warn(e.getMessage(), e);
						}
					}
					
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						logger.warn(e.getMessage(), e);
					}
				}
			}
		});
		
		thread.start();
	}


	protected void sendEvent(InteractionEvent ie) {
		for (EventListener el : this.listeners) {
			try {
				el.receiveEvent(ie);
			}
			catch (RuntimeException e) {
			}
		}
	}


	@Override
	public void stop() throws IOException {
		this.running = false;
	}
	
}
