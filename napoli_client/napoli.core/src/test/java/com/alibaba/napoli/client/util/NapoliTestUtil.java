/**
 * Project: napoli.client
 * 
 * File Created at Aug 12, 2009
 * $Id: NapoliTestUtil.java 150227 2012-02-29 09:22:16Z yanny.wangyy $
 * 
 * Copyright 2008 Alibaba.com Croporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Alibaba Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Alibaba.com.
 */
package com.alibaba.napoli.client.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.napoli.AbstractTestBase;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.common.persistencestore.KVStore;
import com.alibaba.napoli.common.util.HttpUtil;
import com.alibaba.napoli.connector.ConsoleConnector;

/**
 * @author ding.lid
 */
public class NapoliTestUtil {

    private static Properties properties = null;
    static String             address;
    static String             user;
    static String             passwd;
    private static final Log log = LogFactory.getLog("NapoliTestUtil");

    static {
        log.info("before load napoli.properties");

        if (properties == null) {
            InputStream is = ClassLoader.getSystemResourceAsStream("napoli.properties");
            log.info("yanny: " + ClassLoader.class);

            if (is == null) {
                log.info("yanny: " + Thread.currentThread().getContextClassLoader());

                is = Thread.currentThread().getContextClassLoader().getResourceAsStream("napoli.properties");

                if (is == null) {
                    try {
                        is = (InputStream) (new FileInputStream(
                                "D:\\svn\\Napoli\\client\\napoli.core\\src\\test\\resources\\napoli.properties"));
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }

            properties = new Properties();
            try {
                properties.load(is);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (address == null) {
            address = properties.getProperty("napoli.func.address");
            System.out.println("console address is " + address);
        }

        if (user == null) {
            user = properties.getProperty("napoli.func.username");
        }

        if (passwd == null) {
            passwd = properties.getProperty("napoli.func.password");
        }
    }

    // Suppresses default constructor, ensuring non-instantiability.
    public NapoliTestUtil() {
    }

    /**
     * 递归删除文件。即如果是目录，则删除整个目录。
     * 
     * @param filepath 要删除的文件或是目录的路径
     */
    public static boolean rDel(final String filepath) {
        boolean result = true;
        final File file = new File(filepath); //定义文件路径

        if (!file.exists())
            return result;

        //判断是文件还是目录，并且目录下有文件
        if (file.isDirectory() && file.listFiles().length > 0) {
            final File list[] = file.listFiles();
            for (final File f : list) {
                result = result & rDel(f.getAbsolutePath()); //递归调用del方法并取得子目录路径 
            }
        }

        return result & file.delete();
    }

    /**
     * 文件数组按文件名升序排。
     * 
     * @param list
     */
    public static void sortFile(final File[] list) {
        Arrays.sort(list, new Comparator<File>() {
            public int compare(final File f1, final File f2) {
                return f1.getName().compareTo(f2.getName());
            }
        });
    }

    public static long getStoreSize(KVStore store) {
        if (store == null) {
            return 0;
        } else {
            return store.getStoreSize();
        }
    }

    public static String getAddress() {
        return address;
    }

    public static String getUser() {
        return user;
    }

    public static String getPasswd() {
        return passwd;
    }

    public static String getProperty(String name) {
        return properties.getProperty(name);
    }

    public static <T> void printArray(ArrayList<T> array) {
        Iterator it = array.iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
        }
    }

    public static String createName(String name) {
        return String.valueOf(name + (System.nanoTime() + "," + (long) Math.random() * 100000));
    }

    public static void delFiles(String filepath) throws IOException {
        File f = new File(filepath);//定义文件路径
        if (f.exists() && f.isDirectory()) {//判断是文件还是目录
            if (f.listFiles().length == 0) {//若目录下没有文件则直接删除
                f.delete();
            } else {//若有则把文件放进数组，并判断是否有下级目录
                File delFile[] = f.listFiles();
                int i = f.listFiles().length;
                for (int j = 0; j < i; j++) {
                    if (delFile[j].isDirectory()) {
                        delFiles(delFile[j].getAbsolutePath());//递归调用del方法并取得子目录路径
                    }
                    delFile[j].delete();//删除文件
                }
            }
        } else if (f.exists()) {
            f.delete();
        }
    }

    public static void sleepSecond(double second) {
        try {
            Thread.sleep((int) (1000 * second));
        } catch (InterruptedException e) {
        }
    }

    public static void sleep(int milSecond) {
        try {
            Thread.sleep(milSecond);
        } catch (InterruptedException e) {
        }
    }

    public static void waitUntilLocalStoreEmpty(ConsoleConnector connector, String name, String clientType) {
        long size = 0;
        do {
            sleep(100);
            KVStore store = ConsoleConnector.getorCreateBdbKvStore(connector.getStorePath(), name, clientType);
            size = store.getStoreSize();
        } while (size > 0);
    }
    
    public static void setExpectedSessionCount(DefaultAsyncReceiver receiver){ 
        AbstractTestBase.expectedConsumerSession = receiver.getExpectedInstances();
    }
    
    public static boolean isBlank(String str){
        if(str==null || str.equals("")){
            return true;
        }else{
            return false;
        }
            
    }
}
