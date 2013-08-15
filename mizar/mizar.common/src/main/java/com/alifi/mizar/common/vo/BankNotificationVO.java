package com.alifi.mizar.common.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * 放贷到银行卡通知信息
 * @author tongpeng.chentp
 *
 */
public class BankNotificationVO implements Serializable {

    /**
     * 自动生成唯一序列号
     */
    private static final long serialVersionUID = -2412179639942449492L;
    
    /**
     * 用户支付宝ID
     */
    private String alipayId;
    
    /**
     * 业务流水号
     */
    private String businessNo;
    
    /**
     * 放贷金额
     */
    private double amount;
    
    /**
     * 放贷日期
     */
    private Date date;

    /**
     * 放贷状态  1-成功  4-失败  5-退票
     */
    private int issueStatus;
    
    /**
     * 错误码
     */
    private String errorCode;
    
    /**
     * 错误信息
     */
    private String errorMessage;

    
    public String getAlipayId() {
        return alipayId;
    }

    
    public void setAlipayId(String alipayId) {
        this.alipayId = alipayId;
    }

    
    public String getBusinessNo() {
        return businessNo;
    }

    
    public void setBusinessNo(String businessNo) {
        this.businessNo = businessNo;
    }

    
    public double getAmount() {
        return amount;
    }

    
    public void setAmount(double amount) {
        this.amount = amount;
    }

    
    public Date getDate() {
        return date;
    }

    
    public void setDate(Date date) {
        this.date = date;
    }

    
    public int getIssueStatus() {
        return issueStatus;
    }

    
    public void setIssueStatus(int issueStatus) {
        this.issueStatus = issueStatus;
    }

    
    public String getErrorCode() {
        return errorCode;
    }

    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    
    public String getErrorMessage() {
        return errorMessage;
    }

    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
