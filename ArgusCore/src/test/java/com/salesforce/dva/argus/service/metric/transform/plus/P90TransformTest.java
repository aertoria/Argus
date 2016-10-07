package com.salesforce.dva.argus.service.metric.transform.plus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.inject.Inject;
import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.service.metric.transform.Transform;
import com.salesforce.dva.argus.service.metric.transform.plus.P90Transform;
@RunWith(MockitoJUnitRunner.class)
public class P90TransformTest {
	 private static final String TEST_SCOPE = "test-scope";
	 private static final String TEST_METRIC = "test-metric";
	 
	 @Test
	 public void P90_dev(){
		Transform transform = new P90Transform();	 
		Map<Long, String> datapoints_1 = new HashMap<Long, String>();
        datapoints_1.put(100L, "195.0");
        //datapoints_1.put(200L, "1.0");
        datapoints_1.put(300L, "590.0");
        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
        metric_1.setDatapoints(datapoints_1);
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(metric_1);
        
		Map<Long, String> datapoints_2 = new HashMap<Long, String>();
        datapoints_2.put(100L, "870.0");
        datapoints_2.put(200L, "209.0");
        datapoints_2.put(300L, "435.0");
        Metric metric_2 = new Metric(TEST_SCOPE, TEST_METRIC);
        metric_2.setDatapoints(datapoints_2);
        metrics.add(metric_2);
        
        List<String> constants = new ArrayList<String>();
        constants.add("500");
        
        List<Metric> result = transform.transform(metrics,constants);
        System.out.println("\n\nINPUT>>>\n"+metrics);
        System.out.println("\n\nOUTPUT>>>\n"+result);
	 }
}
