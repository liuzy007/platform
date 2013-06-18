/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class AppNameUtils {
    private static final Log log = LogFactory.getLog(AppNameUtils.class);

    private static final String[] DEFAULT_EXCLUDES = { "jmx-console.war" };

    private static final String[] SUFFIXS = { ".ear", ".spring", ".war" };


    public static String getAppName() {
        return getAppName(DEFAULT_EXCLUDES);
    }


    public static String getAppName(String[] excludes) {
        String[] appNames = getAllAppNames(excludes);
        if (appNames == null || appNames.length == 0) {
            log.error("没找到任何后缀名为“.ear”、“.spring”或“.war”的包");
            return null;
        }
        if (appNames.length > 1) {
            log.warn("找到超过一个后缀名为“.ear”、“.spring”或“.war”的包" + Arrays.toString(appNames) + "，返回第一个");
        }
        return appNames[0];
    }


    public static String[] getAllAppNames() {
        return getAllAppNames(DEFAULT_EXCLUDES);
    }


    public static String[] getAllAppNames(String[] excludes) {
        File classpath = getClasspath();
        File deployDir = classpath.getParentFile();
        List<String> appNames = new LinkedList<String>();
        for (String suffix : SUFFIXS) {
            File[] files = listFiles(deployDir, suffix, excludes);
            addFilesToAppNames(files, appNames, suffix);
        }
        return appNames.toArray(new String[appNames.size()]);
    }


    private static void addFilesToAppNames(File[] files, List<String> appNames, String suffix) {
        if (files != null) {
            for (File file : files) {
                String filename = file.getName();
                String appName = filename.substring(0, filename.length() - suffix.length());
                appNames.add(appName);
            }
        }
    }


    private static File[] listFiles(File dir, final String suffix, final String[] excludes) {
        return dir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return !inExcludes(file, excludes) && file.getName().toLowerCase().endsWith(suffix);
            }
        });
    }


    private static boolean inExcludes(File file, String[] excludes) {
        for (String exclude : excludes) {
            if (file.getName().equalsIgnoreCase(exclude)) {
                return true;
            }
        }
        return false;
    }


    private static File getClasspath() {
        String classpath = AppNameUtils.class.getResource("/").getPath();
        log.info("The classpath is " + classpath);
        return new File(classpath);
    }

}
