package com.tawana.demo;

public class AvgJob extends Thread {

	
	@Override
	public void run() {
		
     	int avg=SumJob.sum/10;
		System.out.println("avg >> "+avg);
		
		
	}
}
