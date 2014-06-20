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

import java.util.Date;

import spare.n52.yadarts.entity.Player;

/**
 * A generic Score interface for, representing
 * the current status for one {@link Player}. 
 */
public interface Score {

	/**
	 * this may vary across game-specific Score implementations.
	 * E.g. a X01 game shows the remaining points, a cricket game
	 * shows the gathered points, etc.
	 * 
	 * @return the total score for the player
	 */
	public int getTotalScore();
	
	/**
	 * @return the number of darts the player has thrown
	 */
	public int getThrownDarts();
	
	/**
	 * @return the time when the player finished the game
	 */
	public Date getDateTime();

	/**
	 * @return the total amount of time the player took for his turns
	 */
	public int getTotalTime();

	/**
	 * @return the player for this score
	 */
	public Player getPlayer();
	
	/**
	 * @return true if there was at least one thrown dart
	 * or a miss / bounce out in the current turn
	 */
	public boolean turnHasEvents();

	/**
	 * terminate the last turn of this score. it shall not
	 * be modifiable after termination
	 */
	public void terminateLastTurn();

	/**
	 * @return true if the last turn was terminated correctly
	 */
	public boolean lastTurnTerminatedCorrect();
}
