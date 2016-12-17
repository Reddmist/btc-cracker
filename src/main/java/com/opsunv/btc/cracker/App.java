package com.opsunv.btc.cracker;

import java.io.File;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;

public class App {
	public static void main(String[] args) throws Exception {
		int nDeriveIterations = 49019;
		byte[] salt = Hex.decodeHex("09792b4786f368cb".toCharArray());
		byte[] cryptedKey = Hex.decodeHex("e648d207eb6a457cc3b415e5e6db38759de529051d808b5d34c679c43020a233e6b5161de2e85070127009d61e4c24c8".toCharArray());
		byte[] pubkey = Hex.decodeHex("0313c403e04becbcb83e93fbdd9eb9d1b04d9479bfc0864ef46c49a6ca266b6f1f".toCharArray());
		byte[] ckey = Hex.decodeHex("3e2c64bb5a632d7b3c0b6318d5bc9eb7efc0d3c71a20d4108696a5e9ac750c242764c73978c6809a8f5d033f75575609".toCharArray());
		
		List<String> lines = FileUtils.readLines(new File("c:/nb.txt"));
		final ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<String>(lines.size());
		queue.addAll(lines);
		final AtomicInteger counter = new AtomicInteger();
		
		for(int n=0;n<3;n++){
			final PasswordCracker cracker = new PasswordCracker(salt, cryptedKey, pubkey, ckey, nDeriveIterations);
			new Thread(){
				public void run() {
					while(true){
						String pass = queue.poll();
						if(pass==null){
							break;
						}
						try {
							if(cracker.crack(pass)){
								System.out.println("Found password=> " + pass);
								System.exit(1);
							}
							counter.incrementAndGet();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}.start();
		}
		
		new Thread(){
			@Override
			public void run() {
				while(true){
					try {
						TimeUnit.MINUTES.sleep(1);
						System.out.println("compute: " +counter.get());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
}
