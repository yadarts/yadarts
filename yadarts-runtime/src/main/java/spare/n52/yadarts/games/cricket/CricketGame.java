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
package spare.n52.yadarts.games.cricket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import spare.n52.yadarts.entity.Player;
import spare.n52.yadarts.entity.PointEvent;
import spare.n52.yadarts.entity.impl.HitEvent;
import spare.n52.yadarts.games.AbstractGame;
import spare.n52.yadarts.games.AnnotatedGame;
import spare.n52.yadarts.games.GameStatusUpdateListener;
import spare.n52.yadarts.games.Score;

@AnnotatedGame(highscorePersistentName="CricketGame", displayName="Cricket")
public class CricketGame extends AbstractGame {
    
    public static final Integer[] VALID_NUMBERS = new Integer[] {15, 16, 17, 18, 19, 20, 25};
    public static final Set<Integer> VALID_NUMBERS_SET = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(VALID_NUMBERS)));
    
    private Player currentPlayer;
    private CricketScore currentScore;
    private final Map<Player, CricketScore> cricketScores = new HashMap<>();
    
    private int rounds = 1;
    
    private boolean gameFinished;
    
    private List<Player> winners;
    
    public CricketGame(List<Player> players) {
        if (players == null || players.size() == 0) {
            throw new IllegalArgumentException("players must not be null or empty.");
        }
        
        this.setPlayers(players);
        
        for (Player player : players) {
            CricketScore cs = new CricketScore(player);
            this.setScore(player, cs);
            this.cricketScores.put(player, cs);
        }
        
        this.currentPlayer = players.get(0);
        this.currentScore = getCurrentScore();
    }
    
    private CricketScore getCurrentScore() {
        return this.cricketScores.get(this.currentPlayer);
    }
    
    @Override
    public synchronized void registerGameListener(
            GameStatusUpdateListener listener) {
        super.registerGameListener(listener);
        this.gameListener.onRoundStarted(this.rounds);
    }
    
    @Override
    public String getShortName() {
        return "Cricket";
    }
    
    @Override
    protected void onNextPlayer() {
        this.currentScore.finishCurrentTurn();
        
        List<Player> ps = getPlayers();
        int i = ps.indexOf(this.currentPlayer);
        i = (i + 1) % ps.size();
        this.currentPlayer = ps.get(i);
        
        if (i == 0) {
            newRoundStarted();
        }
        
        this.currentScore = getCurrentScore();
        
        this.gameListener.onNextPlayerPressed();
        this.gameListener.onCurrentPlayerChanged(currentPlayer, currentScore);
    }
    
    private void newRoundStarted() {
        if (isFinished()) {
            this.gameListener.onGameFinished(getScores(), getWinners());
        }
        this.rounds++;
        this.gameListener.onRoundStarted(this.rounds);
    }
    
    @Override
    public List<Player> getWinners() {
        if (this.winners == null || this.winners.isEmpty()) {
            this.winners = new ArrayList<>();
            
            List<CricketScore> scoreList = new ArrayList<>(this.cricketScores.values());
            
            /*
            * find the maximum total score
            */
            CricketScore best = scoreList.get(0);
            for (CricketScore cs : scoreList) {
                if (cs.getTotalScore() >= best.getTotalScore()) {
                    best = cs;
                }
            }
            
            /*
            * find max total score with lowest total dart count
            */
            for (CricketScore cs : scoreList) {
                if (cs.getTotalScore() == best.getTotalScore()) {
                    if (cs.getThrownDarts() <= best.getThrownDarts()) {
                        best = cs;
                    }
                }
            }
            
            this.winners.add(best.getPlayer());
            
            /*
            * find those scores that have the same score and dart count
            */
            for (CricketScore cs : scoreList) {
                if (cs.getTotalScore() == best.getTotalScore()) {
                    if (cs.getThrownDarts() == best.getThrownDarts()) {
                        if (cs != best) {
                            this.winners.add(cs.getPlayer());
                        }
                    }
                }
            }
        }
        
        return this.winners;
    }
    
    @Override
    protected void onDartMissed() {
        if (!this.currentScore.hasThrowsLeft()) {
            return;
        }
        processPointEvent(HitEvent.singleHitInner(0));
        this.gameListener.onDartMissedPressed();
    }
    
    @Override
    protected void onBounceOut() {
        this.currentScore.removeLastHit();
        this.gameListener.onBounceOutPressed();
    }
    
    @Override
    protected void processPointEvent(PointEvent event) {
        if (!this.currentScore.hasThrowsLeft()) {
            return;
        }
        
        if (!isNumberClosed(event.getBaseNumber())) {
            if (playerIsLastToClose(event.getBaseNumber())) {
                this.currentScore.onPointEvent(event, true);
            }
            else {
                this.currentScore.onPointEvent(event);
            }
        }
        else {
            this.currentScore.onPointEvent(HitEvent.singleHitInner(0));
        }
        
        int num = event.getBaseNumber();
        if (num > 0 && num <= 20 || num == 25) {
            this.gameListener.onPointEvent(event, null);
        }
        
        if (!this.currentScore.hasThrowsLeft()) {
            this.gameListener.onTurnFinished(currentPlayer, currentScore);
        }
        
        if (isFinished()) {
            this.gameListener.onGameFinished(convertScore(cricketScores), getWinners());
            for (Player winner : winners) {
                this.gameListener.onPlayerFinished(winner);
            }
        }
    }
    
    private boolean playerIsLastToClose(int number) {
        for (Player p : this.cricketScores.keySet()) {
            CricketScore s = this.cricketScores.get(p);
            if (p != this.currentPlayer && !s.playerHasOpened(number)) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public void undoEvent() {
    }
    
    @Override
    public void redoEvent() {
    }
    
    @Override
    public Player getCurrentPlayer() {
        return this.currentPlayer;
    }
    
    private boolean isNumberClosed(int number) {
        for (Player p : this.cricketScores.keySet()) {
            CricketScore s = this.cricketScores.get(p);
            if (!s.playerHasOpened(number)) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public boolean isFinished() {
        if (this.gameFinished) {
            return true;
        }
        
        for (CricketScore cs : this.cricketScores.values()) {
            if (!cs.isFinished()) {
                return false;
            }
        }
        
        this.gameFinished = true;
        
        return true;
    }

    private Map<Player, Score> convertScore(Map<Player, CricketScore> cricketScores) {
        Map<Player, Score> result = new HashMap<>(cricketScores.size());
        
        for (Player player : cricketScores.keySet()) {
            result.put(player, cricketScores.get(player));
        }
        
        return result;
    }
    
}
