package com.opsunv.btc.cracker;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.digest.DigestUtils;
import org.bitcoinj.core.ECKey;

public class PasswordCracker {
	private byte[] salt;
	private byte[] cryptedKey;
	private int nDeriveIterations;
	private byte[] pubkey;
	private byte[] ckey;
	
	private IvParameterSpec ivKey;
	
	public PasswordCracker(byte[] salt,byte[] cryptedKey,byte[] pubkey,byte[] ckey,int nDeriveIterations) {
		this.salt = salt;
		this.cryptedKey = cryptedKey;
		this.nDeriveIterations = nDeriveIterations;
		this.pubkey = pubkey;
		this.ckey = ckey;
		
		byte[] iv = new byte[16];
		System.arraycopy(Utils.sha256Twice(pubkey), 0, iv, 0, 16);
		ivKey = new IvParameterSpec(iv);
	}
	
	public boolean crack(String password) throws Exception{
		byte[] pass = password.getBytes();
		byte[] data = new byte[pass.length+salt.length];
		
		System.arraycopy(pass, 0, data, 0, pass.length);
		System.arraycopy(salt, 0, data, pass.length, salt.length);
		
		for(int i=0;i<nDeriveIterations;i++){
			data = DigestUtils.sha512(data);
		}
		
		byte[] key = new byte[32];
		byte[] iv = new byte[16];
		
		System.arraycopy(data, 0, key, 0, 32);
		System.arraycopy(data, 32, iv, 0, 16);
		
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		Cipher mCipher = null;
		try{
			mCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			mCipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
			byte[] masterKey = mCipher.doFinal(cryptedKey);
			mCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(masterKey, "AES"), ivKey);
		}catch (Exception e) {
			return false;
		}
		
		byte[] priKey = mCipher.doFinal(ckey);
		
		ECKey ecKey = ECKey.fromPrivate(priKey);
		return Arrays.equals(ecKey.getPubKey(), pubkey);
	}
}
