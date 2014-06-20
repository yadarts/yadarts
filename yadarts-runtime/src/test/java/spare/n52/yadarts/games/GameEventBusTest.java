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
import spare.n52.yadarts.entity.impl.PlayerImpl;
import spare.n52.yadarts.games.x01.Three01Game;

public class GameEventBusTest {

	AbstractGame game = new Three01Game(Arrays.asList(new Player[] {new PlayerImpl("ha"), new PlayerImpl("ho")})); 

	@Test
	public void testGameStart() throws GameAlreadyActiveException, InterruptedException {
		GameEventBus.instance().startGame(game);
		
		try {
			GameEventBus.instance().startGame(game);
		}
		catch (GameAlreadyActiveException e) {
			Assert.assertNotNull(e);
		}
		
		Thread.sleep(1000);
	}
	
	@Test
	public void testGameEnd() throws GameAlreadyActiveException, InterruptedException, NoGameActiveException {
		try {
			GameEventBus.instance().endGame(game);
		} catch (NoGameActiveException e) {
			Assert.assertNotNull(e);
		}
		
		GameEventBus.instance().startGame(game);
		
		GameEventBus.instance().endGame(game);
		
		Thread.sleep(1000);
	}
	
}
