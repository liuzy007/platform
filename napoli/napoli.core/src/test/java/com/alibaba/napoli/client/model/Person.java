/**
 * Project: dubbo.test
 * 
 * File Created at 2010-11-17
 * $Id: Person.java 149160 2012-02-27 06:34:18Z haihua.chenhh $
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
package com.alibaba.napoli.client.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Comment of Person
 * 
 * @author tony.chenl
 */
public class Person implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    // personId
    String                    personId;

    // ��½��
    String                    loginName;

    // person��״̬
    PersonStatus status;

    // �����ʼ�
    String                    email;

    // ����
    String                    penName;

    // ������Ϣ
    PersonInfo                infoProfile;

    Object content;

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public PersonInfo getInfoProfile() {
        return infoProfile;
    }

    public void setInfoProfile(PersonInfo infoProfile) {
        this.infoProfile = infoProfile;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public void setStatus(PersonStatus status) {
        this.status = status;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPenName(String penName) {
        this.penName = penName;
    }

    public Person() {

    }

    /**
     * @param id dd
     */
    public Person(String id) {
        this.personId = id;
    }

    public String getEmail() {
        return this.email;
    }

    public String getLoginName() {
        return this.loginName;
    }

    public PersonStatus getStatus() {
        return this.status;
    }

    public String getPenName() {
        return penName;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public static Person createObjMessage(int size) {
        Person objectMessage = new Person();
        objectMessage.setPersonId("superman111");
        objectMessage.setLoginName("superman");
        objectMessage.setEmail("sm@1.com");
        objectMessage.setPenName("pname");
        objectMessage.setStatus(PersonStatus.ENABLED);
        ArrayList<Phone> phones = new ArrayList<Phone>();
        Phone phone1 = new Phone("86", "0571", "87654321", "001");
        Phone phone2 = new Phone("86", "0571", "87654322", "002");
        phones.add(phone1);
        phones.add(phone2);
        PersonInfo pi = new PersonInfo();
        pi.setPhones(phones);
        Phone fax = new Phone("86", "0571", "87654321", null);
        pi.setFax(fax);
        FullAddress addr = new FullAddress("CN", "zj", "3480", "wensanlu", "315000");
        pi.setFullAddress(addr);
        pi.setMobileNo("13584652131");
        pi.setMale(true);
        pi.setDepartment("b2b");
        pi.setHomepageUrl("www.capcom.com");
        pi.setJobTitle("qa");
        pi.setName("superman");
        objectMessage.setInfoProfile(pi);
        if (size > 1196) {
            objectMessage.setContent(new byte[size - 1196]);
        }
        return objectMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;

        Person person = (Person) o;

        if (content != null ? !content.equals(person.content) : person.content != null) return false;
        if (email != null ? !email.equals(person.email) : person.email != null) return false;
        if (infoProfile != null ? !infoProfile.equals(person.infoProfile) : person.infoProfile != null) return false;
        if (loginName != null ? !loginName.equals(person.loginName) : person.loginName != null) return false;
        if (penName != null ? !penName.equals(person.penName) : person.penName != null) return false;
        if (personId != null ? !personId.equals(person.personId) : person.personId != null) return false;
        if (status != person.status) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = personId != null ? personId.hashCode() : 0;
        result = 31 * result + (loginName != null ? loginName.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (penName != null ? penName.hashCode() : 0);
        result = 31 * result + (infoProfile != null ? infoProfile.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }
}
