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
package com.salesforce.dva.argus.service.metric.transform;
/**
 * Scales multiple time series. "M X N is"+metricNameSet+" X "+tagSet
 *
 * @author  aertoria ethan.wang@salesforce.com
 */
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.system.SystemAssert;

public class ScaleMatchTransform implements Transform{
	private Metrics ms;
	private String matchingPattern=".*";
	
	@Override
	public List<Metric> transform(List<Metric> metrics) {
		throw new UnsupportedOperationException("This transform requires at least one constant. Constant1: tag name. Constant 2,optional, matching pattern for metrics");
	}

	@Override
	public List<Metric> transform(List<Metric> metrics, List<String> constants) {
		SystemAssert.requireArgument(metrics!=null && metrics.size()>0,"can not be emtpy metrics");
		SystemAssert.requireArgument(constants!=null && constants.size()<=2, "This transform requires at least one constant. Constant1: tag name. Constant 2,optional, matching pattern for metrics");
		ms=new Metrics(metrics);
		String tagName=constants.get(0);
		SystemAssert.requireArgument(ms.containsTag(tagName), "Tag is not valid");

		Set<String> metricNameSet=ms.getUniqueMetricName();
		Set<String> tagSet=ms.getUniqueTagName(tagName);
		
		if (constants.size()==2){
			matchingPattern=constants.get(1);
		}
		
		List<Metric> toBeSumed=new ArrayList<Metric>();
		//for the same device, it multiplies all matching metrics.
		for (String tagValue:tagSet){
			List<Metric> toBeScaled=new ArrayList<Metric>();
			for(String metricName:metricNameSet.stream().filter(m->Pattern.matches(matchingPattern,m)).collect(Collectors.toSet())){
				List<Metric> collected=ms.getMetricsByMetricName(metricName).getMetricsByTagName(tagName,tagValue).getMetrics();
				if (collected.size()!=0){
					toBeScaled.add(collected.get(0));
				}
			}
			if (toBeScaled.size()>1){
				toBeSumed.addAll(scale(toBeScaled));
			}
		}
		return sum(toBeSumed);
	}

	@Override
	public List<Metric> transform(List<Metric>... metrics) {
		throw new UnsupportedOperationException("This class is deprecated!");
	}

	@Override
	public String getResultScopeName() {
		return TransformFactory.Function.SCALE_MATCH.name();
	}

	private List<Metric> scale(List<Metric> metrics) {
		return new MetricZipperTransform(new ScaleValueZipper()).transform(metrics);
	}
	
	private List<Metric> sum(List<Metric> metrics) {
		return new MetricReducerOrMappingTransform(new SumValueReducerOrMapping()).transform(metrics);
	}
	
	private class Metrics{
		private List<Metric> metrics;
		Metrics(List<Metric> metrics){
			this.metrics=metrics;
		}
		
		private List<Metric> getMetrics(){
			return this.metrics;
		}
		
		private boolean containsTag(String tagLabel){
			for(Metric m:metrics){
				if(m.getTag(tagLabel)!=null){
					return true;
				};
			}
			return false;
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
		
		private Metrics getMetricsByMetricName(String metricValue){
			return new Metrics(this.metrics.stream().filter(m -> m.getMetric().equals(metricValue)).collect(Collectors.toList()));
		}
		
		private Metrics filterByMatchingMetricName(String metricMatchingValue){
			return new Metrics(this.metrics.stream().filter(m -> Pattern.matches(metricMatchingValue, m.getMetric())).collect(Collectors.toList()));
		}
		
		private Metrics getMetricsByTagName(String tagLabel,String tagValue){
			return new Metrics(this.metrics.stream().filter(m -> m.getTag(tagLabel).equals(tagValue)).collect(Collectors.toList()));
		}	
	}
}
/* Copyright (c) 2016, Salesforce.com, Inc.  All rights reserved. */

