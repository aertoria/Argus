package com.salesforce.dva.argus.sdk.transfer;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

class SimpleExecutor {
	public static void main(String[] args) {
		ExecutorService es = Executors.newFixedThreadPool(2);
		for (int i = 0; i < 6; i++) {
			Runnable job = new MyJob2();
			es.execute(job);
		}
		System.out.println("All jobs submitted...");
		es.shutdown();
		System.out.println("Executor service shut down...");
	}
}



class MyJob2 implements Runnable {
	private static int nextId = 0;
	private int myId = nextId++;
	List<Integer> l=Arrays.asList(1,2,3,4);
	
	@Override
	public void run() {
		l.stream().map(i -> URLEncoder.encode(String.valueOf(i)))
		.collect(Collectors.toList());
		
		System.out.println("Job " + myId + " starting on thread " + Thread.currentThread().getName());
		try {
//			Thread.sleep(ThreadLocalRandom.current().nextInt(500, 3000));
			System.out.println("local start");
			Thread.sleep(3000);
			System.out.println("local finish");
		} catch (InterruptedException ex) {
			Logger.getLogger(MyJob2.class.getName()).log(Level.SEVERE, null, ex);
		}
		System.out.println("Job " + myId + " endingXXX");
	}
}