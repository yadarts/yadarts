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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spare.n52.yadarts.entity.Player;
import spare.n52.yadarts.entity.PointEvent;
import spare.n52.yadarts.entity.impl.HitEvent;
import spare.n52.yadarts.games.AbstractGame;

public class CricketGame extends AbstractGame {
	
	private Player currentPlayer;
	private CricketScore currentScore;
	private Map<Player, CricketScore> cricketScores = new HashMap<>();

	public CricketGame(List<Player> players) {
		if (players == null || players.size() == 0) {
			throw new IllegalArgumentException("players must not be null or empty.");
		}
		
		this.setPlayers(players);
		
		for (Player player : players) {
			CricketScore cs = new CricketScore(player);
			this.setScore(player, cs);
			this.cricketScores.put(player, cs);
		}
		
		this.currentPlayer = players.get(0);
		this.currentScore = getCurrentScore();
	}
	
	private CricketScore getCurrentScore() {
		return this.cricketScores.get(this.currentPlayer);
	}
	
	@Override
	public String getShortName() {
		return "Cricket";
	}

	@Override
	protected void onNextPlayer() {
		this.currentScore.finishCurrentTurn();
		
		List<Player> ps = getPlayers();
		int i = ps.indexOf(this.currentPlayer);
		this.currentPlayer = ps.get((i + 1) % ps.size());
		this.currentScore = getCurrentScore();
		
		this.gameListener.onNextPlayerPressed();
	}

	@Override
	protected void onDartMissed() {
		this.currentScore.onPointEvent(HitEvent.singleHitInner(0));
		this.gameListener.onDartMissedPressed();
	}

	@Override
	protected void onBounceOut() {
		this.currentScore.removeLastHit();
		this.gameListener.onBounceOutPressed();
	}

	@Override
	protected void processPointEvent(PointEvent event) {
		if (!this.currentScore.hasThrowsLeft()) {
			return;
		}
		
		if (!isNumberClosed(event.getBaseNumber())) {
			this.currentScore.onPointEvent(event);
		}
		else {
			this.currentScore.onPointEvent(HitEvent.singleHitInner(0));
		}
		
		this.gameListener.onPointEvent(event);
	}

	@Override
	public void undoEvent() {
	}

	@Override
	public void redoEvent() {
	}

	@Override
	public Player getCurrentPlayer() {
		return this.currentPlayer;
	}
	
	private boolean isNumberClosed(int number) {
		for (Player p : this.cricketScores.keySet()) {
			CricketScore s = this.cricketScores.get(p);
			if (!s.playerHasOpened(number)) {
				return false;
			}
		}
		
		return true;
	}

}
