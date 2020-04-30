package com.tawana.demo;

public class SumJob extends Thread {

	static int sum=0;
	@Override
	public void run() {
		for(int i=0;i<100000;i++) {
			sum += i;
		}
		System.out.println("sum is >> "+ sum);
	}
}
