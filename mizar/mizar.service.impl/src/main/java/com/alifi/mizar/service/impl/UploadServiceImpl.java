package com.alifi.mizar.service.impl;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import com.alifi.mizar.service.UploadService;
import com.aliyun.openservices.oss.OSSClient;
import com.aliyun.openservices.oss.model.ObjectMetadata;

public class UploadServiceImpl implements UploadService {

	private OSSClient ossClient;
	private String bucketName;

	public void uploadToOSS(InputStream is, String key, long length) {
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setExpirationTime(nextMonth());
		metadata.setContentLength(length);
		ossClient.putObject(bucketName, key, is, metadata);
	}

	private Date nextMonth() {
		Calendar now = Calendar.getInstance();
		now.add(Calendar.MONTH, 1);
		return now.getTime();
	}

	public void setOssClient(OSSClient ossClient) {
		this.ossClient = ossClient;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}
}