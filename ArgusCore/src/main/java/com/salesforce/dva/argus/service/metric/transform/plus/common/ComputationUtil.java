/*
 * Copyright (c) 2016, Salesforce.com, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of Salesforce.com nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.salesforce.dva.argus.service.metric.transform.plus.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.service.metric.transform.TransformFactory;

/**
 * 
 * @author aertoria ethan.wang@salesforce.com
 *
 */
final public class ComputationUtil {
	@Inject
	private Provider<TransformFactory> _transformFactory;
	
	protected List<Metric> scale_match(List<Metric> metrics) {
		try{
			List<String> constants = new ArrayList<String>();
			constants.add("device");
			constants.add(".*");
			return _transformFactory.get().getTransform("SCALE_MATCH").transform(metrics, constants);
		}catch(RuntimeException e){
			throw new RuntimeException("Error During ComputationUtil.scale_match"+e+metrics);
		}
	}
	
	protected List<Metric> scale(List<Metric> metrics1,List<Metric> metrics2) {
		assert(metrics1!=null&&metrics1.size()==1&&metrics2!=null&&metrics2.size()==1):"metric inputs has to be single inputs each";
		List<Metric> toBeScaled = new ArrayList<Metric>();
		toBeScaled.addAll(metrics1);
		toBeScaled.addAll(metrics2);
		return scale(toBeScaled);
	}
	
	protected List<Metric> scale(List<Metric> metrics) {
		return _transformFactory.get().getTransform("SCALE").transform(metrics);
	}
	
	protected List<Metric> divide(List<Metric> metrics) throws RuntimeException {
		assert(metrics!=null && metrics.size()==2):"metrics should have two metric inside. No and divisor";
		try{
			Metric divisor=metrics.get(1);
			divisor.getDatapoints().entrySet().forEach(e->{
				if(Float.valueOf(e.getValue()).equals(0f)){
					throw new RuntimeException(" A divisor is empty in this divisor metircs: "+divisor.getMetric());
				}
			});
			return _transformFactory.get().getTransform("DIVIDE").transform(metrics);
		}catch(RuntimeException e){
			throw new RuntimeException("Error During ComputationUtil.Divide"+e+metrics);
		}	
	}
	
	protected Metric removeZeroMetric(Metric m) {
		Metric output = new Metric(m);
		output.setDatapoints(removeZero(m.getDatapoints()));
		return output;
	}
	
	private Map<Long, String> removeZero(Map<Long, String> input) {
		Map<Long, String> output = new HashMap<Long, String>();
		input.entrySet().stream().filter(e -> Float.valueOf(e.getValue()) > 0)
				.forEach(e -> output.put(e.getKey(), e.getValue()));
		return output;
	}
	
	protected List<Metric> sum(List<Metric> metrics) {
		return _transformFactory.get().getTransform("SUM").transform(metrics);
	}
	
	protected List<Metric> sumWithUnion(List<Metric> metrics) {
		List<String> constants = new ArrayList<String>();
		constants.add("union");
		return _transformFactory.get().getTransform("SUM").transform(metrics, constants);
	}

	protected List<Metric> consecutive(List<Metric> metrics) {
		List<String> constants = new ArrayList<String>();
		constants.add("4m");// Define as connect distance
		constants.add("1m");
		return _transformFactory.get().getTransform("CONSECUTIVE").transform(metrics, constants);
	}

	protected List<Metric> cull_below(List<Metric> metrics, int threshold) {
		List<Metric> metricsCopy=Collections.unmodifiableList(metrics.stream()
																	 .map(m -> new Metric(m))
																	 .collect(Collectors.toList()));
		List<String> constants = new ArrayList<String>();
		constants.add(String.valueOf(threshold));
		constants.add("value");
		return _transformFactory.get().getTransform("CULL_BELOW").transform(metricsCopy, constants);
	}

	public List<Metric> downsample(String distance, List<Metric> metrics) {
		List<Metric> mutable = new ArrayList<Metric>();
		metrics.forEach(m -> mutable.add(new Metric(m)));
		List<String> constants = new ArrayList<String>();
		constants.add(distance);// Define as connect distance
		return _transformFactory.get().getTransform("DOWNSAMPLE").transform(mutable, constants);
	}
	
