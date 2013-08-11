/**
 * 
 */
package com.taobao.tddl.matrix.ddal.cache;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.common.tair.DataEntry;
import com.taobao.common.tair.Result;
import com.taobao.common.tair.ResultCode;
import com.taobao.common.tair.impl.DefaultTairManager;

/**
 * 由于 DefaultTairManager 初始化方法不捕获异常，不兼容的版本致使应用无法启动
 * 
 * @author hu.weih
 * 
 */
public class ProxyTairManager extends DefaultTairManager {

	private static final Log log = LogFactory.getLog(ProxyTairManager.class);

	private boolean cacheEnable = true;

	public boolean isCacheEnable() {
		return cacheEnable;
	}

	private static ResultCode cachedisableRC = new ResultCode(611,
			"cache disabled");

	public void setCacheEnable(boolean cacheEnable) {
		this.cacheEnable = cacheEnable;
	}

	public void init() {
		
		if(cacheEnable){
			try {
				super.init();
				log.warn(">>>>>>taircache enable=true ");
			} catch (Throwable e) {			
				cacheEnable = false;
				log.warn(">>>>>>init taircache fail,set cache enable=false ",e);

			}
		}else{
			log.warn(">>>>>>taircache enable=false ");
		}
		

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.taobao.common.tair.TairManager#decr(int, java.lang.Object, int,
	 * int)
	 */
	public Result<Integer> decr(int namespace, Object key, int value,
			int defaultValue) {
		if (!cacheEnable) {
			return new Result<Integer>(cachedisableRC);
		}
		return super.decr(namespace, key, value, defaultValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.taobao.common.tair.TairManager#delete(int, java.lang.Object)
	 */
	public ResultCode delete(int namespace, Object key) {
		if (!cacheEnable) {
			return cachedisableRC;
		}
		return super.delete(namespace, key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.taobao.common.tair.TairManager#get(int, java.lang.Object)
	 */
	public Result<DataEntry> get(int namespace, Object key) {
		if (!cacheEnable) {
			return new Result<DataEntry>(cachedisableRC);
		}
		return super.get(namespace, key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.taobao.common.tair.TairManager#getVersion()
	 */
	public String getVersion() {

		return super.getVersion();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.taobao.common.tair.TairManager#incr(int, java.lang.Object, int,
	 * int)
	 */
	public Result<Integer> incr(int namespace, Object key, int value,
			int defaultValue) {
		if (!cacheEnable) {
			return new Result<Integer>(cachedisableRC);
		}
		return super.incr(namespace, key, value, defaultValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.taobao.common.tair.TairManager#invalid(int, java.lang.Object)
	 */
	public ResultCode invalid(int namespace, Object key) {
		if (!cacheEnable) {
			return cachedisableRC;
		}
		return super.invalid(namespace, key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.taobao.common.tair.TairManager#mdelete(int, java.util.List)
	 */
	public ResultCode mdelete(int namespace, List<Object> keys) {
		if (!cacheEnable) {
			return cachedisableRC;
		}
		return super.mdelete(namespace, keys);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.taobao.common.tair.TairManager#mget(int, java.util.List)
	 */
	public Result<List<DataEntry>> mget(int namespace, List<Object> keys) {
		if (!cacheEnable) {
			return new Result<List<DataEntry>>(cachedisableRC);
		}
		return super.mget(namespace, keys);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.taobao.common.tair.TairManager#put(int, java.lang.Object,
	 * java.io.Serializable)
	 */
	public ResultCode put(int namespace, Object key, Serializable value) {
		if (!cacheEnable) {
			return cachedisableRC;
		}
		return super.put(namespace, key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.taobao.common.tair.TairManager#put(int, java.lang.Object,
	 * java.io.Serializable, int)
	 */
	public ResultCode put(int namespace, Object key, Serializable value,
			int version) {
		if (!cacheEnable) {
			return cachedisableRC;
		}
		return super.put(namespace, key, value, version);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.taobao.common.tair.TairManager#put(int, java.lang.Object,
	 * java.io.Serializable, int, int)
	 */
	public ResultCode put(int namespace, Object key, Serializable value,
			int version, int expireTime) {
		if (!cacheEnable) {
			return cachedisableRC;
		}
		return super.put(namespace, key, value, version, expireTime);
	}

}
