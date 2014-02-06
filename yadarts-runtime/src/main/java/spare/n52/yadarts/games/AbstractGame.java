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
package spare.n52.yadarts.games;

import spare.n52.yadarts.entity.InteractionEvent;
import spare.n52.yadarts.entity.PointEvent;
import spare.n52.yadarts.entity.UserCausedEvent;
import spare.n52.yadarts.event.EventListener;

/**
 * A basic implementation of a {@link Game}.
 * It encapsulates the {@link EventListener} logic
 * by defining abstract methods on specific events.
 */
public abstract class AbstractGame implements Game, EventListener {

	@Override
	public void receiveEvent(InteractionEvent event) {
		if (event instanceof UserCausedEvent) {
			processUserCausedEvent((UserCausedEvent) event);
		}
		else if (event instanceof PointEvent) {
			processPointEvent((PointEvent) event);
		}
	}

	protected void processUserCausedEvent(UserCausedEvent event) {
		switch (event.getType()) {
		case BOUNCE_OUT:
			onBounceOut();
			break;
		case DART_MISSED:
			onDartMissed();
			break;
		case NEXT_PLAYER:
			onNextPlayer();
			break;
		default:
			break;
		}
	}
	
	protected boolean isTripleHit(PointEvent e) {
		return e.getMultiplier() == 3;
	}
	
	protected boolean isDoubleHit(PointEvent e) {
		return e.getMultiplier() == 2;
	}
	
	protected boolean isBullseye(PointEvent e) {
		return e.getMultiplier() == 1 && e.getBaseNumber() == 25;
	}
	
	protected boolean isDoubleBullseye(PointEvent e) {
		return e.getMultiplier() == 2 && e.getBaseNumber() == 25;
	}

	/**
	 * called when a "next player" action was requested
	 */
	protected abstract void onNextPlayer();

	/**
	 * called when the "dart missed" action was triggered
	 */
	protected abstract void onDartMissed();

	/**
	 * called when the "bounce out" button was hit
	 */
	protected abstract void onBounceOut();
	
	/**
	 * called whenever a normal point hit was triggered
	 * 
	 * @param event a common point hit
	 */
	protected abstract void processPointEvent(PointEvent event);

}
