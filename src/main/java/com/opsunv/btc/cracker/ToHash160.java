package com.opsunv.btc.cracker;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.commons.io.FileUtils;
import org.bitcoinj.core.Base58;
import org.spongycastle.util.Arrays;

public class ToHash160 {
	public static void main(String[] args) throws Exception {
		FileOutputStream out = new FileOutputStream("c:/11.txt");
		for(String line:FileUtils.readLines(new File("c:/blk00000.dat.txt"))){
			byte[] data = Base58.decode(line);
			out.write(Arrays.copyOfRange(data, 1,data.length-4));
		}
		out.close();
	}
}