	protected List<Metric> downsample(String distance, List<Metric> metrics, Long timeStamp){
		assert (metrics!=null && metrics.size()==1):"only take single metric to downsample";
		List<Metric> downsampledResult=downsample(distance,metrics);
		assert(downsampledResult!=null&&downsampledResult.size()==1&&downsampledResult.get(0).getDatapoints().size()==1):"downsampled result should be single data points";
		Metric m=new Metric(downsampledResult.get(0));
		Map<Long,String> datapoints=new HashMap<Long,String>();
		datapoints.put(timeStamp, downsampledResult.get(0).getDatapoints().entrySet().iterator().next().getValue());
		m.setDatapoints(datapoints);
		return Collections.unmodifiableList(Arrays.asList(m));
	}

	protected List<Metric> downsampleAndFill(ReportRange range, int resolutionInMin, String distance, List<Metric> metrics){
		List<Metric> downsampledImpactedMin=downsample(distance, metrics);
		List<Metric> filledImpactedMin=mergeZero(range,resolutionInMin,downsampledImpactedMin);
		return Collections.unmodifiableList(filledImpactedMin);
	}
	
	protected List<Metric> zeroFill(List<Metric> m) {
		assert (m != null && m.size() > 1) : "list of metric has to be valid";
		Metric output = new Metric(m.get(0));
		List<Metric> outputlist = new LinkedList<Metric>();
		Map<Long, String> outdatapoints = new HashMap<Long, String>();
		m.get(0).getDatapoints().entrySet().stream().forEach(e -> outdatapoints.put(e.getKey(), "0"));
		output.setDatapoints(outdatapoints);
		outputlist.add(0, output);
		return outputlist;
	}

	protected List<Metric> fill(String distance, List<Metric> metrics) {
		if (metrics.get(0).getDatapoints() == null || metrics.get(0).getDatapoints().size() == 0) {
			return null;// if the incoming metrics are all zeros, then return
						// null
		}
		List<String> constants = new ArrayList<String>();
		constants.add(distance);// Define as connect distance
		constants.add("0m");
		constants.add("0");
		return _transformFactory.get().getTransform("FILL").transform(metrics, constants);
	}
	
	protected List<Metric> mergeZero(ReportRange range, int resolutionInMin, List<Metric> listMetric) {
		assert(listMetric!=null && listMetric.size()==1):"Only one item is allowed in the boxing";
		Metric m = new Metric(listMetric.get(0));
		Map<Long, String> resultDatapoints=fillZero(range.getStart(),range.getEnd(),Long.valueOf(resolutionInMin*1000*60),listMetric.get(0).getDatapoints());
		m.setDatapoints(resultDatapoints);
		return Arrays.asList(m);
	}
	
	protected List<Metric> negate(List<Metric> metrics){
		assert (metrics != null && metrics.size() > 0) : "list of metric has to be valid";
		Metric m=new Metric(metrics.get(0));
		m.setDatapoints(negate(m.getDatapoints()));
		List<Metric> returnList=Collections.unmodifiableList(Arrays.asList(m));
		return returnList;
	}
	
	private Map<Long, String> negate(Map<Long, String> input) {
		Map<Long, String> output = new HashMap<Long, String>();
		input.entrySet().forEach(e -> output.put(e.getKey(), String.valueOf((1f - Float.valueOf(e.getValue())) * 100)));
		return output;
	}

	protected Map<Long, String> fillZero(Long start,Long end, Long resolution, Map<Long, String> datapoints){
		Map<Long, String> resultDatapoints=new HashMap<Long, String>(datapoints);
		for(Long timestamp=start;timestamp<=end;timestamp+=resolution){
			if (!resultDatapoints.keySet().contains(timestamp)){
				resultDatapoints.put(timestamp, String.valueOf("0"));
			}
		}
		return resultDatapoints;
	}

	/**Givin a time series, return any datapoints that is above 500 unit for 5 consecutive timestamp, could be null**/
	protected List<Metric> detectAPT(List<Metric> input, String objectAddress){
		assert(input != null && input.get(0).getDatapoints() != null) : "input not valid";
		List<Metric> cull_below_filter = cull_below(input, 500);
		
		if (cull_below_filter.get(0).getDatapoints().size() == 0) {
			System.out.println("cull_below_filter return. No data has been found that is above 500 @"+objectAddress);
			return cull_below_filter;
		}
	
		List<Metric> consecutive_filter = consecutive(cull_below_filter);
		if (consecutive_filter.get(0).getDatapoints().size() == 0) {
			System.out.println("consecutive return. No data has been found that is consecutive more than 5 @"+objectAddress);
			return consecutive_filter;
		}
		assert(consecutive_filter.get(0).getDatapoints().size() > 0) : "till now, some data should be detected";
		
		return consecutive_filter;
		
	}
	
