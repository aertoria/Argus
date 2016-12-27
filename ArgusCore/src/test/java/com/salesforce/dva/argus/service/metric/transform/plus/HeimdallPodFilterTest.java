package com.salesforce.dva.argus.service.metric.transform.plus;

import static org.hamcrest.MatcherAssert.assertThat; 
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

/** import com.google.inject.Inject;  **/ 
import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.service.metric.transform.Transform;
import com.salesforce.dva.argus.service.metric.transform.plus.HeimdallPodFilter;

public class HeimdallPodFilterTest {
	
	 private static final String TEST_SCOPE_1 = "db.CHI.SP4.na27";
	 private static final String TEST_METRIC_1 = "NA27DB1CHI.replication_type";
	 private static final String TEST_SCOPE_2 = "db.WAS.SP4.na30";
	 private static final String TEST_METRIC_2 = "NA30DB1WAS.replication_type";	
	 private static final String TEST_SCOPE_3 = "db.WAS.SP4.na44";
	 private static final String TEST_METRIC_3 = "NA44DB1WAS.replication_type";	
	 
	 private static final String TEST_SCOPE_4 = "db.CHI.SP4.na27";
	 private static final String TEST_METRIC_4 = "NA27DB1CHI.remote_dg_transport_lag";
	 private static final String TEST_SCOPE_5 = "db.WAS.SP4.na30";
	 private static final String TEST_METRIC_5 = "NA30DB1WAS.local_dataguard_lag";	
	 private static final String TEST_SCOPE_6 = "db.WAS.SP4.na44";
	 private static final String TEST_METRIC_6 = "NA44DB1WAS.remote_dg_transport_lag";	
	 
	 
	 @Test
	 public void podFilterTest(){
		    Transform transform = new HeimdallPodFilter();	
		 
			Map<Long, String> datapoints_1 = new HashMap<Long, String>();
	        datapoints_1.put(1473389110000L, "1.0");
	        datapoints_1.put(1473389162000L, "1.0");
	        datapoints_1.put(1473389221000L, "1.0");
	        datapoints_1.put(1473389282000L, "1.0");
	        datapoints_1.put(1473389341000L, "1.0");
	        datapoints_1.put(1473389402001L, "1.0");
	        datapoints_1.put(1473389221002L, "1.0");
	        datapoints_1.put(1473389282002L, "-1.0");
	        datapoints_1.put(1473389341002L, "1.0");
	        datapoints_1.put(1473389402003L, "1.0"); 
	       
	        
	        Metric metric_1 = new Metric(TEST_SCOPE_1, TEST_METRIC_1);
	        metric_1.setDatapoints(datapoints_1);
	        
			Map<Long, String> datapoints_2 = new HashMap<Long, String>();
	        datapoints_2.put(1473390730000L, "-1.0");
	        datapoints_2.put(1473390782000L, "0.0");
	        datapoints_2.put(1473390842002L, "0.0");
	        datapoints_2.put(1473390902004L, "0.0");
	        datapoints_2.put(1473390962003L, "0.0");
	        datapoints_2.put(1473391022003L, "0.0");
	        datapoints_2.put(1473390601002L, "0.0");
	        datapoints_2.put(1473390662001L, "-1.0");
	        datapoints_2.put(1473390730007L, "0.0");
	        datapoints_2.put(1473390782002L, "0.0"); 
	       
	        
	        Metric metric_2 = new Metric(TEST_SCOPE_2, TEST_METRIC_2);
	        metric_2.setDatapoints(datapoints_2);

	        Map<Long, String> datapoints_3 = new HashMap<Long, String>();
	        Metric metric_3 = new Metric(TEST_SCOPE_3, TEST_METRIC_3);
	        metric_3.setDatapoints(datapoints_3);
	        //empty data-points
	        
	        
	        Metric metric_4 = new Metric(TEST_SCOPE_4, TEST_METRIC_4);
	        Map<Long, String> datapoints_4 = new HashMap<Long, String>();
	        datapoints_4.put(1473389110000L, "3.0");
	        datapoints_4.put(1473389162000L, "2.0");
	        datapoints_4.put(1473389221000L, "4.0");
	        datapoints_4.put(1473389282000L, "1.0");
	        datapoints_4.put(1473389341000L, "2.0");
	        metric_4.setDatapoints(datapoints_4);
	        
	        Metric metric_5 = new Metric(TEST_SCOPE_5, TEST_METRIC_5);
	        Metric metric_6 = new Metric(TEST_SCOPE_6, TEST_METRIC_6);
	        List<Metric> metrics = new ArrayList<Metric>();
	        metrics.add(metric_1);
	        metrics.add(metric_2);
	        metrics.add(metric_3);
	        metrics.add(metric_4);
	        metrics.add(metric_5);
	        metrics.add(metric_6);
	        
	        List<String> constants = new ArrayList<String>();

	        //Looking for DGoWAN pods
	        constants.add("1.00");
	        
	        
	        metrics.forEach(m -> System.out.println(m.toString()));
	        //System.out.println("Threshold: 120sec");
	        List<Metric> result = transform.transform(metrics,constants);
	        //System.out.print(result.toString()); 
	        System.out.println("\n\n");
	        result.forEach(m -> System.out.println(m.toString()));
	        
	        // only na27 related metrics should be seen after using filter(na27 is DGoWAN)
	        assertEquals(result.size(), 1);
	        assertEquals(metric_4.getMetric(), result.get(0).getMetric());
	        assertEquals(metric_4.getScope(), result.get(0).getScope());
	        
	        
	 }

}
