package com.pktech.oal.compile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;

public class DynamicUrlClassLoader extends URLClassLoader {

    public DynamicUrlClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public DynamicUrlClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> clazz = null;
        try {
            clazz = super.findClass(name);
        } catch (ClassNotFoundException ex) {
            if (clazz == null)
                clazz = _findClass(name);
            if (clazz == null)
                throw ex;
        }
        return clazz;
    }

    private Class<?> _findClass(String fullFilename) throws ClassNotFoundException {
        Class<?> clazz = null;
        try {
            fullFilename = "D:/tool/repository/git/platform/mizar/mizar.protocol/target/classes/test/DynaClass.class";
            FileInputStream fileInputStream = new FileInputStream(new File(fullFilename));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int size, SIZE = 4096;
            byte[] buffer = new byte[SIZE];
            while ((size = fileInputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, size);
            }
            fileInputStream.close();
            byte[] classBytes = outputStream.toByteArray();
            outputStream.close();
            clazz = defineClass("test.DynaClass", classBytes, 0, classBytes.length);
        } catch (Exception ex) {
            throw new ClassNotFoundException(fullFilename);
        }
        return clazz;
    }

    public Class<?> findClass(String filePath, String file) throws ClassNotFoundException {
        return this.findClass(classPathParser(filePath) + file);
    }

    public Class<?> loadClass(String fullName, JavaClassObject jco) {
        byte[] classData = jco.getBytes();
        return this.defineClass(fullName, classData, 0, classData.length);
    }

    private String pathParser(String path) {
        return path.replaceAll("\\\\", "/");
    }

    private String classPathParser(String path) {

        String classPath = pathParser(path);
//        if (!classPath.startsWith("file:")) {
//            classPath = "file:" + classPath;
//        }

        if (!classPath.endsWith("/")) {
            classPath = classPath + "/";
        }
        return classPath;

    }
}
