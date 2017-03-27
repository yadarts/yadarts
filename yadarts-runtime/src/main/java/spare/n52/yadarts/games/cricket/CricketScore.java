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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spare.n52.yadarts.entity.Player;
import spare.n52.yadarts.entity.PointEvent;
import spare.n52.yadarts.entity.impl.HitEvent;
import spare.n52.yadarts.games.Score;
import spare.n52.yadarts.games.Turn;

public class CricketScore implements Score {
    
    private final Player player;
    private int turnThrowCount = 0;
    
    private final Map<Integer, Integer> numberHits = new HashMap<>();
    private int totalDarts;
    private PointEvent lastHit;
    
    public CricketScore(Player player) {
        this.player = player;
        
        for (int i : CricketGame.VALID_NUMBERS) {
            numberHits.put(i, 0);
        }
    }
    
    @Override
    public int getTotalScore() {
        int result = 0;
        
        for (int n : this.numberHits.keySet()) {
            int value = this.numberHits.get(n);
            if (value > 3) {
                result += (value - 3) * n;
            }
        }
        
        return result;
    }
    
    @Override
    public int getThrownDarts() {
        return this.totalDarts;
    }
    
    @Override
    public Date getDateTime() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public int getTotalTime() {
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    public Player getPlayer() {
        return this.player;
    }
    
    @Override
    public boolean turnHasEvents() {
        return this.turnThrowCount > 0;
    }
    
    @Override
    public void terminateLastTurn() {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public boolean lastTurnTerminatedCorrect() {
        return true;
    }
    
    public void onPointEvent(PointEvent event) {
        onPointEvent(event, false);
    }
    
    public void onPointEvent(PointEvent event, boolean playerIsLastToClose) {
        if (this.hasThrowsLeft()) {
            this.lastHit = event;
            this.turnThrowCount++;
            this.totalDarts++;
            
            if (event.getBaseNumber() >= 15) {
                Integer target = numberHits.get(event.getBaseNumber());
                int newTarget;
                if (playerIsLastToClose) {
                    newTarget = Math.min(3, target + event.getMultiplier());
                }
                else {
                    newTarget = target + event.getMultiplier();
                }
                numberHits.put(event.getBaseNumber(), newTarget);
            }
        }
    }
    
    public boolean playerHasOpened(int number) {
        if (number < 15) {
            return false;
        }
        return numberHits.get(number) >= 3;
    }
    
    public boolean hasThrowsLeft() {
        return this.turnThrowCount < 3;
    }
    
    public void removeLastHit() {
        if (this.lastHit == null) {
            return;
        }
        
        if (this.lastHit.getBaseNumber() >= 15) {
            Integer target = numberHits.get(this.lastHit.getBaseNumber());
            numberHits.put(this.lastHit.getBaseNumber(), target - this.lastHit.getMultiplier());
            this.lastHit = null;
        }
    }
    
    public void finishCurrentTurn() {
        while (hasThrowsLeft()) {
            onPointEvent(HitEvent.singleHitInner(0));
        }
        
        this.turnThrowCount = 0;
    }
    
    public int getRemainingClosedSlots(int i) {
        Integer target = this.numberHits.get(i);
        if (target != null) {
            return Math.max(0, 3 - target);
        }
        return 3;
    }
    
    public int getHitsFor(int i) {
        if (CricketGame.VALID_NUMBERS_SET.contains(i)) {
            return numberHits.get(i);
        }
        return 0;
    }
    
    @Override
    public Turn getLastTurn() {
        return new Turn() {
            @Override
            public boolean isBusted() {
                return false;
            }
        };
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("CricketScore for Player ");
        sb.append(this.player.getName());
        sb.append("; ");
        
        List<Integer> numbers = new ArrayList<>(this.numberHits.keySet());
        Collections.sort(numbers);
        
        for (Integer n : numbers) {
            sb.append(n);
            sb.append("=");
            sb.append(this.numberHits.get(n));
            sb.append("; ");
        }
        
        return sb.toString();
    }
    
    
    public boolean isFinished() {
        for (int i : CricketGame.VALID_NUMBERS) {
            if (!this.playerHasOpened(i)) {
                return false;
            }
        }
        return true;
    }
    
    
}
