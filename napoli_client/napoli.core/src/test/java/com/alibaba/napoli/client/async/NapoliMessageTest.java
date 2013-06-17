package com.alibaba.napoli.client.async;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alibaba.napoli.common.NapoliException;
import com.alibaba.napoli.common.util.NapoliMessageUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import junit.framework.Assert;

import org.junit.Test;

import com.alibaba.napoli.client.model.Person;

/**
 * NapoliMessageTest
 * 
 * @author munch.wangr
 */
public class NapoliMessageTest {
    /**
     * 测试NapoliMessage的序列化
     */
    @Test
    public void testSerialize() throws Exception {
        NapoliMessage nm = new NapoliMessage("zhuanggl006");

        //Object() is not serializable, when writeObject, it will be removed, 
        nm.setProperty("1", new Person("3"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(nm);
        oos.flush();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        NapoliMessage nm1 = (NapoliMessage) ois.readObject();
        Assert.assertEquals(nm.getContent().toString(), nm1.getContent().toString());
        Assert.assertTrue("deseriazed Person() object doesn't equal to original value", nm.getProperty("1").equals(nm1.getProperty("1")));
    }

    @Test
    public void testSerializeWithNonSeriazbleProperty() throws Exception {
        NapoliMessage nm = new NapoliMessage("zhuanggl006");

        //Object() is not serializable, when writeObject, it will be removed, 
        nm.setProperty("1", new Object());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(nm);
        oos.flush();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        NapoliMessage nm1 = (NapoliMessage) ois.readObject();
        Assert.assertEquals(nm.getContent().toString(), nm1.getContent().toString());
        Assert.assertNull(nm1.getProperty("1"));
    }

    /**
     * NapoliMessage的本地存储测试
     */
    @Test
    public void testCanStore2Local() {
        NapoliMessage nm = new NapoliMessage(new Object());
        Assert.assertFalse(nm.canStore2Local());
        nm = new NapoliMessage("---------");
        Assert.assertTrue(nm.canStore2Local());
        nm.setStore2Local(true);
        Assert.assertTrue(nm.canStore2Local());
        nm.setStore2Local(false);
        Assert.assertFalse(nm.canStore2Local());
        Message m = mock(Message.class);
        nm = new NapoliMessage(m);
        Assert.assertFalse(nm.canStore2Local());
    }

    /**
     * 消息类型转化
     */
    //@Test
    /*public void testToJmsMessageAmq() throws Exception {
        Session session = mock(Session.class);
        TextMessage tm = mock(TextMessage.class);
        ActiveMQObjectMessage om = mock(ActiveMQObjectMessage.class);
        when(session.createTextMessage()).thenReturn(tm);
        when(session.createObjectMessage()).thenReturn(om);
        when(om.getContent())
        NapoliMessage nm = new NapoliMessage("---------------");
        nm.setExpiration(0);
        nm.setPriority(0);
        nm.setStore2Local(false);
        Assert.assertEquals(AMQMessageUtil.toJmsMessage(nm,session), tm);
        nm = new NapoliMessage(new Integer(100));
        Assert.assertEquals(AMQMessageUtil.toJmsMessage(nm,session), om);
    }*/

    /**
     * 消息类型转化
     */
    @Test
    public void testFromJmsMessage() throws Exception {
        TextMessage tm = mock(TextMessage.class);
        when(tm.getText()).thenReturn("-------------");

        //mock queue Name.
        when(tm.getStringProperty(NapoliMessage.MSG_PROP_KEY_QUEUENAME)).thenReturn("text_queue");

        ObjectMessage om = mock(ObjectMessage.class);
        when(om.getObject()).thenReturn(new Integer(100));
        when(om.getStringProperty(NapoliMessage.MSG_PROP_KEY_QUEUENAME)).thenReturn("object_queue");

        NapoliMessage nm = NapoliMessageUtil.fromJmsMessage(tm);
        Assert.assertTrue(nm.getContent() instanceof String);
        nm = NapoliMessageUtil.fromJmsMessage(om);
        Assert.assertTrue(nm.getContent() instanceof Serializable);
    }

    /**
     * 消息类型转化测试
     */
    @Test(expected = NapoliException.class)
    public void testFromJmsMessageWithThrow() throws Exception {
        MapMessage mm = mock(MapMessage.class);
        NapoliMessageUtil.fromJmsMessage(mm);
    }

    /**
     * 测试消息有效期，在消息过期时间之后消息还没有被发送到目的地，则该消息被清除。
     */
    @Test
    public void testGetExpiration() {
        NapoliMessage nm = new NapoliMessage("-----------");
        nm.setProperty(NapoliMessage.MSG_PROP_KEY_EXPIRATION, new Long(6));
        Assert.assertEquals(nm.getExpiration(), 6);
        nm.setProperty(NapoliMessage.MSG_PROP_KEY_EXPIRATION, "6");
        Assert.assertEquals(nm.getExpiration(), 0);
    }

    /**
     * 测试消息的优先级
     */
    @Test
    public void testGetPriority() {
        NapoliMessage nm = new NapoliMessage("-----------");
        nm.setProperty(NapoliMessage.MSG_PROP_KEY_PRIORITY, new Integer(9));
        Assert.assertEquals(nm.getPriority(), 9);
    }
}
