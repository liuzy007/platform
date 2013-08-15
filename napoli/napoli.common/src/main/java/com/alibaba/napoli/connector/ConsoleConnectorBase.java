package com.alibaba.napoli.connector;

import com.alibaba.napoli.common.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.napoli.common.ConnectionParam;
import com.alibaba.napoli.common.constants.NapoliConstant;

/**
 * User: heyman Date: 2/3/12 Time: 3:01 下午
 */
public abstract class ConsoleConnectorBase {
    private static final Log   log                        = LogFactory.getLog(ConsoleConnectorBase.class);

    protected String           address;
    protected int              sendTimeout                = 3000;                                         //默认超时3秒
    protected int              connectionTimeout          = 2000;
    protected String           jmsUserName                = "napoli";
    protected String           jmsPassword                = "napoli";
    protected int              prefetch                   = 5;
    protected int              poolSize                   = 10;
    protected int              idlePeriod                 = 5 * 60 * 1000;

    protected String           storePath                  = "./target/napoli_failover_data";

    protected boolean          sendSessionControl         = true;
    protected int              dataBatchReadCount         = NapoliConstant.MAX_BATCH_READ_SIZE;
    protected int              interval                   = NapoliConstant.MIN_CONFIGLOAD_INTERVAL;
    protected int              connectionCheckPeriod      = NapoliConstant.CONNECTION_CHECK_PERIOD;

    public final static String PROP_ADDRESS               = "address";
    public final static String PROP_STOREPATH             = "storePath";
    public final static String PROP_CONFIGRELOADINTERVAL  = "interval";
    public final static String PROP_SENDTIMEOUT           = "sendTimeout";
    public final static String PROP_CONNECTIONTIMEOUT     = "connectionTimeout";
    public final static String PROP_PREFETCH              = "prefetch";
    public final static String PROP_SENDER_POOLSIZE       = "poolSize";
    public final static String PROP_IS_SENDER_POOLCONTROL = "sendSessionControl";
    public final static String PROP_DATABATCHREADCOUNT    = "dataBatchReadCount";

    public String getAddress() {
        return address;
    }

    public int getSendTimeout() {
        return sendTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public String getJmsUserName() {
        return jmsUserName;
    }

    public String getJmsPassword() {
        return jmsPassword;
    }

    public int getPrefetch() {
        return prefetch;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public String getStorePath() {
        return storePath;
    }

    public int getDataBatchReadCount() {
        return dataBatchReadCount;
    }

    public int getIdlePeriod() {
        return idlePeriod;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setSendTimeout(int sendTimeout) {
        this.sendTimeout = sendTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public void setJmsUserName(String jmsUserName) {
        this.jmsUserName = jmsUserName;
    }

    public void setJmsPassword(String jmsPassword) {
        this.jmsPassword = jmsPassword;
    }

    public void setPrefetch(int prefetch) {
        this.prefetch = prefetch;
    }

    public void setPoolSize(int poolSize) {
        if (poolSize > NapoliConstant.MAX_POOL_SIZE) {
            this.poolSize = NapoliConstant.MAX_POOL_SIZE;
        } else if (poolSize < NapoliConstant.MIN_POOL_SIZE) {
            this.poolSize = NapoliConstant.MIN_POOL_SIZE;
        } else {
            this.poolSize = poolSize;
        }
    }

    public void setStorePath(String storePath) {
        this.storePath = storePath;
        String sysStorePath = System.getProperty(NapoliConstant.SYS_STORE_PATH);
        if (!StringUtils.isEmpty(sysStorePath)) {
            this.storePath = sysStorePath;
        }
    }

    public void setInterval(int interval) {
        if (interval < NapoliConstant.MIN_CONFIGLOAD_INTERVAL) {
            log.warn("config fetch interval set small than "+NapoliConstant.MIN_CONFIGLOAD_INTERVAL+"!");
        } 
        this.interval = interval;
    }

    public void setIntervalForTest(int interval) {
        this.interval = interval;
    }

    public void setSendSessionControl(boolean sendSessionControl) {
        this.sendSessionControl = sendSessionControl;
    }

    public void setDataBatchReadCount(int dataBatchReadCount) {
        if (dataBatchReadCount < 10) {
            this.dataBatchReadCount = 10;
        } else {
            this.dataBatchReadCount = dataBatchReadCount;
        }
    }

    public void setConnectionCheckPeriod(int connectionCheckPeriod) {
        this.connectionCheckPeriod = connectionCheckPeriod;
    }

    public void setIdlePeriod(int idlePeriod) {
        this.idlePeriod = idlePeriod;
    }

    public ConnectionParam getConnectionParam() {
        ConnectionParam connectionParam = new ConnectionParam();
        connectionParam.setConnectionTimeout(getConnectionTimeout());
        connectionParam.setJmsUserName(getJmsUserName());
        connectionParam.setJmsPassword(getJmsPassword());
        connectionParam.setSendTimeout(getSendTimeout());
        connectionParam.setPrefetch(getPrefetch());
        connectionParam.setTransacted(false);
        if (sendSessionControl) {
            connectionParam.setSendPoolsize(getPoolSize());
        } else {
            connectionParam.setSendPoolsize(0);
        }
        connectionParam.setPrefetch(getPrefetch());
        connectionParam.setStorePath(getStorePath());
        connectionParam.setIdlePeriod(getIdlePeriod());
        return connectionParam;
    }

    public void setProps(String key, String value) {
        if (PROP_ADDRESS.equalsIgnoreCase(key)) {
            setAddress(value);
        } else if (PROP_STOREPATH.equalsIgnoreCase(key)) {
            setStorePath(value);
        } else if (PROP_CONFIGRELOADINTERVAL.equalsIgnoreCase(key)) {
            setInterval(Integer.valueOf(value));
        } else if (PROP_SENDTIMEOUT.equalsIgnoreCase(key)) {
            setSendTimeout(Integer.valueOf(value));
        } else if (PROP_CONNECTIONTIMEOUT.equalsIgnoreCase(key)) {
            setConnectionTimeout(Integer.valueOf(value));
        } else if (PROP_PREFETCH.equalsIgnoreCase(key)) {
            setPrefetch(Integer.valueOf(value));
        } else if (PROP_SENDER_POOLSIZE.equalsIgnoreCase(key)) {
            setPoolSize(Integer.valueOf(value));
        } else if (PROP_IS_SENDER_POOLCONTROL.equalsIgnoreCase(key)) {
            setSendSessionControl(Boolean.valueOf(value));
        } else if (PROP_DATABATCHREADCOUNT.equalsIgnoreCase(key)) {
            setDataBatchReadCount(Integer.valueOf(value));
        } else {
            if (log.isWarnEnabled()) {
                log.warn("the property " + key + " is not support! the value " + value + " is be ignored");
            }
        }
    }
}
