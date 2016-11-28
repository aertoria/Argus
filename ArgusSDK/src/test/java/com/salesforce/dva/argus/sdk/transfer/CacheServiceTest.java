package com.salesforce.dva.argus.sdk.transfer;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.salesforce.dva.argus.sdk.AbstractTest;
import com.salesforce.dva.argus.sdk.ArgusService;
import com.salesforce.dva.argus.sdk.entity.Metric;
import com.salesforce.dva.argus.sdk.propertysdk.Property;
import com.salesforce.dva.argus.sdk.ETLsdk.CachedETL;
/**
 * TODO: 
 * 1,mutliThreading
 * 2,ProgressBar
 * 3,Error handling.Error detection for re-run
 * 
 * @author ethan.wang
 *
 */
public class CacheServiceTest {
	static ArgusService sourceSVC;
	static ArgusService targetSVC;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sourceSVC = ArgusService.getInstance("http://ewang-ltm.internal.salesforce.com:8080/argusws", 10);
		//sourceSVC = ArgusService.getInstance("https://argus-ws.data.sfdc.net/argusws", 10);
		targetSVC = ArgusService.getInstance("https://argus-ws.data.sfdc.net/argusws", 10);

		@SuppressWarnings("unchecked")
		Map<String,String> property=Property.of("src/test/resources/etl.properties").get();
		sourceSVC.getAuthService().login(property.get("Username"),property.get("Password"));
		targetSVC.getAuthService().login(property.get("Username"),property.get("Password"));	
		
		System.out.println("System initalized, login finished");

		
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		sourceSVC.close();
		targetSVC.close();
	}

	
	@Test 
	public void testProperty() throws IOException{	
		TransferService ts=TransferService.getTransferService(sourceSVC, targetSVC);
		
		int currentTimeStamp=Math.round((System.currentTimeMillis())/1000);
		System.out.println(currentTimeStamp);
		int startTimeStamp=currentTimeStamp-11*(24*3600);
		System.out.println(startTimeStamp);
	}
	
}


//	
//	
////	@Test
//	public void transfer() throws IOException {
//		TransferService ts=TransferService.getTransferService(sourceSVC, targetSVC);
////		String expression="HEIMDALL("
////				+ "1477094400:1477180800:core.CHI.SP3.na5:SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode*.Last_1_Min_Avg{device=*-app*-*.ops.sfdc.net}:avg, "
////				+ "1477094400:1477180800:core.CHI.SP3.na5:SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode*.Last_1_Min_Avg{device=*-app*-*.ops.sfdc.net}:avg,"
////				+ "#IMPACTPOD#)";
//		List<String> pods=getPods();
//		int count=pods.size();
//		getPods().forEach(exp -> {
//			makeATransfer(ts,exp);
//		});
//	}
//	
//	private void makeATransfer(TransferService ts, String exp){
//		String sourceExp=getSource(exp);
//		String targetScope=getTargetScopeName(exp);
//		System.out.println("Loading to "+targetScope+" from expression"+sourceExp);
//		try {
//			//List<Metric> ms=ts.readFromSource(Arrays.asList(sourceExp));
//			//ms.forEach(m -> System.out.println(m.getMetric()+m.getDatapoints()));
//			ts.transfer(sourceExp,targetScope,"IMPACTPOD");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}	
//	
//	private List<String> getPods() throws IOException{
//		InputStream is=getClass().getResourceAsStream("/pods.txt");
//		BufferedReader br = new BufferedReader(new InputStreamReader(is));
//		String line;
//		List<String> pods=new ArrayList<String>();
//		while ((line = br.readLine()) != null) {
//			pods.add(line);
//		}
//		return pods;
//	}
//	
//	private String getSource(String t) {
//		String expression="HEIMDALL("
//				+ "1477094400:1477180800:core."+t+":SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode*.Last_1_Min_Avg{device=*-app*-*.ops.sfdc.net}:avg, "
//				+ "1477094400:1477180800:core."+t+":SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode*.Last_1_Min_Avg{device=*-app*-*.ops.sfdc.net}:avg,"
//				+ "#POD#)";
//		return expression;
//	}
//	
//	
//	private String getTargetScopeName(String t) {
//		String expression="REDUCEDTEST.core."+t;
//		return expression;
//	}
//	
//}
//
//
//class CompletableCacheJob implements Serializable{
//	public CompletableFuture<String> doETL(String podAddress,Long StartTime, Long EndTime) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	
//	
//	/**
//	 * given pod address, fetch the 5 result about this pod each hour, load it to predified scope metric
//	 * @param transferService
//	 * @param podAddress
//	 */
//	public void makeATransfer(final TransferService transferService, final String podAddress){
//		final String sourceExp=getExpressionFromAddress(podAddress,1477094400L,1477180800L);
//
//		final String targetScope=getTargetScopeName(podAddress);
//		
//		try {
//			System.out.println("\nexecuting\n"+sourceExp+"\n");
//			transferService.transfer(sourceExp,targetScope);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	/**
//	 * Return a executable expression to retrieve result from source
//	 * @param podAddress
//	 * @param startTime
//	 * @param endTime
//	 * @return
//	 */
//	private String getExpressionFromAddress(String podAddress, Long startTime, Long endTime) {
//		final String expression="HEIMDALL("
//				+ startTime + ":" + endTime + ":core."+podAddress+":SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode*.Last_1_Min_Avg{device=*-app*-*.ops.sfdc.net}:avg, "
//				+ startTime + ":" + endTime + ":core."+podAddress+":SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode*.Last_1_Min_Avg{device=*-app*-*.ops.sfdc.net}:avg,"
//				+ "#POD#)";
//		return expression;
//	}
//	
//	/**
//	 * Retrun a scoe name
//	 * @param podAddress
//	 * @return
//	 */
//	private String getTargetScopeName(String podAddress) {
//		final String scopeName="REDUCEDTEST2.core."+podAddress;
//		return scopeName;
//	}
//}
//
//
//
//
////CLASS OF CLASS CLASS
//class CompFutureExample {
//	public static CompletableFuture<String> readFromFile(String filename) {
//		CompletableFuture<String> cfs = new CompletableFuture<>();
//		new Thread(() -> {
//			System.out.println("Starting async file read (not really!) on file " + filename);
//			delay();
//			String result = "I read this data from the file " + filename;
//			cfs.complete(result);
//		}).start();
//		return cfs;
//	}
//
//	public static void delay() {
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException ex) {
//		}
//	}
//
//	public static void main(String[] args) {
//		CompletableFuture<String> cfs = CompletableFuture.supplyAsync(() -> {
//			delay();
//			System.out.println("StageOne processing...");
//			return "theDataFile.txt";
//		});
//		System.out.println("Stage one configured...");
//		cfs = cfs.thenComposeAsync(CompFutureExample::readFromFile);
//		System.out.println("File Reader configured...");
//		cfs = cfs.thenApplyAsync(x -> {
//			delay();
//			System.out.println("StageOne processing...");
//			return "Stage two processed " + x;
//		});
//		System.out.println("Stage two configured...");
//		cfs.thenAcceptAsync(System.out::println);
//		System.out.println("Final stage configured...");
//		cfs.join();
//		System.out.println("Pipeline completed...");
//	}
//}



