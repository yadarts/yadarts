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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spare.n52.yadarts.entity.InteractionEvent;
import spare.n52.yadarts.entity.Player;
import spare.n52.yadarts.entity.PointEvent;
import spare.n52.yadarts.entity.UserCausedEvent;
import spare.n52.yadarts.event.EventListener;

/**
 * A basic implementation of a {@link Game}.
 * It encapsulates the {@link EventListener} logic
 * by defining abstract methods on specific events.
 */
public abstract class AbstractGame implements Game, EventListener {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractGame.class);
	protected GameStatusUpdateListener gameListener = new CascadingGameListener();
	private List<GameStatusUpdateListener> listeners = new ArrayList<>();
	private Map<Player, Score> scores = new HashMap<>();
	private List<Player> players;
	private List<Player> winners;
	private Map<Player, Score> finalScores;

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
	
	
	@Override
	public synchronized void registerGameListener(GameStatusUpdateListener listener) {
		this.listeners.add(listener);
	}
	
	private void setScore(Player p, Score s) {
		this.scores.put(p, s);
	}
	
	public Map<Player, Score> getScores() {
		return scores;
	}

	public List<Player> getWinners() {
		return winners;
	}

	public Map<Player, Score> getFinalScores() {
		return finalScores;
	}
	

	public List<Player> getPlayers() {
		return players;
	}

	public void setPlayers(List<Player> players) {
		this.players = players;
	}



	private class CascadingGameListener implements GameStatusUpdateListener {

		@Override
		public synchronized void onFinishingCombination(
				List<List<PointEvent>> finishingCombinations) {
			for (GameStatusUpdateListener g : listeners) {
				try {
					g.onFinishingCombination(finishingCombinations);
				} catch (RuntimeException e) {
					logger.warn(e.getMessage(), e);
				}
			}
		}

		@Override
		public synchronized void onCurrentPlayerChanged(Player currentPlayer, Score score) {
			setScore(currentPlayer, score);
			for (GameStatusUpdateListener g : listeners) {
				try {
					g.onCurrentPlayerChanged(currentPlayer, score);
				} catch (RuntimeException e) {
					logger.warn(e.getMessage(), e);
				}
			}			
		}

		@Override
		public synchronized void onBust(Player currentPlayer, Score score) {
			setScore(currentPlayer, score);
			for (GameStatusUpdateListener g : listeners) {
				try {
					g.onBust(currentPlayer, score);
				} catch (RuntimeException e) {
					logger.warn(e.getMessage(), e);
				}
			}				
		}

		@Override
		public synchronized void onRoundStarted(int roundNumber) {
			for (GameStatusUpdateListener g : listeners) {
				try {
					g.onRoundStarted(roundNumber);
				} catch (RuntimeException e) {
					logger.warn(e.getMessage(), e);
				}
			}				
		}

		@Override
		public synchronized void onTurnFinished(Player player, Score score) {
			setScore(player, score);
			for (GameStatusUpdateListener g : listeners) {
				try {
					g.onTurnFinished(player, score);
				} catch (RuntimeException e) {
					logger.warn(e.getMessage(), e);
				}
			}				
		}

		@Override
		public synchronized void onRemainingScoreForPlayer(Player player, Score score) {
			setScore(player, score);
			for (GameStatusUpdateListener g : listeners) {
				try {
					g.onRemainingScoreForPlayer(player, score);
				} catch (RuntimeException e) {
					logger.warn(e.getMessage(), e);
				}
			}				
		}

		@Override
		public synchronized void requestNextPlayerEvent() {
			for (GameStatusUpdateListener g : listeners) {
				try {
					g.requestNextPlayerEvent();
				} catch (RuntimeException e) {
					logger.warn(e.getMessage(), e);
				}
			}				
		}

		@Override
		public synchronized void onPlayerFinished(Player player) {
			for (GameStatusUpdateListener g : listeners) {
				try {
					g.onPlayerFinished(player);
				} catch (RuntimeException e) {
					logger.warn(e.getMessage(), e);
				}
			}				
		}

		@Override
		public synchronized void onGameFinished(Map<Player, Score> playerScoreMap, List<Player> winners) {
			AbstractGame.this.winners = winners;
			AbstractGame.this.finalScores = playerScoreMap;
			
			for (GameStatusUpdateListener g : listeners) {
				try {
					g.onGameFinished(playerScoreMap, winners);
				} catch (RuntimeException e) {
					logger.warn(e.getMessage(), e);
				}
			}				
		}

		@Override
		public synchronized void onPointEvent(PointEvent event) {
			for (GameStatusUpdateListener g : listeners) {
				try {
					g.onPointEvent(event);
				} catch (RuntimeException e) {
					logger.warn(e.getMessage(), e);
				}
			}				
		}

		@Override
		public synchronized void onNextPlayerPressed() {
			for (GameStatusUpdateListener g : listeners) {
				try {
					g.onNextPlayerPressed();
				} catch (RuntimeException e) {
					logger.warn(e.getMessage(), e);
				}
			}				
		}

		@Override
		public synchronized void onBounceOutPressed() {
			for (GameStatusUpdateListener g : listeners) {
				try {
					g.onBounceOutPressed();
				} catch (RuntimeException e) {
					logger.warn(e.getMessage(), e);
				}
			}				
		}

		@Override
		public synchronized void onDartMissedPressed() {
			for (GameStatusUpdateListener g : listeners) {
				try {
					g.onDartMissedPressed();
				} catch (RuntimeException e) {
					logger.warn(e.getMessage(), e);
				}
			}				
		}
		
	}

}
