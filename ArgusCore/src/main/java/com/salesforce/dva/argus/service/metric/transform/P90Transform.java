package com.salesforce.dva.argus.service.metric.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.system.SystemAssert;


public class P90Transform implements Transform{
	protected final String defaultMetricName="P90-SERVICE";
	
	@Override
	public List<Metric> transform(List<Metric> metrics) {		
		throw new UnsupportedOperationException("Have to have one constant");
	}

	@Override
	public List<Metric> transform(List<Metric> metrics, List<String> constants) {
		// TODO Auto-generated method stub
		SystemAssert.requireArgument(constants.size()==1, "This transform needs constants");
        Float threshold=Float.parseFloat(constants.get(0));
		
        
        Map<Long, String> rateTS = new HashMap<Long, String>();
        Map<Long, String> goodPodTS = new HashMap<Long, String>();
        Map<Long, String> totalPodTS = new HashMap<Long, String>();
        
		Map<Long, List<String>> collated = collate(metrics);
		for (Map.Entry<Long, List<String>> entry : collated.entrySet()) {
			List<String> es=entry.getValue().stream().filter(e -> Float.parseFloat(e) <= threshold).collect(Collectors.toList());
			float rate=(float)es.size()/entry.getValue().size();	
			rateTS.put(entry.getKey(), String.valueOf(rate));
			goodPodTS.put(entry.getKey(), String.valueOf(es.size()));
			totalPodTS.put(entry.getKey(), String.valueOf(entry.getValue().size()));
		}

        List<Metric> newMetricsList = new ArrayList<Metric>();
        
		MetricDistiller distiller = new MetricDistiller();
        distiller.distill(metrics);
        newMetricsList.add(generateMetric(distiller,rateTS,"P90Rate"));
        newMetricsList.add(generateMetric(distiller,goodPodTS,"goodPod"));
        newMetricsList.add(generateMetric(distiller,totalPodTS,"totalPod"));
        return newMetricsList;
	}

	private Metric generateMetric(MetricDistiller distiller,Map<Long, String> targetTS,String newMetricName){
        Metric newMetric = new Metric(defaultMetricName, newMetricName);
        newMetric.setDisplayName(distiller.getDisplayName());
        newMetric.setUnits(distiller.getUnits());
        newMetric.setTags(distiller.getTags());
        newMetric.setDatapoints(targetTS);
        return newMetric;
	}
	
	private Map<Long, List<String>> collate(List<Metric> metrics) {
        Map<Long, List<String>> collated = new HashMap<Long, List<String>>();
        for (Metric metric : metrics) {
            for (Map.Entry<Long, String> point : metric.getDatapoints().entrySet()) {
                if (!collated.containsKey(point.getKey())) {
                    collated.put(point.getKey(), new ArrayList<String>());
                }
                collated.get(point.getKey()).add(point.getValue());
            }
        }
        return collated;
    }

	@Override
	public List<Metric> transform(List<Metric>... metrics) {
		return null;
	}

	@Override
	public String getResultScopeName() {
		return null;
	}

}
