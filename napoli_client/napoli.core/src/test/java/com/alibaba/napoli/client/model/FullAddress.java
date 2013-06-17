/**
 * Project: morgan.domain
 * 
 * File Created at 2009-6-11
 * $Id: FullAddress.java 149160 2012-02-27 06:34:18Z haihua.chenhh $
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

/**
 * ��ϸ��ַ���󡣲��ṩֵ��set������
 * 
 * @author xk1430
 */
public class FullAddress implements Serializable {

    private static final long serialVersionUID = 5163979984269419831L;

    // ��Ҵ��
    private String            countryId;

    // ���?
    private String            countryName;

    // ʡ��
    private String            provinceName;

    // ���д��?
    private String            cityId;

    // ����
    private String            cityName;

    // ��ַ
    private String            streetAddress;

    // �ʱ�
    private String            zipCode;

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    /**
     * Ĭ�ϵĹ��캯��
     */
    public FullAddress() {
    }

    /**
     * ��ȫ�Ĺ��캯��
     * 
     * @param countryId ��Ҵ��
     * @param provinceName ʡ��
     * @param cityId ���д��?
     * @param streetAddress ��ַ
     * @param zipCode �ʱ�
     */
    public FullAddress(String countryId, String provinceName, String cityId, String streetAddress,
                       String zipCode) {
        this.countryId = countryId;
        this.countryName = countryId;
        this.provinceName = provinceName;
        this.cityId = cityId;
        this.cityName = cityId;
        this.streetAddress = streetAddress;
        this.zipCode = zipCode;
    }

    /**
     * ��ȫ�Ĺ��캯��
     * 
     * @param countryId ��Ҵ��
     * @param countryName ���?
     * @param provinceName ʡ��
     * @param cityId ���д��?
     * @param cityName ����
     * @param streetAddress ��ַ
     * @param zipCode �ʱ�
     */
    public FullAddress(String countryId, String countryName, String provinceName, String cityId,
                       String cityName, String streetAddress, String zipCode) {
        this.countryId = countryId;
        this.countryName = countryName;
        this.provinceName = provinceName;
        this.cityId = cityId;
        this.cityName = cityName;
        this.streetAddress = streetAddress;
        this.zipCode = zipCode;
    }

    public String getCountryId() {
        return countryId;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public String getCityId() {
        return cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (countryName != null && countryName.length() > 0) {
            sb.append(countryName);
        }
        if (provinceName != null && provinceName.length() > 0) {
            sb.append(" ");
            sb.append(provinceName);
        }
        if (cityName != null && cityName.length() > 0) {
            sb.append(" ");
            sb.append(cityName);
        }
        if (streetAddress != null && streetAddress.length() > 0) {
            sb.append(" ");
            sb.append(streetAddress);
        }
        return sb.toString();
    }

}
