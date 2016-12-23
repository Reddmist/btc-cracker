package com.opsunv.btc.cracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

public class CollisionMaker {
	private BloomFilter<byte[]>[] filters;
	private int filterNum;
	
	private FileOutputStream logOutput;
	
	@SuppressWarnings("unchecked")
	public CollisionMaker(String bloomDir,String logFile) {
		try {
			logOutput = new FileOutputStream(logFile,true);
		} catch (FileNotFoundException e1) {
			throw new RuntimeException();
		}
		
		File[] files = new File(bloomDir).listFiles();
		filterNum = files.length;
		filters = new BloomFilter[filterNum];
		ExecutorService loaderExecutor = Executors.newFixedThreadPool(4);
		List<Callable<Void>> tasks = new ArrayList<>();
		for(final File f:files){
			tasks.add(new Callable<Void>() {
				
				@Override
				public Void call() throws Exception {
					int index = Integer.valueOf(f.getName().replace(".bin", ""));
					System.out.println("Load bloom filter "+index);
					FileInputStream in = null;
					try {
						in = new FileInputStream(f);
						filters[index]=BloomFilter.readFrom(in, Funnels.byteArrayFunnel());
					} catch (Exception e) {
						e.printStackTrace();
					} finally{
						IOUtils.closeQuietly(in);
					}
					return null;
				}
			});
		}
		
		try {
			for(Future<Void> future:loaderExecutor.invokeAll(tasks)){
				future.get();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		loaderExecutor.shutdown();
	}
	
	private synchronized void log(ECKey key){
		String data = Utils.publickKeyToAddress(key.getPubKey())+","+key.getPrivateKeyAsWiF(MainNetParams.get())+"\n";
		System.out.println(data);
		try {
			logOutput.write(data.getBytes());
			logOutput.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void compute(){
		System.out.println("Computing");
		long start = System.currentTimeMillis();
		int n = 0;
		while(true){
			n++;
			ECKey key = ECKey.fromPrivate(SecureRandom.getSeed(32));
			byte[] hash160 = key.getPubKeyHash();
			if(filters[Math.abs(hash160[0]%filterNum)].mightContain(hash160)){
				log(key);
			}
			if(System.currentTimeMillis()-start>60*1000){
				System.out.println(Thread.currentThread().getName()+" - "+ n/60+"/s");
				start = System.currentTimeMillis();
				n = 0;
			}
		}
	}
	
	public static void main(String[] args) {
		final CollisionMaker maker = new CollisionMaker("c:/bloom", "c:/Collision.txt");
		
		int max = 6;
		
		for(int i=0;i<max;i++){
			new Thread("Worker-"+i){
				@Override
				public void run() {
					maker.compute();
				}
			}.start();
		}
	}
}
