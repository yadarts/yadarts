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
import spare.n52.yadarts.games.x01.GenericX01Game;
import spare.n52.yadarts.games.x01.Three01Game;

public class ComplexX01WorkflowTest {

	Player one = new PlayerImpl("ha");
	Player two = new PlayerImpl("ho");
	
	
	@Test
	public void testWorkflow() {
		GenericX01Game game = createGame();
		
		int c = game.getScores().get(one).getTotalScore();
		Assert.assertTrue("Unexpected total score! expted 101 , was" +c, c == 101);
		c = game.getScores().get(two).getTotalScore();
		Assert.assertTrue("Unexpected total score! expted 101 , was" +c, c == 101);
		
		/*
		 * one makes 3 hits, and two more which shall be ignored
		 */
		game.receiveEvent(HitEvent.singleHitInner(20));
		c = game.getScores().get(one).getTotalScore();
		Assert.assertTrue("Unexpected total score! expted 81 , was" +c, c == 81);
		game.receiveEvent(HitEvent.singleHitInner(20));
		c = game.getScores().get(one).getTotalScore();
		Assert.assertTrue("Unexpected total score! expted 61 , was" +c, c == 61);
		game.receiveEvent(HitEvent.singleHitInner(20));
		c = game.getScores().get(one).getTotalScore();
		Assert.assertTrue("Unexpected total score! expted 41 , was" +c, c == 41);
		game.receiveEvent(HitEvent.singleHitInner(18));
		c = game.getScores().get(one).getTotalScore();
		Assert.assertTrue("Unexpected total score! expted 41 , was" +c, c == 41);
		game.receiveEvent(HitEvent.singleHitInner(18));
		c = game.getScores().get(one).getTotalScore();
		Assert.assertTrue("Unexpected total score! expted 41 , was" +c, c == 41);
		
		game.receiveEvent(ButtonEvent.nextPlayer());
		Assert.assertTrue("Wrong player!", game.getCurrentPlayer() == two);
		
		game.receiveEvent(ButtonEvent.dartMissed());
		c = game.getScores().get(two).getTotalScore();
		Assert.assertTrue("Unexpected total score! expted 101 , was" +c, c == 101);
		c = game.getScores().get(two).getThrownDarts();
		Assert.assertTrue("Unexpected thrown darts! expted 1 , was" +c, c == 1);
		
		game.receiveEvent(HitEvent.singleHitInner(20));
		c = game.getScores().get(two).getTotalScore();
		Assert.assertTrue("Unexpected total score! expted 81 , was" +c, c == 81);
		
		game.receiveEvent(ButtonEvent.bounceOut());
		c = game.getScores().get(two).getTotalScore();
		Assert.assertTrue("Unexpected total score! expted 101 , was" +c, c == 101);
		c = game.getScores().get(two).getThrownDarts();
		Assert.assertTrue("Unexpected thrown darts! expted 2 , was" +c, c == 2);
		
		game.receiveEvent(ButtonEvent.nextPlayer());
		c = game.getScores().get(two).getThrownDarts();
		Assert.assertTrue("Unexpected thrown darts! expted 3 , was" +c, c == 3);
		
		Assert.assertTrue("Wrong player!", game.getCurrentPlayer() == one);
		
		c = game.getScores().get(one).getThrownDarts();
		Assert.assertTrue("Unexpected thrown darts! expted 3 , was" +c, c == 3);
		
		/*
		 * bust!
		 */
		game.receiveEvent(HitEvent.tripleHit(20));
		c = game.getScores().get(one).getTotalScore();
		Assert.assertTrue("Unexpected total score! expted 41 , was" +c, c == 41);
		
		c = game.getScores().get(one).getThrownDarts();
		Assert.assertTrue("Unexpected thrown darts! expted 4 , was" +c, c == 4);
		
		/*
		 * do it one more time. events shall be ignored due to bust
		 */
		game.receiveEvent(HitEvent.tripleHit(20));
		c = game.getScores().get(one).getTotalScore();
		Assert.assertTrue("Unexpected total score! expted 41 , was" +c, c == 41);
		
		c = game.getScores().get(one).getThrownDarts();
		Assert.assertTrue("Unexpected thrown darts! expted 4 , was" +c, c == 4);
		
		/*
		 * undo the busting throw
		 */
		game.undoEvent();
		
		c = game.getScores().get(one).getThrownDarts();
		Assert.assertTrue("Unexpected thrown darts! expted 3 , was" +c, c == 3);
		
		game.receiveEvent(HitEvent.doubleHit(19));
		c = game.getScores().get(one).getTotalScore();
		Assert.assertTrue("Unexpected total score! expted 3 , was" +c, c == 3);
		
		c = game.getScores().get(one).getThrownDarts();
		Assert.assertTrue("Unexpected thrown darts! expted 4 , was" +c, c == 4);
		
		/*
		 * finish it
		 */
		game.receiveEvent(HitEvent.singleHitInner(3));
		c = game.getScores().get(one).getTotalScore();
		Assert.assertTrue("Unexpected total score! expted 0 , was" +c, c == 0);
		
		
		game.receiveEvent(ButtonEvent.nextPlayer());
		Assert.assertTrue("Wrong player!", game.getCurrentPlayer() == two);
		c = game.getScores().get(two).getThrownDarts();
		Assert.assertTrue("Unexpected thrown darts! expted 3 , was" +c, c == 3);
		
		game.receiveEvent(ButtonEvent.nextPlayer());
		Assert.assertTrue("Wrong player!", game.getCurrentPlayer() == two);
		c = game.getScores().get(two).getThrownDarts();
		Assert.assertTrue("Unexpected thrown darts! expted 6 , was" +c, c == 6);
		game.receiveEvent(ButtonEvent.nextPlayer());
		Assert.assertTrue("Wrong player!", game.getCurrentPlayer() == two);
		game.receiveEvent(ButtonEvent.nextPlayer());
		Assert.assertTrue("Wrong player!", game.getCurrentPlayer() == two);
		
		game.undoEvent();
		Assert.assertTrue("Wrong player!", game.getCurrentPlayer() == two);
		c = game.getScores().get(two).getThrownDarts();
		Assert.assertTrue("Unexpected thrown darts! expted 3 , was" +c, c == 3);
		game.undoEvent();
		game.undoEvent();
		game.undoEvent();
		game.undoEvent();
		game.undoEvent();
		game.undoEvent();
		
		c = game.getScores().get(two).getThrownDarts();
		Assert.assertTrue("Unexpected thrown darts! expted 3 , was" +c, c == 3);
		
		game.receiveEvent(HitEvent.tripleHit(20));
		c = game.getScores().get(two).getTotalScore();
		Assert.assertTrue("Unexpected total score! expted 41 , was" +c, c == 41);
		
		/*
		 * bust
		 */
		game.receiveEvent(HitEvent.tripleHit(20));
		c = game.getScores().get(two).getTotalScore();
		Assert.assertTrue("Unexpected total score! expted 101 , was" +c, c == 101);
		
		c = game.getScores().get(two).getThrownDarts();
		Assert.assertTrue("Unexpected thrown darts! expted 5 , was" +c, c == 5);
		
		game.receiveEvent(ButtonEvent.nextPlayer());
		
		List<Player> winners = game.getWinners();
		Assert.assertTrue("Wrong winner count!", winners.size() == 1);
		Assert.assertTrue("Wrong winner!", winners.get(0) == one);
	}

	private GenericX01Game createGame() {
		GenericX01Game game = Three01Game.create(Arrays.asList(new Player[] {one, two}), 101); 
		return game;
	}
	
}
