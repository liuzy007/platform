/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.server.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.diamond.utils.ResourceUtils;


public class SystemConfig {
	

    public static final String LOCAL_IP = getHostAddress();
    
    public int getDbConnLostDealing() {
		return dbConnLostDealing;
	}


	public static int EXIT = 0x01;
	public static int LOAD_DUMP = 0x02;
	public static int ONLINE = 0x00;
	public static int OFFLINE = 0x01;
	public static int mode = ONLINE;
	
	
	
    
    
	private int dbConnLostDealing = EXIT;
	
    public void setDbConnLostDealing(int dbConnLostDealing) {
		this.dbConnLostDealing = dbConnLostDealing;
	}


	/**
     * Dump配置信息的时间间隔，默认10分钟
     */
    private static int dumpConfigInterval = 600;

    /**
     * 加载分组信息时间间隔
     */
    private static int loadGroupInterval = 300;

    static final Log log = LogFactory.getLog(SystemConfig.class);

    static {
        InputStream in = null;
        try {
            in = ResourceUtils.getResourceAsStream("system.properties");
            Properties props = new Properties();
            props.load(in);
            dumpConfigInterval = Integer.parseInt(props.getProperty("dump_config_interval", "600"));
            loadGroupInterval = Integer.parseInt(props.getProperty("load_group_interval", "300"));
        }
        catch (IOException e) {
            log.error("加载system.properties出错", e);
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException e) {
                    log.error("关闭system.properties出错", e);
                }
            }
        }
    }


    public SystemConfig() {

    }


    public static int getDumpConfigInterval() {
        return dumpConfigInterval;
    }


    public static int getLoadGroupInterval() {
        return loadGroupInterval;
    }
    public static boolean isOfflineMode(){
    	return mode==OFFLINE;
    }
    public static boolean isOnlineMode(){
    	return mode!=OFFLINE;
    }
    public static void system_pause() {
		System.out.println("press ANY KEY to QUIT.");
		Scanner scanner = new Scanner(System.in);
		scanner.next();
	}
    public static void setOffline(){
    	mode=OFFLINE;
    }
    public static  boolean isWindowsOs(){
    	return System.getProperty("os.name").contains("Windows");
    }
    static String[] invalidChars = new String[]{":","\\?","\\|","\\\\","/","\\*"};
    static String[] encodeChars = new String[]{"_COMMA_","_Q_MARK_","_PIPE_","_BACK_SLASH_","_SLASH_","_ASTERISK_"};
    static String[] decodeChars = new String[]{":","?","|","\\","/","*"};
    public static String encodeDataIdForFNIfUnderWin(String dataId){
    	if(!isWindowsOs())return dataId;
    	String encode = dataId;
    	String[] ss = invalidChars;
    	String[] sp = encodeChars;
    	for(int i=0;i<invalidChars.length;i++){
    		String s = ss[i];
    		String  p = sp[i];
    		encode = encode.replaceAll(s,p);
    	}
    	return encode;
    }
    public static String decodeFnForDataIdIfUnderWin(String fn){
    	if(!isWindowsOs())return fn;
    	String decode = fn;
    	String[] ss = encodeChars;
    	String[] sp = invalidChars;
    	for(int i=0;i<invalidChars.length;i++){
    		String s = ss[i];
    		String  p = sp[i];
    		decode = decode.replaceAll(s,p);
    	}
    	return decode;
    }
    
    private static String getHostAddress() {
        String address = "127.0.0.1";
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface ni = en.nextElement();
                Enumeration<InetAddress> ads = ni.getInetAddresses();
                while (ads.hasMoreElements()) {
                    InetAddress ip = ads.nextElement();
                    if (!ip.isLoopbackAddress() && ip.isSiteLocalAddress()) {
                        return ip.getHostAddress();
                    }
                }
            }
        }
        catch (Exception e) {            
        }
        return address;
    }
    
    public static void main(String[] args) {
		String dataId = "com.taobao.diamond.test.xxx:version|1.0.0?.0.*";
		System.out.println("dataId:"+dataId);
		String encode = SystemConfig.encodeDataIdForFNIfUnderWin(dataId);
		System.out.println("dataId after encode:"+encode);
		String decode = SystemConfig.decodeFnForDataIdIfUnderWin(encode);
		System.out.println("dataId after decode:"+decode);
		
//		for(Object o:System.getProperties().keySet()){
//			System.out.println(o+":"+System.getProperty((String)o));
//		}
//		 if(System.getProperty("os.name").contains("Windows")){
//	        	System.out.println("the os is windows");
//	        }
	}
    
}
