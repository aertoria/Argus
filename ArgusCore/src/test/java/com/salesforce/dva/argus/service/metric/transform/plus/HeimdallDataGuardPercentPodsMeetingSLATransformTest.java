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

package com.salesforce.dva.argus.service.metric.transform.plus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.service.metric.transform.Transform;

/**
 * Test transform calculates correct percentage of pods that meet SLA threshold.
 * @author Charles Kuo <ckuo@salesforce.com>
 *
 */
public class HeimdallDataGuardPercentPodsMeetingSLATransformTest {
	protected List<Metric> metrics;
	protected Transform transform;
	
	@Before
	public void setup() {
		Metric metric;
		Map<Long, String> points;
		
		transform = new HeimdallDataGuardPercentPodsMeetingSLATransform();
		metrics = new ArrayList<Metric>();
		
		points = new HashMap<Long, String>();
		points.put(1480291200000L, "90");
		points.put(1480896000000L, "91");
		points.put(1481500800000L, "87");
		points.put(1482105600000L, "96");
		points.put(1482710400000L, "93");
		points.put(1483315200000L, "91");
		metric = new Metric("dg.sla.prd.wk", "na1.dg_remote_transport_lag");
		metric.setDatapoints(points);
		metrics.add(metric);
		
		points = new HashMap<Long, String>();
		points.put(1480291200000L, "92");
		points.put(1480896000000L, "95");
		points.put(1481500800000L, "97");
		points.put(1482105600000L, "86");
		points.put(1482710400000L, "89");
		points.put(1483315200000L, "93");
		metric = new Metric("dg.sla.prd.wk", "na2.dg_remote_transport_lag");
		metric.setDatapoints(points);
		metrics.add(metric);

		points = new HashMap<Long, String>();
		points.put(1480291200000L, "89");
		points.put(1480896000000L, "92");
		points.put(1481500800000L, "92");
		points.put(1482105600000L, "99");
		points.put(1482710400000L, "93");
		points.put(1483315200000L, "85");
		metric = new Metric("dg.sla.prd.wk", "na3.dg_remote_transport_lag");
		metric.setDatapoints(points);
		metrics.add(metric);
		
		points = new HashMap<Long, String>();
		points.put(1480291200000L, "97");
		points.put(1480896000000L, "98");
		points.put(1481500800000L, "100");
		points.put(1482105600000L, "72");
		points.put(1482710400000L, "84");
		points.put(1483315200000L, "99");
		metric = new Metric("dg.sla.prd.wk", "na4.dg_remote_transport_lag");
		metric.setDatapoints(points);
		metrics.add(metric);
	}
	
	@Test
	public void test() {
		List<String> constants = new ArrayList<String>();
		constants.add("95");

		List<Metric> result = transform.transform(metrics, constants);
		assertEquals(result.size(), 1);

		Map<Long, String> points = new HashMap<Long, String>();
		points.put(1480291200000L, "25.0");
		points.put(1480896000000L, "50.0");
		points.put(1481500800000L, "50.0");
		points.put(1482105600000L, "50.0");
		points.put(1482710400000L, "0.0");
		points.put(1483315200000L, "25.0");
		assertTrue(points.equals(result.get(0).getDatapoints()));
	}
}
