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

import java.util.HashMap;
import java.util.List;

import spare.n52.yadarts.entity.Player;
import spare.n52.yadarts.entity.PointEvent;
import spare.n52.yadarts.games.AbstractGame;
import spare.n52.yadarts.games.Score;

/**
 * Class represents the workflow of a generic X01 game.
 * Currently, double/triple-in/out is not supported.
 */
public class GenericX01Game extends AbstractGame implements X01Host {
	
	private HashMap<Player, Score> playerScoreMap = new HashMap<>();
	private int targetScore;
	private int currentPlayerIndex;
	private List<Player> players;
	private Player currentPlayer;
	private X01Score currentScore;
	private int rounds = 1;
	private boolean playerFinished;
	private boolean gameFinished;

	public GenericX01Game(List<Player> players, int targetScore) {
		this.targetScore = targetScore;
		this.players = players;
		
		for (Player player : players) {
			playerScoreMap.put(player, new X01Score(this));
		}
		
		this.currentPlayer = this.players.get(0);
		this.currentScore = (X01Score) this.playerScoreMap.get(this.currentPlayer);
		this.currentScore.startTurn();
		
		this.gameListener.onRoundStarted(this.rounds);
		provideStatusUpdate();
	}

	@Override
	protected void onNextPlayer() {
		if (this.gameFinished) {
			return;
		}
		
		this.currentPlayerIndex = (this.currentPlayerIndex + 1) % players.size();
		
		if (this.currentPlayerIndex == 0) {
			if (this.playerFinished) {
				this.gameListener.onGameFinished(this.playerScoreMap);
				this.gameFinished = true;
				return;
			}
			this.rounds++;
			this.gameListener.onRoundStarted(this.rounds);
			
		}
		
		this.currentPlayer = this.players.get(currentPlayerIndex);
		this.currentScore = (X01Score) this.playerScoreMap.get(currentPlayer);
		this.currentScore.startTurn();
		
		provideStatusUpdate();
	}

	private void provideStatusUpdate() {
		this.gameListener.onCurrentPlayerChanged(this.currentPlayer, this.currentScore);
		
		this.currentScore.checkFinishingPossibility();
	}

	@Override
	protected void onDartMissed() {
		if (this.gameFinished) {
			return;
		}
		
		if (!this.currentScore.turnHasRemainingThrows()) {
			this.gameListener.requestNextPlayerEvent();
			return;
		}
		
		this.currentScore.addScoreValue(0);
		this.gameListener.onDartMissedPressed();
	}

	@Override
	protected void onBounceOut() {
		if (this.gameFinished) {
			return;
		}
		
		if (!this.currentScore.turnHasRemainingThrows()) {
			this.gameListener.requestNextPlayerEvent();
			return;
		}
		
		this.currentScore.invalidateLastThrow();
		this.gameListener.onBounceOutPressed();
		this.gameListener.onRemainingScoreForPlayer(this.currentPlayer, this.currentScore);
	}

	@Override
	protected void processPointEvent(PointEvent event) {
		if (this.gameFinished) {
			return;
		}
		
		if (!this.currentScore.turnHasRemainingThrows()) {
			this.gameListener.requestNextPlayerEvent();
			return;
		}
		
		this.gameListener.onPointEvent(event);
		this.currentScore.addScoreValue(event.getScoreValue());
	}

	@Override
	public void firePlayerFinishedEvent() {
		this.playerFinished = true;
		this.gameListener.onPlayerFinished(this.currentPlayer);
	}
	
	@Override
	public void turnEnded() {
		this.gameListener.onTurnFinished(this.currentPlayer, this.currentScore);
	}
	
	@Override
	public void bust(X01Score score) {
		this.gameListener.onBust(this.currentPlayer, score);
	}
	
	@Override
	public void provideRemainingScore() {
		this.gameListener.onRemainingScoreForPlayer(this.currentPlayer, this.currentScore);
	}

	@Override
	public int getTargetScore() {
		return targetScore;
	}

	@Override
	public void provideFinishingCombination(
			List<List<PointEvent>> finishingCombinations) {
		this.gameListener.onFinishingCombination(finishingCombinations);
	}
	
}
