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

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import com.salesforce.dva.argus.sdk.ArgusService;
import com.salesforce.dva.argus.sdk.ArgusService.PutResult;
import com.salesforce.dva.argus.sdk.entity.Metric;

/**
 * Base class for implementing ETL process.
 * @author Charles Kuo <ckuo@salesforce.com>
 *
 */
public abstract class HeimdallETL {
	/**
	 * Start time for ETL process.
	 */
	protected long startTime;
	
	/**
	 * End time for ETL process.
	 */
	protected long endTime;
	
	/**
	 * Properties for ETL process.
	 */
	protected Properties properties;
	
	/**
	 * Argus source service.
	 */
	protected ArgusService sourceService;
	
	/**
	 * Argus target service.
	 */
	protected ArgusService targetService;
	
	/**
	 * ETL should not write to target service.
	 */
	protected boolean readOnly;
	
	/**
	 * Set start time for ETL process.
	 * @param startTime
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
	/**
	 * Set end time for ETL process.
	 * @param endTime
	 */
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	
	/**
	 * Set properties for ETL process.
	 * @param properties
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	/**
	 * Set Argus source service.
	 * @param sourceService
	 */
	public void setSourceService(ArgusService sourceService) {
		this.sourceService = sourceService;
	}
	
	/**
	 * Set Argus target service.
	 * @param targetService
	 */
	public void setTargetService(ArgusService targetService) {
		this.targetService = targetService;
	}
	
	/**
	 * Set whether ETL should be read-only (not write to target ETL).
	 * @param readOnly
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	/**
	 * Init ETL. Must override.
	 * @param args
	 * @throws Exception
	 */
	public abstract void init() throws Exception;
	
	/**
	 * Execute ETL. Must override.
	 * @throws Exception
	 */
	public abstract void execute() throws Exception;
	
	/**
	 * Print metric to console.
	 * @param metric
	 */
	public static void printMetric(Metric metric) {
		System.out.println(metric.getScope() + ":" + metric.getMetric() + " [" + metric.getDisplayName() + "]");
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		List<Long> times = new ArrayList<Long>();
		for (Long time : metric.getDatapoints().keySet()) {
			times.add(time);
		}
		Collections.sort(times);
		
		for (Long time : times) {
			System.out.print(dateFormat.format(new Date(time)));
			System.out.print(" = ");
			System.out.print(metric.getDatapoints().get(time));
			System.out.println();
		}
	}
	
	/**
	 * Print Argus put result to console.
	 * @param result
	 */
	public static void printPutResult(PutResult result) {
	    System.out.println(
	      MessageFormat.format(
	          "Succeeded: {0}, Failed: {1}.",
	          result.getSuccessCount(),
	          result.getFailCount()
	      )
	    );
	}
	
	/**
	 * Return metric in JSON string format.
	 * @param metric
	 * @return
	 */
	public static String metricToJson(Metric metric) {
		StringBuilder json = new StringBuilder();
		json.append("{\n");
		
		if (metric.getNamespace() != null)
			json.append("\t\"namespace\": \"" + metric.getNamespace() + "\"\n");
		
		if (metric.getMetric() != null)
			json.append("\t\"metric\": \"" + metric.getMetric() + "\"\n");
		
		if (metric.getScope() != null)
			json.append("\t\"scope\": \"" + metric.getScope() + "\"\n");
		
		if (metric.getDisplayName() != null)
			json.append("\t\"displayname\": \"" + metric.getDisplayName() + "\"\n");
		
		if (metric.getUnits() != null)
			json.append("\t\"units\": \"" + metric.getUnits() + "\"\n");
		
		if (metric.getTags() != null) {
			StringBuilder tags = new StringBuilder();
			for (String name : metric.getTags().keySet()) {
				if (tags.length() != 0)
					tags.append(",");
				tags.append(name + "=" + metric.getTags().get(name));
			}
			json.append("\t\"tags\": \"" + tags.toString() + "\"\n");
		}
		
		if (metric.getDatapoints() != null) {
			json.append("\t\"datapoints\":\n");
			json.append("\t{\n");
			
			List<Long> times = new ArrayList<Long>();
			for (Long time : metric.getDatapoints().keySet()) {
				times.add(time);
			}
			Collections.sort(times);
			
			StringBuilder points = new StringBuilder();
			for (Long time : times) {
				json.append("\t\t\"" + time + "\": \"" + metric.getDatapoints().get(time) + "\"\n");
			}
			
			json.append("\t}\n");
		}
		
		json.append("}");
		return json.toString();
	}
}
