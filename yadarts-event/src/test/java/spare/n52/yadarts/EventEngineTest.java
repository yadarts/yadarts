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
package spare.n52.yadarts;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import spare.n52.yadarts.entity.InteractionEvent;
import spare.n52.yadarts.event.EventListener;
import spare.n52.yadarts.event.EventProducer;

public class EventEngineTest {
	
	@Mock
	EventProducer mockProducer;
	
	@Before
	public void initProducer() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testBasicWorkflow() throws InitializationException, InterruptedException {
		EventEngine.instance();
		
		Thread.sleep(2000);
		
		Assert.assertTrue("Exepcted list to be empty", DummyListener.expected.isEmpty());
		
		EventEngine.instance().shutdown();
	}
	
	public static class DummyListener implements EventListener {

		static List<InteractionEvent> expected;
		
		public DummyListener() {
			List<InteractionEvent> q = DummyEventProducer.cyclicQueue();
			synchronized (DummyListener.class) {
				if (expected == null) {
					 expected = new ArrayList<>(q);
				}
			}
		}
		
		@Override
		public void receiveEvent(InteractionEvent event) {
			expected.remove(event);
		}

		
	}
	
	
}
