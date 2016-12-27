package com.opsunv.btc.cracker;

import java.io.File;

import org.apache.commons.io.FileUtils;

public class AddressBalanceChecker {
	public static void main(String[] args) throws Exception{
		for(String str:FileUtils.readLines(new File("c:/1.txt"))){
			String[] arr = str.split(",");
			String info = Utils.getAddressInfo(arr[0], true);
			if(info!=null){
				System.out.println(str+","+info);
			}
		}
	}
}
