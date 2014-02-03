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
import java.util.HashMap;
import java.util.List;

import spare.n52.yadarts.entity.Player;
import spare.n52.yadarts.entity.PointEvent;
import spare.n52.yadarts.games.AbstractGame;
import spare.n52.yadarts.games.GameStatusUpdateListener;

public class GenericX01Game extends AbstractGame {
	
	private HashMap<Player, Score> playerScoreMap = new HashMap<>();
	private int targetScore;
	private int currentPlayerIndex;
	private List<Player> players;
	private Player currentPlayer;
	private Score currentScore;
	private GameStatusUpdateListener gameListener;
	private int rounds = 1;
	public CombinationCalculator combinationCalculator;

	public GenericX01Game(List<Player> players, int targetScore, GameStatusUpdateListener gl) {
		this.targetScore = targetScore;
		this.players = players;
		this.gameListener = gl;
		
		for (Player player : players) {
			playerScoreMap.put(player, new Score());
		}
	}

	@Override
	protected void onNextPlayer() {
		this.currentPlayerIndex = (this.currentPlayerIndex + 1) % players.size();
		
		if (this.currentPlayerIndex == 0) {
			this.rounds++;
		}
		
		this.currentPlayer = this.players.get(currentPlayerIndex);
		this.currentScore = this.playerScoreMap.get(currentPlayer);
		this.currentScore.startTurn();
		
		provideStatusUpdate();
	}

	private void provideStatusUpdate() {
		this.gameListener.onCurrentPlayerChanged(this.currentPlayer);
		
		this.gameListener.roundStarted(this.rounds);
		
		if (this.currentScore.canFinish()) {
			this.gameListener.provideFinishingCombination(this.currentScore.calculateFinishingCombinations());
		}
	}

	@Override
	protected void onDartMissed() {
		if (!this.currentScore.turnHasRemainingThrows()) {
			return;
		}
		
		this.currentScore.addScoreValue(0);
	}

	@Override
	protected void onBounceOut() {
		if (!this.currentScore.turnHasRemainingThrows()) {
			return;
		}
		
		this.currentScore.invalidateLastThrow();
	}

	@Override
	protected void processPointEvent(PointEvent event) {
		if (!this.currentScore.turnHasRemainingThrows()) {
			return;
		}
		
		this.currentScore.addScoreValue(event.getScoreValue());
	}

	public void bust(Score score) {
		this.gameListener.onBust(this.currentPlayer);
	}
	
	private class Score {

		private List<Turn> turns;
		private Turn currentTurn;

		public boolean turnHasRemainingThrows() {
			return this.currentTurn.getThrowCount() < 3;
		}
		
		public void invalidateLastThrow() {
			this.currentTurn.invalidateLastThrow();
		}

		public void addScoreValue(int i) {
			this.currentTurn.addThrow(i);
			
			if (this.getTotalScore() + i > targetScore) {
				bust(this);
				return;
			}
			
			if (this.currentTurn.hasRemainingThrows()) {
				
			}
		}

		private int getTotalScore() {
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
			return combinationCalculator.canFinishWith(count, targetScore - getTotalScore());
		}
		
		public List<List<PointEvent>> calculateFinishingCombinations() {
			int count = this.currentTurn.getRemainingThrows();
			return combinationCalculator.calculateFinishingCombinations(count, targetScore - getTotalScore());
		}
		
	}
	
	private class Turn {

		List<Integer> throwz = new ArrayList<>();
		
		public void addThrow(int i) {
			throwz.add(i);
		}
		
		public boolean hasRemainingThrows() {
			return throwz.size() < 3;
		}
		
		public int getRemainingThrows() {
			return 3 - throwz.size();
		}

		public void invalidateLastThrow() {
			throwz.remove(throwz.size()-1);
			throwz.add(0);
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
		
	}


}
