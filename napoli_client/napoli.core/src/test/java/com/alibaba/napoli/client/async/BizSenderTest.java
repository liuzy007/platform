package com.alibaba.napoli.client.async;

import com.alibaba.napoli.AbstractTestBase;
import com.alibaba.napoli.client.NapoliClientException;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alibaba.napoli.client.model.MyWorker;
import com.alibaba.napoli.client.util.NapoliTestUtil;
import com.alibaba.napoli.common.constants.NapoliConstant;
import com.alibaba.napoli.common.util.HttpUtil;
import com.alibaba.napoli.common.util.JmxUtil;
import java.util.ArrayList;
import java.util.Date;
import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.matchers.StringContains;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 测试事务消息中的回调函数
 * 
 * @author munch.wangr
 */
public class BizSenderTest extends AbstractTestBase {
    private static final Log log  = LogFactory.getLog("BizSenderTest");

    static Date              time = new Date();
    static String            suffix;
    NapoliMessage            nm;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        queueName = "BizSenderTest_" + System.currentTimeMillis();

        HttpUtil.createQueue(NapoliTestUtil.getAddress(), queueName,
                NapoliTestUtil.getProperty("napoli.func.amqGroupName"));

        logger.info("yanny: BizSenderTest started with queueName=" + queueName);

        AbstractTestBase.setUpBeforeClass();

        connector.setPoolSize(5);
        connector.setSendTimeout(1000 * 60 * 2);
        log.info("yanny before init");
        connector.init();

        suffix = time.toString();

