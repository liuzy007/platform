package com.alibaba.napoli.common.constants;

/**
 * User: heyman
 * Date: 9/2/11
 * Time: 3:22 下午
 */
public class NapoliConstant {
    public static final String CLIENT_DOMAIN_ASYNC = "async";
    public static final String CLIENT_TYPE_SENDER = "sender";
    public static final String CLIENT_TYPE_RECEIVER = "receiver";
    
    public static final String SYS_STORE_PATH = "napoli.storepath";

    //public static final int MIN_CONFIGLOAD_INTERVAL = 1 * 1000 * 60;   //config load最小时间，默认1分钟
    public static final int MIN_CONFIGLOAD_INTERVAL = 3 * 1000 * 60;   //config load最小时间，默认3分钟
    public static final int MIN_POOL_SIZE = 1;    //往topic下各个队列发送消息的最小线程数
    public static final int MAX_POOL_SIZE = 20;    //往topic下各个队列发送消息的最大线程数
    public static final int MAX_BATCH_READ_SIZE = 1000; //从bdb批量读取数据的个数

    public static final float DEFAULT_CONFIG_VERSION = 1.29f;
    public static final int DEFAULT_CONFIG_BATCH_SIZE = 7;  //一次最多从console上拿队列更数的个数

    public static final int AUTO_ACKNOWLEDGE = 1;

    public static final int CLIENT_ACKNOWLEDGE = 2;

    public static final int DUPS_OK_ACKNOWLEDGE = 3;

    public static final int SESSION_TRANSACTED = 0;

    public static long MAX_IDLE_TIME = 60000 * 30; //连接最大的idle时间，30分钟

    public static int CONNECTION_CHECK_PERIOD = 20000;

    public static final int SESSION_POOL_TIMEOUT = 5;

    public static final int PENDING_BATCH_SIZE = 1000;  //pending_batch

    /**
     * 检查连接的周期的最小值1分钟。
     */
    public static int MIN_HEARTBEAT_PERIOD = 60 * 1000;
    
    public static final String version = "1.5.9";

    public NapoliConstant() {
    }
}