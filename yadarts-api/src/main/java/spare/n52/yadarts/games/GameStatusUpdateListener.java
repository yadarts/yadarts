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

public interface GameStatusUpdateListener {

	void provideFinishingCombination(
			List<List<PointEvent>> finishingCombinations);

	void onCurrentPlayerChanged(Player currentPlayer, int remainingScore);

	void onBust(Player currentPlayer, int remainingScore);

	void roundStarted(int rounds);

	void onTurnFinished(Player finishedPlayer, int totalScore);

	void remainingScoreForPlayer(Player currentPlayer, int remainingScore);

	void requestNextPlayerEvent();

	void playerFinished(Player currentPlayer);

	void onGameFinished(Map<Player, Score> playerScoreMap);
	
	void onPointEvent(PointEvent event);
	
	void onNextPlayerPressed();
	
	void onBounceOutPressed();
	
	void onDartMissedPressed();

}
