package com.alibaba.napoli;

import com.alibaba.napoli.client.model.Person;
import com.alibaba.napoli.client.model.PersonStatus;
import com.alibaba.napoli.common.util.HessianUtil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * User: heyman
 * Date: 1/30/12
 * Time: 3:25 下午
 */
public class HessianUtilTest {
    @Test
    public void testObject() throws Exception{
        Person  person = new Person("100");
        person.setLoginName("superman");
        person.setEmail("sm@1.com");
        person.setPenName("pname");
        person.setStatus(PersonStatus.ENABLED);
        
        byte[] personSerialize =  HessianUtil.serialize(person);
        System.out.println(personSerialize.length);
        Person person1 = (Person)HessianUtil.deserialize(personSerialize);
        assertEquals(person.getPersonId(), person1.getPersonId());
        assertTrue(person.equals(person1));
    }
    
    @Test
    public void testString() throws Exception{
        String sample = "!@#$%^&*()_+.,mnbvcxzadfff-=  ";
        byte[] serializeBytes =  HessianUtil.serialize(sample);
        System.out.println(serializeBytes.length);
        String deserialize = (String)HessianUtil.deserialize(serializeBytes);
        assertTrue(sample.equals(deserialize));
    }
    
    @Test
    public void testOther() throws Exception{
        byte[] sample = new byte[]{0,1,1,1,1,1};
        byte[] serializeBytes =  HessianUtil.serialize(sample);
        System.out.println(serializeBytes.length);
        byte[] deserialize = (byte[]) HessianUtil.deserialize(serializeBytes);
        assertTrue(sample.length == deserialize.length);
        for (int i=0;i< sample.length ; i++){
            assertTrue(sample[i] == deserialize[i]);
        }
        byte[] bigContent =  new byte[1024*1024];
        byte[] bigContentS = HessianUtil.serialize(bigContent);
        byte[] bigContent2 = (byte[])HessianUtil.deserialize(bigContentS);
        assertTrue(bigContent.length == bigContent2.length);
        
        int intSample = 90000;
        testSerizlize(intSample);
        long longSample = 343432;
        testSerizlize(longSample);
    }
    
    
    
    
    private void testSerizlize(Object sample) throws Exception{
        byte[] serializeBytes =  HessianUtil.serialize(sample);
        System.out.println(serializeBytes.length);
        Object deserialize = HessianUtil.deserialize(serializeBytes);
        assertTrue(sample.equals(deserialize));
    }
}
