package com.hyls.thread;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class UnsafeSequence implements Runnable{
	private static AtomicLong vAtomicLong = new AtomicLong(0);
	private static AtomicReference<BigInteger> lastNumber = new AtomicReference<BigInteger>();
	private static int value =0; 
	/*public int getNext() {
		return value++;
	}*/
	public void run() {
		// TODO Auto-generated method stub
		//value++;
		vAtomicLong.incrementAndGet();
		
	}
	
	public static void main(String[] args) {
		for(int i=0;i<100;i++) {
			new Thread(new UnsafeSequence()).start();;
		}
		try {
			Thread.sleep(3000);
			System.out.println(vAtomicLong.get());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
