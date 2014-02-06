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
package spare.n52.yadarts.usb;

import static org.hamcrest.CoreMatchers.*;

import org.junit.Assert;
import org.junit.Test;

import spare.n52.yadarts.entity.InteractionEvent;
import spare.n52.yadarts.entity.PointEvent;
import spare.n52.yadarts.entity.UserCausedEvent;
import spare.n52.yadarts.entity.UserCausedEvent.Type;
import spare.n52.yadarts.usb.handler.EmprexEventHandler;

public class EmprexHandlerTest {

	private EmprexEventHandler handler = new EmprexEventHandler();

	@Test
	public void shouldCreateSingleHit() {
		InteractionEvent result = handler.createEvent(new int[] {
				Integer.parseInt("02", 16), Integer.parseInt("8a", 16),
				Integer.parseInt("00", 16), Integer.parseInt("00", 16),
				Integer.parseInt("00", 16), Integer.parseInt("00", 16),
				Integer.parseInt("00", 16)
				});
		
		Assert.assertThat(result, is(instanceOf(PointEvent.class)));
		Assert.assertThat(((PointEvent) result).getMultiplier(), is(1));
		Assert.assertThat(((PointEvent) result).getBaseNumber(), is(10));
	}
	
	@Test
	public void shouldCreateInnerSingleHit() {
		InteractionEvent result = handler.createEvent(new int[] {
				Integer.parseInt("02", 16), Integer.parseInt("28", 16),
				Integer.parseInt("00", 16), Integer.parseInt("00", 16),
				Integer.parseInt("00", 16), Integer.parseInt("00", 16),
				Integer.parseInt("00", 16)
				});
		
		Assert.assertThat(result, is(instanceOf(PointEvent.class)));
		Assert.assertThat(((PointEvent) result).getMultiplier(), is(1));
		Assert.assertThat(((PointEvent) result).getBaseNumber(), is(8));
		Assert.assertThat(((PointEvent) result).isOuterRing(), is(not(true)));
	}
	
	@Test
	public void shouldCreateDoubleHit() {
		InteractionEvent result = handler.createEvent(new int[] {
				Integer.parseInt("02", 16), Integer.parseInt("42", 16),
				Integer.parseInt("00", 16), Integer.parseInt("00", 16),
				Integer.parseInt("00", 16), Integer.parseInt("00", 16),
				Integer.parseInt("00", 16)
				});
		
		Assert.assertThat(result, is(instanceOf(PointEvent.class)));
		Assert.assertThat(((PointEvent) result).getMultiplier(), is(2));
		Assert.assertThat(((PointEvent) result).getBaseNumber(), is(2));
	}
	
	@Test
	public void shouldCreateTripleHit() {
		InteractionEvent result = handler.createEvent(new int[] {
				Integer.parseInt("02", 16), Integer.parseInt("73", 16),
				Integer.parseInt("00", 16), Integer.parseInt("00", 16),
				Integer.parseInt("00", 16), Integer.parseInt("00", 16),
				Integer.parseInt("00", 16)
				});
		
		Assert.assertThat(result, is(instanceOf(PointEvent.class)));
		Assert.assertThat(((PointEvent) result).getMultiplier(), is(3));
		Assert.assertThat(((PointEvent) result).getBaseNumber(), is(19));
	}
	
	@Test
	public void shouldCreateBullsEyeHit() {
		InteractionEvent result = handler.createEvent(new int[] {
				Integer.parseInt("02", 16), Integer.parseInt("39", 16),
				Integer.parseInt("00", 16), Integer.parseInt("00", 16),
				Integer.parseInt("00", 16), Integer.parseInt("00", 16),
				Integer.parseInt("00", 16)
				});
		
		Assert.assertThat(result, is(instanceOf(PointEvent.class)));
		Assert.assertThat(((PointEvent) result).getMultiplier(), is(1));
		Assert.assertThat(((PointEvent) result).getBaseNumber(), is(25));
	}
	
	
	@Test
	public void shouldCreateBounceOut() {
		InteractionEvent result = handler.createEvent(new int[] {
				Integer.parseInt("02", 16), Integer.parseInt("04", 16),
				Integer.parseInt("00", 16), Integer.parseInt("00", 16),
				Integer.parseInt("00", 16), Integer.parseInt("00", 16),
				Integer.parseInt("00", 16)
				});
		
		Assert.assertThat(result, is(instanceOf(UserCausedEvent.class)));
		Assert.assertThat(((UserCausedEvent) result).getType(), is(Type.BOUNCE_OUT));
	}
	
	@Test
	public void shouldCreateDartMissed() {
		InteractionEvent result = handler.createEvent(new int[] {
				Integer.parseInt("02", 16), Integer.parseInt("03", 16),
				Integer.parseInt("00", 16), Integer.parseInt("00", 16),
				Integer.parseInt("00", 16), Integer.parseInt("00", 16),
				Integer.parseInt("00", 16)
				});
		
		Assert.assertThat(result, is(instanceOf(UserCausedEvent.class)));
		Assert.assertThat(((UserCausedEvent) result).getType(), is(Type.DART_MISSED));
	}
	
	@Test
	public void shouldCreateNextPlayer() {
		InteractionEvent result = handler.createEvent(new int[] {
				Integer.parseInt("02", 16), Integer.parseInt("01", 16),
				Integer.parseInt("00", 16), Integer.parseInt("00", 16),
				Integer.parseInt("00", 16), Integer.parseInt("00", 16),
				Integer.parseInt("00", 16)
				});
		
		Assert.assertThat(result, is(instanceOf(UserCausedEvent.class)));
		Assert.assertThat(((UserCausedEvent) result).getType(), is(Type.NEXT_PLAYER));
	}
	
}
