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
package com.salesforce.dva.argus.service.metric.transform.plus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.service.CollectionService;
import com.salesforce.dva.argus.service.TSDBService;
import com.salesforce.dva.argus.service.metric.transform.MetricDistiller;
import com.salesforce.dva.argus.service.metric.transform.Transform;
import com.salesforce.dva.argus.service.metric.transform.TransformFactory;
import com.salesforce.dva.argus.service.metric.transform.TransformFactory.Function;
import com.salesforce.dva.argus.system.SystemAssert;
import com.salesforce.dva.argus.system.SystemConfiguration;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;


public class HeimdallTotalAvaTransform implements Transform{
	private final String defaultMetricName="HEIMDALL";
	private final String TagName="device";

	public enum KafkaPattern {
		APT_TIME(".*SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode.*"),
		APT_COUNT(".*SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode.*"),
		ACT("??.*SFDC_type-Stats-name1-System-name2-trustActiveSessionCountRACNode.*"),
		CPU(".*result.*");
		private final String pattern;

	    /**
	     * @param pattern
	     * @return 
	     */
	    private KafkaPattern(final String pattern) {
	        this.pattern = pattern;
	    }

	    /* (non-Javadoc)
	     * @see java.lang.Enum#toString()
	     */
	    @Override
	    public String toString() {
	        return pattern;
	    }
	}
	
	@Inject
	private Provider<TransformFactory> _transformFactory;
	
	///for HD PRESENTATION. Every min level, give raw data
	@Override
	public List<Metric> transform(List<Metric> metrics, List<String> constants) {
		if(constants==null || constants.size()==0){
			return transform(metrics);
		}
		
		//HD MODEL
        final List<Metric> metricsLineup=downsample("1m-avg",metrics);
        final List<Metric> metricsAPTTime=metricsLineup.stream().filter(m->Pattern.matches(KafkaPattern.APT_TIME.toString(), m.getMetric())).collect(Collectors.toList());
        final List<Metric> metricsAPTCount=metricsLineup.stream().filter(m->Pattern.matches(KafkaPattern.APT_COUNT.toString(), m.getMetric())).collect(Collectors.toList());
        final List<Metric> metricsCPU=metricsLineup.stream().filter(m -> Pattern.matches(KafkaPattern.CPU.toString(), m.getMetric())).collect(Collectors.toList());
        
        assert(metricsAPTTime.size()>0):"should have at least a valid metricsAPTTime";
        assert(metricsAPTCount.size()>0):"should have at least a valid metricsAPTCount";
        assert(metricsCPU.size()>0):"should have at least a valid CPU";
        final List<Metric> ZEROMETRIC=zeroFill(metricsLineup);
        
	    List<Metric> averagedAPT=getRacLevelAPT(ZEROMETRIC,metricsAPTTime,metricsAPTCount);
	    List<Metric> cpu=fillWithTemplate(ZEROMETRIC, metricsCPU);
	    
//	    System.out.println(averagedAPT.get(0).getDatapoints().size());
//	    System.out.println(cpu.get(0).getDatapoints().size());
	    return constructingReturn(ZEROMETRIC, averagedAPT.get(0).getDatapoints(), null,cpu.get(0).getDatapoints());
	}
	
	
	@Override
	public List<Metric> transform(List<Metric> metrics) {
		System.out.println("\n**PRIVATE TRANSFORM**Heimdall TotalAva");
        SystemAssert.requireArgument(metrics!=null && metrics.size()>0,"Requires two metrics,");
        final List<Metric> metricsLineup=downsample("1m-avg",metrics);//alignup
        final List<Metric> metricsAPTTime=metricsLineup.stream().filter(m->Pattern.matches(KafkaPattern.APT_TIME.toString(), m.getMetric())).collect(Collectors.toList());
        final List<Metric> metricsAPTCount=metricsLineup.stream().filter(m->Pattern.matches(KafkaPattern.APT_COUNT.toString(), m.getMetric())).collect(Collectors.toList());
        final List<Metric> metricsCPU=metricsLineup.stream().filter(m -> Pattern.matches(KafkaPattern.CPU.toString(), m.getMetric())).collect(Collectors.toList());
        
        assert(metricsAPTTime.size()>0):"should have at least a valid metricsAPTTime";
        assert(metricsAPTCount.size()>0):"should have at least a valid metricsAPTCount";
        assert(metricsCPU.size()>0):"should have at least a valid CPU";
        final List<Metric> ZEROMETRIC=zeroFill(metricsLineup);

        List<Metric> averagedAPT=getRacLevelAPT(ZEROMETRIC,metricsAPTTime,metricsAPTCount);
        
        List<Metric> impactedMin=fillWithTemplate(ZEROMETRIC,getImpactedMin(averagedAPT,metricsCPU));
        Map<Long, String> tsImpactMins = (impactedMin).get(0).getDatapoints();
        List<Metric> denominator=fillWithTemplate(ZEROMETRIC,downsample("1h-count",metricsLineup));//Meaning how many data I collected per hour
        
        //System.out.println("de"+reduceMetricListSafe(impactedMin,cull_below(denominator,0)));
        List<Metric> avarate=fillWithTemplate(ZEROMETRIC,divide(reduceMetricListSafe(impactedMin,cull_below(denominator,0))));
        Map<Long, String> tsTotalAvaRate = (avarate).get(0).getDatapoints();
        
        List<Metric> cull_below_metricsCPU=cull_below(metricsCPU,65);
        List<Metric> cpu=fillWithTemplate(ZEROMETRIC,downsample("1h-count",cull_below_metricsCPU));
        
        Map<Long, String> tsCPU = (cpu).get(0).getDatapoints();
        return constructingReturn(ZEROMETRIC, negate(tsTotalAvaRate), tsImpactMins,tsCPU);
	}
	
	
	private final List<Metric> getRacLevelAPT(List<Metric> template,List<Metric> appLevelApt,List<Metric> appLevelCount){
		List<Metric> m_product=fillWithTemplate(template, scale_match(reduceMetricListSafe(appLevelApt,appLevelCount)));
		List<Metric> m_divisor=sum(appLevelCount);
        List<Metric> averagedAPT=fillWithTemplate(template, divide(reduceMetricListSafe(m_product,m_divisor)));
        return averagedAPT;
	}
	
