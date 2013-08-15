package com.alifi.mizar.chain;

import com.alifi.mizar.common.vo.GatewayServiceInfo;
import com.alifi.mizar.util.GatewayContext;
import com.alifi.mizar.util.OperationStringUtil;
import com.alifi.signature.service.SignatureService;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: tomp
 * Date: 13-6-18
 * Time: 下午5:16
 * To change this template use File | Settings | File Templates.
 */
public class AddSignCommand implements Command {

    private SignatureService signatureService;

    public boolean execute(Context context) throws Exception {
        GatewayContext gatewayContext = (GatewayContext) context;
        if (!gatewayContext.isSuccess()) {
            return false;
        }
        GatewayServiceInfo serviceInfo = (GatewayServiceInfo) gatewayContext.get("gateway_service_info");
        if (!serviceInfo.isEndorseSignOut()) {
            return false;
        }
        Object object = gatewayContext.getObject();
        String toSign = null;
        if (object instanceof Map) {
            toSign = OperationStringUtil.getSortParameters((Map<String, String>) object);
        } else {
            toSign = object.toString();
        }
        String charset = gatewayContext.containNotBlankInputParam("charset") ? gatewayContext.getInputParam("charset") : "UTF-8";
        String signType = gatewayContext.containNotBlankInputParam("signType") ? gatewayContext.getInputParam("signType") : "DSA";
        String partner = gatewayContext.getInputParam("partner");
        gatewayContext.put("sign", signatureService.sign(toSign, signType, partner, charset));
        gatewayContext.put("signType", signType);

        return false;
    }

    public void setSignatureService(SignatureService signatureService) {
        this.signatureService = signatureService;
    }
}
