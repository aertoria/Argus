package com.salesforce.dva.argus.service.metric.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.salesforce.dva.argus.entity.Metric;

public class HeimdallTotalAvaTransformTest {
	 private static final String TEST_SCOPE = "test-scope";
	 private static final String TEST_METRIC = "test-metric";
	 
	 @Test
	 public void P90_dev(){
		Transform transform = new HeimdallTotalAvaTransform();
		
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
        
//        List<String> constants = new ArrayList<String>();
//        constants.add("500");
        
        List<Metric> result = transform.transform(metrics);
        System.out.println("\n\nINPUT>>>\n"+metrics);
        System.out.println("\n\nOUTPUT>>>\n"+result);
	 }
}