	private final List<Metric> getImpactedMin(List<Metric> racLevelApt,List<Metric> racLevelCPU){
		assert(racLevelApt!=null && racLevelApt.get(0).getDatapoints()!=null):"input not valid";
		List<Metric> cull_below_filter=cull_below(racLevelApt,500);
		
        if (cull_below_filter.get(0).getDatapoints().size()==0){
        	System.out.println("cull_below_filter return. No data has been found that is above 500");
        	return zeroFill(racLevelApt);
        }
        
        List<Metric> consecutive_filter=consecutive(cull_below_filter);
        if (consecutive_filter.get(0).getDatapoints().size()==0){
        	System.out.println("consecutive return. No data has been found that is consecutive more than 5");
        	return zeroFill(racLevelApt);
        }
        assert(consecutive_filter.get(0).getDatapoints().size()>0):"till now, some data should be detected";
        List<Metric> cull_below_metricsCPU=cull_below(racLevelCPU,65);
        
        //System.out.println("before APT+"+consecutive_filter);
        //System.out.println("before CPU+"+cull_below_metricsCPU);
        List<Metric> logicMerge=sumWithUnion(reduceMetricList(consecutive_filter, cull_below_metricsCPU));
        //System.out.println("logic"+logicMerge);
        List<Metric> impactedMin=fill("1h",downsample("1h-count",logicMerge));
        return impactedMin;
	}
	
	
	
//	private final Map<Long, String> caculateTTM(Map<Long, String> racLevelCPU, Map<Long, String> racLevelAPT){
//		assert(racLevelCPU.entrySet().size()>0&&racLevelAPT.entrySet().size()>0):"Input can not be null";
//		Map<Long, String> tsCPU = new HashMap<Long,String>();
//		//assert(racLevelCPU.keySet().equals(racLevelAPT.keySet())):"racLevelCPU and APT should filled up all zeros and align up";
//		return null;
//	}
	
	
	private final List<Metric> constructingReturn(List<Metric> template, Map<Long, String> tsTotalAvaRate, Map<Long, String> tsImpactMins, Map<Long, String> tsCPU){
//		System.out.println("OUTPUTING...tsTotalAvaRate"+tsTotalAvaRate);
//		System.out.println("OUTPUTING...tsImpactMins"+tsImpactMins);
//		System.out.println("OUTPUTING...tsCPU"+tsCPU);
		
		List<Metric> newMetricsList = new ArrayList<Metric>();
        MetricDistiller distiller = new MetricDistiller();
        distiller.distill(template);
        newMetricsList.add(generateMetric(distiller,tsTotalAvaRate,"TotalAvaRate"));
        newMetricsList.add(generateMetric(distiller,tsImpactMins,"ImpactMins"));
        newMetricsList.add(generateMetric(distiller,tsCPU,"CPU Impacts"));
        return newMetricsList;
	}
	
	private final List<Metric> zeroFill(List<Metric> m){
		assert(m!=null&&m.size()>1):"list of metric has to be valid";
		Metric output=new Metric(m.get(0));
		List<Metric> outputlist=new LinkedList<Metric>();
		Map<Long, String> outdatapoints=new HashMap<Long,String>();
		m.get(0).getDatapoints().entrySet().stream()
									.forEach(e -> outdatapoints.put(e.getKey(), "0"));
		output.setDatapoints(outdatapoints);
		outputlist.add(0, output);
		return outputlist;
	}
	
