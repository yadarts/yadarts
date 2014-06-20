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

import org.junit.Assert;
import org.junit.Test;

import spare.n52.yadarts.entity.Player;
import spare.n52.yadarts.entity.impl.ButtonEvent;
import spare.n52.yadarts.entity.impl.HitEvent;
import spare.n52.yadarts.entity.impl.PlayerImpl;
import spare.n52.yadarts.games.x01.GenericX01Game;
import spare.n52.yadarts.games.x01.Three01Game;

public class UndoEventsThrowTest {

	Player one = new PlayerImpl("ha");
	Player two = new PlayerImpl("ho");
	
	@Test
	public void testUndoOfHitEvent() {
		GenericX01Game game = createGame();
		
		game.receiveEvent(HitEvent.singleHitInner(20));
		game.receiveEvent(HitEvent.singleHitInner(19));
		game.receiveEvent(HitEvent.singleHitInner(18));
		
		game.undoEvent();
		game.undoEvent();
		game.undoEvent();
		game.undoEvent();
		game.undoEvent();
		game.undoEvent();
		game.undoEvent();
		
		int c = game.getScores().get(one).getThrownDarts();
		Assert.assertTrue("Unexpected dart count: expected 0, was "+c, c == 0);
		
		game.receiveEvent(HitEvent.singleHitInner(20));
		game.receiveEvent(HitEvent.singleHitInner(19));
		
		c = game.getScores().get(one).getThrownDarts();
		Assert.assertTrue("Unexpected dart count: expected 2, was "+c, c == 2);
		
		game.receiveEvent(HitEvent.singleHitInner(5));
		c = game.getScores().get(one).getThrownDarts();
		Assert.assertTrue("Unexpected dart count: expected 3, was "+c, c == 3);
		
		c = game.getScores().get(one).getTotalScore();
		Assert.assertTrue("Unexpected total score! expted 257 , was" +c, c == 257);
	}
	
	@Test
	public void testUndoOfNextPlayerEvent() {
		GenericX01Game game = createGame();
		
		/*
		 * one makes 3 hits
		 */
		game.receiveEvent(HitEvent.singleHitInner(20));
		game.receiveEvent(HitEvent.singleHitInner(19));
		game.receiveEvent(HitEvent.singleHitInner(18));
		
		/*
		 * switch to two
		 */
		game.receiveEvent(ButtonEvent.nextPlayer());
		/*
		 * switch to one (two did not make any throws)
		 */
		game.receiveEvent(ButtonEvent.nextPlayer());
		
		Player p = game.getCurrentPlayer();
		
		Assert.assertTrue("Wrong current player!", p == one);
		
		/*
		 * undo switching to one, should now be two again
		 */
		game.undoEvent();
		
		p = game.getCurrentPlayer();
		
		Assert.assertTrue("Wrong current player!", p == two);
		
		/*
		 * make one hit and undo twice --> next player cannot be undone
		 * when the new turn has at least one throw
		 */
		game.receiveEvent(HitEvent.singleHitInner(18));
		game.undoEvent();
		game.undoEvent();
		
		p = game.getCurrentPlayer();
		
		Assert.assertTrue("Wrong current player!", p == two);
		
		int c = game.getScores().get(two).getTotalScore();
		Assert.assertTrue("Unexpected total score! expted 301 , was" +c, c == 301);
		
	}

	private GenericX01Game createGame() {
		Three01Game game = new Three01Game(Arrays.asList(new Player[] {one, two})); 
		return game;
	}
	
}
