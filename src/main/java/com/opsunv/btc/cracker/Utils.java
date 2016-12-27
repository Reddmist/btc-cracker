package com.opsunv.btc.cracker;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class Utils {
	public static ObjectMapper objectMapper = new ObjectMapper();
	
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
	
	public static String getURLContent(String urlStr){
		for(int i=0;i<3;i++){
			InputStream in = null;
			try{
				URL url = new URL(urlStr);
				in = (InputStream)url.getContent();
				return IOUtils.toString(in);
			} catch (Exception e) {
			} finally{
				IOUtils.closeQuietly(in);
			}
		}
		
		throw new RuntimeException();
	}
	
	public static long getBalance(String addr){
		return Long.valueOf(getURLContent("https://blockchain.info/q/addressbalance/"+addr));
	}
	
	public static long getReceivedBalance(String addr){
		return Long.valueOf(getURLContent("https://blockchain.info/q/getreceivedbyaddress/"+addr));
	}
	
	public static void show(BigInteger x){
		ECKey k = ECKey.fromPrivate(x);
		String addr = Utils.publickKeyToAddress(k.getPubKey());
		long balance = Utils.getReceivedBalance(addr);
		if(balance>0){
			System.out.println(x+","+k.getPrivateKeyAsHex()+","+addr+","+balance+","+Utils.getBalance(addr));
		}
	}
	
	public static String getAddressInfo(String addr,boolean haveTx){
		String content = getURLContent("https://blockchain.info/address/"+addr+"?format=json&limit=1");
		JsonNode json = null;
		try {
			json = objectMapper.readTree(content);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		int nTx = json.get("n_tx").asInt();
		if(haveTx&&nTx<1){
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(nTx+",");
		sb.append(json.get("total_received").asLong()+",");
		sb.append(json.get("final_balance").asLong()+",");
		
		ArrayNode arr = (ArrayNode)json.get("txs");
		if(arr.size()>0){
			Date date = new Date(arr.get(0).get("time").asLong()*1000);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			sb.append(sdf.format(date));
		}
		
		return sb.toString();
	}
	
}
