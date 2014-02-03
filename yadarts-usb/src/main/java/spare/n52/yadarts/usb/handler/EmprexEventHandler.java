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
package spare.n52.yadarts.usb.handler;


import spare.n52.yadarts.entity.InteractionEvent;
import spare.n52.yadarts.entity.impl.ButtonEvent;
import spare.n52.yadarts.entity.impl.HitEvent;

public class EmprexEventHandler implements EventHandler {

	@Override
	public InteractionEvent createEvent(int[] rawData) {
		return parsePacket(rawData[0], rawData[1]);
	}

	private InteractionEvent parsePacket(int base, int value) {
		if (value >= 129) {
			/*
			 * outer ring
			 */
			return HitEvent.singleHitOuter(value - 128);
		}
		
		if (value >= 97) {
			return HitEvent.tripleHit(value - 96);
		}
		
		if (value >= 65) {
			return HitEvent.doubleHit(value - 64);
		}
		
		if (value == 57) {
			return HitEvent.singleHitInner(25);
		}
		
		if (value >= 33) {
			/*
			 * inner ring
			 */
			return HitEvent.singleHitInner(value - 32);
		}
		
		if (value == 4) {
			return ButtonEvent.bounceOut();
		}
		
		if (value == 3) {
			return ButtonEvent.dartMissed();
		}
		
		if (value == 1) {
			return ButtonEvent.nextPlayer();
		}
		
		if (value == 0) {
			return new ConfirmationEvent();
		}
		
		return null;
	}

}
