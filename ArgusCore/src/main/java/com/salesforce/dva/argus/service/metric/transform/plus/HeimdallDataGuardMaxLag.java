package com.salesforce.dva.argus.service.metric.transform.plus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.collections.ListUtils;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.service.metric.transform.Transform;
import com.salesforce.dva.argus.service.metric.transform.TransformFactory;

/**
 * init by story https://gus.my.salesforce.com/apex/adm_userstorydetail?id=a07B0000002aHd3IAE&sfdc.override=1
 * This transform 
 * 
 * @author ethan.wang
 *
 */
public class HeimdallDataGuardMaxLag implements Transform{
	@Inject
	private Provider<TransformFactory> _transformFactory;
	
	/*
	 * duration threshold in minutes
	 */
	private Float durationThreshold;
	
	/**
	 * before use me: propate and downsample
	 * @param metrics
	 * @return
	 */
	@Override
	public List<Metric> transform(List<Metric> metrics) {
		throw new UnsupportedOperationException("require at least one metrics");
	}
	
	@Override
	public List<Metric> transform(List<Metric> metrics, List<String> constants) {
		
		assert(constants.size()==2):"2 constants need for DataGuardMaxLag transform";
		
		List<Metric> result = new ArrayList<Metric>();
		
		try{
			setDurationThreshold(constants);
		}
		catch(NumberFormatException e){
			System.out.println("NumberFormatException: cannot parse duration threshold constant");
			return	result;
		}
		
		for(Metric m:metrics){
			
			System.out.println("processing"+m.getScope()+" received"+m.getDatapoints().size());
			
			
			try{
				SingleHeimdallDataGuardMaxLag sdg=new SingleHeimdallDataGuardMaxLag(_transformFactory);
				List<Metric> localresult=sdg.transform(Arrays.asList(m), constants);
				if (localresult!=null){
					
					if(checkDurationThreshold(localresult)){
						result.addAll(localresult);
					}
					
				}
				
				System.out.println(m.getScope()+":"+localresult);
			}catch(Exception e){
				System.out.println("Exception found"+m.getScope()+e.getMessage()+e.getStackTrace().toString());
				continue;
			}
			
		}
		
//		List<Metric> result = 
//			  metrics.stream()
//			 .map(m -> new SingleHeimdallDataGuardMaxLag(_transformFactory).transform(Arrays.asList(m), constants))
//			 .flatMap(lm -> lm.stream())
//			 .collect(Collectors.toList());
		return result;
		
	}
	
	@Override
	public List<Metric> transform(List<Metric>... metrics) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getResultScopeName() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public void setDurationThreshold(List<String> constants){
		
		this.durationThreshold = (float)(Float.valueOf(constants.get(1))/60.0);
		return;
	}
	
	/*
	 *	aruikar@salesforce.com
	 *  Check duration of each incident and filter 
	 *  out incidents greater than given threshold
	 */
	public boolean checkDurationThreshold(List<Metric> localResults) {
		
		boolean aboveThreshold = false;
		
			for(Metric m:localResults){
				
				try{
		
					if(m.getMetric().equals("maxLagDuration")) {
						Map.Entry<Long,String> entry = m.getDatapoints().entrySet().iterator().next();
						int	retval = Float.compare(this.durationThreshold, Float.valueOf(entry.getValue()));
			    		if(retval <= 0) {
			    			aboveThreshold = true;
			    		}
			    		else {
			    			aboveThreshold = false;
			    		}	
			    		
			    		break;
					}
					else {
						continue;
					}
				
				}
				catch(Exception e){
					System.out.println("CheckDurationThreshold: Float compare excetion");
					continue;
				}
		
			}

		
		return aboveThreshold;
		
	}

}



/**
 * 
 * @author ethan.wang
 *
 */
final class SingleHeimdallDataGuardMaxLag{
	final private Provider<TransformFactory> _transformFactory;
	private NavigableMap<Long,String> sortedDatapoints;
	private Map<String, Long> resultMap=new HashMap<String, Long>();
	private String scope;
	private float threshold;
	
	protected SingleHeimdallDataGuardMaxLag(Provider<TransformFactory> _transformFactory){
		this._transformFactory=_transformFactory;
	}
	
