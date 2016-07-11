/*
 * Copyright (c) 2016, Salesforce.com, Inc.
 * ArgusCore+ use
 *
______ ______  _____  _   _   ___   _____  _____       _____ ______   ___   _   _  _____ ______  _____ ______ ___  ___
| ___ \| ___ \|_   _|| | | | / _ \ |_   _||  ___|     |_   _|| ___ \ / _ \ | \ | |/  ___||  ___||  _  || ___ \|  \/  |
| |_/ /| |_/ /  | |  | | | |/ /_\ \  | |  | |__         | |  | |_/ // /_\ \|  \| |\ `--. | |_   | | | || |_/ /| .  . |
|  __/ |    /   | |  | | | ||  _  |  | |  |  __|        | |  |    / |  _  || . ` | `--. \|  _|  | | | ||    / | |\/| |
| |    | |\ \  _| |_ \ \_/ /| | | |  | |  | |___        | |  | |\ \ | | | || |\  |/\__/ /| |    \ \_/ /| |\ \ | |  | |
\_|    \_| \_/ \___/  \___/ \_| |_/  \_/  \____/        \_/  \_| \_/\_| |_/\_| \_/\____/ \_|     \___/ \_| \_/\_|  |_/
 *
 *
 *
 *ethan.wang@salesforce.com
 */
package com.salesforce.dva.argus.service.metric.transform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.service.CollectionService;
import com.salesforce.dva.argus.service.TSDBService;
import com.salesforce.dva.argus.service.metric.transform.Transform;
import com.salesforce.dva.argus.system.SystemAssert;
import com.salesforce.dva.argus.system.SystemConfiguration;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class HeimdallTotalAvaTransform implements Transform{
	private final String TagName="device";
	private final String APT_TIME_PATTERN=".*SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.*";
	private final String APT_COUNT_PATTERN=".*SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.*";
	private final String ACT_PATTERN="??.*SFDC_type-Stats-name1-System-name2-trustActiveSessionCountRACNode2.*";
	
	@Inject
	Provider<TransformFactory> _transformFactory;
	
	@Override
	public List<Metric> transform(List<Metric> metrics) {		
		System.out.println("\n**PRIVATE TRANSFORM**\nHeimdall TotalAva\n");
        SystemAssert.requireArgument(metrics!=null && metrics.size()>0,"Requires two metrics,");
        
        final List<Metric> metricsAPTTime=metrics.stream().filter(m->Pattern.matches(APT_TIME_PATTERN, m.getMetric())).collect(Collectors.toList());
        final List<Metric> metricsAPTCount=metrics.stream().filter(m->Pattern.matches(APT_COUNT_PATTERN, m.getMetric())).collect(Collectors.toList());
        

        List<Metric> m_product=scale_match(reduceMetricList(metricsAPTTime,metricsAPTCount));
        List<Metric> m_divisor=sum(metricsAPTCount);
        List<Metric> averagedAPT=divide(reduceMetricList(m_product,m_divisor));
        List<Metric> cull_below_filter=cull_below(averagedAPT);
        if (cull_below_filter.get(0).getDatapoints().size()==0){
        	System.out.println("cull_below_filter return");
        	return cull_below_filter;
        }
        List<Metric> consecutive_filter=consecutive(cull_below_filter);
        if (consecutive_filter.get(0).getDatapoints().size()==0){
        	System.out.println("consecutive return");
        	return consecutive_filter;
        }
        
        List<Metric> nominator=downsample(consecutive_filter);
        
        //System.out.println(fill(nominator));
        List<Metric> denominator=downsample(m_divisor);
        //System.out.println(denominator);
        return divide(reduceMetricList(fill(nominator), denominator));
	}
	

	private List<Metric> fill(List<Metric> metrics) {
		List<String> constants = new ArrayList<String>();
		constants.add("1m");//Define as connect distance
		constants.add("0m");
		constants.add("0");
		return _transformFactory.get().getTransform("FILL").transform(metrics,constants);
	}
	
	
	private List<Metric> reduceMetricList(List<Metric> m1, List<Metric> m2){
		List<Metric> returnMetric=new ArrayList<Metric>();
		m1.forEach(m -> returnMetric.add(new Metric(m)));
		m2.forEach(m -> returnMetric.add(new Metric(m)));
		return returnMetric;
	}
	
	private List<Metric> downsample(List<Metric> metrics) {
		List<String> constants = new ArrayList<String>();
		constants.add("1h-count");//Define as connect distance
		return _transformFactory.get().getTransform("DOWNSAMPLE").transform(metrics,constants);
	}
	
	private List<Metric> consecutive(List<Metric> metrics) {
		List<String> constants = new ArrayList<String>();
		constants.add("4m");//Define as connect distance
		constants.add("1m");
		return _transformFactory.get().getTransform("CONSECUTIVE").transform(metrics,constants);
	}
	
	private List<Metric> cull_below(List<Metric> metrics) {
		List<String> constants = new ArrayList<String>();
		constants.add("500");
		constants.add("value");
		return _transformFactory.get().getTransform("CULL_BELOW").transform(metrics,constants);
	}
	
	private List<Metric> divide(List<Metric> metrics) {
		return _transformFactory.get().getTransform("DIVIDE").transform(metrics);
	}
	
	private List<Metric> scale_match(List<Metric> metrics) {
		List<String> constants = new ArrayList<String>();
        constants.add(TagName);
        constants.add(".*");
		return _transformFactory.get().getTransform("SCALE_MATCH").transform(metrics,constants);
	}
	
	private List<Metric> sum(List<Metric> metrics) {
		return _transformFactory.get().getTransform("SUM").transform(metrics);
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
		return TransformFactory.Function.HEIMDALL_TOTALAVA.name(); 
	}
	

}
