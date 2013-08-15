package com.alifi.mizar.gateway.module.screen;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.napoli.client.async.AsyncSender;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.HessianOutput;

public class Hessian {
	
	private final static Log logger = LogFactory.getLog(Hessian.class);

	@Resource(name="asyncSender")
	private AsyncSender asyncSender;
	
    public void execute(HttpServletRequest request, HttpServletResponse response) throws IOException, FileUploadException {
    	InputStream inputStream = request.getInputStream();
    	Hessian2Input hessianInput = new Hessian2Input(inputStream);
    	if (hessianInput == null) {
    		logger.info("no input stream");
    		return;
    	}
    	final int code = hessianInput.read();
        if (code != 'c') {
            throw new IOException("expected 'c' in hessian input at " + code);
        }

        readUnusedParams(hessianInput);
        
        String methodName = hessianInput.readMethod();
        Map<?, ?> params = (Map<?, ?>) hessianInput.readObject(Map.class);
    	logger.info("request from b2b to ctu:[" +  params + "]");
    	boolean sendResult = asyncSender.send((Serializable) params);
    	
    	writeResponse(response.getOutputStream(), sendResult);
    }
    
    private void writeResponse(OutputStream os, boolean result) throws IOException {
    	HessianOutput hessianOutput = new HessianOutput(os);
    	try {
	    	hessianOutput.startReply();
	    	hessianOutput.writeBoolean(result);
	    	hessianOutput.completeReply();
    	} finally {
    		hessianOutput.close();
    		os.close();
    	}
    }
    
    private void readUnusedParams(Hessian2Input hessianInput) throws IOException {
    	hessianInput.read();	//read major
    	hessianInput.read();	//read minor
    	readHeader(hessianInput);
    }
    
    private void readHeader(Hessian2Input hessianInput) throws IOException {
    	while (hessianInput.readHeader() != null) {
    		hessianInput.readObject();
    	}
    }
    
    private MultipartHttpServletRequest getMultipartRequest(HttpServletRequest request) {
    	if (request instanceof MultipartHttpServletRequest) {
    		return (MultipartHttpServletRequest) request;
    	}
    	return getMultipartRequest((HttpServletRequestWrapper) request);
    }
}