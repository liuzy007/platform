package com.alibaba.napoli.client.async;

import static org.junit.Assert.*;

import com.alibaba.napoli.domain.client.ClientDestination;
import com.alibaba.napoli.domain.client.ClientQueue;
import com.alibaba.napoli.domain.client.ClientVirtualTopic;
import java.io.File;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.napoli.AbstractTestBase;
import com.alibaba.napoli.client.NapoliClientException;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.client.model.WorkerFailure;
import com.alibaba.napoli.client.util.NapoliTestUtil;
import com.alibaba.napoli.common.constants.NapoliConstant;
import com.alibaba.napoli.common.persistencestore.KVStore;
import com.alibaba.napoli.common.util.HttpUtil;


public class EmptyQueueTopicTest extends AbstractTestBase {

    @Before
    public void setup() {
        connector = new NapoliConnector();
        connector.setAddress(NapoliTestUtil.getAddress());
        connector.setStorePath(NapoliTestUtil.getProperty("napoli.func.storePath") + "\\" + new Date().hashCode());
        logger.info("yanny connector store path is " + connector.getStorePath());

        connector.setSendTimeout(1000 * 60 * 2);
        System.out.println("yanny before init");
        connector.init();
    }

    @After
    public void stop() throws NapoliClientException {
        connector.close();
        super.stop();
    }

