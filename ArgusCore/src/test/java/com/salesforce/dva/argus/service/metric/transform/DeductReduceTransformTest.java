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

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;
import com.salesforce.dva.argus.entity.Metric;

public class DeductReduceTransformTest {
	private static final String TEST_SCOPE = "test-scope";
	private static final String TEST_METRIC = "test-metric";

	 @Test(expected = IllegalArgumentException.class)
	 public void testDeductMetricWithConstant() {
	 Transform transform = new DeductReduceTransform();
	 List<Metric> metrics = null;
	 List<String> constants = new ArrayList<String>();
	
	 constants.add("1s");
	 constants.add("2s");
	 transform.transform(metrics, constants);
	 }
	
	@Test
	public void testDeductMetricEmptyDatapoints() {
		Transform transform = new DeductReduceTransform();
		Map<Long, String> datapoints_1 = new HashMap<Long, String>();
		Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
		metric_1.setDatapoints(datapoints_1);

		List<Metric> metrics = new ArrayList<Metric>();
		metrics.add(metric_1);

		Map<Long, String> expected_1 = new TreeMap<Long, String>();

		List<Metric> result = transform.transform(metrics);
		assertEquals("Result length should match", result.get(0).getDatapoints().size(), expected_1.size());
		assertEquals("Result value should match", expected_1, result.get(0).getDatapoints());
	}
	
	@Test
	public void testDeductMetricNoToBeDeducted() {
		Transform transform = new DeductReduceTransform();
		Map<Long, String> datapoints_1 = new HashMap<Long, String>();
		datapoints_1.put(1000L, "1.0");
		datapoints_1.put(2000L, "2.0");
		datapoints_1.put(3000L, "3.0");
		Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
		metric_1.setDatapoints(datapoints_1);

		List<Metric> metrics = new ArrayList<Metric>();
		metrics.add(metric_1);

		Map<Long, String> expected_1 = new TreeMap<Long, String>();
		expected_1.put(1000L, "1.0");
		expected_1.put(2000L, "2.0");
		expected_1.put(3000L, "3.0");

		List<Metric> result = transform.transform(metrics);
		assertEquals("Result length should match", result.get(0).getDatapoints().size(), expected_1.size());
		assertEquals("Result value should match", expected_1, result.get(0).getDatapoints());
	}
	
	@Test
	public void testDeductMetricSingle() {
		Transform transform = new DeductReduceTransform();
		Map<Long, String> datapoints_1 = new HashMap<Long, String>();
		datapoints_1.put(1000L, "1.0");
		datapoints_1.put(2000L, "2.0");
		datapoints_1.put(3000L, "3.0");
		Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
		metric_1.setDatapoints(datapoints_1);

		Map<Long, String> datapoints_2 = new HashMap<Long, String>();
		datapoints_2.put(1000L, "3.0");
		datapoints_2.put(2000L, "-2.0");
		datapoints_2.put(3000L, "1.0");
		Metric metric_2 = new Metric(TEST_SCOPE, TEST_METRIC);
		metric_2.setDatapoints(datapoints_2);

		
		List<Metric> metrics = new ArrayList<Metric>();
		metrics.add(metric_1);
		metrics.add(metric_2);

		Map<Long, String> expected_1 = new TreeMap<Long, String>();
		expected_1.put(1000L, "-2.0");
		expected_1.put(2000L, "4.0");
		expected_1.put(3000L, "2.0");

		List<Metric> result = transform.transform(metrics);
		assertEquals("Result length should match", result.get(0).getDatapoints().size(), expected_1.size());
		assertEquals("Result value should match", expected_1, result.get(0).getDatapoints());
	}
	
	@Test
	public void testDeductMetricMutliple() {
		Transform transform = new DeductReduceTransform();
		Map<Long, String> datapoints_1 = new HashMap<Long, String>();
		datapoints_1.put(1000L, "1.0");
		datapoints_1.put(2000L, "2.0");
		datapoints_1.put(3000L, "3.0");
		Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
		metric_1.setDatapoints(datapoints_1);

		Map<Long, String> datapoints_2 = new HashMap<Long, String>();
		datapoints_2.put(1000L, "3.0");
		datapoints_2.put(2000L, "2.0");
		datapoints_2.put(3000L, "1.0");
		Metric metric_2 = new Metric(TEST_SCOPE, TEST_METRIC);
		metric_2.setDatapoints(datapoints_2);

		Map<Long, String> datapoints_3 = new HashMap<Long, String>();
		datapoints_3.put(1000L, ".1");
		datapoints_3.put(2000L, ".1");
		datapoints_3.put(3000L, ".1");
		Metric metric_3 = new Metric(TEST_SCOPE, TEST_METRIC);
		metric_3.setDatapoints(datapoints_3);

		List<Metric> metrics = new ArrayList<Metric>();
		metrics.add(metric_1);
		metrics.add(metric_2);
		metrics.add(metric_3);

		Map<Long, String> expected_1 = new TreeMap<Long, String>();
		expected_1.put(1000L, "-2.1");
		expected_1.put(2000L, "-0.1");
		expected_1.put(3000L, "1.9");

		List<Metric> result = transform.transform(metrics);
		assertEquals("Result length should match", result.get(0).getDatapoints().size(), expected_1.size());
		assertEquals("Result value should match", expected_1, result.get(0).getDatapoints());
	}
	
	@Test
	public void testDeductMetricMutlipleNotMatching() {
		Transform transform = new DeductReduceTransform();
		Map<Long, String> datapoints_1 = new HashMap<Long, String>();
		datapoints_1.put(1000L, "1.0");
		datapoints_1.put(2000L, "2.0");
		datapoints_1.put(3000L, "3.0");
		Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
		metric_1.setDatapoints(datapoints_1);

		Map<Long, String> datapoints_2 = new HashMap<Long, String>();
		datapoints_2.put(1000L, "3.0");
//		datapoints_2.put(2000L, "2.0");
		datapoints_2.put(3000L, "1.0");
		Metric metric_2 = new Metric(TEST_SCOPE, TEST_METRIC);
		metric_2.setDatapoints(datapoints_2);

		List<Metric> metrics = new ArrayList<Metric>();
		metrics.add(metric_1);
		metrics.add(metric_2);

		Map<Long, String> expected_1 = new TreeMap<Long, String>();
		expected_1.put(1000L, "2.0");
		expected_1.put(2000L, "2.0");
		expected_1.put(3000L, "2.0");

		List<Metric> result = transform.transform(metrics);
		assertEquals("Result length should match", result.get(0).getDatapoints().size(), expected_1.size());
		assertEquals("Result value should match", expected_1, result.get(0).getDatapoints());
	}
}
/* Copyright (c) 2016, Salesforce.com, Inc. All rights reserved. */