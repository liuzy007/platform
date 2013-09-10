package com.pktech.oal.compile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class DynamicClassLoader extends ClassLoader {

    public DynamicClassLoader(ClassLoader parent) {
        super(parent);
    }

    public Class<?> loadClass(String classPath, String className) throws ClassNotFoundException {
        try {
            String url = classPathParser(classPath) + classNameParser(className);
            System.out.println(url);
            URL myUrl = new URL(url);
            URLConnection connection = myUrl.openConnection();
            InputStream input = connection.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int data = input.read();
            while (data != -1) {
                buffer.write(data);
                data = input.read();
            }
            input.close();
            byte[] classData = buffer.toByteArray();
            return defineClass(noSuffix(className), classData, 0, classData.length);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    private String pathParser(String path) {
        return path.replaceAll("\\\\", "/");
    }

    private String classPathParser(String path) {

        String classPath = pathParser(path);
        if (!classPath.startsWith("file:")) {
            classPath = "file:" + classPath;
        }

        if (!classPath.endsWith("/")) {
            classPath = classPath + "/";
        }
        return classPath;

    }

    public static String classNameParser(String className) {
        return className.replaceAll("\\.", "/");
    }

    public static String noSuffix(String className) {
        return className.substring(0, className.lastIndexOf("."));
    }

    public static void main(String[] args) {
       System.out.println(noSuffix("com.ddk.ff"));
    }

}
