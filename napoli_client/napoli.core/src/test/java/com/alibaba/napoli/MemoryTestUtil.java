package com.alibaba.napoli;

import java.lang.management.ManagementFactory;

import java.text.NumberFormat;
import java.util.concurrent.CountDownLatch;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * User: heyman
 * Date: 1/30/12
 * Time: 10:59 上午
 */
public class MemoryTestUtil {

    public static long getYoungGC() {
        try {
            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName;
            if (mbeanServer.isRegistered(new ObjectName("java.lang:type=GarbageCollector,name=ParNew"))) {
                objectName = new ObjectName("java.lang:type=GarbageCollector,name=ParNew");
            } else if (mbeanServer.isRegistered(new ObjectName("java.lang:type=GarbageCollector,name=Copy"))) {
                objectName = new ObjectName("java.lang:type=GarbageCollector,name=Copy");
            } else {
                objectName = new ObjectName("java.lang:type=GarbageCollector,name=PS Scavenge");
            }

            return (Long) mbeanServer.getAttribute(objectName, "CollectionCount");
        } catch (Exception e) {
            throw new RuntimeException("error");
        }
    }

    public static long getFullGC() {
        try {
            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName;

            if (mbeanServer.isRegistered(new ObjectName("java.lang:type=GarbageCollector,name=ConcurrentMarkSweep"))) {
                objectName = new ObjectName("java.lang:type=GarbageCollector,name=ConcurrentMarkSweep");
            } else if (mbeanServer.isRegistered(new ObjectName("java.lang:type=GarbageCollector,name=MarkSweepCompact"))) {
                objectName = new ObjectName("java.lang:type=GarbageCollector,name=MarkSweepCompact");
            } else {
                objectName = new ObjectName("java.lang:type=GarbageCollector,name=PS MarkSweep");
            }

            return (Long) mbeanServer.getAttribute(objectName, "CollectionCount");
        } catch (Exception e) {
            throw new RuntimeException("error");
        }
    }
    
    public static void perf(final int threadCount,final int LOOP_COUNT,final PerfCase perfCase) throws Exception{
        perfCase.setup();
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; ++i) {
            Thread thread = new Thread() {
                public void run() {
                    try {
                        startLatch.await();
                        for (int i = 0; i < LOOP_COUNT; ++i) {
                            perfCase.excute();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } 
                    endLatch.countDown();
                }
            };
            thread.start();
        }
        long startMillis = System.currentTimeMillis();
        long startYGC = MemoryTestUtil.getYoungGC();
        long startFullGC = MemoryTestUtil.getFullGC();
        startLatch.countDown();
        endLatch.await();
        

        long millis = System.currentTimeMillis() - startMillis;
        long ygc = MemoryTestUtil.getYoungGC() - startYGC;
        long fullGC = MemoryTestUtil.getFullGC() - startFullGC;
        long tps = (threadCount*LOOP_COUNT*1000)/millis;
        perfCase.close();

        System.out.println("thread " + threadCount + " LOOP_COUNT " + LOOP_COUNT + " tps :" + tps + " millis : "
                + NumberFormat.getInstance().format(millis) + ", YGC " + ygc + " FGC " + fullGC);
    }
}
