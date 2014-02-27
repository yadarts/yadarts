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
 * Default impl for {@link GameStatusUpdateListener}, doing nothing.
 */
public class GameStatusUpdateAdapter implements GameStatusUpdateListener {

	@Override
	public void onFinishingCombination(
			List<List<PointEvent>> finishingCombinations) {
	}

	@Override
	public void onCurrentPlayerChanged(Player currentPlayer, Score score) {
	}

	@Override
	public void onBust(Player currentPlayer, Score score) {
	}

	@Override
	public void onRoundStarted(int roundNumber) {
	}

	@Override
	public void onTurnFinished(Player player, Score score) {
	}

	@Override
	public void onRemainingScoreForPlayer(Player player, Score score) {
	}

	@Override
	public void requestNextPlayerEvent() {
	}

	@Override
	public void onPlayerFinished(Player player) {
	}

	@Override
	public void onGameFinished(Map<Player, Score> playerScoreMap,
			List<Player> winner) {
	}

	@Override
	public void onPointEvent(PointEvent event) {
	}

	@Override
	public void onNextPlayerPressed() {
	}

	@Override
	public void onBounceOutPressed() {
	}

	@Override
	public void onDartMissedPressed() {
	}

}
