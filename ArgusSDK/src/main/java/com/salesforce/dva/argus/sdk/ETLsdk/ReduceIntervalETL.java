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

package com.salesforce.dva.argus.sdk.ETLsdk;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import com.salesforce.dva.argus.sdk.ArgusService.PutResult;
import com.salesforce.dva.argus.sdk.entity.Metric;

/**
 * Reduce metric datapoints within a time intervals to a single datapoint.
 * @author Charles Kuo <ckuo@salesforce.com>
 *
 */
public class ReduceIntervalETL extends HeimdallETL {
	/**
	 * Interval duration. Will repeatedly reduce intervals from the ETL start time to end time.
	 */
	protected long interval;
	
	/**
	 * Expression to perform the reduction.
	 */
	protected String expression;
	
	/**
	 * Timestamp in the reduced metric to generate the resulting datapoint per interval.
	 */
	protected long matchTimestamp;
	
	/**
	 * Override metric name.
	 */
	protected String metricName;
	
	/**
	 * Override metric scope.
	 */
	protected String metricScope;
	
	/**
	 * Override metric namespace.
	 */
	protected String metricNamespace;
	
	/**
	 * Override metric display name.
	 */
	protected String metricDisplayName;
	
	/**
	 * Override metric tags.
	 */
	protected Map<String, String> metricTags;
	
	/**
	 * Prepare to run the ETL process.
	 */
	public void init(String args[]) throws Exception {
		// interval
		String interval = properties.getProperty("interval");
		if (interval == null)
			throw new Exception("Interval missing.");
		try {
	    	this.interval = Long.parseLong(interval, 10);
	    }
	    catch (NumberFormatException e) {
	    	throw new Exception("Interval is not a valid number.");
	    }
		
		// expression
		String expression = properties.getProperty("expression");
		if (expression == null)
			throw new Exception("Expression missing.");
		this.expression = expression;
		
		// timestamp
		String timestamp = properties.getProperty("timestamp");
		if (timestamp == null)
			throw new Exception("Timestamp missing.");
		try {
	    	this.matchTimestamp = Long.parseLong(timestamp, 10);
	    }
	    catch (NumberFormatException e) {
	    	throw new Exception("Timestamp is not a valid number.");
	    }
		
		// metric
		metricName = properties.getProperty("metric");
		
		// scope
		metricScope = properties.getProperty("scope");
		
		// namespace
		metricNamespace= properties.getProperty("namespace");
		
		// display name
		metricDisplayName = properties.getProperty("displayname");
		
		// tags
		String tags = properties.getProperty("tags");
		if (tags != null) {
			metricTags = new HashMap<String, String>();
	    	
		    String[] pairs = tags.split(",");
		    for (String pair : pairs) {
		    	String[] tokens = pair.split("=");
		    	metricTags.put(tokens[0], tokens[1]);
		    }
		}
	}
	
	/**
	 * Execute ETL process.
	 */
	public void execute() throws Exception {
		System.out.println("Executing reduce interval ETL ...");
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		for (long time = startTime; time < endTime; time += interval){
			// generate expression for interval
			System.out.println("Reducing time interval: " + dateFormat.format(new Date(time)) + " to " + dateFormat.format(new Date(time + interval)));
			final String executeExpression = URLEncoder.encode(expression.replaceAll("\\$timerange\\$", time + ":" + (time + (interval-1))), "UTF-8");
			final long executeTime = time;
			
			try {
				// read metrics
				List<Metric> metrics = sourceService.getMetricService().getMetrics(Arrays.asList(new String[] { executeExpression }));
				
				final long timestamp = executeTime;
				
				// reduce metrics to single data point
				List<Metric> reduced = new ArrayList<Metric>();
				Set<String> ids = new HashSet<String>();
				
				metrics.stream().forEach(metric -> {
					String name = this.metricName != null ? this.metricName : metric.getMetric();
					String scope = this.metricScope != null ? this.metricScope : metric.getScope();
					String namespace = this.metricNamespace != null ? this.metricNamespace : metric.getNamespace();
					String id = (namespace != null ? namespace + ":" : "") + (scope != null ? scope + ":" : "") + name;
					
					// skip if id already exists (prevent duplicate metrics)
					if (ids.contains(id))
						return;
					ids.add(id);
					
					Metric metric2 = new Metric();
					metric2.setMetric(name);
					metric2.setScope(scope);
					metric2.setNamespace(namespace);
					metric2.setDisplayName(this.metricDisplayName != null ? this.metricDisplayName : metric.getDisplayName());
					metric2.setUnits(metric.getUnits());
					
					if (metricTags != null)
						metric2.setTags(metricTags);
					
					String value = "0";
					
					Map<Long, String> points = metric.getDatapoints();
					if (points != null)
						value = points.get(matchTimestamp);
					
					Map<Long, String> points2 = new HashMap<Long, String>();
					points2.put(timestamp, value);
					metric2.setDatapoints(points2);

					reduced.add(metric2);
				});
				
				// write reduced metrics
				for (Metric metric : reduced) {
					System.out.println(metricToJson(metric));
					//printMetric(metric);
				}
				
				if (!readOnly) {
					PutResult result = targetService.getMetricService().putMetrics(reduced);
					printPutResult(result);
				}
			}
			catch (Exception e) {
				System.err.println("Error reducing metrics: " + e.getLocalizedMessage());
			}
		}
	}
}
