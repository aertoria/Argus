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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.salesforce.dva.argus.sdk.ArgusService;
import com.salesforce.dva.argus.sdk.ArgusService.PutResult;
import com.salesforce.dva.argus.sdk.entity.Metric;
import com.salesforce.dva.argus.sdk.propertysdk.Property;
import com.salesforce.dva.argus.sdk.transfer.TransferService;

/**
 *
 * @author ethan.wang
 *
 */
public class CachedETL implements Serializable{
	private static String PODSADDR="src/test/resources/pods.txt";
	private static String ETLADDR="src/test/resources/etl.properties";
	private static int CONCURRENCY = 10;
	private static int START =-0;
	private static int END =-25;
	
	private final TransferService _transferService;
	
	/**
	 * Get the DR relationship of each datacenter
	 * @author ethan.wang
	 *
	 */
	private static enum DRDataCenters{
		DFW("PHX"),
		PHX("DFW"),
		WAS("CHI"),
		CHI("WAS"),
		FRF("LON"),
		LON("FRF"),
		TYO("TYO"),
		SJL("SJL"),
		PAR("LON"),
		WAX("WAX");
		private final String drDataCenter;
		private DRDataCenters(String drDataCenter){
			this.drDataCenter=drDataCenter;
		}
		private String getDrDataCenter(){
			return drDataCenter;
		}
	}
	
	/**
	 * 
	 * @param podAddress
	 * @return
	 */
	private static String getDR(String podAddress){
		assert(podAddress!=null && podAddress.split("\\.").length==3):"input not valid";
		String dc=podAddress.split("\\.")[0];
		String drdc=DRDataCenters.valueOf(dc).getDrDataCenter();
		return drdc+"."+podAddress.split("\\.")[1]+"."+podAddress.split("\\.")[2];
	}
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
	public static void main(String[] args) throws IOException{
		if (args.length>0){
			ETLADDR=args[0];
			PODSADDR=args[1];
		}
		System.out.println("ArgusETL Service v12.5");
		System.out.println("System loading ETL property from +"+ETLADDR);
		System.out.println("System loading pod property from +"+PODSADDR);
		

		@SuppressWarnings("unchecked")
		Map<String,String> property=Property.of(ETLADDR).get();
		
		CONCURRENCY=Integer.valueOf(property.get("CONCURRENCY"));
		START=Integer.valueOf(property.get("START"));
		END=Integer.valueOf(property.get("END"));
		
		ExecutorService es = Executors.newFixedThreadPool(CONCURRENCY);
		ArgusService sourceSVC = ArgusService.getInstance(property.get("SourceSVCendpoint"), CONCURRENCY);
		ArgusService targetSVC = ArgusService.getInstance(property.get("TargetSVCendpoint"), CONCURRENCY);	
		TransferService ts=TransferService.getTransferService(sourceSVC, targetSVC);
		sourceSVC.getAuthService().login(property.get("Username"),property.get("Password"));
		targetSVC.getAuthService().login(property.get("Username"),property.get("Password"));
		List<String> podAddressList=getPods();
		List<Long> timeRange=getTimeRange(START,END);
		
		
		for (int i = 0; i < podAddressList.size(); i++){
			int localCount=i;
			int totalCount=podAddressList.size();
			String podAddress=podAddressList.get(i);
			String drPodAddress = getDR(podAddress);
			Runnable r=CompletableCacheJob.schedule(ts, podAddress, drPodAddress, timeRange.get(0), timeRange.get(1),localCount,totalCount);
			es.execute(r);
		}
		
		es.shutdown();
		System.out.println("ALL TASK FINISHED");
	}
	
