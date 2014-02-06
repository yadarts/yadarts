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

import spare.n52.yadarts.entity.PointEvent;

public class HitEvent implements PointEvent {

	private long time;
	private int base;
	private int multi;
	private boolean outerRing;

	private HitEvent(int base) {
		this(base, true);
	}
	
	private HitEvent(int base, int multi) {
		this(base, multi, false);
	}
	
	private HitEvent(int base, boolean outer) {
		this(base, 1, outer);
	}
	
	private HitEvent(int base, int multi, boolean outer) {
		this.time = System.currentTimeMillis();
		this.base = base;
		this.multi = multi;
		this.outerRing = outer;
	}
	
	@Override
	public long getTimestamp() {
		return this.time;
	}

	@Override
	public int getBaseNumber() {
		return this.base;
	}

	@Override
	public int getMultiplier() {
		return this.multi;
	}
	
	@Override
	public boolean isOuterRing() {
		return outerRing;
	}
	
	@Override
	public int getScoreValue() {
		return this.base * this.multi;
	}

	@Override
	public String toString() {
		switch (this.multi) {
		case 1:
			return String.format("%d - %s (%s)", this.base, this.outerRing ? "outer" : "inner", this.time);
		case 2:
			return String.format("Double %d (%s)", this.base, this.time);
		case 3:
			return String.format("Triple %d (%s)", this.base, this.time);
		default:
			return String.format("%d - %s (%s)", this.base, this.outerRing ? "outer" : "inner", this.time);
		}
	}
	
	public static PointEvent singleHitOuter(int number) {
		return new HitEvent(number);
	}
	
	public static PointEvent singleHitInner(int number) {
		return new HitEvent(number, false);
	}
	
	public static PointEvent doubleHit(int number) {
		return new HitEvent(number, 2);
	}
	
	public static PointEvent tripleHit(int number) {
		return new HitEvent(number, 3);
	}
	
}
