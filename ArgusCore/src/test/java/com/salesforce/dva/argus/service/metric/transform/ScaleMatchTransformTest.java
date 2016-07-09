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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.salesforce.dva.argus.entity.Metric;

public class ScaleMatchTransformTest {
	 private static final String TEST_SCOPE = "test-scope";
	 private static final String TEST_METRIC = "test-metric";
	 
	 @Test(expected = UnsupportedOperationException.class)
	 public void ScaleMatchTransformWithEmtpyConstant(){
		Transform transform = new ScaleMatchTransform();	
        List<Metric> metrics = new ArrayList<Metric>();     
        List<Metric> result = transform.transform(metrics);
	 }
	 
	 @Test(expected = IllegalArgumentException.class)
	 public void ScaleMatchTransformWithEmtpyMetric(){
		Transform transform = new ScaleMatchTransform();	
        List<Metric> metrics = new ArrayList<Metric>();     
        List<String> constants = new ArrayList<String>();
        constants.add("device");
        List<Metric> result = transform.transform(metrics,constants);
	 }
	 
	 @Test(expected = IllegalArgumentException.class)
	 public void ScaleMatchTransformWithInvalidTagName(){
		Transform transform = new ScaleMatchTransform();	
        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
        datapoints_1.put(100L, "200.0");
        datapoints_1.put(200L, "100.0");
        metric_1.setDatapoints(datapoints_1);
        metric_1.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
        metric_1.setMetric("TrustTime");
        
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(metric_1);
        
        List<String> constants = new ArrayList<String>();
        constants.add("podId");
        
        List<Metric> result = transform.transform(metrics,constants);
	 }
	 
	 
	 @Test
	 public void ScaleMatchTransformdev(){
		Transform transform = new ScaleMatchTransform();
		
        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
        datapoints_1.put(100L, "200.0");
        datapoints_1.put(200L, "100.0");
        metric_1.setDatapoints(datapoints_1);
        metric_1.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
        metric_1.setMetric("TrustTime");
        
        Metric metric_2 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_2 = new HashMap<Long, String>();
        datapoints_2.put(100L, "400.0");
        datapoints_2.put(200L, "350.0");
        metric_2.setDatapoints(datapoints_2);
        metric_2.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
        metric_2.setMetric("TrustTime");
        
        Metric metric_3 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_3 = new HashMap<Long, String>();
        datapoints_3.put(100L, "2.0");
        datapoints_3.put(200L, "3.0");
        metric_3.setDatapoints(datapoints_3);
        metric_3.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
        metric_3.setMetric("TrustCount");
        
        Metric metric_4 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_4 = new HashMap<Long, String>();
        datapoints_4.put(100L, "1.0");
        datapoints_4.put(200L, "9.0");
        metric_4.setDatapoints(datapoints_4);
        metric_4.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
        metric_4.setMetric("TrustCount");
        
        
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(metric_1);
        metrics.add(metric_2);
        metrics.add(metric_3);
        metrics.add(metric_4);
        
        List<String> constants = new ArrayList<String>();
        constants.add("device");
        constants.add("Trust.*");
        List<Metric> result = transform.transform(metrics,constants);

        Map<Long, String> expected = new HashMap<Long, String>();
        expected.put(100L, "800.0");
        expected.put(200L, "3450.0");
        assertEquals(result.get(0).getDatapoints().size(), expected.size());
        assertEquals(expected, result.get(0).getDatapoints());
	 }
	 
	 @Test
	 public void ScaleMatchTransformWithPartinal(){
		Transform transform = new ScaleMatchTransform();
		
        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
        datapoints_1.put(100L, "200.0");
        datapoints_1.put(200L, "100.0");
        metric_1.setDatapoints(datapoints_1);
        metric_1.setTag("device", "na11-dapp1-1-chi.ops.sfdc.net");
        metric_1.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
        
        Metric metric_2 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_2 = new HashMap<Long, String>();
        datapoints_2.put(100L, "400.0");
        datapoints_2.put(200L, "350.0");
        metric_2.setDatapoints(datapoints_2);
        metric_2.setTag("device", "na11-dapp1-2-chi.ops.sfdc.net");
        metric_2.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
        
        Metric metric_3 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_3 = new HashMap<Long, String>();
        datapoints_3.put(100L, "2.0");
        datapoints_3.put(200L, "3.0");
        metric_3.setDatapoints(datapoints_3);
        metric_3.setTag("device", "na11-dapp1-1-chi.ops.sfdc.net");
        metric_3.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
        
        Metric metric_4 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_4 = new HashMap<Long, String>();
        datapoints_4.put(100L, "1.0");
        datapoints_4.put(200L, "9.0");
        metric_4.setDatapoints(datapoints_4);
        metric_4.setTag("device", "na11-dapp1-2-chi.ops.sfdc.net");
        metric_4.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
        
        
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(metric_1);
        metrics.add(metric_2);
        metrics.add(metric_3);
        metrics.add(metric_4);
        
        List<String> constants = new ArrayList<String>();
        constants.add("device");
        constants.add("SFDC_type-Stats-name1-System-name2-trustAptRequest.*RACNode2.*");
        List<Metric> result = transform.transform(metrics,constants);
        
        Map<Long, String> expected = new HashMap<Long, String>();
        expected.put(100L, "800.0");
        expected.put(200L, "3450.0");
        assertEquals(result.get(0).getDatapoints().size(), expected.size());
        assertEquals(expected, result.get(0).getDatapoints());
	 }
	 
	 
	 @Test
	 public void ScaleMatchTransformWithSignalMetrics(){
		Transform transform = new ScaleMatchTransform();
		
        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
        datapoints_1.put(100L, "200.0");
        datapoints_1.put(200L, "100.0");
        metric_1.setDatapoints(datapoints_1);
        metric_1.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
        metric_1.setMetric("TrustTime");
        
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(metric_1);
        
        List<String> constants = new ArrayList<String>();
        constants.add("device");
        List<Metric> result = transform.transform(metrics,constants);

        Map<Long, String> expected = new HashMap<Long, String>();
        assertEquals(result.get(0).getDatapoints().size(), expected.size());
        assertEquals(expected, result.get(0).getDatapoints());
	 }

