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
