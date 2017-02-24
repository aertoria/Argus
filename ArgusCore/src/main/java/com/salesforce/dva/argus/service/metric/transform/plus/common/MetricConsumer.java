package com.salesforce.dva.argus.service.metric.transform.plus.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.salesforce.dva.argus.entity.Metric;


/**
 * Class MetricConsumer
 * 
 * @author aertoria ethan.wang@salesforce.com
 * <a>This Class parse incoming metric into pojo for other common classes</a>
 * 
 **/
final public class MetricConsumer {
	private String racServerAddress;
	private String appServerAddress;
	private ConsumerTypes consmuerType;
	private Map<Long,String> datapoints;
	
	private MetricConsumer(){}
	/**static factory**/
	public static MetricConsumer getMetricConsumer(Metric m){
		//Differeciate m, make it consumeable
		MetricConsumer self=new MetricConsumer();
		String scopeSource=m.getScope();
		String metricSource=m.getMetric();
		String tagSource=m.getTag("device");
		
		if(Pattern.matches("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode.*.Last_1_Min_Avg",metricSource)){
			//SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg
			self.consmuerType=ConsumerTypes.APT_TIME_APPLEVEL;
			
			String[] racAddressSplit = metricSource.split("\\.");
			ArrayList<String> racAddressSplitList=new ArrayList<String>(Arrays.asList(racAddressSplit));
			assert(racAddressSplitList.size()==2):"should include one dot in the middle";
			String racAddress=racAddressSplitList.get(0).substring(61);
			
			String podAddress=scopeSource.substring(5);
			assert(racAddress!=null&&racAddress.length()>0&&podAddress!=null&&podAddress.length()>10):"Invalid pod or rac address+"+scopeSource+"."+metricSource;
			self.racServerAddress=podAddress+".Rac"+racAddress;
			
			String appAddress=tagSource.substring(5, 11);
			assert(appAddress!=null&&appAddress.length()>4):"Invalid app address+"+appAddress;
			self.appServerAddress=appAddress;
			
		}else if(Pattern.matches("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode.*.Last_1_Min_Avg",metricSource)){
			//SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg
			self.consmuerType=ConsumerTypes.APT_TRAFFIC_APPLEVEL;
			
			String[] racAddressSplit = metricSource.split("\\.");
			ArrayList<String> racAddressSplitList=new ArrayList<String>(Arrays.asList(racAddressSplit));
			assert(racAddressSplitList.size()==2):"should include one dot in the middle";
			String racAddress=racAddressSplitList.get(0).substring(62);
			
			String podAddress=scopeSource.substring(5);
			assert(racAddress!=null&&racAddress.length()>0&&podAddress!=null&&podAddress.length()>10):"Invalid pod or rac address+"+scopeSource+"."+metricSource;
			self.racServerAddress=podAddress+".Rac"+racAddress;
			
			String appAddress=tagSource.substring(5, 11);
			assert(appAddress!=null&&appAddress.length()>4):"Invalid app address+"+appAddress;
			self.appServerAddress=appAddress;
			
		}else if(Pattern.matches(".*.active__sessions",metricSource)){
			//CSDB15.CSDB15-3.active__sessions
			self.consmuerType=ConsumerTypes.ACT_RACLEVEL;
			
			String[] racAddressSplit = metricSource.split("\\.");
			ArrayList<String> racAddressSplitList=new ArrayList<String>(Arrays.asList(racAddressSplit));
			assert(racAddressSplitList.size()==3):"act should be in format such as CNADB11, NADB11-1, active__sessions";
			ArrayList<String> racAddressLocalList=new ArrayList<String>(Arrays.asList(racAddressSplitList.get(1).split("-")));
			assert(racAddressLocalList!=null && racAddressLocalList.size()==2):"local act should be format as NADB11-1";
			String racAddress=racAddressLocalList.get(1);
			
			String podAddress=scopeSource.substring(10);
			//TEMP
			podAddress=t_superPodConvert(podAddress);
			assert(racAddress!=null&&racAddress.length()>0&&podAddress!=null&&podAddress.length()>10):"Invalid pod or rac address+"+scopeSource+"."+metricSource;
			self.racServerAddress=podAddress+".Rac"+racAddress;
			self.appServerAddress="RACLEVEL";
		}else if(Pattern.matches("CpuPerc.cpu.system",metricSource)){
			//CpuPerc.cpu.system
			self.consmuerType=ConsumerTypes.CPU_SYS_RACLEVEL;			
			String device=m.getTag("device");
			String[] racAddressSplit = device.split("-");
			assert(racAddressSplit!=null && racAddressSplit.length>=4):"format should be cs15-db1-1-chi.ops.sfdc.net however get:"+device;
			String racAddress=racAddressSplit[2];
			
			String podAddress=scopeSource.substring(7);
			//TEMP
			podAddress=t_superPodConvert(podAddress);
			assert(podAddress!=null&&podAddress.length()>9&&podAddress.split("\\.").length==3):"podAddress should have format such as CHI.SP2.cs15, however is"+podAddress;
			self.racServerAddress=podAddress+".Rac"+racAddress;
			self.appServerAddress="RACLEVEL";
			
		}else if(Pattern.matches("CpuPerc.cpu.user",metricSource)){
			//CpuPerc.cpu.system
			self.consmuerType=ConsumerTypes.CPU_USER_RACLEVEL;			
			String device=m.getTag("device");
			String[] racAddressSplit = device.split("-");
			assert(racAddressSplit!=null && racAddressSplit.length>=4):"format should be cs15-db1-1-chi.ops.sfdc.net however get:"+device;
			String racAddress=racAddressSplit[2];
			
			String podAddress=scopeSource.substring(7);
			//TEMP
			podAddress=t_superPodConvert(podAddress);
			assert(podAddress!=null&&podAddress.length()>9&&podAddress.split("\\.").length==3):"podAddress should have format such as CHI.SP2.cs15, however is"+podAddress;
			self.racServerAddress=podAddress+".Rac"+racAddress;
			self.appServerAddress="RACLEVEL";
			
		}else{
			throw new RuntimeException("This type of input is not supported: metric name "+metricSource);
		}
		
		self.datapoints=m.getDatapoints();
		self.cleanup();
		return self;
	}
	
