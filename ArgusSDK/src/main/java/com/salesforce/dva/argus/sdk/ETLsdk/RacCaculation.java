package com.salesforce.dva.argus.sdk.ETLsdk;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.salesforce.dva.argus.sdk.ArgusService;
import com.salesforce.dva.argus.sdk.entity.Metric;
import com.salesforce.dva.argus.sdk.propertysdk.Property;
import com.salesforce.dva.argus.sdk.transfer.TransferService;

public class RacCaculation {
	private static String PODSADDR="src/test/resources/pods.txt";
	private static String ETLADDR="src/test/resources/etl.properties";
	private static int CONCURRENCY = 10;
	private static int START =-0;
	private static int END =-25;

	volatile public static int globalcount=0;
	volatile public static int globaltotal=0;
	
	private final TransferService _transferService;
	
	public RacCaculation(TransferService transferService){
		this._transferService=transferService;
	}
	
	
	public static void main(String[] args) throws IOException{
		if (args.length>0){
			ETLADDR=args[0];
			PODSADDR=args[1];
		}
		
		@SuppressWarnings("unchecked")
		Map<String,String> property=Property.of(ETLADDR).get();
		
		System.out.println("ArgusETL Service v12.5");
		System.out.println("System loading ETL property from +"+ETLADDR);
		System.out.println("System loading pod property from +"+PODSADDR);
		CONCURRENCY=Integer.valueOf(property.get("CONCURRENCY"));
		
		ExecutorService es = Executors.newFixedThreadPool(CONCURRENCY);
		ArgusService sourceSVC = ArgusService.getInstance(property.get("SourceSVCendpoint"), CONCURRENCY);
		ArgusService targetSVC = ArgusService.getInstance(property.get("TargetSVCendpoint"), CONCURRENCY);	
		TransferService ts=TransferService.getTransferService(sourceSVC, targetSVC);
		sourceSVC.getAuthService().login(property.get("Username"),property.get("Password"));
		targetSVC.getAuthService().login(property.get("Username"),property.get("Password"));
		

		final String PRODSANDBOX="PROD";	
		final String start="1480291200000";
		final String end="1480895940000";

		List<String> pods=getListOfPod(start,end,sourceSVC, PRODSANDBOX);
		
		for(String pod:pods){
			Runnable r=new CompletableRacCaculationJob(sourceSVC,pod,start,end);
			es.execute(r);
		}
		es.shutdown();
		System.out.println("ALL TASK FINISHED");
	}
	
	 /** UTIL: for get a list of pod name
	 * @param sourceSVC
	 * @param prodOrSand
	 * @return
	 * @throws IOException
	 */
	private static List<String> getListOfPod(final String start, final String end, final ArgusService sourceSVC,final String PROD_SANDBOX) throws IOException{
		final List<String> dcs=Arrays.asList("CHI","WAS","PHX","DFW","FRF","LON","PAR","TYO","WAX");
		List<String> collected=new ArrayList<String>();
		for (String ex:dcs){
			final String processed = start+":"+end+":REDUCED.db."+PROD_SANDBOX+"."+ex+".*:Traffic:avg";
			List<Metric> localmetrics=sourceSVC.getMetricService().getMetrics(expressionCleanUp(Arrays.asList(processed)));
			collected.addAll(localmetrics.stream().map(m -> m.getScope()).collect(Collectors.toList()));
		}
			
		return collected.stream().map(s -> {
			String supperPod=s.split("\\.")[4];
//			if (supperPod.equals("NONE")){
//				supperPod="AGG";
//			}
			String pod=s.split("\\.")[3]+"."+supperPod+"."+s.split("\\.")[5];
			return pod;
		}).collect(Collectors.toList());
	}
	
	protected static List<String> expressionCleanUp(List<String> expressions){
		assert(expressions!=null && expressions.size()>0):"input not valid";
		List<String> r= expressions
				.stream().sequential()
				.map(e -> URLEncoder.encode(e))
				.collect(Collectors.toList());
		return r;
	}

}



class CompletableRacCaculationJob implements Runnable{	
	final private String start;
	final private String end;
	final private String pod;
	final private ArgusService sourceSVC;
	
	public CompletableRacCaculationJob(
			ArgusService sourceSVC,
			final String pod,
			final String start,
			final String end
			){
		this.sourceSVC=sourceSVC;
		this.pod=pod;
		this.start=start;
		this.end=end;
	}
	
	
	@Override
	public void run() {
		System.out.println("\tJob acknowledged.... Reducing: " + pod + " on thread " + Thread.currentThread().getName());
		
		String localexp="DOWNSAMPLE(HEIMDALL("
				+ start+":"+end+":core."+pod+":SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode*.Last_1_Min_Avg{device=*-app*-*.ops.sfdc.net}:avg, "
				+ start+":"+end+":core."+pod+":SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode*.Last_1_Min_Avg{device=*-app*-*.ops.sfdc.net}:avg, "
				+ start+":"+end+":db.oracle."+pod+":*.active__sessions{device=*}:avg, "
				+ start+":"+end+":system."+pod+":CpuPerc.cpu.system{device=*-db*ops.sfdc.net}:avg,"
				+ start+":"+end+":system."+pod+":CpuPerc.cpu.user{device=*-db*ops.sfdc.net}:avg, "
				+ "#RACHOUR#),#100d-sum#)";
		List<Metric> resultms=new ArrayList<Metric>();
		
		try {
			resultms = sourceSVC.getMetricService().getMetrics(RacCaculation.expressionCleanUp(Arrays.asList(localexp)));
		} catch (Exception e1) {
			System.out.println("ERROR AT.....");
			System.out.println(localexp);
			e1.printStackTrace();
		}
		
		assert(resultms.size()>0):"can not continue";
		Map<String,Double> ImpactedMap=new HashMap<String, Double>();
		Map<String,Double> CollectedMap=new HashMap<String, Double>();
		for(Metric m:resultms){
			final String scope=m.getScope();
			final String metricname=m.getMetric();
			if(scope.equals("ImpactedMin")){
				ImpactedMap.put(m.getMetric(),getFirstValueAndCast(m.getDatapoints()));
			}
			if(scope.equals("CollectedMin")){
				CollectedMap.put(m.getMetric(),getFirstValueAndCast(m.getDatapoints()));
			}
		}
		
		//resultms.forEach(m -> System.out.println(m.getScope()+m.getMetric()+m.getDatapoints()));
		
		//NOW CACULATE
		assert(ImpactedMap.size()==CollectedMap.size()):"two hashmap should align up";
		
		int localcount=0;
		int localtotal=0;
		for(Entry<String, Double> e:ImpactedMap.entrySet()){
			final Double impactedMin=e.getValue();
			final Double collectedMin=CollectedMap.get(e.getKey());
			final Double avaRate=1.0-(impactedMin/collectedMin);
			
			localtotal++;
			if(avaRate>=0.999){
				localcount++;
			}
			System.out.println(localcount+"/"+localtotal+" "+e.getKey()+": "+avaRate);
		}
//		System.out.println("CurrentThread: "+localcount+" / "+localtotal);
		
		synchronized (this) {
			RacCaculation.globalcount+=localcount;
			RacCaculation.globaltotal+=localtotal;
			System.out.println("globalcount"+RacCaculation.globalcount+"/"+RacCaculation.globaltotal);
		}
	}

	
	/**
	 * 
	 * @param datapoints
	 * @return
	 */
	private static Double getFirstValueAndCast(Map<Long,String> datapoints){
		assert(datapoints.size()>0):"empty datapoints";
		return Double.valueOf((String) datapoints.values().toArray()[0]);
	}
}