package com.alifi.mizar.service;

import java.io.InputStream;

public interface UploadService {

	public void uploadToOSS(InputStream is, String key, long length);
}