        AbstractTestBase.getQueueConsumerSessionCount();
    }

    @Before
    public void start() throws Exception {
        JmxUtil.deleteAllMessage(NapoliTestUtil.getAddress(), queueName);
    }

    @Test
    public void testSendSuccessWithoutPendingNotify() {
        DefaultAsyncSender sender = new DefaultAsyncSender();
        DefaultAsyncReceiver receiver = new DefaultAsyncReceiver();

        long beginQueueSize = JmxUtil.getQueueSize(NapoliTestUtil.getAddress(), queueName);

        int count = 15;
        try {

            sender.setConnector(connector);

            sender.setName(queueName);
            sender.init();

            receiver.setConnector(connector);
            receiver.setName(queueName);
            receiver.setInstances(1);
            receiver.setStoreEnable(true);
            MyWorker worker = new MyWorker();
            worker.logEnabled = true;

            receiver.setWorker(worker);
            receiver.init();
            log.info("yanny before receiver start");
            receiver.start();

            NapoliTestUtil.setExpectedSessionCount(receiver);

            worker.reset();

            long startCount = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), queueName);

            Runnable bizCall = new Runnable() {

                public void run() {
                    log.info(new Date().toString() + " yanny callback succeed");
                }
            };

            nm = new NapoliMessage("Succeed_" + suffix);

            ArrayList<String> messages = new ArrayList<String>();
            for (int i = 0; i < count; i++) {
                nm = new NapoliMessage("Succeed_" + suffix + "_" + i);

                messages.add(nm.getContent().toString());
                sender.send(nm, bizCall);
            }

            JmxUtil.waitTillQueueSizeAsTarget(NapoliTestUtil.getAddress(), queueName, beginQueueSize);

            long endCount = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), queueName);

            log.info(new Date().toString() + " end - start = " + (endCount - startCount));

            log.info("consumer doWork() called " + worker.getAccessNum());
            String errorMessage = "";

            if (endCount != startCount + count) {
                errorMessage += "with bizcall success, the message should be consumable quickly, but start is "
                        + startCount + "; end is " + endCount + " expected to be " + (startCount + count);
            }

            if (worker.getAccessNum() != count) {
                errorMessage += ";consumer doWork() should only be called " + count + " but was called "
                        + worker.getAccessNum();
            }

            //verify the message is correct
            for (String message : messages) {
                if (!worker.getReceivedIds().contains(message)) {
                    errorMessage += "; message " + message + " doesn't exist in consume message lists";
                }
            }

            for (String message : worker.getReceivedIds()) {
                if (!messages.contains(message)) {
                    errorMessage += "; message " + message + " is consumed but not been sent.";
                }
            }

            assertTrue(errorMessage, NapoliTestUtil.isBlank(errorMessage));

        } catch (Exception e) {
            log.info("yanny exception happened");
            e.printStackTrace();

            Assert.fail("yanny exception happened " + e.getMessage());
        } finally {
            receiver.close();
        }

    }

    @Test
    public void testSendFailureWithoutPendingNotify() {
        DefaultAsyncReceiver receiver = new DefaultAsyncReceiver();
        DefaultAsyncSender sender = new DefaultAsyncSender();
        long beginQueueSize = 0;
        long startCount = 0;

        MyWorker worker = new MyWorker();
        //int count = 5;

        try {

            sender.setConnector(connector);
            log.info("yanny sender before pendingNotify");

            sender.setName(queueName);
            sender.init();

            beginQueueSize = JmxUtil.getQueueSize(NapoliTestUtil.getAddress(), queueName);

            receiver.setConnector(connector);
            receiver.setName(queueName);
            receiver.setInstances(1);
            receiver.setStoreEnable(true);

            receiver.setWorker(worker);
            receiver.init();
            receiver.start();

            NapoliTestUtil.setExpectedSessionCount(receiver);

            startCount = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), queueName);

            final RuntimeException t = new RuntimeException();
            Runnable bizCall = new Runnable() {

                public void run() {
                    log.info(new Date().toString() + " yanny callback failed");

                    throw t;
                }
            };

            nm = new NapoliMessage("Failed_" + suffix);

            sender.send(nm, bizCall);

        } catch (BizInvokationException e) {
            System.err.println("yanny:" + e.getCause());
            e.printStackTrace();

            assertThat("yanny assertion failed " + e.getMessage(), new StringContains("BizCall Error Happened!"));

            // 失败后，应该在pendingTaskTime中被rollback掉，but since PendingNotify is not set, the scheduled task is not started, the message should stay in queue for ever (until other sender with pendingTask rollback this message.

            try {
                Thread.sleep(1000 * 5);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            long endCount = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), queueName);

            long endQueueSize = JmxUtil.getQueueSize(NapoliTestUtil.getAddress(), queueName);

            log.info("yanny start count is " + startCount + " end count is " + endCount);
            assertEquals("Bizcallback exception, the message should be rollback. startCount=" + startCount
                    + "+1 should equal to endCount=" + endCount, startCount, endCount);

            assertEquals(
                    "Bizcallback exception, but pendingNotify not set, the message should stuck in queue. beginQueueSize="
                            + beginQueueSize + "+1 should equal to endQueueSize=" + endQueueSize, beginQueueSize,
                    endQueueSize);

            assertEquals("consumer doWork() should not be called at all", 0, worker.getAccessNum());

            DefaultAsyncSender sender2 = new DefaultAsyncSender();

            sender2.setConnector(connector);

            sender2.setPendingInterval(1000 * 3);
            log.info("yanny sender before pendingNotify");
            sender2.setPendingNotifier(new PendingNotify() {

                public PendingNotifyStateEnum notify(NapoliMessage message) {
                    log.info(new Date().toString() + " yanny: call pendingNotifiy for" + message);

                    try {
                        throw new Exception("yanny PendingNotifyStateEnum stacktrace");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return PendingNotifyStateEnum.COMMIT;
                }

            });

            sender2.setName(queueName);

            try {
                sender2.init();
                //The PendingNotify function doesn't matter. rollback biz call failed message only need the schedule task.

                Thread.sleep(1000 * 5);

                endQueueSize = JmxUtil.getQueueSize(NapoliTestUtil.getAddress(), queueName);
                assertEquals("Bizcallback exception, the message should be rollback. beginQueueSize=" + beginQueueSize
                        + " should equal to endQueueSize=" + endQueueSize, beginQueueSize, endQueueSize);

                assertEquals("consumer doWork() should not be called at all", 0, worker.getAccessNum());

            } catch (Exception ex) {
                ex.printStackTrace();
                fail("error happened " + ex.getMessage());
            } finally {
                sender2.close();
            }

        } catch (Exception e) {
            log.info("yanny exception happened");
            e.printStackTrace();

            Assert.fail("yanny exception happened " + e.getMessage());
        } finally {
            receiver.close();
            sender.close();
        }
    }

    @Test
    public void testSendWithCallbackSucceed() {

        DefaultAsyncReceiver receiver = new DefaultAsyncReceiver();
        DefaultAsyncSender sender = new DefaultAsyncSender();
        int count = 15;
        try {
            sender.setConnector(connector);

            sender.setPendingInterval(1000 * 10);
            log.info("yanny sender before pendingNotify");
            sender.setPendingNotifier(new PendingNotify() {

                public PendingNotifyStateEnum notify(NapoliMessage message) {
                    log.info(new Date().toString() + " yanny: call pendingNotifiy for" + message);

                    try {
                        throw new Exception("yanny PendingNotifyStateEnum stacktrace");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return PendingNotifyStateEnum.COMMIT;
                }

            });
            sender.setPendingBatch(100);
            sender.setPendingInterval(60 * 1000);
            sender.setPendingTimeout(30 * 1000);
            sender.setName(queueName);
            sender.init();

            log.info("yanny before receiver");
            long beginQueueSize = JmxUtil.getQueueSize(NapoliTestUtil.getAddress(), queueName);

            receiver.setConnector(connector);
            receiver.setName(queueName);
            receiver.setInstances(1);
            receiver.setStoreEnable(true);
            MyWorker worker = new MyWorker();
            worker.logEnabled = true;

            receiver.setWorker(worker);
            receiver.init();
            log.info("yanny before receiver start");
            receiver.start();

            NapoliTestUtil.setExpectedSessionCount(receiver);

            JmxUtil.waitTillQueueSizeAsTarget(NapoliTestUtil.getAddress(), queueName, beginQueueSize);

            worker.reset();

            long startCount = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), queueName);

            Runnable bizCall = new Runnable() {

                public void run() {

                    log.info(new Date().toString() + " yanny callback succeed");
                }
            };

            ArrayList<String> messages = new ArrayList<String>();
            for (int i = 0; i < count; i++) {
                sender.send("Succeed_" + suffix + "_" + i, bizCall);
                messages.add("Succeed_" + suffix + "_" + i);
            }

            JmxUtil.waitTillQueueSizeAsTarget(NapoliTestUtil.getAddress(), queueName, beginQueueSize);

            long endCount = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), queueName);

            log.info(new Date().toString() + " end - start = " + (endCount - startCount));

            log.info("consumer doWork() called " + worker.getAccessNum());

            String errorMessage = "";

            if (endCount != startCount + count) {
                errorMessage += "with bizcall success, the message should be consumable quickly, but start is "
                        + startCount + "; end is " + endCount + " expected to be " + (startCount + count);
            }

            if (worker.getAccessNum() != count) {
                errorMessage += ";consumer doWork() should only be called " + count + " but was called "
                        + worker.getAccessNum();
            }

            //verify the message is correct
            for (String message : messages) {
                if (!worker.getReceivedIds().contains(message)) {
                    errorMessage += "; message " + message + " doesn't exist in consume message lists";
                }
            }

            for (String message : worker.getReceivedIds()) {
                if (!messages.contains(message)) {
                    errorMessage += "; message " + message + " is consumed but not been sent.";
                }
            }

            assertTrue(errorMessage, NapoliTestUtil.isBlank(errorMessage));

        } catch (Exception e) {
            log.info("yanny exception happened");
            e.printStackTrace();

            Assert.fail("yanny exception happened " + e.getMessage());
        } finally {
            sender.close();
            receiver.close();
        }
    }

    @Test
    public void testSendWithCallbackFailed() {
        DefaultAsyncReceiver receiver = new DefaultAsyncReceiver();
        DefaultAsyncSender sender = new DefaultAsyncSender();
        long beginQueueSize = 0;
        long startCount = 0;
        MyWorker worker = new MyWorker();

        final RuntimeException t = new RuntimeException();
        Runnable bizCall = new Runnable() {

            public void run() {
                log.info(new Date().toString() + " yanny callback failed");

                throw t;
            }
        };

        try {

            sender.setConnector(connector);

            sender.setPendingInterval(1000 * 2);
            log.info("yanny sender before pendingNotify");
            sender.setPendingNotifier(new PendingNotify() {

                public PendingNotifyStateEnum notify(NapoliMessage message) {
                    log.info(new Date().toString() + " yanny: call pendingNotifiy for" + message);

                    try {
                        throw new Exception("yanny PendingNotifyStateEnum stacktrace");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return PendingNotifyStateEnum.ROLLBACK;
                }

            });

            sender.setName(queueName);
            sender.init();

            beginQueueSize = JmxUtil.getQueueSize(NapoliTestUtil.getAddress(), queueName);

            receiver.setConnector(connector);
            receiver.setName(queueName);
            receiver.setInstances(1);
            receiver.setStoreEnable(true);

            receiver.setWorker(worker);
            receiver.init();
            receiver.start();

            NapoliTestUtil.setExpectedSessionCount(receiver);

            startCount = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), queueName);

            nm = new NapoliMessage("Failed_testSendWithCallbackFailed");
            nm.setProperty(NapoliMessage.MSG_PROP_KEY_QUEUENAME, queueName);

            sender.send(nm, bizCall);

            fail("send should fail");

        } catch (BizInvokationException e) {
            System.err.println("yanny:" + e.getCause());
            e.printStackTrace();

            assertThat("yanny assertion failed " + e.getMessage(), new StringContains("BizCall Error Happened!"));

            // 失败后，应该在pendingTaskTime中被rollback掉，这个task默认每10分钟执行一次，用sender.setPendingInternval修改为10秒，所以等15秒

            try {
                Thread.sleep(1000 * 4);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            long endCount = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), queueName);

            // JmxUtil.waitTillQueueSizeAsTarget(NapoliTestUtil.getAddress(), queueName, beginQueueSize);
            JmxUtil.waitTillQueueSizeAsTarget(NapoliTestUtil.getAddress(), queueName, 0);

            log.info("yanny start count is " + startCount + " end count is " + endCount);
            assertEquals("Bizcallback exception, the message should be rollback. startCount=" + startCount
                    + "+1 should equal to endCount=" + endCount, startCount, endCount);

            assertEquals("consumer doWork() should not be called at all", 0, worker.getAccessNum());

            startCount = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), queueName);

            try {
                log.info("yanny send second failure message");
                sender.send("fail message 2", bizCall);
            } catch (BizInvokationException ex) {
                System.err.println("yanny:" + ex.getCause());
                e.printStackTrace();

                assertThat("yanny assertion failed " + e.getMessage(), new StringContains("BizCall Error Happened!"));

                // 失败后，应该在pendingTaskTime中被rollback掉，这个task默认每10分钟执行一次，用sender.setPendingInternval修改为10秒，所以等15秒

                try {
                    Thread.sleep(1000 * 4);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                endCount = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), queueName);

                JmxUtil.waitTillQueueSizeAsTarget(NapoliTestUtil.getAddress(), queueName, beginQueueSize);

                log.info("yanny start count is " + startCount + " end count is " + endCount);
                assertEquals("Bizcallback exception, the message should be rollback. startCount=" + startCount
                        + "+1 should equal to endCount=" + endCount, startCount, endCount);

                assertEquals("consumer doWork() should not be called at all", 0, worker.getAccessNum());
            } catch (Exception e3) {
                log.info("yanny exception happened sending second failed message");
                e.printStackTrace();

                Assert.fail("yanny exception happened " + e3.getMessage());
            }

        } catch (Exception e) {
            log.info("yanny exception happened");
            e.printStackTrace();

            Assert.fail("yanny exception happened " + e.getMessage());
        } finally {
            receiver.close();
            sender.close();
        }

    }

    @Test
    public void testSeRndWithCallSlowPendingnotifyCommit() {
        DefaultAsyncSender senderCommit = new DefaultAsyncSender();
        DefaultAsyncReceiver receiver = new DefaultAsyncReceiver();
        NapoliConstant.MAX_IDLE_TIME = 60000 * 30;

        try {

            senderCommit.setConnector(connector);
            senderCommit.setPendingInterval(1000 * 2);
            log.info("sait.xuc sender before pendingNotify commit");
            senderCommit.setPendingNotifier(new PendingNotify() {

                public PendingNotifyStateEnum notify(NapoliMessage message) {
                    log.info(new Date().toString() + " sait.xuc: call pendingNotifiy for" + message);
                    return PendingNotifyStateEnum.COMMIT;
                }

            });

            senderCommit.setName(queueName);
            try {
                senderCommit.init();
            } catch (NapoliClientException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            log.info("sait.xuc before receiver");
            long beginQueueSize = JmxUtil.getQueueSize(NapoliTestUtil.getAddress(), queueName);

            receiver.setConnector(connector);
            receiver.setName(queueName);
            receiver.setInstances(1);
            receiver.setStoreEnable(true);
            MyWorker worker = new MyWorker();
            worker.logEnabled = true;

            try {
                receiver.setWorker(worker);
                receiver.init();
                log.info("sait.xuc before receiver start");
                receiver.start();
            } catch (Exception e) {
                e.printStackTrace();
            }

            NapoliTestUtil.setExpectedSessionCount(receiver);

            JmxUtil.waitTillQueueSizeAsTarget(NapoliTestUtil.getAddress(), queueName, beginQueueSize);
            worker.reset();
            long startCount = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), queueName);

            Runnable bizCall = new Runnable() {

                public void run() {
                    log.info(new Date().toString() + " sait.xuc callback succeed");
                    try {
                        Thread.sleep(12000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            };

            nm = new NapoliMessage("BizCall Commitslow_" + suffix);
            nm.setProperty(NapoliMessage.MSG_PROP_KEY_QUEUENAME, queueName);

            senderCommit.send(nm, bizCall);

            JmxUtil.waitTillQueueSizeAsTarget(NapoliTestUtil.getAddress(), queueName, beginQueueSize);

            long endCount = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), queueName);

            log.info(new Date().toString() + " end - start = " + (endCount - startCount));

            log.info("consumer doWork() called " + worker.getAccessNum());
            Assert.assertEquals("with bizcall success, the message should be consumable quickly, but start is "
                    + startCount + "; end is " + endCount, startCount + 1, endCount);

            assertEquals("consumer doWork() should only be called once", 1, worker.getAccessNum());

        } catch (Exception e) {
            log.info("yanny exception happened");
            e.printStackTrace();

            Assert.fail("yanny exception happened " + e.getMessage());
        } finally {
            receiver.close();
            senderCommit.close();
        }

    }

    @Test
    public void testSendWithCallSlowPendingnotifyRollBack() {
        log.info("sait.xuc before receiver");
        NapoliConstant.MAX_IDLE_TIME = 60000 * 30;

        DefaultAsyncSender senderRollback = new DefaultAsyncSender();
        DefaultAsyncReceiver receiver = new DefaultAsyncReceiver();

        try {
            senderRollback.setConnector(connector);
            // sender.setPendingNotifier(notifier);
            senderRollback.setPendingInterval(1000 * 3);
            log.info("sait.xuc sender before pendingNotify Rollback");
            senderRollback.setPendingNotifier(new PendingNotify() {

                public PendingNotifyStateEnum notify(NapoliMessage message) {
                    log.info(new Date().toString() + " sait.xuc: call pendingNotifiy for" + message);
                    return PendingNotifyStateEnum.ROLLBACK;
                }

            });

            senderRollback.setName(queueName);
            try {
                senderRollback.init();
            } catch (NapoliClientException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            long beginQueueSize = JmxUtil.getQueueSize(NapoliTestUtil.getAddress(), queueName);

            receiver.setConnector(connector);
            receiver.setName(queueName);
            receiver.setInstances(1);
            receiver.setStoreEnable(true);
            MyWorker worker = new MyWorker();
            worker.logEnabled = true;

            try {
                receiver.setWorker(worker);
                receiver.init();
                log.info("sait.xuc before receiver start");
                receiver.start();
            } catch (Exception e) {
                e.printStackTrace();
            }

            NapoliTestUtil.setExpectedSessionCount(receiver);

            JmxUtil.waitTillQueueSizeAsTarget(NapoliTestUtil.getAddress(), queueName, beginQueueSize);
            worker.reset();
            long startCount = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), queueName);

            Runnable bizCall = new Runnable() {

                public void run() {
                    log.info(new Date().toString() + " sait.xuc callback succeed");
                    try {
                        Thread.sleep(15000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            };

            nm = new NapoliMessage("BizCall Rollbackslow_" + suffix);
            nm.setProperty(NapoliMessage.MSG_PROP_KEY_QUEUENAME, queueName);

            senderRollback.send(nm, bizCall);

            JmxUtil.waitTillQueueSizeAsTarget(NapoliTestUtil.getAddress(), queueName, beginQueueSize);

            long endCount = JmxUtil.getEnQueue(NapoliTestUtil.getAddress(), queueName);

            log.info(new Date().toString() + " end - start = " + (endCount - startCount));

            log.info("consumer doWork() called " + worker.getAccessNum());
            Assert.assertEquals("with bizcall success, the message should be consumable quickly, but start is "
                    + startCount + "; end is " + endCount, startCount + 1, endCount);

            assertEquals("consumer doWork() should only be called once", 1, worker.getAccessNum());

        } catch (Exception e) {
            log.info("yanny exception happened");
            e.printStackTrace();

            Assert.fail("yanny exception happened " + e.getMessage());
        } finally {
            receiver.close();
            senderRollback.close();
        }
    }
}
