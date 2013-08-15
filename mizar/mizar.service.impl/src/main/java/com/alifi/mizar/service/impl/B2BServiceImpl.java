package com.alifi.mizar.service.impl;

import java.util.Map;

import com.alifi.agds.scout.model.B2bComplaint;
import com.alifi.agds.scout.service.B2bComplaintService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alifi.mizar.service.B2BService;
import com.alifi.mizar.util.OperationStringUtil;
import com.alifi.signature.service.SignatureService;
import com.alifi.agds.scout.model.B2bComplaint;
import com.alifi.agds.scout.service.B2bComplaintService;

import static com.alifi.mizar.util.AlipayUtil.map2Object;
import static com.alifi.mizar.util.OperationStringUtil.convertMapOfArrayToString;

public class B2BServiceImpl implements B2BService {

    private static final Log logger = LogFactory.getLog(B2BServiceImpl.class);
    private static final String SUCCESS_MESSAGE = "success";
    private static final String FAILED_MESSAGE = "failed";
    private static final String KEY_NAME = "B2B";

    private SignatureService signatureService;

    private B2bComplaintService b2bComplaintService;

    public String receiveComplaint(Map<String, String[]> queryBody) {
        Map<String, String> params = convertMapOfArrayToString(queryBody);
        logger.info("query string from b2b complaint is " + params.toString());
        if (!checkSignature(params)) {
            logger.error("signature cannot verify when receive complaint from b2b!");
            return FAILED_MESSAGE;
        }
        B2bComplaint complaint = map2Object(params, B2bComplaint.class);
        return b2bComplaintService.complaintNotice(complaint) ? SUCCESS_MESSAGE : FAILED_MESSAGE;
    }

    private boolean checkSignature(Map<String, String> params) {
        if (!params.containsKey("sign")) {
            logger.error("query string don't contains sign param");
            return false;
        }
        String sign = params.get("sign");
        params.remove("sign");
        params.remove("sign_type");
        String content = OperationStringUtil.getSortParameters(params);
        return signatureService.check(content, sign, KEY_NAME);
    }

    public void setSignatureService(SignatureService signatureService) {
        this.signatureService = signatureService;
    }

    public void setB2bComplaintService(B2bComplaintService b2bComplaintService) {
        this.b2bComplaintService = b2bComplaintService;
    }
}
