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
package spare.n52.yadarts.dummy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import spare.n52.yadarts.entity.InteractionEvent;
import spare.n52.yadarts.entity.impl.ButtonEvent;
import spare.n52.yadarts.entity.impl.HitEvent;
import spare.n52.yadarts.event.EventListener;
import spare.n52.yadarts.event.EventProducer;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class ConsoleEventProducer implements EventProducer {
    
    private List<EventListener> listeners = new ArrayList<>();
    
    private boolean running = true;
    
    protected static List<InteractionEvent> eventQueue;
    private BufferedReader reader;
    
    
    @Override
    public void registerEventListener(EventListener el) {
        this.listeners.add(el);
    }
    
    @Override
    public void removeEventListener(EventListener el) {
        this.listeners.remove(el);
    }
    
    @Override
    public void start() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        reader = new BufferedReader(inputStreamReader);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (running) {
                    System.out.println("Enter event [sx, dx, tx, next, miss]:");
                    String input;
                    try {
                        input = reader.readLine().trim();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        return;
                    }
                    if (input.isEmpty()) {
                        continue;
                    }

                    char first = input.charAt(0);
                    if (first == 's' || first == 'd' || first == 't') {
                        try {
                            int num = Integer.parseInt(input.substring(1, input.length()));
                            if (num <= 0 || num > 25 || (num > 20 && num < 25)) {
                                System.out.println("number out of range");
                            }
                            switch (first) {
                                case 's':
                                    sendEvent(HitEvent.singleHitInner(num));
                                    break;
                                case 'd':
                                    sendEvent(HitEvent.doubleHit(num));
                                    break;
                                case 't':
                                    sendEvent(HitEvent.tripleHit(num));
                                    break;
                                default:
                                    break;
                            }
                        }
                        catch (NumberFormatException e) {
                            System.out.println("invalid number: "+input);
                        }
                    }
                    else if (input.equals("next")) {
                        sendEvent(ButtonEvent.nextPlayer());
                    }
                    else if (input.equals("miss")) {
                        sendEvent(ButtonEvent.dartMissed());
                    }
                }
            }
        }).start();
    }
    
    protected void sendEvent(InteractionEvent ie) {
        if (!running) {
            return;
        }
        
        for (EventListener el : this.listeners) {
            try {
                el.receiveEvent(ie);
            }
            catch (RuntimeException e) {
            }
            
            if (!running) {
                break;
            }
        }
    }
    
    
    @Override
    public void stop() throws IOException {
        this.running = false;
        if (reader != null) {
            reader.close();
        }
    }
    
}
