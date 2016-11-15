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
import com.salesforce.dva.argus.service.metric.transform.plus.HeimdallDataGuardTransform;;
/** @RunWith(MockitoJUnitRunner.class)  **/
public class HeimdallDataGuardTransformTest {
	
	 private static final String TEST_SCOPE_1 = "db.CHI.SP4.na27";
	 private static final String TEST_METRIC_1 = "NA27DB1CHI.remote_dataguard_lag";
	 private static final String TEST_SCOPE_2 = "db.WAS.SP4.na30";
	 private static final String TEST_METRIC_2 = "NA30DB1WAS.remote_dataguard_lag";	
	 private static final String TEST_SCOPE_3 = "db.WAS.SP4.na44";
	 private static final String TEST_METRIC_3 = "NA44DB1WAS.remote_dataguard_lag";	
	 
	 
	 @Test
	 public void DataGuardTest(){
		 
		 Transform transform = new HeimdallDataGuardTransform();	
		 
			Map<Long, String> datapoints_1 = new HashMap<Long, String>();
	        datapoints_1.put(1473389110000L, "18.0");
	        datapoints_1.put(1473389162000L, "2.0");
	        datapoints_1.put(1473389221000L, "19.0");
	        datapoints_1.put(1473389282000L, "1.0");
	        datapoints_1.put(1473389341000L, "6.0");
	        datapoints_1.put(1473389402001L, "1.0");
	        datapoints_1.put(1473389221002L, "-1.0");
	        datapoints_1.put(1473389282002L, "-1.0");
	        datapoints_1.put(1473389341002L, "60.0");
	        datapoints_1.put(1473389402003L, "140.0"); 
	        // 7/8 = 87.5% compliance, confidence 80%
	        
	        Metric metric_1 = new Metric(TEST_SCOPE_1, TEST_METRIC_1);
	        metric_1.setDatapoints(datapoints_1);
	        
			Map<Long, String> datapoints_2 = new HashMap<Long, String>();
	        datapoints_2.put(1473390730000L, "200.0");
	        datapoints_2.put(1473390782000L, "2.0");
	        datapoints_2.put(1473390842002L, "190.0");
	        datapoints_2.put(1473390902004L, "210.0");
	        datapoints_2.put(1473390962003L, "6.0");
	        datapoints_2.put(1473391022003L, "-1.0");
	        datapoints_2.put(1473390601002L, "-1.0");
	        datapoints_2.put(1473390662001L, "-1.0");
	        datapoints_2.put(1473390730007L, "212.0");
	        datapoints_2.put(1473390782002L, "14.0"); 
	        // 3/7 = 42.85%compliance, confidence 70% 
	        
	        Metric metric_2 = new Metric(TEST_SCOPE_2, TEST_METRIC_2);
	        metric_2.setDatapoints(datapoints_2);

	        Map<Long, String> datapoints_3 = new HashMap<Long, String>();
	        Metric metric_3 = new Metric(TEST_SCOPE_3, TEST_METRIC_3);
	        metric_3.setDatapoints(datapoints_3);
	        //empty data-points
	        
	        List<Metric> metrics = new ArrayList<Metric>();
	        metrics.add(metric_1);
	        metrics.add(metric_2);
	        metrics.add(metric_3);
	        
	        List<String> constants = new ArrayList<String>();
	        /*
	         * setting threshold to 120 sec
	         * */
	        constants.add("120.00");
	        
	        //System.out.println("Threshold: 120sec");
	        List<Metric> result = transform.transform(metrics,constants);
	        assertEquals(result.size(), 3);
	        
	        
	        Map<Long, String> expected_1 = new HashMap<Long, String>();
	        expected_1.put(0L, "87.5");
	        expected_1.put(1L, "80.0");
	        assertEquals(expected_1, result.get(0).getDatapoints());
	        System.out.print(result.get(0).getDatapoints()); 
	        
	        
	        Map<Long, String> expected_2 = new HashMap<Long, String>();
	        expected_2.put(0L, "42.9");
	        expected_2.put(1L, "70.0");
	        assertEquals(expected_2, result.get(1).getDatapoints());
	        System.out.print(result.get(1).getDatapoints()); 
	        
	        
	        /*
	        System.out.println("\n\nINPUT>>>\n"+metrics);
	        System.out.println("\n\nOUTPUT>>>\n"+result.get(1).getDatapoints());
	        */
	 }
	
	

}
