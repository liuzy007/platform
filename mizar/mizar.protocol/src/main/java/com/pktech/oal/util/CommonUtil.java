package com.pktech.oal.util;

public abstract class CommonUtil {

    public static String classPathParser(String path) {

        String classPath = pathParser(path);

        if (!classPath.endsWith("/")) {
            classPath = classPath + "/";
        }
        return classPath;

    }

    public static String pathParser(String path) {
        return path.replaceAll("\\\\", "/");
    }

    public static String classNameParser(String className) {
        return className.replaceAll("\\.", "/");
    }

    public static String noSimpleClassName(String className) {
        if (className.indexOf(".") < 0) {
            return null;
        }
        return className.substring(0, className.lastIndexOf("."));
    }

    public static String getSimpleClassName(String className) {
        if (null == className || className.isEmpty()) {
            return null;
        }
        String[] values = className.split("\\.");
        return values[values.length - 1];
    }

}
