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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.hamcrest.CoreMatchers;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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
        game.receiveEvent(HitEvent.tripleHit(20)); //20 is open for a
        game.receiveEvent(HitEvent.doubleHit(20)); //score 40 for a
        game.receiveEvent(HitEvent.singleHitInner(20)); //score 20 for a
        
        Assert.assertTrue(game.getScores().get(a).getTotalScore() == 60);
        
        game.receiveEvent(HitEvent.singleHitInner(20)); //turn already ended
        
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
        
        game.receiveEvent(HitEvent.tripleHit(19)); //19 is open for a
        game.receiveEvent(HitEvent.tripleHit(18)); //18 is open for a
        game.receiveEvent(HitEvent.tripleHit(17)); //17 is open for a
        
        Assert.assertTrue(game.getScores().get(a).getTotalScore() == 60);
        Assert.assertTrue(game.getScores().get(b).getTotalScore() == 0);
        
        /*
        * second round player b
        */
        game.receiveEvent(ButtonEvent.nextPlayer());
        
        game.receiveEvent(HitEvent.tripleHit(15)); //15 is open for b
        game.receiveEvent(HitEvent.tripleHit(16)); //16 is open for b
        game.receiveEvent(HitEvent.tripleHit(17)); //17 is open for b
        
        Assert.assertTrue(game.getScores().get(a).getTotalScore() == 60);
        Assert.assertTrue(game.getScores().get(b).getTotalScore() == 0);
        
        /*
        * third round player a
        */
        game.receiveEvent(ButtonEvent.nextPlayer());
        
        game.receiveEvent(HitEvent.singleHitInner(19)); //score 19 for a
        game.receiveEvent(HitEvent.doubleHit(18)); //score 36 for a
        game.receiveEvent(HitEvent.tripleHit(7)); //7 is ignored
        
        Assert.assertTrue(game.getScores().get(a).getTotalScore() == 60+19+18+18);
        Assert.assertTrue(game.getScores().get(b).getTotalScore() == 0);
        
        /*
        * third round player b
        */
        game.receiveEvent(ButtonEvent.nextPlayer());
        
        game.receiveEvent(HitEvent.doubleHit(20)); //20 open for b and closed as a and b have 3+ hits each
        game.receiveEvent(HitEvent.doubleHit(19)); //19 open for b
        
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
        
        game.receiveEvent(HitEvent.tripleHit(15)); //15 is open for a and closed
        game.receiveEvent(HitEvent.tripleHit(16)); //16 is open for a and closed
        game.receiveEvent(HitEvent.tripleHit(17)); //17 is open for a and closed
        
        Assert.assertTrue(game.getScores().get(a).getTotalScore() == 60+19+18+18);
        Assert.assertTrue(game.getScores().get(b).getTotalScore() == 0);
        
        /*
        * fourth round player b
        */
        game.receiveEvent(ButtonEvent.nextPlayer());
        
        game.receiveEvent(HitEvent.tripleHit(18)); //18 is open for b and closed
        game.receiveEvent(HitEvent.doubleHit(25));
        game.receiveEvent(HitEvent.doubleHit(25)); //bulls open for b + 25 score
        
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
        
        /*
        * lets capture some events
        */
        GameStatusUpdateListener listener = Mockito.mock(GameStatusUpdateListener.class);
        game.registerGameListener(listener);
        final List<String> result = new ArrayList<>();
        final List<Player> winnersReceived = new ArrayList<>();
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                winnersReceived.addAll((List<Player>) args[1]);
                result.add("gameFinished");
                return null;
            }
        }).when(listener).onGameFinished(Matchers.any(Map.class), Matchers.any(List.class));
        
        game.receiveEvent(HitEvent.doubleHit(25));
        Assert.assertTrue(!game.isFinished());
        
        game.receiveEvent(HitEvent.doubleHit(25)); //bulls open for b and closed -> all closed!
        game.receiveEvent(HitEvent.doubleHit(25)); //bulls already closed
        
        Assert.assertTrue(game.isFinished());
        Assert.assertThat(result.size(), CoreMatchers.is(1));
        Assert.assertThat(result.get(0), CoreMatchers.is("gameFinished"));
        
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
        
        List<Player> winners = game.getWinners();
        Assert.assertTrue(winners.size() == 1);
        Assert.assertTrue(winners.get(0) == a);
        Assert.assertTrue(winnersReceived.size() == 1);
        Assert.assertTrue(winnersReceived.get(0) == a);
    }
    
    private CricketScore getCricketScore(CricketGame game, Player a) {
        Score s = game.getScores().get(a);
        return (CricketScore) s;
    }
    
}
