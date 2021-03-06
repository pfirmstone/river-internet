/* Copyright (c) 2010-2012 Zeus Project Services Pty Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.river.concurrent;

import org.junit.*;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 *
 * @author peter
 */
public class ReferencedQueueTest {
    Queue<String> instance;
    public ReferencedQueueTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        instance = RC.queue(new LinkedList<Referrer<String>>(), Ref.SOFT, 10000L);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of offer method, of class ReferencedQueue.
     */
    @Test
    public void testOffer() {
        System.out.println("offer");
        String e = "offer";
        boolean expResult = true;
        boolean result = instance.offer(e);
        assertEquals(expResult, result);
    }

    /**
     * Test of remove method, of class ReferencedQueue.
     */
    @Test
    public void testRemove() {
        String expResult = "remove";
        System.out.println(expResult);
        instance.add(expResult);
        Object result = instance.remove();
        assertEquals(expResult, result);
    }

    /**
     * Test of poll method, of class ReferencedQueue.
     */
    @Test
    public void testPoll() {
        String expResult = "poll";
        System.out.println(expResult);
        instance.add(expResult);
        String result = instance.poll();
        assertEquals(expResult, result);
    }

    /**
     * Test of element method, of class ReferencedQueue.
     */
    @Test
    public void testElement() {
        String expResult = "element";
        System.out.println(expResult);
        instance.add(expResult);
        Object result = instance.element();
        assertEquals(expResult, result);
    }

    /**
     * Test of peek method, of class ReferencedQueue.
     */
    @Test
    public void testPeek() {
        String expResult = "peek";
        System.out.println(expResult);
        instance.add(expResult);
        Object result = instance.peek();
        assertEquals(expResult, result);
    }
      
    @Test
    public void testEqualsNotOverridden() {
        final Queue<Referrer<String>> queue = new ConcurrentLinkedQueue<Referrer<String>>();
        final ReferencedQueue<String> item1 = new ReferencedQueue<String>(queue, Ref.STRONG, false, 0);
        final ReferencedQueue<String> item2 = new ReferencedQueue<String>(queue, Ref.STRONG, false, 0);
        assertThat(item1, not(equalTo(item2)));
    }
}
