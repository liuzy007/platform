package com.alifi.mizar.manager.util;

import java.util.Date;
import java.util.Map;

import org.springframework.util.Assert;

public class MapCache {

	private Map<Object, Object> cacher;

	public void init() {
		Assert.notNull(cacher, "cacher not found");
	}

	public void remove(String key) {
		cacher.remove(key);
	}

	public Object get(String key) {
		return cacher.get(key);
	}

	public void put(String key, int expire, Object value) {
		cacher.put(key, value);

	}

	public void destroy() {
		cacher = null;
	}

	public void clear() {
		cacher.clear();
	}

	public Object get(Object key) {
		return cacher.get(key.toString());
	}

	public void put(Object key, Object value) {
		cacher.put(key.toString(), value);
	}
	
	
	 //仅当存储空间中不存在键相同的数据时才保存
    public void add(String key, Object value) {
        cacher.put(key, value);
    }
    
    public void add(String key, Object value, Date expiry) {
        cacher.put(key, value);
    }
    
   
    //不管是否存在键相同的数据，都会保存
    public void set(String key, Object value) {
        cacher.put(key, value);
    }
    
    public void set(String key, Object value, Date expiry) {
        cacher.put(key, value);
    }
    
    public boolean delete(String key) {
        cacher.remove(key.toString());
        return true;
    }
    
    public boolean delete(String key, Date expiry) {
        cacher.remove(key.toString());
        return true;
    }


	public boolean remove(Object key) {
		cacher.remove(key.toString());
		return true;
	}

	public boolean contains(Object key) {
		return cacher.containsKey(key.toString());
	}

	/**
	 * @return the cacher
	 */
	public Map<Object, Object> getCacher() {
		return cacher;
	}

	/**
	 * @param cacher
	 *            the cacher to set
	 */
	public void setCacher(Map<Object, Object> cacher) {
		this.cacher = cacher;
	}

}
