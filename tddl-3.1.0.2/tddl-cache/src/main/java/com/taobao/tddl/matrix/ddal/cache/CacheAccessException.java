//===================================================================
// Created on 2008-1-15
//===================================================================
package com.taobao.tddl.matrix.ddal.cache;

/**
 * <p>
 *  
 * </p>
 * @author kongwang
 * @version 2008-1-15 下午06:47:37 hu.weih 
 */

public class CacheAccessException extends Exception {
	private static final long serialVersionUID = 4796264754607800313L;

	public CacheAccessException() {
		super();
	}

	public CacheAccessException(String message, Throwable cause) {
		super(message, cause);
	}

	public CacheAccessException(String message) {
		super(message);
	}

	public CacheAccessException(Throwable cause) {
		super(cause);
	}

}
