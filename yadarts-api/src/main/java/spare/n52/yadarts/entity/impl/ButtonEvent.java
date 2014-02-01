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
package spare.n52.yadarts.entity.impl;

import spare.n52.yadarts.entity.UserCausedEvent;

public class ButtonEvent implements UserCausedEvent {

	private Type type;
	private long time;

	private ButtonEvent(Type t) {
		this.type = t;
		this.time = System.currentTimeMillis();
	}
	
	@Override
	public long getTimestamp() {
		return this.time;
	}
	
	@Override
	public Type getType() {
		return this.type;
	}
	
	@Override
	public String toString() {
		return String.format("%s (%d)", this.type.toString(), this.time);
	}
	
	public static UserCausedEvent dartMissed() {
		return new ButtonEvent(Type.DART_MISSED);
	}
	
	public static UserCausedEvent bounceOut() {
		return new ButtonEvent(Type.BOUNCE_OUT);
	}
	
	public static UserCausedEvent nextPlayer() {
		return new ButtonEvent(Type.NEXT_PLAYER);
	}

}