	protected List<Metric> transform(List<Metric> metrics, List<String> constants){
		assert(metrics.size()==1 && constants.size()==2):"has to be single metrics 2 constants";
		
		/**
		 * PRE-PROCESS
		 */
		List<Metric> downsampled=getTransform("DOWNSAMPLE").transform(metrics,Arrays.asList("1m-avg"));
		List<Metric> propagated=getTransform("PROPAGATE").transform(downsampled,Arrays.asList("1m"));
		final List<Metric> readytoProcess=Collections.unmodifiableList(propagated.stream().map(m -> new Metric(m)).collect(Collectors.toList()));
		
		
		scope=metrics.get(0).getScope();
		threshold=Float.valueOf(constants.get(0));
		
		sortedDatapoints=new TreeMap<Long, String>(readytoProcess.get(0).getDatapoints());
		final long startTimestamp=sortedDatapoints.firstKey();
		

		Metric returningMetric = new Metric(scope, "maxLagDuration");
		Metric returningMetricPeak = new Metric(scope, "maxLagPeakValue");
		Map<Long,String> returningDatapoints=new HashMap<Long,String>();
		Map<Long,String> returningDatapointsPeak=new HashMap<Long,String>();
		
		if (sortedDatapoints.size()==0){
			returningDatapoints.put(startTimestamp, String.valueOf(0));
			returningMetric.setDatapoints(returningDatapoints);
			
			returningDatapointsPeak.put(startTimestamp, "NA");
			returningMetricPeak.setDatapoints(returningDatapointsPeak);
			return Collections.unmodifiableList(Arrays.asList(returningMetric,returningMetricPeak));
		}

		check_crawler();

		System.out.println("sortedDatapoints"+resultMap);
		
		if(resultMap.isEmpty()) {
			
			returningDatapoints.put(startTimestamp, String.valueOf(0));
			returningMetric.setDatapoints(returningDatapoints);
			
			returningDatapointsPeak.put(startTimestamp, "NA");
			returningMetricPeak.setDatapoints(returningDatapointsPeak);
			return Collections.unmodifiableList(Arrays.asList(returningMetric,returningMetricPeak));
			
		}
		
		final Long maxduration=resultMap.get(Collections.max(resultMap.keySet()));
		returningDatapoints.put(startTimestamp, String.valueOf(maxduration/60000+1));//return value in min
		returningMetric.setDatapoints(returningDatapoints);
		
		final String maxPeak = Collections.max(resultMap.keySet());
		returningDatapointsPeak.put(startTimestamp, maxPeak);//return value in sec, assuming the peak we talk about is sec
		returningMetricPeak.setDatapoints(returningDatapointsPeak);
		
		if (Float.valueOf(maxPeak)==0f){
			return null;
		}
		return Collections.unmodifiableList(Arrays.asList(returningMetric,returningMetricPeak));
	}
	
	private void check_crawler(){
		NavigableMap<Long, String> carryMap=new TreeMap<Long,String>();
		carryMap.put(sortedDatapoints.firstKey(), sortedDatapoints.firstEntry().getValue());
		Long current=sortedDatapoints.firstKey();
		
		while(true){
			
			Long next=sortedDatapoints.higherKey(current);
			
			if (next==null||sortedDatapoints.higherKey(next)==null){
				if (carryMap.size()>0){
					resultMap.put(
							Collections.max(carryMap.entrySet(),Map.Entry.comparingByValue()).getValue(),
							carryMap.lastKey()-carryMap.firstKey()
						);
				}
				break;
			}
			
			Long nextnext=sortedDatapoints.higherKey(next);
			
			//CONNECTING if either next is good, or current and nextnext both are good
			if ((Float.valueOf(sortedDatapoints.get(next))>=threshold)||(Float.valueOf(sortedDatapoints.get(current))>=threshold&&Float.valueOf(sortedDatapoints.get(nextnext))>=threshold)){
				carryMap.put(next, sortedDatapoints.get(next));
				current=next;
				continue;
			}
			
			//DO NOT CONNECT
			
			if (carryMap.size()>1){//RECORD ONLY IF LONGER THAN TWO
				resultMap.put(
						Collections.max(
								carryMap.entrySet(),
								(e1,e2) -> Float.compare(Float.valueOf(e1.getValue()),Float.valueOf(e2.getValue()))
						).getValue(),
						carryMap.lastKey()-carryMap.firstKey()
				);
			}
			
			NavigableMap<Long, String> initmap=new TreeMap<Long,String>();
//			initmap.put(next, sortedDatapoints.get(next));
			carryMap=initmap;
			
			current=next;
			continue;
			
			
		}
	}
	
	
	
	
	
