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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.service.metric.transform.Transform;
import com.salesforce.dva.argus.system.SystemAssert;

/**
 * Calculate percentage of pods meeting specified SLA threshold.
 * @author Charles Kuo <ckuo@salesforce.com>
 *
 */
public class HeimdallDataGuardPercentPodsMeetingSLATransform implements Transform {
	@Override
	public List<Metric> transform(List<Metric> metrics) {
		throw new UnsupportedOperationException("Have to have one constant");
	}
	
	@Override
	public List<Metric> transform(List<Metric> metrics, List<String> constants) {
		SystemAssert.requireArgument(constants.size()==1, "Transform value required: threshold");
		float threshold = Float.parseFloat(constants.get(0));
		
		// get unique, sorted times from all metrics
		SortedSet<Long> times = new TreeSet<Long>();
		for (Metric metric : metrics) {
			for (Long time : metric.getDatapoints().keySet()) {
				times.add(time);
			}
		}
		
		Map<Long, String> points = new HashMap<Long, String>();
		
		Metric metric2 = new Metric("dg", "percent_pods_meeting_sla");
		metric2.setDisplayName("Percent pods meeting SLA");
		metric2.setUnits(null);
		metric2.setTags(null);
		
		// for each unique time, calculate percentage of pods meeting threshold SLA
		for (Long time : times) {
			
			// calculate total pods and total met
			int totalPods = 0;
			int totalMet = 0;
			
			for (Metric metric : metrics) {
				// skip pod if can't read SLA
				String value = metric.getDatapoints().get(time);
				if (value == null)
					continue;
				float sla;
				try {
					sla = Float.parseFloat(value);
				}
				catch (NumberFormatException e) {
					continue;
				}
				
				totalPods++;
				
				if (sla >= threshold)
					totalMet++;
			}
			
			points.put(time, Float.toString(100 * totalMet/(float)totalPods));
		}
		
		metric2.setDatapoints(points);
		return Arrays.asList(metric2);
	}
	
	@Override
	public List<Metric> transform(List<Metric>... metrics) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getResultScopeName() {
		// TODO Auto-generated method stub
		return null;
	}
}
