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

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import spare.n52.yadarts.entity.Player;
import spare.n52.yadarts.entity.impl.HitEvent;
import spare.n52.yadarts.entity.impl.PlayerImpl;
import spare.n52.yadarts.games.cricket.CricketGame;

public class CricketWorkflowTest {

	@Test
	public void testWorkflow() {
		Player a = (Player) new PlayerImpl("a");
		Player b = (Player) new PlayerImpl("b");
		List<Player> players = Arrays.asList(a, b);
		CricketGame game = new CricketGame(players);
		
		game.receiveEvent(HitEvent.tripleHit(20));
		game.receiveEvent(HitEvent.doubleHit(20));
		game.receiveEvent(HitEvent.singleHitInner(20));
		
		Assert.assertTrue(game.getScores().get(a).getTotalScore() == 60);
	}
	
}
