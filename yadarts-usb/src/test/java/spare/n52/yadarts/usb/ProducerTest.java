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

import org.junit.Assert;
import org.junit.Test;

import spare.n52.yadarts.entity.InteractionEvent;
import spare.n52.yadarts.entity.impl.ButtonEvent;
import spare.n52.yadarts.entity.impl.HitEvent;
import spare.n52.yadarts.event.EventListener;

public class ProducerTest {

	@Test
	public void testProducerWorkflow() throws InterruptedException {
		USBEventProducer prod = new USBEventProducer();
		
		DummyListener dl = new DummyListener();
		prod.registerEventListener(dl);
		
		prod.processEvent(new int[] {
				Integer.parseInt("02", 16), Integer.parseInt("04", 16),
				Integer.parseInt("00", 16), Integer.parseInt("00", 16),
				Integer.parseInt("00", 16), Integer.parseInt("00", 16),
				Integer.parseInt("00", 16)
				});
		prod.processEvent(new int[] {
				Integer.parseInt("02", 16), Integer.parseInt("03", 16),
				Integer.parseInt("00", 16), Integer.parseInt("00", 16),
				Integer.parseInt("00", 16), Integer.parseInt("00", 16),
				Integer.parseInt("00", 16)
				});
		prod.processEvent(new int[] {
				Integer.parseInt("02", 16), Integer.parseInt("01", 16),
				Integer.parseInt("00", 16), Integer.parseInt("00", 16),
				Integer.parseInt("00", 16), Integer.parseInt("00", 16),
				Integer.parseInt("00", 16)
				});

		Thread.sleep(2000);
		
		prod.removeEventListener(dl);
		
		prod.processEvent(new int[] {
				Integer.parseInt("02", 16), Integer.parseInt("8a", 16),
				Integer.parseInt("00", 16), Integer.parseInt("00", 16),
				Integer.parseInt("00", 16), Integer.parseInt("00", 16),
				Integer.parseInt("00", 16)
				});
		
		Thread.sleep(2000);
		
		Assert.assertFalse(dl.hitEvent);
		Assert.assertTrue(dl.dartMissed);
		Assert.assertTrue(dl.bounceOut);
		Assert.assertTrue(dl.nextPlayer);
	}
	
	private static final class DummyListener implements EventListener {

		private boolean hitEvent;
		private boolean dartMissed;
		private boolean bounceOut;
		private boolean nextPlayer;

		@Override
		public void receiveEvent(InteractionEvent event) {
			if (event instanceof ButtonEvent) {
				switch (((ButtonEvent) event).getType()) {
				case DART_MISSED:
					this.dartMissed = true;
					break;
				case BOUNCE_OUT:
					this.bounceOut = true;
					break;
				case NEXT_PLAYER:
					this.nextPlayer = true;
					break;
				default:
					break;
				}
			}
			else if (event instanceof HitEvent) {
				this.hitEvent = true;
			}
		}
		
	}
	
}