	/**Givin a time series, return any datapoints that is above 150, could be null**/
	protected List<Metric> detectACT(List<Metric> input, String objectAddress){
		assert(input != null && input.get(0).getDatapoints() != null) : "input not valid";
		return detectAbove(input,150,objectAddress);
	}
	
	/**Givin a time series, return any datapoints that is above 65, could be null**/
	protected List<Metric> detectCPU(List<Metric> input, String objectAddress){
		assert(input != null && input.get(0).getDatapoints() != null) : "input not valid";
		return detectAbove(input,65,objectAddress);
	}
	
	/**Givin a time series, return any datapoints that is above a threshold, could be null**/
	protected List<Metric> detectAbove(List<Metric> input, int threashold, String objectAddress){
		assert(input != null && input.get(0).getDatapoints() != null) : "input not valid";
		List<Metric> cull_below_filter = cull_below(input, threashold);
		if (cull_below_filter.get(0).getDatapoints().size() == 0) {
			System.out.println("cull_below_filter return. No data has been found that is above "+threashold+" @"+ objectAddress);
		}
		return cull_below_filter;
	}
	
	private List<Metric> flattArray(List<Metric>... listMetrics){
		assert(listMetrics!=null):"input metrics can not be null";
		ArrayList<List<Metric>> arrayMetrics=new ArrayList<List<Metric>>();
		arrayMetrics.addAll(Arrays.asList(listMetrics));
		List<Metric> flatArrayMetrics=arrayMetrics.stream()
												.flatMap(l -> l.stream())
												.collect(Collectors.toList());
		return flatArrayMetrics;
	}
	
	protected List<Metric> unionOR(List<Metric>... listMetrics){
		return unionOR(flattArray(listMetrics));
	}
	
	protected List<Metric> unionOR(List<Metric> metrics){
		List<Metric> unionOR=sumWithUnion(metrics);
		assert (unionOR!=null&&unionOR.size()==1):"result of unionOr should be one metric boxed object";
		return Collections.unmodifiableList(Arrays.asList(new Metric(unionOR.get(0))));
	}
	
	protected List<Metric> unionAND(List<Metric>... listMetrics){
		return unionAND(flattArray(listMetrics));
	}
	
	protected List<Metric> unionAND(List<Metric> metrics){
		List<Metric> unionAND=sum(metrics);
		assert (unionAND!=null&&unionAND.size()==1):"result of unionAnd should be one metric boxed object";
		return Collections.unmodifiableList(Arrays.asList(new Metric(unionAND.get(0))));
	}
	
	protected List<Metric> reNameScope(List<Metric> metrics, String scopeName){
		List<Metric> resultMetrics=new ArrayList<Metric>();
		metrics.forEach(m -> {
			Metric newMetric = new Metric(scopeName, m.getMetric());
			newMetric.setTags(m.getTags());
			newMetric.setNamespace(m.getNamespace());
			newMetric.setDatapoints(m.getDatapoints());
			resultMetrics.add(newMetric);
		});
		return Collections.unmodifiableList(resultMetrics);
	}

	protected List<Metric> weightedByTraffic(List<Metric> metrics, List<Metric> traffic, String downsampleDistance, ReportRange reportRange){
		assert (metrics!=null && metrics.size()==1 && traffic!=null && traffic.size()==1):"input not valid";
		List<Metric> product=scale(metrics,traffic);
		List<Metric> productDownsampled=downsample(downsampleDistance, product);
		
		List<Metric> divisor=Arrays.asList(removeZeroMetric(traffic.get(0)));
		List<Metric> divisorDownsampled=downsample(downsampleDistance, divisor);
		
		List<Metric> toBeDivided = new ArrayList<Metric>();
		toBeDivided.addAll(productDownsampled);
		toBeDivided.addAll(divisorDownsampled);
		List<Metric> dividedResult=divide(toBeDivided);
		
		assert(dividedResult!=null):"dividedResult should be valid"; 
		List<Metric> filleddividedResult=mergeZero(reportRange,60,dividedResult);

		return filleddividedResult;
	}

}