	//Take template,m, and fill m with zero and align up X axis with template
	private final List<Metric> fillWithTemplate(List<Metric> template,List<Metric> m){
		assert(template!=null&&template.size()>1):"list of metric has to be valid";
		Metric output=new Metric(m.get(0));
		List<Metric> outputlist=new LinkedList<Metric>();
		
		Map<Long, String> outdatapoints=new HashMap<Long,String>(output.getDatapoints());
		assert(outdatapoints.entrySet().stream()
				   .filter(k -> !template.get(0).getDatapoints().keySet().contains(k))
				   .collect(Collectors.toSet())
				   .size() ==0
				):"The newTS time series has to be a subset of template";
		template.get(0).getDatapoints().entrySet().forEach(e -> outdatapoints.putIfAbsent(e.getKey(), String.valueOf(0)));
		
		output.setDatapoints(outdatapoints);
		outputlist.add(0, output);
		return outputlist;
	}

	private final Map<Long, String> negate(Map<Long, String> input){
		Map<Long, String> output=new HashMap<Long,String>();		
		input.entrySet().forEach(e -> output.put(e.getKey(), String.valueOf(
					(1f-Float.valueOf(e.getValue()))*100
					)));
		return output;
	}
	
	private final Map<Long, String> removeZero(Map<Long, String> input){
		Map<Long, String> output=new HashMap<Long,String>();		
		input.entrySet().stream()
			.filter(e -> Float.valueOf(e.getValue())>0)
			.forEach(e -> output.put(e.getKey(), e.getValue()));
		return output;
	}
		
	private final Metric removeZeroMetric(Metric m){
		Metric output=new Metric(m);
		output.setDatapoints(removeZero(m.getDatapoints()));
		return output;
	}

	private final Metric generateMetric(MetricDistiller distiller,Map<Long, String> targetTS,String newMetricName){
        Metric newMetric = new Metric(defaultMetricName, newMetricName);
        newMetric.setDisplayName(distiller.getDisplayName());
        newMetric.setUnits(distiller.getUnits());
        newMetric.setTags(distiller.getTags());
        newMetric.setDatapoints(targetTS);
        return newMetric;
	}

	private final List<Metric> fill(String distance, List<Metric> metrics) {
		if (metrics.get(0).getDatapoints()==null || metrics.get(0).getDatapoints().size()==0){
			return null;//if the incoming metrics are all zeros, then return null		
		}
		List<String> constants = new ArrayList<String>();
		constants.add(distance);//Define as connect distance
		constants.add("0m");
		constants.add("0");
		return _transformFactory.get().getTransform("FILL").transform(metrics,constants);
	}
	
	private final List<Metric> reduceMetricListSafe(List<Metric> m1, List<Metric> m2){
		List<Metric> returnMetric=new ArrayList<Metric>();
		m1.forEach(m -> returnMetric.add(new Metric(m)));
		m2.forEach(m -> returnMetric.add(removeZeroMetric(new Metric(m))));
		return returnMetric;
	}
	
	private final List<Metric> reduceMetricList(List<Metric> m1, List<Metric> m2){
		List<Metric> returnMetric=new ArrayList<Metric>();
		m1.forEach(m -> returnMetric.add(new Metric(m)));
		m2.forEach(m -> returnMetric.add(new Metric(m)));
		return returnMetric;
	}
	
	private final List<Metric> downsample(String distance,List<Metric> metrics) {
		List<Metric> mutable=new ArrayList<Metric>();
		metrics.forEach(m -> mutable.add(new Metric(m)));
		List<String> constants = new ArrayList<String>();
		constants.add(distance);//Define as connect distance
		return _transformFactory.get().getTransform("DOWNSAMPLE").transform(mutable,constants);
	}
	
	private final List<Metric> consecutive(List<Metric> metrics) {
		List<String> constants = new ArrayList<String>();
		constants.add("4m");//Define as connect distance
		constants.add("1m");
		return _transformFactory.get().getTransform("CONSECUTIVE").transform(metrics,constants);
	}
	
	private final List<Metric> cull_below(List<Metric> metrics,int threshold) {
		List<String> constants = new ArrayList<String>();
		constants.add(String.valueOf(threshold));
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
	
	private List<Metric> sumWithUnion(List<Metric> metrics) {
		List<String> constants = new ArrayList<String>();
		constants.add("union");
		return _transformFactory.get().getTransform("SUM").transform(metrics,constants);
	}
		


	@Override
	public List<Metric> transform(List<Metric>... metrics) {
		return null;
	}

	@Override
	public String getResultScopeName() {
		return TransformFactory.Function.HEIMDALL_TOTALAVA.name(); 
	}
	

}
