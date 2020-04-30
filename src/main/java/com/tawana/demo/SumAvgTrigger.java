package com.tawana.demo;

public class SumAvgTrigger {

	public static void main(String[] args) throws InterruptedException {
		
		System.out.println("Start.....1");
		SumJob job=new SumJob();
	    job.start();
	    job.join();
	    System.out.println("join.....");
	    
	    AvgJob avgJob=new AvgJob();
	    avgJob.start();
	    
	    System.out.println("Avg.....1");
	    
		
	}

}
