package com.opsunv.btc.cracker;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import org.apache.commons.codec.digest.DigestUtils;
import org.bitcoinj.core.Base58;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Utils {
	public static byte[] sha256Twice(byte[] data){
		return DigestUtils.sha256(DigestUtils.sha256(data));
	}
	
	public static String publickKeyToAddress(byte[] publicKey){
        Security.addProvider(new BouncyCastleProvider());  
        MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("RipeMD160");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException();
		}  
        byte[] data = md.digest(DigestUtils.sha256(publicKey));
        
        byte[] checksumData = new byte[data.length+1];
        System.arraycopy(data, 0, checksumData, 1, data.length);
        byte[] hash = Utils.sha256Twice(checksumData);
        
        byte[] addr = new byte[checksumData.length+4];
        System.arraycopy(checksumData, 0, addr, 0, checksumData.length);
        System.arraycopy(hash, 0, addr, checksumData.length, 4);
        
        return Base58.encode(addr);
	}
	
	public static boolean checkPrivateKeyWIF(String pk){
		byte[] data = Base58.decode(pk);
		if(data[0]!=(byte)0x80){
			return false;
		}
		
		byte[] cumputeData = new byte[data.length-4];
		System.arraycopy(data, 0, cumputeData, 0, data.length-4);
		byte[] hash = sha256Twice(cumputeData);
		for(int i=0;i<4;i++){
			if(hash[i]!=data[data.length-4+i]){
				return false;
			}
		}
		
		return true;
	}
	
}