	/**
	 * Temporary pod name.convert from AGG back to NONE so that it can link.
	 * @param pod
	 * @return
	 */
	private static String t_superPodConvert(final String pod){
		String[] podSplit=pod.split("\\.");
		assert(podSplit.length==3):"pod has to be simlar to TYO.AGG.ap2";
		if(podSplit[1].equals("AGG")){
			return podSplit[0]+".NONE."+podSplit[2];
		}
		return pod;
	}
	
	/**return SCOPE:METRIC{tags}            :avg
	 *		  rac :ConsumerType{device=app}:avg
	 ***/
	public Metric getSelfAsMetric(){
		Metric cMetric=new Metric(racServerAddress,this.consmuerType.toString());
		cMetric.setTag("device", this.appServerAddress);
		cMetric.setDatapoints(this.datapoints);
		return cMetric;
	}
	
	/**getters**/
	public String getRacServerAddress(){
		return this.racServerAddress;
	}
	
	public String getAppServerAddress(){
		return this.appServerAddress;
	}
	
	public ConsumerTypes getConsumerType(){
		return this.consmuerType;
	}

	private void cleanup(){
		//if datapoints are missing, fill it with zero.
		//if datapoints are short than expected, fill it with zero.
	}
	
	/**
	 * Internally use for inspection
	 */
	public void inspect(){
		System.out.println("\nInspect object MetricConsumer\nMetricType:\t\t"+this.consmuerType);
		System.out.println("racServerAddress:\t"+this.racServerAddress);
		System.out.println("appServerAddress:\t"+this.appServerAddress);
		//System.out.println("original details:\t\tMetric: "+this.metric.getMetric()+" tags:"+this.metric.getTag("device"));
		System.out.println("dataLength:\t\t"+this.datapoints.size());
		//System.out.println("sample datapoint:\t"+this.metric.getDatapoints());
	}
	
	public static enum ConsumerTypes {
		APT_TIME_APPLEVEL,
		APT_TRAFFIC_APPLEVEL,
		ACT_RACLEVEL,
		CPU_SYS_RACLEVEL,
		CPU_USER_RACLEVEL;
	}

	public static List<MetricConsumer> consumeMetrics(List<Metric> metrics){
		assert(metrics != null):"metrics input can not be null";
		List<MetricConsumer> listConsumers=new ArrayList<MetricConsumer>();
		listConsumers=metrics.stream()
				.map(m -> MetricConsumer.getMetricConsumer(m))
				.collect(Collectors.toList());
		return listConsumers;
	}
}
