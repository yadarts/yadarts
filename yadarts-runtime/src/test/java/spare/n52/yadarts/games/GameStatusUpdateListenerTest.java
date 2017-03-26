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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import spare.n52.yadarts.entity.Player;
import spare.n52.yadarts.entity.PointEvent;
import spare.n52.yadarts.entity.impl.HitEvent;
import spare.n52.yadarts.entity.impl.PlayerImpl;
import spare.n52.yadarts.games.x01.GenericX01Game;
import spare.n52.yadarts.games.x01.Three01Game;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class GameStatusUpdateListenerTest {

    @Test
    public void testSequences() {
        final List<Integer> result = new ArrayList<>();
        
        GameStatusUpdateListener listener = Mockito.mock(GameStatusUpdateListener.class);
        
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                result.add(4);
                return null;
            }
        }).when(listener).onTurnFinished(Matchers.any(Player.class), Matchers.any(Score.class));
        
        final AtomicInteger counter = new AtomicInteger(1);
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                result.add(counter.getAndIncrement());
                return null;
            }
        }).when(listener).onPointEvent(Matchers.any(PointEvent.class), Matchers.any(Turn.class));
        
        invokeTurnExecution(listener);
        
        int i = 1;
        for (Integer integer : result) {
            Assert.assertThat(integer, CoreMatchers.is(i++));
        }
    }

    private void invokeTurnExecution(GameStatusUpdateListener listener) {
        Three01Game game = new Three01Game(Collections.singletonList((Player) new PlayerImpl("piotr")));
        game.registerGameListener(listener);
        game.receiveEvent(HitEvent.tripleHit(2));
        game.receiveEvent(HitEvent.tripleHit(3));
        game.receiveEvent(HitEvent.tripleHit(4));
    }
    
    @Test
    public void testBustSequence() {
        final List<Integer> result = new ArrayList<>();
        
        GameStatusUpdateListener listener = Mockito.mock(GameStatusUpdateListener.class);
        
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                result.add(4);
                return null;
            }
        }).when(listener).onTurnFinished(Matchers.any(Player.class), Matchers.any(Score.class));
        
        final AtomicInteger counter = new AtomicInteger(1);
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                result.add(counter.getAndIncrement());
                return null;
            }
        }).when(listener).onPointEvent(Matchers.any(PointEvent.class), Matchers.any(Turn.class));
        
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                result.add(10);
                return null;
            }
        }).when(listener).onBust(Matchers.any(Player.class), Matchers.any(Score.class));
        
        invokeBustTurnExecution(listener);
        
        Assert.assertThat(result.get(0), CoreMatchers.is(1));
        Assert.assertThat(result.get(1), CoreMatchers.is(2));
        Assert.assertThat(result.get(2), CoreMatchers.is(10));
        Assert.assertThat(result.get(3), CoreMatchers.is(3));
        Assert.assertThat(result.get(4), CoreMatchers.is(4));
    }

    private void invokeBustTurnExecution(GameStatusUpdateListener listener) {
        GenericX01Game game = GenericX01Game.create(Collections.singletonList((Player) new PlayerImpl("piotr")), 101);
        game.registerGameListener(listener);
        game.receiveEvent(HitEvent.tripleHit(20));
        game.receiveEvent(HitEvent.tripleHit(3));
        game.receiveEvent(HitEvent.tripleHit(20));
    }
    
}
