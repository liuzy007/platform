package com.alibaba.napoli.client.benchmark;

import com.alibaba.napoli.client.model.Person;
import com.alibaba.napoli.client.model.PersonStatus;
import com.alibaba.napoli.common.util.JmxUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * User: heyman Date: 6/17/11 Time: 5:40 PM
 */
public class NapoliClientTest extends AbstractNapoliClientTest {

    @Test
    public void mutiSendTest() throws Exception {

        log.info("yanny start to execute NapoliClientTest.mutiSendTest()");

        try {
            int tc = 10;
            final int tp = 20;
            final Semaphore semaphore = new Semaphore(tc);
            final AtomicInteger sumCount = new AtomicInteger();
            final AtomicInteger sumCount1 = new AtomicInteger();
            final AtomicInteger requestCount = new AtomicInteger();
            long startTime = System.currentTimeMillis();
            log.info("Yanny start send request " + startTime);

            for (int i = 0; i < tc; i++) {
                Thread t = new Thread("thread--" + i) {
                    public void run() {
                        try {
                            //构造消息，消息可以是对象、String、Map等，消息对象必须实现Serializable接口
                            semaphore.acquire();
                            for (int j = 0; j < tp; j++) {
                                //      System.out.println("hello");
                                int id = requestCount.incrementAndGet();
                                Person person = new Person();
                                person.setPersonId("" + id);

                                person.setLoginName("superman");
                                person.setEmail("sm@1.com");
                                person.setPenName("pname");
                                person.setStatus(PersonStatus.ENABLED);
                                //建议处理返回值, 发送成功返回true，否则返回false
                                boolean result = vSender.send(person);
                                boolean result1 = qSender.send(person);
                                if (!result) {
                                    log.info("----------------send to topic " + "result is false. personid=" + j);
                                } else {
                                    sumCount.incrementAndGet();
                                }

                                if (!result1) {
                                    log.info("----------------send to q3" + "result is false. personid=" + j);
                                } else {
                                    sumCount1.incrementAndGet();
                                }
                            }
                        } catch (Throwable t) {
                            t.printStackTrace();
                        } finally {
                            semaphore.release();
                        }
                    }
                };
                t.start();
            }

            log.info("yanny start to wait send finish");

            while (semaphore.availablePermits() != tc) {
                Thread.sleep(10);
            }

            int totalRequest = tc * tp;

            long endTime = System.currentTimeMillis();
            log.info("yanny: send " + totalRequest + " message, take " + (endTime - startTime) + " milseconds");

            JmxUtil.waitTillQueueSizeAsTarget(recConnector.getAddress(), queueName1, 0);
            JmxUtil.waitTillQueueSizeAsTarget(recConnector.getAddress(), queueName2, 0);
            JmxUtil.waitTillQueueSizeAsTarget(recConnector.getAddress(), queueName3, 0);

            int allowedDiff = (int) (totalRequest * 0.01);

            log.info("yanny totalRequest " + totalRequest + " send topic success " + sumCount
                    + "; send queue 3 success " + sumCount1);

            long q1BdbCount = 0;
            long q2BdbCount = 0;
            long q3BdbCount = 0;

            String errorMessage = "";

            System.out.println(initConsumeMessage);

            if (sendConnector.getSenderKVStore(q1Receiver.getName()) == null) {
                errorMessage += "q1 kvstore is null";
                System.out.println("q1 kvstore is null");
            } else {
                q1BdbCount = sendConnector.getSenderKVStore(q1Receiver.getName()).getStoreSize();
            }

            if (sendConnector.getSenderKVStore(q2Receiver.getName()) == null) {
                errorMessage += "q2 kvstore is null";
                System.out.println("q2 kvstore is null");
            } else {
                q1BdbCount = sendConnector.getSenderKVStore(q2Receiver.getName()).getStoreSize();
            }

            if (sendConnector.getSenderKVStore(heymanReceiver.getName()) == null) {
                errorMessage += "q3 kvstore is null";
                System.out.println("q3 kvstore is null");
            } else {
                q1BdbCount = sendConnector.getSenderKVStore(heymanReceiver.getName()).getStoreSize();
            }

            log.info("NapoliClientTest_q1's success=" + q1Worker.getAccessNum() + " bdb's size=" + q1BdbCount);
            log.info("NapoliClientTest_q2's success=" + q2Worker.getAccessNum() + " bdb's size=" + q2BdbCount);

            log.info("NapoliClientTest_q3's success=" + q3Worker.getAccessNum() + " bdb's size=" + q3BdbCount);

            if (totalRequest < q1Worker.getAccessNum()) {
                errorMessage += ";q1 should not have success messages(" + q1Worker.getAccessNum() + " more than "
                        + totalRequest;
            }

            if (totalRequest < q2Worker.getAccessNum()) {
                errorMessage += ";q2 should not have success messages(" + q2Worker.getAccessNum() + " more than "
                        + totalRequest;
            }

            if (2 * totalRequest < q3Worker.getAccessNum()) {
                errorMessage += "q3 should not have success messages(" + q3Worker.getAccessNum() + " more than " + 2
                        * totalRequest;
            }

            if (((q1Worker.getAccessNum() + q1BdbCount) - totalRequest) > allowedDiff) {
                errorMessage += ";q1 received message should not have more than totalRequest+ " + allowedDiff
                        + " than allowed (0.1%), gap " + ((q1Worker.getAccessNum() + q1BdbCount) - totalRequest);
            }

            if (((q2Worker.getAccessNum() + q2BdbCount) - totalRequest) > allowedDiff) {
                errorMessage += ";q2 received message should not have more than totalRequest+ " + allowedDiff
                        + " than allowed (0.1%), gap " + ((q2Worker.getAccessNum() + q2BdbCount) - totalRequest);
            }

            if (((q3Worker.getAccessNum() + q3BdbCount) - 2 * totalRequest) > 2 * allowedDiff) {
                errorMessage += ";q3 received message should not have more than totalRequest+ " + allowedDiff
                        + " than allowed (0.1%), gap " + ((q3Worker.getAccessNum() + q3BdbCount) - 2 * totalRequest);
            }

            if ((totalRequest - q1BdbCount) > q1Worker.getAccessNum()) {
                errorMessage += ";q1 received message(" + q1Worker.getAccessNum() + ") less than send succeed ("
                        + (totalRequest - q1BdbCount) + ", message lost";
            }

            if ((totalRequest - q2BdbCount) > q2Worker.getAccessNum()) {
                errorMessage += ";q2 received message(" + q2Worker.getAccessNum() + ") less than send succeed ("
                        + (totalRequest - q2BdbCount) + ", message lost";
            }

            if ((2 * totalRequest - q3BdbCount) > q3Worker.getAccessNum()) {
                errorMessage += ";q3 received message(" + q3Worker.getAccessNum() + ") less than send succeed ("
                        + (2 * totalRequest - q3BdbCount) + ", message lost";
            }

            //some sendSuccess could actually be message stored locally
            verify(
                    napoliSenderStat,
                    times((int) (q1Worker.getAccessNum() + q2Worker.getAccessNum() + q3Worker.getAccessNum()
                            + q3BdbCount + q3BdbCount + q3BdbCount))).sendSuccess(anyLong(), anyLong());
            verify(napoliSenderStat, atLeast((int) (4 * totalRequest - q1BdbCount - q2BdbCount - q3BdbCount)))
                    .sendSuccess(anyLong(), anyLong());
            verify(napoliSenderStat, times((int) (q1BdbCount + q2BdbCount + q3BdbCount))).sendFailure(anyLong(),
                    anyLong());

            verify(napoliReceiverStat,
                    times(q1Worker.getAccessNum() + q2Worker.getAccessNum() + q3Worker.getAccessNum())).receiveSuccess(
                    anyLong(), anyLong());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public static void main(String[] args) {

    }
}
