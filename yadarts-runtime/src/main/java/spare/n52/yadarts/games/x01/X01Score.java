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
package spare.n52.yadarts.games.x01;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import spare.n52.yadarts.entity.Player;
import spare.n52.yadarts.entity.PointEvent;
import spare.n52.yadarts.games.Score;

public class X01Score implements Score {

	private List<Turn> turns = new ArrayList<>();
	private Turn currentTurn;
	public CombinationCalculator combinationCalculator = new CombinationCalculator();
	private X01Host host;
	private Date time;
	private Player player;

	public X01Score(X01Host h, Player player) {
		this.host = h;
		this.player = player;
	}
	
	public int getRemainingScore() {
		return this.host.getTargetScore() - getAdditiveScore();
	}

	public boolean turnHasRemainingThrows() {
		return this.currentTurn.getThrowCount() < 3;
	}
	
	public void invalidateLastThrow() {
		this.currentTurn.invalidateLastThrow();
	}
	
	public boolean playerFinished() {
		return this.getRemainingScore() == 0;
	}
	
	void checkFinishingPossibility() {
		if (this.canFinish()) {
			this.host.provideFinishingCombination(this.calculateFinishingCombinations());
		}		
	}

	public void addScoreValue(int i) {
		if (this.currentTurn.isClosed() || playerFinished()) {
			host.requestNextPlayerEvent();
			return;
		}
		
		if (this.getRemainingScore() - i < 0) {
			this.currentTurn.busted();
			this.host.bust(this);
			return;
		}

		this.currentTurn.addThrow(i);
		
		if (playerFinished()) {
			this.host.firePlayerFinishedEvent();
		}
		
		if (this.currentTurn.hasRemainingThrows()) {
			checkFinishingPossibility();
		}
		else {
			this.host.turnEnded();
		}
		
		this.host.provideRemainingScore();
	}

	private int getAdditiveScore() {
		int result = 0;
		for (Turn t : turns) {
			result += t.getScore();
		}
		return result;
	}

	public void startTurn() {
		this.currentTurn = new Turn();
		this.turns.add(this.currentTurn);
	}

	public boolean canFinish() {
		int count = this.currentTurn.getRemainingThrows();
		return combinationCalculator.canFinishWith(count, this.host.getTargetScore() - getAdditiveScore());
	}
	
	public List<List<PointEvent>> calculateFinishingCombinations() {
		int count = this.currentTurn.getRemainingThrows();
		return combinationCalculator.calculateFinishingCombinations(count, this.host.getTargetScore() - getAdditiveScore());
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Total Score: ");
		sb.append(this.getRemainingScore());
		sb.append("; Total Darts: ");
		sb.append(this.getTotalDarts());
		return sb.toString();
	}

	private int getTotalDarts() {
		int result = 0;
		
		for (Turn t : turns) {
			result += t.getThrowCount();
		}
		
		return result;
	}
	
	@Override
	public int getTotalScore() {
		return getRemainingScore();
	}

	@Override
	public int getThrownDarts() {
		return getTotalDarts();
	}
	
	private class Turn {

		List<Integer> throwz = new ArrayList<>();
		private boolean busted;
		
		public void addThrow(int i) {
			throwz.add(i);
		}
		
		public void busted() {
			int targetSize = Math.min(throwz.size() + 1, 3);
			throwz = new ArrayList<>();
			for (int i = 0; i < targetSize; i++) {
				throwz.add(0);
			}
			busted = true;
		}

		public boolean hasRemainingThrows() {
			return throwz.size() < 3;
		}
		
		public int getRemainingThrows() {
			return 3 - throwz.size();
		}

		public void invalidateLastThrow() {
			if (throwz.size() > 0) {
				throwz.remove(throwz.size()-1);
				throwz.add(0);
			}
		}

		public int getScore() {
			int result = 0;
			
			for (Integer i : throwz) {
				result += i;
			}
			
			return result;
		}
		
		public int getThrowCount() {
			return this.throwz.size();
		}
		
		public boolean isClosed() {
			return this.busted || this.throwz.size() == 3;
		}
		
	}

	@Override
	public Date getDateTime() {
		return this.time;
	}
	
	
	public void setTime(Date time) {
		this.time = time;
	}

	@Override
	public int getTotalTime() {
		return 0;
	}

	@Override
	public Player getPlayer() {
		return this.player;
	}

}
