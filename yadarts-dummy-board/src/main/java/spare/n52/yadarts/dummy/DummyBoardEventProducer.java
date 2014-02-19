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
import spare.n52.yadarts.entity.UserCausedEvent;
import spare.n52.yadarts.entity.UserCausedEvent.Type;
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
		
		eventQueue.add(HitEvent.singleHitInner(15));
		eventQueue.add(HitEvent.singleHitInner(15));
		eventQueue.add(HitEvent.singleHitInner(15));
		eventQueue.add(ButtonEvent.nextPlayer());
		
		eventQueue.add(HitEvent.tripleHit(20));
		eventQueue.add(HitEvent.tripleHit(17));
		eventQueue.add(HitEvent.doubleHit(5));
		eventQueue.add(ButtonEvent.nextPlayer());
		
		eventQueue.add(HitEvent.singleHitInner(25));
		eventQueue.add(HitEvent.singleHitInner(25));
		eventQueue.add(HitEvent.singleHitInner(25));
		eventQueue.add(ButtonEvent.nextPlayer());
		
		eventQueue.add(HitEvent.singleHitOuter(19));
		eventQueue.add(HitEvent.singleHitOuter(19));
		eventQueue.add(HitEvent.singleHitOuter(19));
		eventQueue.add(ButtonEvent.nextPlayer());
		
		eventQueue.add(HitEvent.doubleHit(17));
		eventQueue.add(HitEvent.doubleHit(17));
		eventQueue.add(HitEvent.doubleHit(17));
		eventQueue.add(ButtonEvent.nextPlayer());
		
		eventQueue.add(HitEvent.singleHitInner(8));
		eventQueue.add(ButtonEvent.bounceOut());
		eventQueue.add(HitEvent.singleHitInner(15));
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
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						logger.warn(e.getMessage(), e);
					}
					
					for (InteractionEvent ie : eventQueue) {
						if (!running) {
							break;
						}
						
						sendEvent(ie);
						try {
							if (ie instanceof UserCausedEvent && ((UserCausedEvent) ie).getType() == Type.NEXT_PLAYER) {
								Thread.sleep(2000);
							}
							else {
								Thread.sleep(1000);
							}
						} catch (InterruptedException e) {
							logger.warn(e.getMessage(), e);
						}
					}
				}
			}
		});
		
		thread.start();
	}


	protected void sendEvent(InteractionEvent ie) {
		if (!running) {
			return;
		}
		
		for (EventListener el : this.listeners) {
			try {
				el.receiveEvent(ie);
			}
			catch (RuntimeException e) {
			}
			
			if (!running) {
				break;
			}
		}
	}


	@Override
	public void stop() throws IOException {
		this.running = false;
	}
	
}
