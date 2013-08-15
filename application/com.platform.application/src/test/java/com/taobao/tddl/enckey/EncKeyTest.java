package com.taobao.tddl.enckey;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.taobao.datasource.resource.security.SecureIdentityLoginModule;

public class EncKeyTest {
	public static void main(String[] args) throws Exception {
		String value = SecureIdentityLoginModule.encode("tddl", "tddl");
		System.out.println(value);
		value = SecureIdentityLoginModule.decode("tddl", "4e4d946e5819954f");
		System.out.println(value);
	}
}
