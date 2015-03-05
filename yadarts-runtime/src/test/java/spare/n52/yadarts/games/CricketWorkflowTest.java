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
import spare.n52.yadarts.entity.impl.ButtonEvent;
import spare.n52.yadarts.entity.impl.HitEvent;
import spare.n52.yadarts.entity.impl.PlayerImpl;
import spare.n52.yadarts.games.cricket.CricketGame;
import spare.n52.yadarts.games.cricket.CricketScore;

public class CricketWorkflowTest {

	@Test
	public void testWorkflow() {
		Player a = (Player) new PlayerImpl("a");
		Player b = (Player) new PlayerImpl("b");
		List<Player> players = Arrays.asList(a, b);
		CricketGame game = new CricketGame(players);
		
		/*
		 * first round player a
		 */
		game.receiveEvent(HitEvent.tripleHit(20));
		game.receiveEvent(HitEvent.doubleHit(20));
		game.receiveEvent(HitEvent.singleHitInner(20));
		
		Assert.assertTrue(game.getScores().get(a).getTotalScore() == 60);
		
		game.receiveEvent(HitEvent.singleHitInner(20));
		
		Assert.assertTrue(game.getScores().get(a).getTotalScore() == 60);

		/*
		 * first round player b
		 */
		game.receiveEvent(ButtonEvent.nextPlayer());
		Assert.assertTrue(game.getScores().get(a).getTotalScore() == 60);
		
		game.receiveEvent(HitEvent.doubleHit(20));
		game.receiveEvent(HitEvent.singleHitInner(19));
		
		Assert.assertTrue(game.getScores().get(a).getTotalScore() == 60);
		Assert.assertTrue(game.getScores().get(b).getTotalScore() == 0);
		
		Assert.assertTrue(getCricketScore(game, a).getRemainingClosedSlots(20) == 0);
		Assert.assertTrue(getCricketScore(game, b).getRemainingClosedSlots(20) == 1);
		Assert.assertTrue(getCricketScore(game, b).getRemainingClosedSlots(19) == 2);
		
		game.receiveEvent(ButtonEvent.dartMissed());
		
		/*
		 * hit some more, should be ignored
		 */
		game.receiveEvent(HitEvent.singleHitInner(19));
		game.receiveEvent(HitEvent.singleHitInner(19));
		game.receiveEvent(HitEvent.singleHitInner(19));
		
		Assert.assertTrue(game.getScores().get(a).getTotalScore() == 60);
		Assert.assertTrue(game.getScores().get(b).getTotalScore() == 0);
		
		Assert.assertTrue(getCricketScore(game, a).getRemainingClosedSlots(20) == 0);
		Assert.assertTrue(getCricketScore(game, b).getRemainingClosedSlots(20) == 1);
		Assert.assertTrue(getCricketScore(game, b).getRemainingClosedSlots(19) == 2);
		
		/*
		 * second round player a
		 */
		game.receiveEvent(ButtonEvent.nextPlayer());
		
		game.receiveEvent(HitEvent.tripleHit(19));
		game.receiveEvent(HitEvent.tripleHit(18));
		game.receiveEvent(HitEvent.tripleHit(17));
		
		Assert.assertTrue(game.getScores().get(a).getTotalScore() == 60);
		Assert.assertTrue(game.getScores().get(b).getTotalScore() == 0);
		
		/*
		 * second round player b
		 */
		game.receiveEvent(ButtonEvent.nextPlayer());
		
		game.receiveEvent(HitEvent.tripleHit(15));
		game.receiveEvent(HitEvent.tripleHit(16));
		game.receiveEvent(HitEvent.tripleHit(17));
		
		Assert.assertTrue(game.getScores().get(a).getTotalScore() == 60);
		Assert.assertTrue(game.getScores().get(b).getTotalScore() == 0);
		
		/*
		 * third round player a
		 */
		game.receiveEvent(ButtonEvent.nextPlayer());
		
		game.receiveEvent(HitEvent.singleHitInner(19));
		game.receiveEvent(HitEvent.doubleHit(18));
		game.receiveEvent(HitEvent.tripleHit(7));
		
		Assert.assertTrue(game.getScores().get(a).getTotalScore() == 60+19+18+18);
		Assert.assertTrue(game.getScores().get(b).getTotalScore() == 0);
		
		/*
		 * third round player b
		 */
		game.receiveEvent(ButtonEvent.nextPlayer());
		
		game.receiveEvent(HitEvent.doubleHit(20));
		game.receiveEvent(HitEvent.doubleHit(19));
		
		/*
		 * even player b hit 20 4 times now, he does not have score, as player a also opened the field
		 */
		Assert.assertTrue(game.getScores().get(b).getTotalScore() == 0);
		
		Assert.assertTrue(getCricketScore(game, b).getRemainingClosedSlots(20) == 0);
		Assert.assertTrue(getCricketScore(game, b).getRemainingClosedSlots(19) == 0);
		
		game.receiveEvent(ButtonEvent.dartMissed());
		
		/*
		 * SCORE UNTIL NOW
		 * a=CricketScore for Player a; 15=0; 16=0; 17=3; 18=5; 19=4; 20=6; 25=0;
		 * b=CricketScore for Player b; 15=3; 16=3; 17=3; 18=0; 19=3; 20=3; 25=0;
		 */
		
		/*
		 * fourth round player a
		 */
		game.receiveEvent(ButtonEvent.nextPlayer());
		
		game.receiveEvent(HitEvent.tripleHit(15));
		game.receiveEvent(HitEvent.tripleHit(16));
		game.receiveEvent(HitEvent.tripleHit(17));
		
		Assert.assertTrue(game.getScores().get(a).getTotalScore() == 60+19+18+18);
		Assert.assertTrue(game.getScores().get(b).getTotalScore() == 0);
		
		/*
		 * fourth round player b
		 */
		game.receiveEvent(ButtonEvent.nextPlayer());
		
		game.receiveEvent(HitEvent.tripleHit(18));
		game.receiveEvent(HitEvent.doubleHit(25));
		game.receiveEvent(HitEvent.doubleHit(25));
		
		Assert.assertTrue(game.getScores().get(a).getTotalScore() == 60+19+18+18);
		Assert.assertTrue(game.getScores().get(b).getTotalScore() == 25);
		
		/*
		 * SCORE UNTIL NOW
		 * b=CricketScore for Player b; 15=3; 16=3; 17=3; 18=3; 19=3; 20=3; 25=4;
		 * a=CricketScore for Player a; 15=3; 16=3; 17=3; 18=5; 19=4; 20=6; 25=0;
		 */
		
		/*
		 * fifth round player a
		 */
		game.receiveEvent(ButtonEvent.nextPlayer());
		
		Assert.assertTrue(!game.isFinished());
		
		game.receiveEvent(HitEvent.doubleHit(25));
		Assert.assertTrue(!game.isFinished());
		game.receiveEvent(HitEvent.doubleHit(25));
		game.receiveEvent(HitEvent.doubleHit(25));
		
		Assert.assertTrue(game.isFinished());
		
		Assert.assertTrue(game.getScores().get(a).getTotalScore() == 60+19+18+18);
		Assert.assertTrue(game.getScores().get(b).getTotalScore() == 25);
		
		Assert.assertTrue(getCricketScore(game, a).getRemainingClosedSlots(25) == 0);
		Assert.assertTrue(getCricketScore(game, a).getRemainingClosedSlots(20) == 0);
		Assert.assertTrue(getCricketScore(game, a).getRemainingClosedSlots(19) == 0);
		Assert.assertTrue(getCricketScore(game, a).getRemainingClosedSlots(18) == 0);
		Assert.assertTrue(getCricketScore(game, a).getRemainingClosedSlots(17) == 0);
		Assert.assertTrue(getCricketScore(game, a).getRemainingClosedSlots(16) == 0);
		Assert.assertTrue(getCricketScore(game, a).getRemainingClosedSlots(15) == 0);
		
		Assert.assertTrue(getCricketScore(game, b).getRemainingClosedSlots(25) == 0);
		Assert.assertTrue(getCricketScore(game, b).getRemainingClosedSlots(20) == 0);
		Assert.assertTrue(getCricketScore(game, b).getRemainingClosedSlots(19) == 0);
		Assert.assertTrue(getCricketScore(game, b).getRemainingClosedSlots(18) == 0);
		Assert.assertTrue(getCricketScore(game, b).getRemainingClosedSlots(17) == 0);
		Assert.assertTrue(getCricketScore(game, b).getRemainingClosedSlots(16) == 0);
		Assert.assertTrue(getCricketScore(game, b).getRemainingClosedSlots(15) == 0);
		
		Assert.assertTrue(game.isFinished());
		
		List<Player> winners = game.getWinners();
		Assert.assertTrue(winners.size() == 1);
		Assert.assertTrue(winners.get(0) == a);
	}

	private CricketScore getCricketScore(CricketGame game, Player a) {
		Score s = game.getScores().get(a);
		return (CricketScore) s;
	}
	
}