    @Test
    public void sendToEmptyTopic() {
        String topicName = "NapoliEmptyTopic";

        // verify the topic has no queue under it.
        // TODO: later we could auto create an empty topic.
        ClientDestination dest = connector.getConfigService().fetchDestination(topicName);
        if (dest == null) {
            fail("the test need " + topicName + " be an empty topic, now it doesn't exist");
        } else if (dest instanceof ClientVirtualTopic) {
            ClientVirtualTopic topic = (ClientVirtualTopic) dest;
            assertTrue(topicName + " has " + topic.getClientQueueList().size() + " queues, the test need an empty topic",
                    topic.getClientQueueList().size() == 0);
        } else {
            fail("the test need " + topicName + " be an empty topic, now it's not a topic");
        }

        DefaultAsyncSender sender = new DefaultAsyncSender();

        sender.setName(topicName);
        sender.setConnector(connector);
        sender.setStoreEnable(true);

        try {
            sender.init();
        } catch (NapoliClientException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        int count = 5;
        int success = 0;
        for (int i = 0; i < count; i++) {
            boolean result = sender.send("message" + i);
            if (result) {
                success++;
            }
        }

        // verify send all failed since no queue in the loop, not enter.
        assertTrue(
                "All message send to topic without any queue under it should return false even though store enable=true",
                success == 0);
    }

    @Test
    public void sendToTopicWithEmptyQueueWithStoreEnable() {

        String topicName = "NapoliTopicWithEmptyQueue";

        // verify the topic has no queue under it.
        // TODO: later we could auto create an empty topic.
        ClientDestination dest = connector.getConfigService().fetchDestination(topicName);
        ClientVirtualTopic topic = null;

        if (dest == null) {
            fail("the test need " + topicName + " be a topic with empty queue, now it doesn't exist");
        } else if (dest instanceof ClientVirtualTopic) {
            topic = (ClientVirtualTopic) dest;
            assertTrue(topicName + " has no queues, the test need a topic with empty queues", topic.getClientQueueList()
                    .size() > 0);
            int topicQueueMachineSize = 0;
            for (ClientQueue queue :topic.getClientQueueList()){
                topicQueueMachineSize +=queue.getMachineSet().size();
            }
            assertTrue(topicName + " has queues with physical queues, the test need a topic with empty queues",
                    topicQueueMachineSize == 0);
        } else {
            fail("the test need " + topicName + " be an empty topic, now it's not a topic");
        }

        DefaultAsyncSender sender = new DefaultAsyncSender();

        sender.setName(topicName);
        sender.setConnector(connector);
        sender.setStoreEnable(true);

        try {
            sender.init();
        } catch (NapoliClientException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        int count = 5;
        int success = 0;
        for (int i = 0; i < count; i++) {
            boolean result = sender.send("message" + i);
            if (result) {
                success++;
            }
        }

        // verify send all succeed and the messages are stored locally.
        assertTrue(
                "All message send to topic without any queue under it should return true with store enable=true, but only "
                        + success + " out of total " + count + " return true", success == count);
        String errorMessage = "";

        for (ClientQueue queue : topic.getClientQueueList()) {
            String name = queue.getName();
            KVStore store = connector.getSenderKVStore(name);

            if (store == null) {
                errorMessage += ";queue " + name + "'s kvstore is null";
            } else {
                if (store.getStoreSize() != success) {

                    errorMessage += "; queue" + name + "'s local kvstore size is not correct, expected " + success
                            + " get " + store.getStoreSize();
                }

                // avoid impact next test.
                store.clear();
            }
        }

        assertTrue("verify local store get error message:" + errorMessage, errorMessage.equals(""));
    }

    @Test
    public void sendToTopicWithEmptyQueueWithStoreDisabled() {

        String topicName = "NapoliTopicWithEmptyQueue";

        // verify the topic has no queue under it.
        // TODO: later we could auto create an empty topic.
        ClientDestination dest = connector.getConfigService().fetchDestination(topicName);
        ClientVirtualTopic topic = null;

        if (dest == null) {
            fail("the test need " + topicName + " be a topic with empty queue, now it doesn't exist");
        } else if (dest instanceof ClientVirtualTopic) {
            topic = (ClientVirtualTopic) dest;
            assertTrue(topicName + " has no queues, the test need a topic with empty queues", topic.getClientQueueList()
                    .size() > 0);
            int topicQueueMachineSize = 0;
            for (ClientQueue queue :topic.getClientQueueList()){
                topicQueueMachineSize +=queue.getMachineSet().size();
            }
            assertTrue(topicName + " has queues with physical queues, the test need a topic with empty queues",
                    topicQueueMachineSize == 0);
        } else {
            fail("the test need " + topicName + " be an empty topic, now it's not a topic");
        }

        DefaultAsyncSender sender = new DefaultAsyncSender();

        sender.setName(topicName);
        sender.setConnector(connector);
        sender.setStoreEnable(false);

        try {
            sender.init();
        } catch (NapoliClientException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        int count = 5;
        int success = 0;
        for (int i = 0; i < count; i++) {
            boolean result = sender.send("message" + i);
            if (result) {
                success++;
            }
        }

        // verify send all succeed and the messages are stored locally.
        assertTrue(
                "All message send to topic without any queue under it should return false with store enable=false, but it get "
                        + success + " success out of total " + count + " return true", success == 0);
        String errorMessage = "";

        for (ClientQueue queue : topic.getClientQueueList()) {
            String name = queue.getName();

            String pathKey = connector.getStorePath() + "/" + NapoliConstant.CLIENT_DOMAIN_ASYNC + "-"
                    + NapoliConstant.CLIENT_TYPE_SENDER + "-" + name;

            File f = new File(pathKey);

            if (f.exists()) {
                errorMessage += ";queue " + name + "'s kvstore should not be created since store is disabled";

                KVStore store = connector.getSenderKVStore(name);

                if (store != null) {

                    if (store.getStoreSize() > 0) {

                        errorMessage += "; queue" + name + "'s local kvstore size is not correct, expected 0 get "
                                + store.getStoreSize();
                    }

                    // avoid impact next test.
                    store.clear();
                }
            }
        }

        assertTrue("verify local store get error message:" + errorMessage, errorMessage.equals(""));
    }

    @Test
    public void sendToEmptyQueueWithStoreEnable() {

        String queueName = "NapoliEmptyQueue_" + System.currentTimeMillis();
        HttpUtil.createQueue(NapoliTestUtil.getAddress(), queueName, "");
        logger.info("test sendToEmptyQueueWithStoreEnable with queueName=" + queueName);

        DefaultAsyncSender sender = new DefaultAsyncSender();

        try {
            // verify the topic has no queue under it.

            ClientDestination dest = connector.getConfigService().fetchDestination(queueName);
            ClientQueue queue = null;

            if (dest == null) {
                fail("the test need " + queueName + " be an empty queue, now it doesn't exist");
            } else if (dest instanceof ClientQueue) {
                queue = (ClientQueue) dest;

                assertTrue(queueName + " has queues with physical queues, the test need an empty queues", queue
                        .getMachineSet().size() == 0);
            } else {
                fail("the test need " + queueName + " be an empty queue, now it's not a queue");
            }

            sender.setName(queueName);
            sender.setConnector(connector);
            sender.setStoreEnable(true);

            try {
                sender.init();
            } catch (NapoliClientException e) {
                e.printStackTrace();
                fail(e.getMessage());
            }
            int count = 5;
            int success = 0;
            for (int i = 0; i < count; i++) {
                boolean result = sender.send("message" + i);
                if (result) {
                    success++;
                }
            }

            // verify send all succeed and the messages are stored locally.
            assertTrue("All message send to empty queue should return true with store enable=true, but only " + success
                    + " out of total " + count + " return true", success == count);
            String errorMessage = "";

            KVStore store = connector.getSenderKVStore(queueName);

            if (store == null) {
                errorMessage += ";queue " + queueName + "'s kvstore is null";
            } else {
                if (store.getStoreSize() != success) {

                    errorMessage += "; queue" + queueName + "'s local kvstore size is not correct, expected " + success
                            + " get " + store.getStoreSize();
                }

                // avoid impact next test.
                store.clear();
            }

            assertTrue("verify local store get error message:" + errorMessage, errorMessage == "");
        } finally {
            sender.close();
            HttpUtil.deleteQueue(NapoliTestUtil.getAddress(), queueName);
        }
    }

    @Test
    public void sendToEmptyQueueWithStoreDisabled() {

        String queueName = "NapoliEmptyQueue_" + System.currentTimeMillis();
        HttpUtil.createQueue(NapoliTestUtil.getAddress(), queueName, "");
        logger.info("test sendToEmptyQueueWithStoreDisabled with queueName=" + queueName);
        DefaultAsyncSender sender = new DefaultAsyncSender();

        try {
            // verify the topic has no queue under it.
            // TODO: later we could auto create an empty topic.
            ClientDestination dest = connector.getConfigService().fetchDestination(queueName);
            ClientQueue queue = null;

            if (dest == null) {
                fail("the test need " + queueName + " be an empty queue, now it doesn't exist");
            } else if (dest instanceof ClientQueue) {
                queue = (ClientQueue) dest;

                assertTrue(queueName + " has queues with physical queues, the test need an empty queues", queue
                        .getMachineSet().size() == 0);
            } else {
                fail("the test need " + queueName + " be an empty queue, now it's not a queue");

            }

            sender.setName(queueName);
            sender.setConnector(connector);
            sender.setStoreEnable(false);

            try {
                sender.init();
            } catch (NapoliClientException e) {
                e.printStackTrace();
                fail(e.getMessage());
            }
            int count = 5;
            int success = 0;
            for (int i = 0; i < count; i++) {
                boolean result = sender.send("message" + i);
                if (result) {
                    success++;
                }
            }

            // verify send all succeed and the messages are stored locally.
            assertTrue("All message send to empty queue should return false with store enable=false, but it get "
                    + success + " success out of total " + count + " return true", success == 0);
            String errorMessage = "";

            String pathKey = connector.getStorePath() + "/" + NapoliConstant.CLIENT_DOMAIN_ASYNC + "-"
                    + NapoliConstant.CLIENT_TYPE_SENDER + "-" + queueName;

            File f = new File(pathKey);

            if (f.exists()) {
                errorMessage += ";queue " + queueName + "'s kvstore should not be created since store is disabled";

                KVStore store = connector.getSenderKVStore(queueName);

                if (store != null) {
                    errorMessage += ";queue " + queueName + "'s kvstore should not be created since store is disabled";

                    if (store.getStoreSize() > 0) {

                        errorMessage += "; queue" + queueName + "'s local kvstore size is not correct, expected 0 get "
                                + store.getStoreSize();
                    }

                    // avoid impact next test.
                    store.clear();
                }
            }

            assertTrue("verify local store get error message:" + errorMessage, errorMessage.equals(""));
        } finally {
            sender.close();
            HttpUtil.deleteQueue(NapoliTestUtil.getAddress(), queueName);
        }
    }

    @Test
    //bug NP-196
    public void receiveFromEmptyQueue() {
        String queueName = "NapoliEmptyQueue_" + System.currentTimeMillis();
        HttpUtil.createQueue(NapoliTestUtil.getAddress(), queueName, "");
        logger.info("test receiveFromEmptyQueue with queueName=" + queueName);
        DefaultAsyncReceiver receiver = new DefaultAsyncReceiver();
        try {
            // verify the topic has no queue under it.
            // TODO: later we could auto create an empty topic.
            ClientDestination dest = connector.getConfigService().fetchDestination(queueName);
            ClientQueue queue = null;

            if (dest == null) {
                fail("the test need " + queueName + " be an empty queue, now it doesn't exist");
            } else if (dest instanceof ClientQueue) {
                queue = (ClientQueue) dest;

                assertTrue(queueName + " has queues with physical queues, the test need an empty queues", queue
                        .getMachineSet().size() == 0);
            } else {
                fail("the test need " + queueName + " be an empty queue, now it's not a queue");

            }

            receiver.setConnector(connector);
            receiver.setName(queueName);
            receiver.setInstances(5);

            receiver.setStoreEnable(true);
            receiver.setReprocessInterval(1000);
            WorkerFailure worker = new WorkerFailure();
            receiver.setWorker(worker);
            /***
             * try { receiver.setWorker(worker); } catch (NapoliClientException
             * e) { // TODO Auto-generated catch block e.printStackTrace();
             * fail(e.getMessage()); }
             ***/
            try {
                receiver.init();
                receiver.start();
                NapoliTestUtil.sleep(150);

            } catch (NapoliClientException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                fail(e.getMessage());
            }
            assertTrue("should have no receiver instance since the queue is empty queue", receiver.getInstances() == 0);
        } finally {
            receiver.close();
            HttpUtil.deleteQueue(NapoliTestUtil.getAddress(), queueName);
        }
    }
}
