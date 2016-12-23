package com.opsunv.btc.cracker;

import java.io.File;

import org.apache.commons.io.FileUtils;

public class AddressBalanceChecker {
	public static void main(String[] args) throws Exception{
		for(String str:FileUtils.readLines(new File("c:/1.txt"))){
			String[] arr = str.split(",");
			long balance = Utils.getReceivedBalance(arr[0]);
			if(balance>0){
				System.out.println(str+","+balance+","+Utils.getBalance(arr[0]));
			}
		}
	}
}
