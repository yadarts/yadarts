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
package spare.n52.yadarts.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spare.n52.yadarts.EventEngine;
import spare.n52.yadarts.InitializationException;
import spare.n52.yadarts.entity.Player;
import spare.n52.yadarts.entity.PointEvent;
import spare.n52.yadarts.entity.impl.PlayerImpl;
import spare.n52.yadarts.games.GameStatusUpdateListener;
import spare.n52.yadarts.games.Score;
import spare.n52.yadarts.games.x01.GenericX01Game;

public class LocalTestRuntime {
	
	private static final Logger logger = LoggerFactory.getLogger(LocalTestRuntime.class);

	public static void main(String[] args) throws InitializationException, InterruptedException {
		List<Player> players = preparePlayers(args);
		
		GenericX01Game x301Game = new GenericX01Game(players, 301);
		x301Game.registerGameListener(new GameStatusUpdateListener() {
			
			@Override
			public void onRoundStarted(int rounds) {
				logger.info("+++++++++++++++++++");
				logger.info("Round {} started!", rounds);
				logger.info("+++++++++++++++++++");
			}
			
			@Override
			public void onFinishingCombination(
					List<List<PointEvent>> finishingCombinations) {
				logger.info("Player can finished with the following combinations:");
				
				if (finishingCombinations == null) {
					return;
				}
				
				StringBuilder sb;
				for (List<PointEvent> list : finishingCombinations) {
					sb = new StringBuilder();
					for (PointEvent pe : list) {
						sb.append(pe);
						sb.append(" + ");
					}
					logger.info(sb.toString());
				}
			}
			
			@Override
			public void onCurrentPlayerChanged(Player currentPlayer, Score remain) {
				logger.info("####################");
				logger.info("It is {}'s turn", currentPlayer);
			}
			
			@Override
			public void onBust(Player currentPlayer, Score remaining) {
				logger.info("{} busted!", currentPlayer);
			}

			@Override
			public void onTurnFinished(Player finishedPlayer, Score remainingScore) {
				logger.info("Player {} finished the turn. Remaining points: {}", finishedPlayer, remainingScore.getTotalScore());
			}

			@Override
			public void onRemainingScoreForPlayer(Player currentPlayer,
					Score remainingScore) {
				logger.info("Player {}'s remaining points: {}", currentPlayer, remainingScore.getTotalScore());
			}

			@Override
			public void requestNextPlayerEvent() {
				logger.info("Please press 'Next Player'!");
			}

			@Override
			public void onPlayerFinished(Player currentPlayer) {
				logger.info("Player {} finished!!!!!!! You are a Dart god!", currentPlayer);
			}

			@Override
			public void onGameFinished(Map<Player, Score> playerScoreMap) {
				logger.info("The game has ended!");
				
				for (Player player : playerScoreMap.keySet()) {
					logger.info("{}: {}", player, playerScoreMap.get(player));
				}
			}

			@Override
			public void onPointEvent(PointEvent event) {
				//TODO
			}

			@Override
			public void onNextPlayerPressed() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onBounceOutPressed() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onDartMissedPressed() {
				// TODO Auto-generated method stub
				
			}
		});
		
		EventEngine.instance().registerListener(x301Game);
		
		while (true) {
			Thread.sleep(5000);
		}
	}

	private static List<Player> preparePlayers(String[] args) {
		List<Player> result = new ArrayList<>();
		
		for (String player : args) {
			result.add(new PlayerImpl(player));
		}
		
		return result;
	}
	
}
