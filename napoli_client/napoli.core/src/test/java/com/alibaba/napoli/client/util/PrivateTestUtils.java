package com.alibaba.napoli.client.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class PrivateTestUtils {

    public static final void setProperty(Object src, String name, Object val) throws IllegalAccessException {
        System.out.println("src="+src);
        if (src == null || name == null) {
            throw new IllegalArgumentException("error auguments");
        }
        Class<?> c = src.getClass();
        Field[] fs = c.getDeclaredFields();
        while (!contain(fs, name) && c.getSuperclass() != null) {
            c = c.getSuperclass();
            fs = c.getDeclaredFields();
        }
        for (Field f : fs) {
            if (f.getName().equals(name)) {
                f.setAccessible(true);
                f.set(src, val);
            }
        }
    }

    public static final boolean getBoolean(Object src, String name) throws IllegalAccessException {
        Object ret = getProperty(src, name);
        if (ret instanceof Boolean) {
            return ((Boolean) ret).booleanValue();
        } else {
            throw new ClassCastException("not a boolean return value.");
        }
    }

    public static final int getInt(Object src, String name) throws IllegalAccessException {
        Object ret = getProperty(src, name);
        if (ret instanceof Integer) {
            return ((Integer) ret).intValue();
        } else {
            throw new ClassCastException("not a boolean return value.");
        }
    }

    public static final long getLong(Object src, String name) throws IllegalAccessException {
        Object ret = getProperty(src, name);
        if (ret instanceof Long) {
            return ((Long) ret).longValue();
        } else {
            throw new ClassCastException("not a boolean return value.");
        }
    }

    public static final Object getProperty(Object src, String name) throws IllegalAccessException {
        if (src == null || name == null) {
            throw new IllegalArgumentException("error auguments");
        }
        Class<?> c = src.getClass();
        Field[] fs = c.getDeclaredFields();
        while (!contain(fs, name) && c.getSuperclass() != null) {
            c = c.getSuperclass();
            fs = c.getDeclaredFields();
        }
        for (Field f : fs) {
            if (f.getName().equals(name)) {
                f.setAccessible(true);
                return f.get(src);
            }
        }
        throw new IllegalAccessException("no field : " + name);
    }

    public static final Object invokePrivateMethod(Object src, String name, Object... args)
                                                                                           throws IllegalAccessException,
                                                                                           InvocationTargetException {
        if (src == null || name == null) {
            throw new IllegalArgumentException("error auguments");
        }
        Class<?> c = src.getClass();
        Method[] fs = c.getDeclaredMethods();
        while (!contain(fs, name) && c.getSuperclass() != null) {
            c = c.getSuperclass();
            fs = c.getDeclaredMethods();
        }
        for (Method f : fs) {
            if (f.getName().equals(name)) {
                f.setAccessible(true);
                return f.invoke(src, args);
            }
        }
        throw new IllegalAccessException("no field : " + name);
    }

    private static boolean contain(AccessibleObject[] aos, String name) {
        boolean ret = false;
        for (AccessibleObject ao : aos) {
            if (ao instanceof Method) {
                ret = ((Method) ao).getName().equals(name);
                if (ret) {
                    return ret;
                }
            } else if (ao instanceof Field) {
                ret = ((Field) ao).getName().equals(name);
                if (ret) {
                    return ret;
                }
            } else {
                return false;
            }
        }
        return ret;
    }

    public static Object mockMethodProxy(Object src, String name, String type, Object value) {
        return MockMethodProxy.newInstance(src, name, type, value);
    }

    public static class MockMethodProxy implements MethodInterceptor {

//        private Object src;
        private String name;
        private String type;
        private Object value;

        public Object intercept(Object obj, java.lang.reflect.Method method, Object[] args, MethodProxy proxy)
                                                                                                              throws Throwable {
//            method.setAccessible(true);
            // // if (!method.getName().equals(name)) {
            // // return method.invoke(src, args);
            // // } else {
            Object retValFromSuper = null;
            try {
                retValFromSuper = proxy.invokeSuper(obj, args);
            } catch (Throwable t) {
                throw t.fillInStackTrace();
            }
            if (method.getName().equals(name)) {
                if (type.equals("return")) {
                    retValFromSuper = value;
                } else if (type.equals("exception")) {
                    throw (Exception) value;
                }
            }
            return retValFromSuper;
            // }
        }

        public static final Object newInstance(Object src, String name, String type, Object value, Object... args) {
            Class<?> clazz = src.getClass();
            MockMethodProxy callback = new MockMethodProxy();
//            callback.src = src;
            callback.name = name;
            callback.type = type;
            callback.value = value;
            try {
                Enhancer e = new Enhancer();
                e.setSuperclass(clazz);
                e.setCallback(callback);
                Class<?>[] clazzes = new Class[args.length];
                for (int i = 0; i < clazzes.length; i++) {
                    clazzes[i] = args[i].getClass();
                }
                if (args.length > 0) {
                    return e.create(clazzes, args);
                } else {
                    return e.create();
                }
            } catch (Throwable e) {
                e.printStackTrace();
                throw new Error(e.getMessage());
            }
        }
    }

    static class MockTask {

        public long curTime() {
            return System.currentTimeMillis();
        }
    }

    public static final void main(String[] args) throws Exception {     
        MockTask test = new MockTask();
        MockTask proxy = (MockTask) mockMethodProxy(test, "curTime", "return", 0);
        System.out.println(proxy.curTime());
        proxy = (MockTask) mockMethodProxy(test, "curTime", "exception", new RuntimeException("Mock exception"));
        System.out.println(proxy.curTime());
    }
}