	/**
	 * given retro hours, return start and end time range
	 * @param retroHour
	 * @return
	 */
	private static List<Long> getTimeRange(final int start,final int end){
		Long currentTimeStamp=(System.currentTimeMillis());
		Long startTimeStamp=currentTimeStamp+start*(3600*1000);
		Long endTimeStamp=currentTimeStamp+end*(3600*1000);
		return Arrays.asList(Long.valueOf(endTimeStamp),Long.valueOf(startTimeStamp));
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
	final private String _podAddress,_drPodAddress;
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
	public CompletableCacheJob(final TransferService transferService,final String podAddress,final String drPodAddress,final Long startTimestamp, final Long endTimestamp){
		this._transferService=transferService;
		this._podAddress=podAddress;
		this._drPodAddress=drPodAddress;
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
	public static CompletableCacheJob schedule(final TransferService transferService, final String podAddress, final String drPodAddress, final Long startTimestamp, final Long endTimestamp){
		CompletableCacheJob self=new CompletableCacheJob(transferService,podAddress,drPodAddress,startTimestamp,endTimestamp);
		return self;
	}
	
	/**
	 * override
	 * @param transferService
	 * @param podAddress
	 * @param startTimestamp
	 * @param endTimestamp
	 * @param localCount
	 * @param totalCount
	 * @return
	 */
	public static CompletableCacheJob schedule(final TransferService transferService, final String podAddress, final String drPodAddress, final Long startTimestamp, final Long endTimestamp, final int localCount, final int totalCount){
		CompletableCacheJob self=new CompletableCacheJob(transferService,podAddress,drPodAddress,startTimestamp,endTimestamp);
		self.localCount=localCount;
		self.totalCount=totalCount;
		return self;
	}
	
	/**
	 * Executed by executor
	 */
	@Override
	public void run(){
		// TODO Auto-generated method stub
		System.out.println("\tJob acknowledged.... Reducing: " + _podAddress + " on thread " + Thread.currentThread().getName());
		System.out.print("  "+this.localCount+" / "+this.totalCount);
		
		try{
			makeATransfer(_transferService, _podAddress, _startTimestamp, _endTimestamp);
		} catch (Exception e) {
			System.out.println(">>>>Not successful. Now trying DR site");
			run_drdc();
		}
		System.out.println("Job " + _podAddress + " ending...");	
	}
	
	
	/**
	 * recursively called for each DC, if not successful, try DRDC
	 * @param podAddress
	 */
	private void run_drdc(){
		System.out.println("\tDR Job acknowledged.... Reducing: " + _drPodAddress + " on thread " + Thread.currentThread().getName());
		System.out.print("  "+this.localCount+" / "+this.totalCount);
		try{
			makeATransfer(_transferService, _drPodAddress, _startTimestamp, _endTimestamp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Job " + _drPodAddress + " ending...");	
	}
	
		
	/**
	 * given pod address, fetch the 5 result about this pod each hour, load it to predified scope metric
	 * @param transferService
	 * @param podAddress
	 * @throws IOException 
	 */
	public PutResult makeATransfer(final TransferService transferService, final String podAddress, final Long startTimestamp, final Long endTimestamp) throws IOException{
		final String sourceExp=getExpressionFromAddress(podAddress,startTimestamp,endTimestamp);
		final String targetScope=getTargetScopeNameSplitProductionSandbox(podAddress);
		return transferService.transfer(sourceExp,targetScope);
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
				+ startTime + ":" + endTime + ":core."+podAddress+":SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode*.Last_1_Min_Avg{device=*-app*-*.ops.sfdc.net}:avg,"
				+ startTime + ":" + endTime + ":core."+podAddress+":SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode*.Last_1_Min_Avg{device=*-app*-*.ops.sfdc.net}:avg,"
				+ startTime + ":" + endTime + ":db.oracle."+podAddress+":*.active__sessions{device=*}:avg,"
				+ "#POD#)";
		return expression;
	}
	
	/**
	 * Retrun a scope name
	 * @param podAddress
	 * @return
	 */
	private static String getTargetScopeName(final String podAddress) {
		final String scopeName="REDUCEDTEST2.core."+podAddress;
		return scopeName;
	}
	
	/**
	 * 
	 * OVERLOADS
	 */
	private static String getTargetScopeNameSplitProductionSandbox(final String podAddress) {
		final boolean isMatch = Pattern.matches(".*.cs.*", podAddress);
		if(isMatch){
			return "REDUCED.db.SANDBOX"+"."+podAddress;
		}
		return "REDUCED.db.PROD"+"."+podAddress;
	}
}
