package com.salesforce.dva.argus.sdk.ETLsdk;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URLEncoder;
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
import java.util.stream.Collectors;

import com.salesforce.dva.argus.sdk.ArgusService;
import com.salesforce.dva.argus.sdk.entity.Metric;
import com.salesforce.dva.argus.sdk.propertysdk.Property;
import com.salesforce.dva.argus.sdk.transfer.TransferService;

/**
 *
 * @author ethan.wang
 *
 */
public class CachedETL implements Serializable{
	private static final String PODSADDR="src/test/resources/pods.txt";
	private final TransferService _transferService;
	
	
	/**
	 * 
	 * @param transferService
	 */
	private CachedETL(TransferService transferService){
		this._transferService=transferService;
	}
	
	/**
	 * 
	 * @return
	 */
	public static CachedETL of(TransferService transferService){
		CachedETL self=new CachedETL(transferService);
		return self;
	}
	

	/**
	 * 
	 * @param so
	 * @throws IOException
	 */
	public static void main(String[] so) throws IOException{
		ArgusService sourceSVC = ArgusService.getInstance("http://ewang-ltm.internal.salesforce.com:8080/argusws", 20);
		ArgusService targetSVC = ArgusService.getInstance("https://argus-ws.data.sfdc.net/argusws", 20);
		@SuppressWarnings("unchecked")
		Map<String,String> property=Property.of("src/test/resources/etl.properties").get();
		sourceSVC.getAuthService().login(property.get("Username"),property.get("Password"));
		targetSVC.getAuthService().login(property.get("Username"),property.get("Password"));
		TransferService ts=TransferService.getTransferService(sourceSVC, targetSVC);
		
		
		ExecutorService es = Executors.newFixedThreadPool(20);
		List<String> podAddressList=getPods();
				
		for (int i = 0; i < podAddressList.size(); i++){
			int localCount=i;
			int totalCount=podAddressList.size();
			String podAddress=podAddressList.get(i);
			Runnable r=CompletableCacheJob.schedule(ts, podAddress, 1479163981L, 1479250392L,localCount,totalCount);
			es.execute(r);
		}
		
		es.shutdown();
		System.out.println("ALL TASK FINISHED");
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	private static List<String> getPods() throws IOException{
		InputStream is = new FileInputStream(CachedETL.PODSADDR);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		List<String> pods=new ArrayList<String>();
		while ((line = br.readLine()) != null) {
			pods.add(line);
		}
		return pods;
	}	
}






/**
 * 1, taking logged-in ArgusService as dependencies
 * 2, taking a pod full name as inputs
 * 2, a new thread, do all the struff, and return a completableFuture
 * @author ethan.wang
 */
class CompletableCacheJob implements Runnable{
	final private TransferService _transferService;
	final private String _podAddress;
	final private Long _startTimestamp,_endTimestamp;
	private int localCount;
	private int totalCount;
	
	/**
	 * 
	 * @param transferService
	 * @param podAddress
	 * @param startTimestamp
	 * @param endTimestamp
	 */
	public CompletableCacheJob(final TransferService transferService,final String podAddress,final Long startTimestamp, final Long endTimestamp){
		this._transferService=transferService;
		this._podAddress=podAddress;
		this._startTimestamp=startTimestamp;
		this._endTimestamp=endTimestamp;
	}
	
	/**
	 * static factory method
	 * @param transferService
	 * @param podAddress
	 * @param startTimestamp
	 * @param endTimestamp
	 * @return
	 */
	public static CompletableCacheJob schedule(final TransferService transferService, final String podAddress, final Long startTimestamp, final Long endTimestamp){
		CompletableCacheJob self=new CompletableCacheJob(transferService,podAddress,startTimestamp,endTimestamp);
		return self;
	}
	
	/**
	 * 
	 * @param transferService
	 * @param podAddress
	 * @param startTimestamp
	 * @param endTimestamp
	 * @param localCount
	 * @param totalCount
	 * @return
	 */
	public static CompletableCacheJob schedule(final TransferService transferService, final String podAddress, final Long startTimestamp, final Long endTimestamp, final int localCount, final int totalCount){
		CompletableCacheJob self=new CompletableCacheJob(transferService,podAddress,startTimestamp,endTimestamp);
		self.localCount=localCount;
		self.totalCount=totalCount;
		return self;
	}
	
	/**
	 * Executed by executor
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("\tJob acknowledged.... Reducing: " + _podAddress + " on thread " + Thread.currentThread().getName());
		System.out.print("  "+this.localCount+" / "+this.totalCount);
		makeATransfer(_transferService, _podAddress, _startTimestamp, _endTimestamp);
		System.out.println("Job " + _podAddress + " ending...");	
	}
		
	/**
	 * given pod address, fetch the 5 result about this pod each hour, load it to predified scope metric
	 * @param transferService
	 * @param podAddress
	 */
	public static void makeATransfer(final TransferService transferService, final String podAddress, final Long startTimestamp, final Long endTimestamp){
		final String sourceExp=getExpressionFromAddress(podAddress,startTimestamp,endTimestamp);
		final String targetScope=getTargetScopeName(podAddress);
		
		try {
			transferService.transfer(sourceExp,targetScope);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Return a executable expression to retrieve result from source
	 * @param podAddress
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	private static String getExpressionFromAddress(final String podAddress, final Long startTime, final Long endTime) {
		final String expression="HEIMDALL("
				+ startTime + ":" + endTime + ":core."+podAddress+":SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode*.Last_1_Min_Avg{device=*-app*-*.ops.sfdc.net}:avg, "
				+ startTime + ":" + endTime + ":core."+podAddress+":SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode*.Last_1_Min_Avg{device=*-app*-*.ops.sfdc.net}:avg,"
				+ "#POD#)";
		return expression;
	}
	
	/**
	 * Retrun a scoe name
	 * @param podAddress
	 * @return
	 */
	private static String getTargetScopeName(final String podAddress) {
		final String scopeName="REDUCEDTEST2.core."+podAddress;
		return scopeName;
	}
}








