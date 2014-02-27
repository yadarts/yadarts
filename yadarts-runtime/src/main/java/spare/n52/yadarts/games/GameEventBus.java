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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameEventBus {
	
	private static final Logger logger = LoggerFactory.getLogger(GameEventBus.class);

	private static GameEventBus instance;
	private ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setName("GameEventBus-Thread");
			return t;
		}
	});
	
	protected List<GameEventListener> listeners = new ArrayList<>();
	
	public static synchronized GameEventBus instance() {
		if (instance == null) {
			instance = new GameEventBus();
		}
		
		return instance;
	}
	
	public void startGame(final AbstractGame game) {
		this.executor.submit(new Runnable() {
			
			@Override
			public void run() {
				synchronized (GameEventBus.this) {
					for (GameEventListener l : listeners ) {
						try {
							l.onGameStarted(game);
						}
						catch (RuntimeException e) {
							logger.warn(e.getMessage());
							logger.debug(e.getMessage(), e);
						}
					}					
				}
			}
		});
	}

	public synchronized void registerListener(GameEventListener l) {
		this.listeners.add(l);
	}

	public void endGame(final AbstractGame game) {
		this.executor.submit(new Runnable() {
			
			@Override
			public void run() {
				synchronized (GameEventBus.this) {
					for (GameEventListener l : listeners ) {
						try {
							l.onGameFinished(game);
						}
						catch (RuntimeException e) {
							logger.warn(e.getMessage());
							logger.debug(e.getMessage(), e);
						}
					}					
				}
			}
		});		
	}
	
}
