/**
 * Project: dubbo.test
 * 
 * File Created at 2010-11-17
 * $Id: PersonInfo.java 149160 2012-02-27 06:34:18Z haihua.chenhh $
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
import java.util.List;

/**
 * Comment of PersonInfoModel
 * 
 * @author tony.chenl
 */
public class PersonInfo implements Serializable {
    private static final long serialVersionUID = 7443011149612231882L;

    // �绰��ʴ���?
    List<Phone>               phones;

    // �����ʴ���
    Phone                     fax;

    // ��ϸ��ַ
    FullAddress               fullAddress;

    // �ƶ��绰
    String                    mobileNo;

    // ����
    String  name;

    // �Ա��С�������Ů
    boolean male;

    // �Ա��С�������Ů
    boolean female;

    // ����
    String  department;

    // ְλ���?
    String  jobTitle;

    // ��ҳURL
    String  homepageUrl;

    public List<Phone> getPhones() {
        return phones;
    }

    public void setPhones(List<Phone> phones) {
        this.phones = phones;
    }

    public boolean isMale() {
        return male;
    }

    public void setMale(boolean male) {
        this.male = male;
    }

    public boolean isFemale() {
        return female;
    }

    public void setFemale(boolean female) {
        this.female = female;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public void setFax(Phone fax) {
        this.fax = fax;
    }

    public void setFullAddress(FullAddress fullAddress) {
        this.fullAddress = fullAddress;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public void setHomepageUrl(String homepageUrl) {
        this.homepageUrl = homepageUrl;
    }



    public String getDepartment() {
        return department;
    }

    public Phone getFax() {
        return fax;
    }

    public FullAddress getFullAddress() {
        return fullAddress;
    }

    public String getHomepageUrl() {
        return homepageUrl;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersonInfo)) return false;

        PersonInfo that = (PersonInfo) o;

        if (female != that.female) return false;
        if (male != that.male) return false;
        if (department != null ? !department.equals(that.department) : that.department != null) return false;
        if (fax != null ? !fax.equals(that.fax) : that.fax != null) return false;
        if (fullAddress != null ? !fullAddress.equals(that.fullAddress) : that.fullAddress != null) return false;
        if (homepageUrl != null ? !homepageUrl.equals(that.homepageUrl) : that.homepageUrl != null) return false;
        if (jobTitle != null ? !jobTitle.equals(that.jobTitle) : that.jobTitle != null) return false;
        if (mobileNo != null ? !mobileNo.equals(that.mobileNo) : that.mobileNo != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (phones != null ? !phones.equals(that.phones) : that.phones != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = phones != null ? phones.hashCode() : 0;
        result = 31 * result + (fax != null ? fax.hashCode() : 0);
        result = 31 * result + (fullAddress != null ? fullAddress.hashCode() : 0);
        result = 31 * result + (mobileNo != null ? mobileNo.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (male ? 1 : 0);
        result = 31 * result + (female ? 1 : 0);
        result = 31 * result + (department != null ? department.hashCode() : 0);
        result = 31 * result + (jobTitle != null ? jobTitle.hashCode() : 0);
        result = 31 * result + (homepageUrl != null ? homepageUrl.hashCode() : 0);
        return result;
    }
}
