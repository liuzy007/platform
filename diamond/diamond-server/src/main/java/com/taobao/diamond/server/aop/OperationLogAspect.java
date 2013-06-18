/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.server.aop;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import com.taobao.diamond.server.utils.SessionHolder;

/**
 * 利用AOP记录用户操作日志
 * 
 * @author boyan
 * @date 2010-6-8
 */
@Component
@Aspect
public class OperationLogAspect {

	static final Log log = LogFactory.getLog("opLog");
	static final Log updateLog = LogFactory.getLog("updateLog");
	static final Log deleteLog = LogFactory.getLog("deleteLog");

	@Pointcut("execution(public * post* (..))")
	public void post() {

	}

	@Pointcut("execution(public * add* (..))")
	public void add() {

	}

	@Pointcut("execution(public * *upload (..))")
	public void upload() {

	}

	@Pointcut("execution(public * delete* (..))")
	public void delete() {

	}

	@Pointcut("execution(public * update* (..))")
	public void update() {

	}

	@Pointcut("execution(public * reupload* (..))")
	public void reupload() {

	}

	@Pointcut("execution(public * reload* (..))")
	public void reload() {

	}

	@Pointcut("execution(public * changePassword (..))")
	public void changePassword() {

	}

	@Pointcut("execution(public * setRefuseRequestCount (..))")
	public void setRefuseRequestCount() {

	}

	@Pointcut("execution(* com.taobao.diamond.server.controller.AdminController.* (..))")
	public void adminController() {

	}

	@AfterReturning(pointcut = "(post() || add() || upload() ||  reload()  || changePassword() || setRefuseRequestCount() )"
			+ " && ( adminController())")
	public void logOperation(JoinPoint joinPoint) {
		HttpSession session = SessionHolder.getSession();
		if (session != null) {
			String user = (String) session.getAttribute("user");
			if (user != null) {
				StringBuilder sb = new StringBuilder("用户:");
				sb.append(user).append(",执行:")
						.append(joinPoint.getSignature().getName())
						.append("，参数为：[");
				boolean wasFrist = true;
				String userIp = null;
				if (joinPoint.getArgs() != null) {
					for (Object obj : joinPoint.getArgs()) {
						if (obj instanceof HttpServletRequest) {
							HttpServletRequest request = (HttpServletRequest) obj;
							userIp = getRemoteIp(request);
						}

						if (wasFrist) {
							sb.append(obj);
							wasFrist = false;
						} else {
							sb.append(",").append(obj);
						}
					}
				}
				sb.append("]");
				sb.append(", ip:" + userIp);
				log.info(sb.toString());
			}
		}
	}

	@AfterReturning(pointcut = "(update() || reupload()) && (adminController())")
	public void logUpdateOperation(JoinPoint joinPoint) {
		HttpSession session = SessionHolder.getSession();
		if (session != null) {
			String user = (String) session.getAttribute("user");
			if (user != null) {
				StringBuilder sb = new StringBuilder("用户:");
				sb.append(user).append(",执行:")
						.append(joinPoint.getSignature().getName())
						.append("，参数为：[");
				boolean wasFrist = true;
				String userIp = null;
				if (joinPoint.getArgs() != null) {
					for (Object obj : joinPoint.getArgs()) {
						if (obj instanceof HttpServletRequest) {
							HttpServletRequest request = (HttpServletRequest) obj;
							userIp = getRemoteIp(request);
						}

						if (wasFrist) {
							sb.append(obj);
							wasFrist = false;
						} else {
							sb.append(",").append(obj);
						}
					}
				}
				sb.append("]");
				sb.append(", ip:" + userIp);
				updateLog.warn(sb.toString());
			}
		}
	}

	@AfterReturning(pointcut = "(delete()) && (adminController())")
	public void logDeleteOperation(JoinPoint joinPoint) {
		HttpSession session = SessionHolder.getSession();
		if (session != null) {
			String user = (String) session.getAttribute("user");
			if (user != null) {
				StringBuilder sb = new StringBuilder("用户:");
				sb.append(user).append(",执行:")
						.append(joinPoint.getSignature().getName())
						.append("，参数为：[");
				boolean wasFrist = true;
				String userIp = null;
				if (joinPoint.getArgs() != null) {
					for (Object obj : joinPoint.getArgs()) {
						if (obj instanceof HttpServletRequest) {
							HttpServletRequest request = (HttpServletRequest) obj;
							userIp = getRemoteIp(request);
						}

						if (wasFrist) {
							sb.append(obj);
							wasFrist = false;
						} else {
							sb.append(",").append(obj);
						}
					}
				}
				sb.append("]");
				sb.append(", ip:" + userIp);
				deleteLog.warn(sb.toString());
			}
		}
	}

	private String getRemoteIp(HttpServletRequest request) {
		String remoteIP = request.getHeader("X-Real-IP");
		if (remoteIP == null || remoteIP.isEmpty()) {
			remoteIP = request.getRemoteAddr();
		}
		return remoteIP;
	}
}