	/**
	 * obsolete
	 * @param metrics
	 * @param constants
	 * @return
	 */
	protected List<Metric> transform2(List<Metric> metrics, List<String> constants){
		assert(constants!=null && constants.size()==1):"require a cull below threshold as constant";
		scope=metrics.get(0).getScope();
		/**
		 * PRE-PROCESS
		 */
		List<Metric> downsampled=getTransform("DOWNSAMPLE").transform(metrics,Arrays.asList("1m-avg"));
		List<Metric> propagated=getTransform("PROPAGATE").transform(downsampled,Arrays.asList("1m"));
		final List<Metric> readytoProcess=Collections.unmodifiableList(propagated.stream().map(m -> new Metric(m)).collect(Collectors.toList()));
		
		final String threshold=constants.get(0);
		final long startTimestamp=new TreeMap<Long,String>(readytoProcess.get(0).getDatapoints()).firstKey();
		List<Metric> cull_below_connected = cull_below_connect(readytoProcess,threshold);
		this.sortedDatapoints=new TreeMap<Long, String>(cull_below_connected.get(0).getDatapoints());
		
		//		System.out.println("\n\nCollected below the threshold(sortedDatapoints): "+this.sortedDatapoints);
		
		Metric returningMetric = new Metric(scope, "maxLagDuration");
		Metric returningMetricPeak = new Metric(scope, "maxLagPeakValue");
		Map<Long,String> returningDatapoints=new HashMap<Long,String>();
		Map<Long,String> returningDatapointsPeak=new HashMap<Long,String>();
		
		if (sortedDatapoints.size()==0){
			returningDatapoints.put(startTimestamp, String.valueOf(0));
			returningMetric.setDatapoints(returningDatapoints);
			
			returningDatapointsPeak.put(startTimestamp, "NA");
			returningMetricPeak.setDatapoints(returningDatapointsPeak);
			return Collections.unmodifiableList(Arrays.asList(returningMetric,returningMetricPeak));
		}
		 
		NavigableMap<Long, String> initMap=new TreeMap<Long,String>();
		initMap.put(sortedDatapoints.firstKey(), sortedDatapoints.firstEntry().getValue());
		helper(sortedDatapoints.firstKey(),initMap);
		final Long maxduration=resultMap.get(Collections.max(resultMap.keySet()));
		returningDatapoints.put(startTimestamp, String.valueOf(maxduration/60000 + 1));//return value in min
		returningMetric.setDatapoints(returningDatapoints);
		
		final String maxPeak = Collections.max(resultMap.keySet());
		returningDatapointsPeak.put(startTimestamp, maxPeak);
		returningMetricPeak.setDatapoints(returningDatapointsPeak);
		
		return Collections.unmodifiableList(Arrays.asList(returningMetric,returningMetricPeak));
	}
	
	private Object helper(Long current_timestamp,NavigableMap<Long,String> carryMap){
		//if this is the end. then stop and return
		Long next_timestamp=sortedDatapoints.higherKey(current_timestamp);
		if (next_timestamp == null){
			if (carryMap.size()>0){
				resultMap.put(
						Collections.max(carryMap.entrySet(),Map.Entry.comparingByValue()).getValue(),
						carryMap.lastKey()-carryMap.firstKey()
					);
			}
			return null;
		}
		
		if (next_timestamp - current_timestamp>1000*60L){
			resultMap.put(Collections.max(carryMap.entrySet(),Map.Entry.comparingByValue()).getValue(),
					carryMap.lastKey()-carryMap.firstKey()
			);
			
			NavigableMap<Long, String> nextmap=new TreeMap<Long,String>();
			nextmap.put(next_timestamp, sortedDatapoints.get(next_timestamp));
			return helper(next_timestamp, nextmap);
		}
		
		//connecting
		carryMap.put(next_timestamp, sortedDatapoints.get(next_timestamp));
		return helper(next_timestamp, carryMap);
		
	}
	
	/**
	 * plan:
	 * 1, consecutive(cull_above(M))
	 * 2, TOTAL - 1
	 * 3, return consecutive(2)
	 */
	final private List<Metric> cull_below_connect(final List<Metric> metrics, String threshold){
		final List<Metric> ToBeFilterMetrics=metrics.stream().map(m -> new Metric(m)).collect(Collectors.toList());

		List<Metric> good=getTransform("CULL_ABOVE").transform(ToBeFilterMetrics, Arrays.asList(threshold,"value"));

		System.out.println(scope+" good"+good.get(0).getDatapoints().size());
		
//		System.out.println(good.get(0).getDatapoints());
		List<Metric> good_connect=getTransform("CONSECUTIVE").transform(good,Arrays.asList("1m","1m"));
		
		System.out.println(scope+" good_connect"+good_connect.get(0).getDatapoints().size());
		
		List<Metric> deducted=deduct_transform(ListUtils.union(metrics, good_connect));

		List<Metric> result=getTransform("CONSECUTIVE").transform(deducted,Arrays.asList("1m","1m"));

		return result;
	}

	final private List<Metric> deduct_transform(final List<Metric> metrics){
		assert(metrics.size()==2):"at least two metrics a,b so we can give you a-b";
		final Metric resultMetric = new Metric(metrics.get(0));
		Map<Long,String> datapoints = (Map<Long,String>)metrics.get(0).getDatapoints().entrySet().stream()
				.filter(e -> !metrics.get(1).getDatapoints().containsKey(e.getKey()))
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
		resultMetric.setDatapoints(datapoints);
		return Collections.unmodifiableList(Arrays.asList(resultMetric));
	}
	
	final private Transform getTransform(final String transformName){
		return _transformFactory.get().getTransform(transformName);
	}
	
}




