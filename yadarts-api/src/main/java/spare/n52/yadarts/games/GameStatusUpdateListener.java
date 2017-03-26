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

import java.util.List;
import java.util.Map;

import spare.n52.yadarts.entity.Player;
import spare.n52.yadarts.entity.PointEvent;

/**
 * Components interested in status updates on a game
 * can implement this interface and register themselves
 * at {@link Game#}
 */
public interface GameStatusUpdateListener {

	/**
	 * provides a list of combinations a {@link Player}
	 * can aim for in order to finish the game.
	 * 
	 * @param finishingCombinations
	 */
	void onFinishingCombination(
			List<List<PointEvent>> finishingCombinations);

	/**
	 * @param currentPlayer the new active player
	 * @param score his current score
	 */
	void onCurrentPlayerChanged(Player currentPlayer, Score score);

	/**
	 * @param currentPlayer which player
	 * @param score his score after the bust was applied
	 */
	void onBust(Player currentPlayer, Score score);

	/**
	 * @param roundNumber the new round number
	 */
	void onRoundStarted(int roundNumber);

	/**
	 * called when a player finished his turn (= 3 throws)
	 * 
	 * @param player the player who finished the turn
	 * @param score the score of the player
	 */
	void onTurnFinished(Player player, Score score);

	/**
	 * provides the remaining score for a player. This is normally
	 * called after every throw.
	 * 
	 * @param player the player
	 * @param score the score of the player
	 */
	void onRemainingScoreForPlayer(Player player, Score score);

	/**
	 * called whenever the board was hit, but "Next Player"
	 * has not been pressed.
	 */
	void requestNextPlayerEvent();

	/**
	 * one player finished the game. the turn still goes
	 * on and other players can finish as well.
	 * 
	 * @param player
	 */
	void onPlayerFinished(Player player);

	/**
	 * called after the end of a turn in which a player
	 * finished (see {@link #onPlayerFinished(Player)}
	 * 
	 * @param playerScoreMap scores of all player
	 * @param winner the winner
	 */
	void onGameFinished(Map<Player, Score> playerScoreMap, List<Player> winner);
	
	/**
	 * provides a generic event and its points.
	 * 
	 * @param event the hit
         * @param turn the related turn, can be null
	 */
	void onPointEvent(PointEvent event, Turn turn);
	
	/**
	 * next player button has been pressed
	 */
	void onNextPlayerPressed();
	
	/**
	 * bounce out has been pressed
	 */
	void onBounceOutPressed();
	
	/**
	 * dart missed has been pressed
	 */
	void onDartMissedPressed();

}
