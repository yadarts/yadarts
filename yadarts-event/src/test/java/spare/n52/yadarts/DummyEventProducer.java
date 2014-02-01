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

import java.util.ArrayList;
import java.util.List;

import spare.n52.yadarts.entity.InteractionEvent;
import spare.n52.yadarts.entity.impl.ButtonEvent;
import spare.n52.yadarts.entity.impl.HitEvent;
import spare.n52.yadarts.event.EventListener;
import spare.n52.yadarts.event.EventProducer;

public class DummyEventProducer implements EventProducer {


	private static List<InteractionEvent> queue;
	private List<EventListener> listeners = new ArrayList<>();
	protected boolean running = true;

	@Override
	public void registerEventListener(EventListener el) {
		this.listeners.add(el);
	}

	@Override
	public void removeEventListener(EventListener el) {
		this.listeners.remove(el);
	}
	
	public static synchronized List<InteractionEvent> cyclicQueue() {
		if (queue == null) {
			queue = new ArrayList<>();
			
			queue.add(HitEvent.doubleHit(20));
			queue.add(HitEvent.singleHit(20));
			queue.add(HitEvent.tripleHit(20));
			queue.add(ButtonEvent.nextPlayer());
			
			queue.add(HitEvent.singleHit(1));
			queue.add(ButtonEvent.bounceOut());
			queue.add(ButtonEvent.dartMissed());
			queue.add(ButtonEvent.nextPlayer());
		}
		return queue;
	}

	@Override
	public void start() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (running) {
					List<InteractionEvent> q = cyclicQueue();
					for (InteractionEvent interactionEvent : q) {
						fireEvent(interactionEvent);
						try {
							Thread.sleep(25);
						} catch (InterruptedException e) {
						}
					}
				}
			}
		}).start();
	}

	protected void fireEvent(InteractionEvent interactionEvent) {
		for (EventListener el : this.listeners) {
			try {
				el.receiveEvent(interactionEvent);
			}
			catch (RuntimeException e) {
			}
		}
	}

	@Override
	public void stop() {
		running = false;
	}

}
