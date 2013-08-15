package com.alifi.mizar.chain;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import com.alifi.galaxy.biz.exception.GatewayException;
import com.alifi.mizar.common.util.GatewayErrors;
import com.alifi.mizar.common.vo.GatewayServiceInfo;
import com.alifi.mizar.util.GatewayContext;
import com.alifi.mizar.util.OperationStringUtil;
import com.alifi.signature.service.SignatureService;

/**
 * 签名验证
 * @author tongpeng.chentp
 *
 */
public class ValidateSignCommand implements Command {

    private SignatureService signatureService;

    public boolean execute(Context context) throws Exception {
        GatewayContext gatewayContext = (GatewayContext) context;
        GatewayServiceInfo serviceInfo = (GatewayServiceInfo) context.get("gateway_service_info");
        if (!serviceInfo.isValidateSignIn()) {
            return false;
        }
        String sign = gatewayContext.getInputParam("sign");
        String signType = gatewayContext.getInputParam("signType");
        String partner = gatewayContext.getInputParam("partner");
        String charset = gatewayContext.getInputParam("charset");
        if (charset == null) {
            charset = "UTF-8";
        }

        gatewayContext.removeInputParam("sign");
        gatewayContext.removeInputParam("signType");
        String content = OperationStringUtil.getSortParameters(gatewayContext.getInputParams());
        if (!signatureService.check(content, sign, signType, partner, charset)) {
            throw new GatewayException(GatewayErrors.INVALID_SIGN);
        }
        return false;
    }

    public void setSignatureService(SignatureService signatureService) {
        this.signatureService = signatureService;
    }
}