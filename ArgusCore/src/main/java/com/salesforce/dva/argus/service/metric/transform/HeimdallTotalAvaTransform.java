package com.salesforce.dva.argus.service.metric.transform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.service.metric.transform.Transform;
import com.salesforce.dva.argus.system.SystemAssert;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class HeimdallTotalAvaTransform implements Transform{
	@Override
	public List<Metric> transform(List<Metric> metrics) {
		System.out.println("An approach of doing scale with wildcard");
		return scale_matching(metrics);
	}
	
	
	private List<Metric> scale_matching(List<Metric> metrics){
		Metrics ms=new Metrics(metrics);
		Set<String> metricNameSet=ms.getUniqueMetricName();
		Set<String> deviceSet=ms.getUniqueTagName("device");
		
		System.out.println("MXN is"+metricNameSet+" X "+deviceSet);
		
		List<Metric> toBeSumed=new ArrayList<Metric>();
		for (String device:deviceSet){
			List<Metric> toBeScaled=new ArrayList<Metric>();
			for(String metricName:metricNameSet){
				List<Metric> collected=new Metrics(ms.getMetricsByMetricName(metricName)).getMetricsByTagName("device",device);
				SystemAssert.requireState(collected!=null && collected.size()==1, "Transform hit no or multiple target");
				toBeScaled.add(collected.get(0));
			}
			toBeSumed.addAll(local_scale(toBeScaled));
		}
		//System.out.println(toBeSumed);
		//System.out.println(local_sum(toBeSumed));
		return local_sum(toBeSumed);
	}
	
	
	private List<Metric> local_scale(List<Metric> metrics) {
		Transform sumTransform = new MetricZipperTransform(new ScaleValueZipper());
		return sumTransform.transform(metrics);
	}
	
	private List<Metric> local_sum(List<Metric> metrics) {
		Transform sum_vTransform = new MetricReducerOrMappingTransform(new SumValueReducerOrMapping());
		return sum_vTransform.transform(metrics);
	}
	
	@Override
	public List<Metric> transform(List<Metric> metrics, List<String> constants) {
		// TODO Auto-generated method stub
		return null;
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
	
	private class Metrics{
		private List<Metric> metrics;
		Metrics(List<Metric> metrics){
			this.metrics=metrics;
		}
		
		private List<Metric> getMetrics(){
			return this.metrics;
		}
		private Set<String> getUniqueMetricName(){
			Set<String> result = new HashSet<String>();
			for(Metric m:metrics){
				result.add(m.getMetric());
			}
			return result;
		}
		
		private Set<String> getUniqueTagName(String tagLabel){
			Set<String> result = new HashSet<String>();
			for(Metric m:metrics){
				result.add(m.getTag(tagLabel));
			}
			return result;
		}
		
		private List<Metric> getMetricsByMetricName(String metricValue){
			return this.metrics.stream().filter(m -> m.getMetric().equals(metricValue)).collect(Collectors.toList());
		}
		
		private List<Metric> getMetricsByTagName(String tagLabel,String tagValue){
			return this.metrics.stream().filter(m -> m.getTag(tagLabel).equals(tagValue)).collect(Collectors.toList());
		}
	}

}
