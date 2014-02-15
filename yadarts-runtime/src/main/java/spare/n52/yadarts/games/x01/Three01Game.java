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
package spare.n52.yadarts.games.x01;

import java.util.List;

import spare.n52.yadarts.entity.Player;
import spare.n52.yadarts.games.AnnotatedGame;

@AnnotatedGame(highscorePersistentName="301Game", displayName="301")
public class Three01Game extends GenericX01Game {

	public Three01Game(List<Player> players) {
		super(players, 301);
	}

}
