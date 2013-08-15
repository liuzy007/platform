package com.alifi.mizar.gateway.module.screen;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.service.requestcontext.parser.ParserRequestContext;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.fastjson.JSON;
import com.alibaba.napoli.client.async.AsyncSender;
import com.alifi.mizar.common.util.CommonResult;
import com.alifi.mizar.service.GatewayService;
import com.alifi.mizar.service.UploadService;
import com.alifi.mizar.util.XMLSerializeHelper;

public class Gateway {
	
	private static final Log log = LogFactory.getLog(Gateway.class);
	private static final int BUFFER_SIZE = 8 * 1024;
	
	@Autowired
	ParserRequestContext parser;
	
	@Resource(name="gatewayService.local")
	private GatewayService gatewayService;
	
	@Resource(name="uploadService.local")
	private UploadService uploadService;
	
	@Resource(name="uploadInfoSender")
	private AsyncSender uploadInfoSender;

    public void execute(@Param(name="format", defaultValue="json") String format,
                        @Param(name="charset", defaultValue="UTF-8") String charset,
                        HttpServletRequest request,
                        HttpServletResponse response) throws IOException, XMLStreamException, FactoryConfigurationError, FileUploadException {
    	Map<String, String[]> params = request.getParameterMap();
    	if (params.size() == 0) {
    		return;
    	}
    	if (FileUpload.isMultipartContent(new ServletRequestContext(request))) {
    		FileItem fileItem = parser.getParameters().getFileItem("file");
    		HashMap<String, String> uploadInfo = new HashMap<String, String>();
			for (Entry<String, String[]> entry : params.entrySet()) {
				String[] value = entry.getValue();
				if (value == null || value.length == 0) {
					continue;
				}
				uploadInfo.put(entry.getKey(), value[0]);
			}
    		String path = "partner/" + uploadInfo.get("taskId") + "/" + uploadInfo.get("subCategoryCode") + "/" + fileItem.getName();
    		String type = getExtension(fileItem.getName());
    		uploadInfo.put("path", path);
			uploadInfo.put("type", type);
			log.info("上传接口参数:" + uploadInfo);
			
			uploadService.uploadToOSS(fileItem.getInputStream(), path, fileItem.getSize());
			uploadInfoSender.send(uploadInfo);
			
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
		String bodyResult = null;
		if ("json".equals(format)) {
			bodyResult = JSON.toJSONString(result);
		} else if ("xml".equals(format)) {
			final CharArrayWriter cawriter = new CharArrayWriter(BUFFER_SIZE);
            final XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(cawriter);
            try {
                writer.writeStartDocument(charset, "1.0");
                writer.writeStartElement("result");
                XMLSerializeHelper.objectSerialize(writer, result);
                writer.writeEndElement();
                writer.writeEndDocument();
                writer.flush();
            } finally {
                writer.close();
            }
			bodyResult = cawriter.toString();
		}
        response.setCharacterEncoding(charset);
        response.setContentType("application/" + format + ";charset=" + charset);
		response.getWriter().write(bodyResult);
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
