package com.hyls.utils;

import java.util.UUID;

public class UuidUtils {
	
	
	public static String getUUID() {

		return UUID.randomUUID().toString().replace("-", "");
	}

	public static void main(String[] args) {
		for(int i=0;i<=123;i++)
		System.out.println(getUUID());
	}
}	