	 @Test
	 public void ScaleMatchTransformWithSignalMetricMultipleTS(){
		Transform transform = new ScaleMatchTransform();
		
        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
        datapoints_1.put(100L, "200.0");
        datapoints_1.put(200L, "100.0");
        metric_1.setDatapoints(datapoints_1);
        metric_1.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
        metric_1.setMetric("TrustTime");
        
        
        Metric metric_2 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_2 = new HashMap<Long, String>();
        datapoints_2.put(100L, "2.0");
        datapoints_2.put(200L, "3.0");
        metric_2.setDatapoints(datapoints_2);
        metric_2.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
        metric_2.setMetric("TrustCount");
        
      
        
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(metric_1);
        metrics.add(metric_2);
        
        List<String> constants = new ArrayList<String>();
        constants.add("device");
        constants.add("Trust.*");
        List<Metric> result = transform.transform(metrics,constants);

        Map<Long, String> expected = new HashMap<Long, String>();
        expected.put(100L, "400.0");
        expected.put(200L, "300.0");
        assertEquals(result.get(0).getDatapoints().size(), expected.size());
        assertEquals(expected, result.get(0).getDatapoints());
	 }
	 
	 
	 @Test
	 public void ScaleMatchTransformWithMultipleMetricMultipleTS(){
		Transform transform = new ScaleMatchTransform();
		
        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
        datapoints_1.put(100L, "200.0");
        datapoints_1.put(200L, "100.0");
        metric_1.setDatapoints(datapoints_1);
        metric_1.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
        metric_1.setMetric("TrustTime");
        
        
        Metric metric_2 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_2 = new HashMap<Long, String>();
        datapoints_2.put(100L, "2.0");
        datapoints_2.put(200L, "3.0");
        metric_2.setDatapoints(datapoints_2);
        metric_2.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
        metric_2.setMetric("TrustCount");
        
        Metric metric_3 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_3 = new HashMap<Long, String>();
        datapoints_3.put(100L, "200.0");
        datapoints_3.put(200L, "100.0");
        metric_3.setDatapoints(datapoints_1);
        metric_3.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
        metric_3.setMetric("TrustTime");
        
        
        Metric metric_4 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_4 = new HashMap<Long, String>();
        datapoints_4.put(100L, "2.0");
        datapoints_4.put(200L, "3.0");
        metric_4.setDatapoints(datapoints_2);
        metric_4.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
        metric_4.setMetric("TrustCount");
        
        
        Metric metric_5 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_5 = new HashMap<Long, String>();
        datapoints_5.put(100L, "200.0");
        datapoints_5.put(200L, "100.0");
        metric_5.setDatapoints(datapoints_1);
        metric_5.setTag("device", "na11-app1-3-chi.ops.sfdc.net");
        metric_5.setMetric("TrustTime");
        
        
        Metric metric_6 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_6 = new HashMap<Long, String>();
        datapoints_6.put(100L, "2.0");
        datapoints_6.put(200L, "3.0");
        metric_6.setDatapoints(datapoints_2);
        metric_6.setTag("device", "na11-app1-3-chi.ops.sfdc.net");
        metric_6.setMetric("TrustCount");
        
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(metric_1);
        metrics.add(metric_2);
        metrics.add(metric_3);
        metrics.add(metric_4);
        metrics.add(metric_5);
        metrics.add(metric_6);
        
        List<String> constants = new ArrayList<String>();
        constants.add("device");
        constants.add("Trust.*");
        List<Metric> result = transform.transform(metrics,constants);
        
        Map<Long, String> expected = new HashMap<Long, String>();
        expected.put(100L, "1200.0");
        expected.put(200L, "900.0");
        assertEquals(result.get(0).getDatapoints().size(), expected.size());
        assertEquals(expected, result.get(0).getDatapoints());
	 }
	 
}

/* Copyright (c) 2016, Salesforce.com, Inc.  All rights reserved. */




