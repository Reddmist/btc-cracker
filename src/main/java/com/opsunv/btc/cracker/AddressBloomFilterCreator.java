package com.opsunv.btc.cracker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bitcoinj.core.Base58;
import org.spongycastle.util.Arrays;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

public class AddressBloomFilterCreator {
	private File[] addressFiles;
	
	private List<BloomFilter<byte[]>> filters;
	
	private int filterNum;
	
	public AddressBloomFilterCreator(String cacheDir,int filterNum,int expectedInsertions) {
		this.filterNum = filterNum;
		addressFiles = new File(cacheDir).listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("blk")&&name.endsWith(".dat.txt");
			}
		});
		
		filters = new ArrayList<>(filterNum);
		for(int i=0;i<filterNum;i++){
			filters.add(BloomFilter.create(Funnels.byteArrayFunnel(), expectedInsertions,0.0000001));
		}
	}
	
	public void start(){
		for(File f:addressFiles){
			System.out.println(f);
			process(f);
		}
	}
	
	private void process(File file){
		List<String> lines = null;
		try {
			lines = FileUtils.readLines(file);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		for(String address:lines){
			byte[] data = Base58.decode(address);
			byte[] hash160 = Arrays.copyOfRange(data, 1, data.length-4);
			filters.get(Math.abs(hash160[0]%filterNum)).put(hash160);
		}
	}
	
	public void saveBloomFilters(String saveDir){
		int i=0;
		for(BloomFilter<byte[]> b:filters){
			try{
				FileOutputStream fos = new FileOutputStream(saveDir+"/"+(i++)+".bin");
				b.writeTo(fos);
				fos.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		AddressBloomFilterCreator creator = new AddressBloomFilterCreator("c:/blk_cache", 16, 20000000);
		creator.start();
		creator.saveBloomFilters("c:/bloom");
	}
}
