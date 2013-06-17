package com.alibaba.napoli.common.util;

import com.alibaba.com.caucho.hessian.io.Hessian2Input;
import com.alibaba.com.caucho.hessian.io.Hessian2Output;
import com.alibaba.com.caucho.hessian.io.SerializerFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: heyman Date: 1/30/12 Time: 1:36 下午
 */
public class HessianUtil {

    private static final Log log = LogFactory.getLog(HessianUtil.class);
    private static final SerializerFactory serializerFactory = new SerializerFactory();
    
    public static Serializable deserialize(byte[] array) throws IOException {
        Object obj = null;
        ByteArrayInputStream in = new ByteArrayInputStream(array);
        Hessian2Input hin = new Hessian2Input(in);
        hin.setSerializerFactory(serializerFactory);
        try {
            obj = hin.readObject();
        } finally {
            try {
                hin.close();
                in.close();
            } catch (IOException ex) {
                log.error("Failed to close stream.", ex);
            }
        }
        return (Serializable)obj;
        //return (Serializable)decodeObject(array);
    }

    public static byte[] serialize(Object data) throws java.io.IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        Hessian2Output h2o = new Hessian2Output(out);
        h2o.setSerializerFactory(serializerFactory);
        try {
            h2o.writeObject(data);
        } finally {
            try {
                h2o.close();
                out.close();
            } catch (IOException ex) {
                log.error("Failed to close stream.", ex);
            }
        }
        //h2o.flushBuffer();
        return out.toByteArray();
        //return encodeObject(data);
    }

    /*public static byte[] encodeObject(Object obj) throws IOException{
        ByteArrayOutputStream baos =  new ByteArrayOutputStream(1024);
        Hessian2Output output = new Hessian2Output(baos);
        output.setSerializerFactory(serializerFactory);
        try {
            output.startMessage();
            output.writeObject(obj);
            output.completeMessage();
        } finally {
            try {
                output.close();
                baos.close();
            } catch (IOException ex) {
                log.error("Failed to close stream.", ex);
            }
        }
        return baos.toByteArray();
    }

    public static Object decodeObject(byte[] in) throws IOException{
        Object obj = null;
        ByteArrayInputStream bais = new ByteArrayInputStream(in);
        Hessian2Input input = new Hessian2Input(bais);
        input.setSerializerFactory(serializerFactory);
        try {
            input.startMessage();
            obj = input.readObject();
            input.completeMessage();
        } finally {
            try {
                input.close();
                bais.close();
            } catch (IOException ex) {
                log.error("Failed to close stream.", ex);
            }
        }
        return obj;
    }*/
}
