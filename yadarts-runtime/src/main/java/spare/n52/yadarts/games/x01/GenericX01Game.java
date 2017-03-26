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
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spare.n52.yadarts.entity.Player;
import spare.n52.yadarts.entity.PointEvent;
import spare.n52.yadarts.games.AbstractGame;
import spare.n52.yadarts.games.AnnotatedGame;
import spare.n52.yadarts.games.Score;

/**
 * Class represents the workflow of a generic X01 game.
 * Currently, double/triple-in/out is not supported.
 */
@AnnotatedGame(highscorePersistentName="X01Game", displayName="Generic X01")
public class GenericX01Game extends AbstractGame implements X01Host {
	
	private static final Logger logger = LoggerFactory.getLogger(GenericX01Game.class);
	
	private HashMap<Player, Score> playerScoreMap = new HashMap<>();
	private int targetScore;
	private int currentPlayerIndex;
	private List<Player> players;
	private Player currentPlayer;
	private X01Score currentScore;
	private int rounds = 1;
	private boolean playerFinished;
	private boolean gameFinished;
	
	public static GenericX01Game create(List<Player> players, int targetScore) {
		switch (targetScore) {
		case 301:
			return new Three01Game(players);
		case 501:
			return new Five01Game(players);
		case 701:
			return new Seven01Game(players);			
		default:
			return new GenericX01Game(players, targetScore);
		}
	}

	protected GenericX01Game(List<Player> players, int targetScore) {
		super.setPlayers(players);
		
		this.targetScore = targetScore;
		this.players = players;
		
		for (Player player : players) {
			playerScoreMap.put(player, new X01Score(this, player));
			super.setScore(player, playerScoreMap.get(player));
		}
		
		this.currentPlayer = this.players.get(0);
		this.currentScore = (X01Score) this.playerScoreMap.get(this.currentPlayer);
		this.currentScore.startTurn();
		
		this.gameListener.onRoundStarted(this.rounds);
                
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                        }
                        provideStatusUpdate();
                    }
                }).start();
	}

	@Override
	protected void onNextPlayer() {
		if (this.gameFinished) {
			return;
		}

		this.currentScore.endTurn();
		
		this.currentPlayerIndex = (this.currentPlayerIndex + 1) % players.size();
		
		if (this.currentPlayerIndex == 0) {
			if (this.playerFinished) {
				this.gameListener.onGameFinished(this.playerScoreMap, getWinners());
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
		this.gameListener.onNextPlayerPressed();
	}

	public List<Player> getWinners() {
		List<Player> winner = new ArrayList<>();
		
		int minDarts = Integer.MAX_VALUE;
		for (Player p : playerScoreMap.keySet()) {
			Score score = playerScoreMap.get(p);
			if (score.getTotalScore() == 0) {
				if (score.getThrownDarts() <= minDarts) {
					winner.add(p);
				}
			}
		}
		
		return winner;
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

                this.gameListener.onDartMissedPressed();
		this.currentScore.addScoreValue(0);
		
		terminatePreviousTurn();
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
		
		terminatePreviousTurn();
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

                this.currentScore.addScoreValue(event.getScoreValue());
		this.gameListener.onPointEvent(event, currentScore.getLastTurn());
		
		terminatePreviousTurn();
	}

	private void terminatePreviousTurn() {
		int prev = determinePreviousPlayer();
		Player previous = this.players.get(prev);
		Score score = this.playerScoreMap.get(previous);
		if (score.getThrownDarts() > 0) {
			score.terminateLastTurn();
		}
	}

	@Override
	public void firePlayerFinishedEvent() {
		this.playerFinished = true;
		this.currentScore.setTime(new Date());
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

	@Override
	public void requestNextPlayerEvent() {
		this.gameListener.requestNextPlayerEvent();
	}

	@Override
	public String getShortName() {
		return Integer.toString(targetScore).concat("-Game");
	}

	@Override
	public void undoEvent() {
		if (this.currentScore.turnHasEvents()) {
			
			if (!this.currentScore.turnIsTerminated()) {
				undoLastThrow();
			}
		}
		else {
			undoNextPlayer();
		}
	}

	private void undoNextPlayer() {
		if (this.currentScore.alreadyReceivedScoreEvents()) {
			logger.warn("Undo of Next Player event ignored as the turn already had score events");
			return;
		}
		
		int prev = determinePreviousPlayer();
		Player previous = this.players.get(prev);
		Score score = this.playerScoreMap.get(previous);
		
		if (score.lastTurnTerminatedCorrect()) {
			logger.warn("Undo of Next Player event ignored as the previous turn was terminated correctly");
			return;
		}
		
		this.currentScore.removeCurrentTurn();
		this.currentPlayerIndex = prev;
		this.currentPlayer = previous;
		this.currentScore = (X01Score) this.playerScoreMap.get(currentPlayer);
		this.currentScore.reopenTurn();
		this.provideStatusUpdate();
		
		this.gameFinished = false;
	}

	private int determinePreviousPlayer() {
		return (this.currentPlayerIndex - 1 + players.size()) % players.size();
	}

	private void undoLastThrow() {
		this.currentScore.undoLastThrow();
	}

	@Override
	public void redoEvent() {
		
	}

	@Override
	public Player getCurrentPlayer() {
		return this.currentPlayer;
	}

	@Override
	public boolean isFinished() {
		return this.gameFinished;
	}
	
}
