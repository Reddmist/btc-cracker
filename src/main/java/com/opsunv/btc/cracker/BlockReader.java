package com.opsunv.btc.cracker;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.utils.BlockFileLoader;

public class BlockReader {
	private File cacheDir;
	
	private File[] blocks;
	
	private NetworkParameters params;
	
	public BlockReader(String cacheDir,String dataDir) {
		params = MainNetParams.get();
		Context.getOrCreate(params);
		
		this.cacheDir = new File(cacheDir);
		File dir = new File(dataDir);
		blocks = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("blk")&&name.endsWith("dat");
			}
		});
	}
	
	public void start(){
		for(File file:blocks){
			if(Integer.valueOf(file.getName().replace("blk", "").replace(".dat", ""))<=415){
				continue;
			}
			process(file);
		}
	}
	
	private void process(File file){
		BlockFileLoader loader = new BlockFileLoader(params, Arrays.asList(file));
		Set<String> addrSet = new HashSet<>(5000000);
		Set<String> memoSet = new HashSet<>();
		Set<String> unknowSet = new HashSet<>();
		while(loader.hasNext()){
			Block block = loader.next();
			List<Transaction> transactions = block.getTransactions();
			for(Transaction tx:transactions){
				if(tx.getMemo()!=null){
					memoSet.add(tx.getMemo());
				}
				 for(TransactionOutput output:tx.getOutputs()){
					 Script script = null;
					 try{
						 script = output.getScriptPubKey();
					 }catch(Exception e){
						 continue;
					 }
					 String addr = null;
					 if(script.isSentToAddress() || script.isPayToScriptHash()){
						 addr = script.getToAddress(params).toBase58();
					 }else if(script.isSentToRawPubKey()){
						 addr = Utils.publickKeyToAddress(output.getScriptPubKey().getPubKey());
					 }else{
						 try{
							 unknowSet.add(output.toString());
						 }catch(Exception e){
							 
						 }
					 }
					 
					 if(addr!=null){
						 addrSet.add(addr);
					 }
				 }
			}
		}
		
		try {
			FileUtils.writeLines(new File(cacheDir,file.getName()+".txt"), addrSet);
			FileUtils.writeLines(new File(cacheDir,file.getName()+".memo.txt"), memoSet);
			FileUtils.writeLines(new File(cacheDir,file.getName()+".unknow.txt"), unknowSet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("proceed=> "+file.getName());
		addrSet.clear();
		addrSet = null;
	}
	
	public static void main(String[] args) throws Exception{
		new BlockReader("c:/blk_cache", "E:/bitcoin-0.13.1/data/blocks").start();
	}
}
