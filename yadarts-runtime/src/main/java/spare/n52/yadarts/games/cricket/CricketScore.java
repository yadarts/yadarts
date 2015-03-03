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
package spare.n52.yadarts.games.cricket;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import spare.n52.yadarts.entity.Player;
import spare.n52.yadarts.entity.PointEvent;
import spare.n52.yadarts.entity.impl.HitEvent;
import spare.n52.yadarts.games.Score;

public class CricketScore implements Score {

	private Player player;
	private int turnThrowCount = 0;
	
	private Map<Integer, Integer> numberHits = new HashMap<>();
	private int totalDarts;
	private PointEvent lastHit;

	public CricketScore(Player player) {
		this.player = player;
		
		numberHits.put(15, 0);
		numberHits.put(16, 0);
		numberHits.put(17, 0);
		numberHits.put(18, 0);
		numberHits.put(19, 0);
		numberHits.put(20, 0);
		numberHits.put(25, 0);
	}

	@Override
	public int getTotalScore() {
		int result = 0;
		
		for (Integer n : this.numberHits.keySet()) {
			Integer value = this.numberHits.get(n);
			if (value > 3) {
				result += (value - 3) * n;
			}
		}
		
		return result;
	}

	@Override
	public int getThrownDarts() {
		return this.totalDarts;
	}

	@Override
	public Date getDateTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getTotalTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Player getPlayer() {
		return this.player;
	}

	@Override
	public boolean turnHasEvents() {
		return this.turnThrowCount > 0;
	}

	@Override
	public void terminateLastTurn() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean lastTurnTerminatedCorrect() {
		return true;
	}

	public void onPointEvent(PointEvent event) {
		if (this.hasThrowsLeft()) {
			this.lastHit = event;
			this.turnThrowCount++;
			this.totalDarts++;
			
			if (event.getBaseNumber() >= 15) {
				Integer target = numberHits.get(event.getBaseNumber());
				numberHits.put(event.getBaseNumber(), target + event.getMultiplier());
			}
		}
	}
	
	public boolean playerHasOpened(int number) {
		return numberHits.get(number) >= 2;
	}

	public boolean hasThrowsLeft() {
		return this.turnThrowCount < 3;
	}

	public void removeLastHit() {
		if (this.lastHit == null) {
			return;
		}
		
		if (this.lastHit.getBaseNumber() >= 15) {
			Integer target = numberHits.get(this.lastHit.getBaseNumber());
			numberHits.put(this.lastHit.getBaseNumber(), target - this.lastHit.getMultiplier());
			this.lastHit = null;
		}
	}

	public void finishCurrentTurn() {
		while (hasThrowsLeft()) {
			onPointEvent(HitEvent.singleHitInner(0));
		}
	}

}
