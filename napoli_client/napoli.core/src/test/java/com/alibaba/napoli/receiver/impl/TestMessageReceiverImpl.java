package com.alibaba.napoli.receiver.impl;

import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.connector.NapoliConnector;
import junit.framework.TestCase;

import com.alibaba.napoli.client.mock.EmbededConnector;
import com.alibaba.napoli.client.mock.MockMachine;


/**
 * default msg receiver testing
 * 
 * @author <a href="mailto:wenjiong.caowj@alibaba-inc.com">Laurence Cao</a> Oct 2009
 */
public class TestMessageReceiverImpl extends TestCase {

  
    protected String                queueName;
    protected MockMachine           machine;
    //protected MessageReceiverImpl   impl;
    
    protected DefaultAsyncReceiver receiver= new DefaultAsyncReceiver();

    NapoliConnector connector = new EmbededConnector();    
    
    protected void setUp() throws Exception {
        queueName = "dummy";
        machine = new MockMachine();     
        receiver.setConnector(connector);
        //impl = new MessageReceiverImpl(queueName, machine, receiver);        
    }

    protected void tearDown() throws Exception {
    }  
   
    /**
     * works correctly of instance counting and instance adjusting
     * 
     * <PRE>
     * 1. initial instance count
     * 2. adjust and check instance
     * 3. restart adjust and check
     * </PRE>
     */
    public void testInstanceCount() {
        /*impl.init();
        impl.start();
        assertEquals(1, impl.getInstance());
        int inst = (int) (Math.random() * 100);
        impl.setInstance(inst);
        impl.heartBeat();
        assertEquals(inst, impl.getInstance());

        inst = (int) (Math.random() * 100);
        impl.setInstance(inst);
        impl.heartBeat();
        assertEquals(inst, impl.getInstance());*/
    }

    /**
     * idle function
     */
    public void testIdle() {
        /*impl.init();
        impl.start();
        int instance = 20;
        impl.setInstance(instance);
        impl.heartBeat();
        impl.setIdlePeriod(1000); // 1s idle
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        impl.heartBeat();
        assertEquals(instance, impl.consumers.size());*/
    }

    /**
     * sessions creation and destruction
     */
    public void testSessionsLifeCycle() {
        /*impl.init();
        int dest = 10;
        impl.setInstance(1);
        impl.start();

        impl.heartBeat();
        impl.setInstance(dest);
        impl.heartBeat();
        assertEquals(dest, impl.consumers.size());

        impl.setInstance(1);
        impl.heartBeat();
        assertEquals(1, impl.consumers.size());*/
    }

    /**
     * receiver life cycle of opening and closing
     */
    public void testReceiverLifeCycle() {
        //if dest =0, the real output will be 1 which is by design
        /*int dest = (int) (Math.random() * 100) + 1;
        impl.setInstance(dest);
        impl.init();
        impl.start();
        impl.heartBeat();
        assertEquals(dest,  impl.consumers.size());
        impl.stop();
        assertEquals(0,  impl.consumers.size());*/
    }

    // session maintain already taken so no need of heartBeat()

    /**
     * exception on receiver working abnormal
     */
    public void testAbnormal() {
        int dest = 10;
        /*impl.setInstance(dest);
        impl.init();
        impl.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        MockNapoliConsumer.abnormal = true;
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(dest,  impl.consumers.size());*/
    }
}
