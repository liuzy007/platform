package com.alibaba.napoli.client.async;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class NapoliMessage implements Externalizable {
    private static final long   serialVersionUID           = -4631061134341143501L;
    public static final String  MSG_PROP_KEY_PRIORITY      = "napoli_priority_key";
    public static final String  MSG_PROP_KEY_EXPIRATION    = "napoli_expiration_key";
    public static final String  MSG_PROP_KEY_STORE_ENABLE  = "napoli_store_key";

    public static final String  MSG_PROP_KEY_SRCIP         = "_srcip";
    public static final String  MSG_PROP_KEY_SRC_HOSTNAME  = "_hostname";
    public static final String  MSG_PROP_KEY_APPNUM        = "_appnum";
    public static final String  MSG_PROP_KEY_TARGETADDRESS = "_targetAddress";
    public static final String  MSG_PROP_KEY_QUEUENAME     = "_queuename";
    public static final String  MSG_PROP_KEY_MSGSIZE       = "_msgsize";
    public static final String  MSG_PROP_KEY_BEGINTIME     = "_beginTime";
    public static final String  MSG_PROP_KEY_LOCALSTORE    = "_localStore";
    public static final String  MSG_PROP_KEY_REPROCESSNUM    = "_reprocessNum";
    public static final String  _URGENTID                  = "_urgentid";

    private Object              content;
    private Map<String, Object> props                      = new HashMap<String, Object>(10);

    public NapoliMessage() {

    }

    public NapoliMessage(Object content) {
        if (content == null) {
            throw new IllegalArgumentException("message body can not be null.");
        }
        //just get String length
        this.content = content;
    }

    public void setProperty(String key, Object value) {
        if (key == null || value == null) {
            return;
        }
        props.put(key, value);
    }

    public Object getProperty(String key) {
        if (key == null) {
            return null;
        }
        return props.get(key);
    }

    public Object removeProperty(String key) {
        return props.remove(key);
    }

    public void setStore2Local(boolean storeEnable) {
        setProperty(MSG_PROP_KEY_STORE_ENABLE, storeEnable);
    }

    public boolean canStore2Local() {
        if (!(content instanceof Serializable)) {
            return false;
        }
        if (isLocalStoreMessage()) {
            return false;
        }
        if (props.containsKey(MSG_PROP_KEY_STORE_ENABLE)) {
            return (Boolean) props.get(MSG_PROP_KEY_STORE_ENABLE);
        }
        return true;
    }

    public void setPriority(int priority) {
        setProperty(MSG_PROP_KEY_PRIORITY, priority);
    }

    public int getPriority() {
        Object p = props.get(MSG_PROP_KEY_PRIORITY);
        if (p instanceof Integer) {
            return (Integer) p;
        }
        return 4;
    }

    public void setExpiration(long expiration) {
        setProperty(MSG_PROP_KEY_EXPIRATION, expiration);
    }
    
    public void setReprocessNum(Integer num){
        setProperty(MSG_PROP_KEY_REPROCESSNUM,num);
    }

    public long getExpiration() {
        Object e = props.get(MSG_PROP_KEY_EXPIRATION);
        if (e instanceof Long) {
            return ((Long) e).intValue();
        }
        return 0;
    }

    public Object getContent() {
        return content;
    }

    public String toString() {
        if (props == null || props.size() == 0) {
            return "[" + (content != null ? content : "null") + "]";
        }
        return "[" + (content != null ? content : "null") + ":" + props + "]";
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        content = in.readObject();
        props = (Map) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(content);
        //clean none serialize property

        Map<String, Object> newProps = new HashMap<String, Object>();

        for (Map.Entry<String, Object> entry : props.entrySet()) {
            Object v = entry.getValue();
            if (v instanceof Serializable) {
                newProps.put(entry.getKey(), v);
            }
        }

        /*
         * for (String k : props.keySet()) { Object v = props.get(k); if (!(v
         * instanceof Serializable)) { props.remove(k); } }
         */
        out.writeObject(newProps);
    }

    public int getSize() {
        Object o = getProperty(MSG_PROP_KEY_MSGSIZE);
        if (o != null && o instanceof Integer) {
            return (Integer) o;
        }
        return 0;
    }

    public long getBeginTime() {
        Object o = getProperty(MSG_PROP_KEY_BEGINTIME);
        if (o != null && o instanceof Long) {
            return (Long) o;
        }
        return 0;
    }

    public String getSrcIp() {
        Object o = getProperty(MSG_PROP_KEY_SRCIP);
        if (o != null && o instanceof String) {
            return (String) o;
        }
        return "N/A";
    }

    public String getSrcHostname() {
        Object o = getProperty(MSG_PROP_KEY_SRC_HOSTNAME);
        if (o != null && o instanceof String) {
            return (String) o;
        }
        return "N/A";
    }

    public String getTargetAddress() {
        Object o = getProperty(MSG_PROP_KEY_TARGETADDRESS);
        if (o != null && o instanceof String) {
            return (String) o;
        }
        return "N/A";
    }

    public String getQueueName() {
        Object o = getProperty(MSG_PROP_KEY_QUEUENAME);
        if (o != null && o instanceof String) {
            return (String) o;
        }
        return "N/A";
    }

    public String getAppnum() {
        Object o = getProperty(MSG_PROP_KEY_APPNUM);
        if (o != null && o instanceof String) {
            return (String) o;
        }
        return "N/A";
    }

    public void setLocalStoreMessage(Boolean localStoreMessage) {
        setProperty(MSG_PROP_KEY_LOCALSTORE, localStoreMessage);
    }

    public boolean isLocalStoreMessage() {
        Object o = getProperty(MSG_PROP_KEY_LOCALSTORE);
        return o != null && o instanceof Boolean && (Boolean) o;
    }
    
    public int getReprocessNum(){
        Object o = getProperty(MSG_PROP_KEY_REPROCESSNUM);
        if (o != null && o instanceof Integer){
            return (Integer)o;
        }else{
            return 0;
        }
    }

    public Map<String, Object> getProps() {
        return props;
    }
    
    public String getUrgentid(){
        return (String)getProperty(_URGENTID);
    }
    
    public void setUrgentid(String urgentid){
        props.put(_URGENTID,urgentid);
    }
}
