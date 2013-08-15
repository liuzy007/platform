package com.alifi.mizar.service;

import java.util.HashMap;
import java.util.Map;

import com.alifi.agds.scout.model.B2bComplaint;
import com.alifi.agds.scout.service.B2bComplaintService;
import com.alifi.mizar.service.impl.B2BServiceImpl;
import com.alifi.signature.service.SignatureService;

import junit.framework.TestCase;

import static org.mockito.Mockito.*;

public class B2BServiceTest extends TestCase {
    
    private B2BServiceImpl b2bService;
    
    public void setUp() {
        
        B2bComplaintService b2bComplaintService = mock(B2bComplaintService.class);
        when(b2bComplaintService.complaintNotice(isA(B2bComplaint.class))).thenReturn(true);
        
        SignatureService signatureService = mock(SignatureService.class);
        when(signatureService.check(isA(String.class), isA(String.class), isA(String.class))).thenReturn(true);
        
        b2bService = new B2BServiceImpl();
        b2bService.setB2bComplaintService(b2bComplaintService);
        b2bService.setSignatureService(signatureService);
    }

    public void testReceiveComplaint() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("id", new String[]{"12345"});
        params.put("complaintId", new String[]{"CT12345"});
        params.put("cMemberId", new String[]{"1234"});
        params.put("aMemberId", new String[]{"4321"});
        params.put("type", new String[]{"Cheat"});
        params.put("subject", new String[]{"just test"});
        params.put("tradeId", new String[]{"TD001"});
        params.put("tradeType", new String[]{"alipay"});
        params.put("aTel", new String[]{"110"});
        params.put("aMobilePhone", new String[]{"13012345678"});
        params.put("gmtCreate", new String[]{"2010-10-12 14:28:00"});
        params.put("gmtPreInterpose", new String[]{"2010-10-12 14:28:00"});
        params.put("gmtInterpose", new String[]{"2010-10-12 14:28:00"});
        params.put("gmtAccept", new String[]{"2010-10-12 14:28:00"});
        params.put("gmtResolve", new String[]{"2010-10-12 14:28:00"});
        params.put("status", new String[]{"1"});
        params.put("result", new String[]{"finished"});
        params.put("companyName", new String[]{"nani"});
        params.put("content", new String[]{"just kidding"});
        params.put("tradeUrl", new String[]{"http://www.google.com.hk"});
        params.put("isTransit", new String[]{"Y"});
        params.put("isGuarValid", new String[]{"Y"});
        params.put("isEligible", new String[]{"Y"});
        params.put("aTelArea", new String[]{"0571"});
        params.put("subType", new String[]{"what"});
        params.put("gmtArbitBegin", new String[]{"2010-10-12 14:28:00"});
        params.put("gmtArbitEnd", new String[]{"2010-10-12 14:28:00"});
        params.put("arbitOperator", new String[]{"1111"});
        params.put("isSuspectCheat", new String[]{"N"});
        params.put("punishmentResult", new String[]{"��ʾ"});
        params.put("fullType", new String[]{"1"});
        params.put("secondJudgeReason", new String[]{"readed"});
        params.put("paidStatus", new String[]{"paid"});
        params.put("gmtModified", new String[]{"2010-10-12 14:28:00"});
        params.put("tradeIdValid", new String[]{"valided"});
        params.put("tradeIdentify", new String[]{"4444"});
        params.put("complaintTelephone", new String[]{"110"});
        params.put("product", new String[]{"condom"});
        params.put("amount", new String[]{"100.23"});
        params.put("demands", new String[]{"RETURN_GOODS"});
        params.put("sign", new String[]{"test"});
        params.put("sign_type", new String[]{"DSA"});
        assertEquals("success", b2bService.receiveComplaint(params));
    }
}
