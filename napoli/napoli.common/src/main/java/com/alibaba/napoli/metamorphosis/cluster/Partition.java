/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
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
 * Authors:
 *   wuhua <wq163@163.com> , boyan <killme2008@gmail.com>
 */
package com.alibaba.napoli.metamorphosis.cluster;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author boyan
 * @Date 2011-4-26
 * 
 */
public class Partition implements Comparable<Partition>, Serializable {
    private final int brokerId;
    private final int partition;
    private final String partStr;

    private transient boolean autoAck = true;
    private transient boolean acked = false;
    private transient boolean rollback = false;
    static final long serialVersionUID = -1L;

    public static final Partition RandomPartiton = new Partition(-1, -1);

    private final ConcurrentHashMap<String, Object> attributes = new ConcurrentHashMap<String, Object>();


    private Partition(final int brokerId, final int partition, final String partStr) {
        super();
        this.brokerId = brokerId;
        this.partition = partition;
        this.partStr = partStr;
    }


    /**
     * 
     * @return
     */
    public boolean isAutoAck() {
        return this.autoAck;
    }


    /**
     * 
     * @param autoAck
     */
    public void setAutoAck(final boolean autoAck) {
        this.autoAck = autoAck;
    }


    /**
     * 
     * @param key
     * @param value
     */
    public Object setAttribute(final String key, final Object value) {
        return this.attributes.put(key, value);
    }


    /**
     * 
     * @see java.util.concurrent.ConcurrentHashMap#putIfAbsent(Object, Object)
     * @param key
     * @param value
     */
    public Object setAttributeIfAbsent(final String key, final Object value) {
        return this.attributes.putIfAbsent(key, value);
    }


    /**
     * 
     * @param key
     * @return
     */
    public Object getAttribute(final String key) {
        return this.attributes.get(key);
    }


    /**
     * 
     * @return
     */
    public Set<String> attributeKeySet() {
        return this.attributes.keySet();
    }


    /**
     * 
     * @param key
     * @return
     */
    public Object removeAttribute(final String key) {
        return this.attributes.remove(key);
    }


   
    public void clearAttributes() {
        this.attributes.clear();
    }


   
    public Partition duplicate() {
        return new Partition(this.brokerId, this.partition, this.partStr);
    }


    public Partition(final String str) {
        if (str != null && str.equals(RandomPartiton.toString())) {
            this.partStr = str;
            this.brokerId = RandomPartiton.getBrokerId();
            this.partition = RandomPartiton.getPartition();
        }
        else {
            if (str == null) {
                throw new IllegalArgumentException("null string");
            }
            final String[] tmps = str.split("-");
            this.partStr = str;
            this.brokerId = Integer.parseInt(tmps[0]);
            this.partition = Integer.parseInt(tmps[1]);
        }
    }


    public Partition(final int brokerId, final int partition) {
        super();
        this.brokerId = brokerId;
        this.partition = partition;
        this.partStr = brokerId + "-" + partition;
    }


    
    public int getBrokerId() {
        return this.brokerId;
    }


    
    public int getPartition() {
        return this.partition;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.brokerId;
        result = prime * result + this.partition;
        return result;
    }


    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final Partition other = (Partition) obj;
        if (this.brokerId != other.brokerId) {
            return false;
        }
        if (this.partition != other.partition) {
            return false;
        }
        return true;
    }


    @Override
    public int compareTo(final Partition o) {
        if (this.brokerId != o.brokerId) {
            return this.brokerId > o.brokerId ? 1 : -1;
        }
        else if (this.partition != o.partition) {
            return this.partition > o.partition ? 1 : -1;
        }
        return 0;
    }


    
    public void ack() {
        if (this.isAutoAck()) {
            throw new IllegalStateException("Partition is in auto ack mode");
        }
        if (this.isRollback()) {
            throw new IllegalStateException("Could not ack rollbacked partition");
        }
        this.acked = true;
    }


  
    public boolean isAcked() {
        return this.isAutoAck() || this.acked;
    }


 
    public boolean isRollback() {
        return !this.isAutoAck() && this.rollback;
    }


    public void rollback() {
        if (this.isAutoAck()) {
            throw new IllegalStateException("Partition is  in auto ack mode");
        }
        if (this.isAcked()) {
            throw new IllegalStateException("Could not rollback acked partition");
        }
        this.rollback = true;
    }


    public void reset() {
        this.rollback = false;
        this.acked = false;
    }


    @Override
    public String toString() {
        return this.partStr;
    }

}