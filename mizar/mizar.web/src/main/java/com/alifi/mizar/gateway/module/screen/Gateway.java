package com.alifi.mizar.gateway.module.screen;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.service.requestcontext.parser.ParserRequestContext;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alifi.mizar.common.util.CommonResult;
import com.alifi.mizar.service.GatewayService;

public class Gateway {
	
	private static final Log log = LogFactory.getLog(Gateway.class);
	private static final int BUFFER_SIZE = 8 * 1024;
	
	@Autowired
	ParserRequestContext parser;
	
	@Resource(name="gatewayService.local")
	private GatewayService gatewayService;
	

    public void execute(@Param(name="format", defaultValue="xml") String format,
                        @Param(name="charset", defaultValue="UTF-8") String charset,
                        HttpServletRequest request,
                        HttpServletResponse response) throws IOException, XMLStreamException, FactoryConfigurationError, FileUploadException {
    	@SuppressWarnings("unchecked")
        Map<String, String[]> params = request.getParameterMap();
    	if (params.size() == 0) {
    		return;
    	}
    	

		CommonResult<?> commonResult = gatewayService.invokeService(params);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("success", commonResult.isSuccess());
		result.put("errorCode", commonResult.getErrorCode());
		result.put("errorMessage", commonResult.getErrorMessage());
		result.put("object", commonResult.getObject());
        result.put("sign", commonResult.getSign());
        result.put("signType", commonResult.getSignType());
		setNullValueToEmpty(result);
		
        response.setCharacterEncoding(charset);
        response.setContentType("application/" + format + ";charset=" + charset);
		response.getWriter().write(commonResult.getObject().toString());
    }
    
    private void setNullValueToEmpty(Map<String, Object> map) {
    	for (Entry<String, Object> entry : map.entrySet()) {
    		Object value = entry.getValue();
    		if (value instanceof Map) {
    			setNullValueToEmpty((Map<String, Object>) entry.getValue());
    		} else if (value == null) {
    			map.put(entry.getKey(), "");
    		}
    	}
    }
    
    private String getExtension(String fileName) {
    	int index = fileName.indexOf(".");
    	if (index == -1) {
    		return null;
    	}
    	return fileName.substring(index + 1);
    }
}
