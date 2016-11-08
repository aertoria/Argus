package com.salesforce.dva.argus.service.metric.transform.plus;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.service.TSDBService;
import com.salesforce.dva.argus.service.broker.DefaultJSONService;
import com.salesforce.dva.argus.service.metric.transform.Transform;
import com.salesforce.dva.argus.service.metric.transform.TransformFactory;
import com.salesforce.dva.argus.system.SystemConfiguration;

public class HeimdallMetricReducerTestFULL {
	private static final String TEST_SCOPE = "core.CHI.SP2.cs15";
	private static final String TEST_SCOPE_ACT = "db.oracle.CHI.SP2.cs15";
	private static final String TEST_SCOPE_CPU = "system.CHI.SP2.cs15";
	private static final String TEST_METRIC = "holder to be changed";
	private static Injector injector;
	private static SystemConfiguration configuration;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		configuration=new SystemConfiguration(new Properties());
		configuration.setProperty("service.property.json.endpoint", "https://localhost:443/argusws");
		injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				new FactoryModuleBuilder().build(TransformFactory.class);
				bind(TSDBService.class).to(DefaultJSONService.class);
				bind(SystemConfiguration.class).toInstance(configuration);
			}
		});
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		injector=null;
	}

	
	@Test
	public void HeimdallTotalAvaTransform_TRAFFIC(){
		Transform transform=injector.getInstance(HeimdallMetricReducer.class);
		int offset=1000*60;//1MIN
		long start=0L;
		long hourstart=1000*3600L;
        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
        datapoints_1.put(start+1L*offset, "100.0");
        datapoints_1.put(start+2L*offset, "100.0");
        datapoints_1.put(start+3L*offset, "100.0");
        datapoints_1.put(start+4L*offset, "100.0");
        datapoints_1.put(start+5L*offset, "40.0");
        datapoints_1.put(start+6L*offset, "00.0");
        metric_1.setDatapoints(datapoints_1);
        metric_1.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
        metric_1.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
        
        Metric metric_2 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_2 = new HashMap<Long, String>();
        datapoints_2.put(start+1L*offset, "140.0");
        datapoints_2.put(start+2L*offset, "130.0");
        datapoints_2.put(start+3L*offset, "150.0");
        datapoints_2.put(start+4L*offset, "90.0");
        datapoints_2.put(start+5L*offset, "90.0");
        datapoints_2.put(start+6L*offset, "50.0");
        metric_2.setDatapoints(datapoints_2);
        metric_2.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
        metric_2.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
        
        Metric metric_3 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_3 = new HashMap<Long, String>();
        datapoints_3.put(start+1L*offset, "2.0");
        datapoints_3.put(start+2L*offset, "3.0");
        datapoints_3.put(start+3L*offset, "3.0");
        datapoints_3.put(start+4L*offset, "2.0");
        datapoints_3.put(start+5L*offset, "4.0");
        datapoints_3.put(start+6L*offset, "2.0");
        metric_3.setDatapoints(datapoints_3);
        metric_3.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
        metric_3.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
        
        Metric metric_4 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_4 = new HashMap<Long, String>();
        datapoints_4.put(start+1L*offset, "1.0");
        datapoints_4.put(start+2L*offset, "9.0");
        datapoints_4.put(start+3L*offset, "2.0");
        datapoints_4.put(start+4L*offset, "2.0");
        datapoints_4.put(start+5L*offset, "9.0");
        datapoints_4.put(start+6L*offset, "1.0");
        metric_4.setDatapoints(datapoints_4);
        metric_4.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
        metric_4.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
        
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(metric_1);
        metrics.add(metric_2);
        metrics.add(metric_3);
        metrics.add(metric_4);

        
        Metric metric_5 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_5 = new HashMap<Long, String>();
        datapoints_5.put(start+1L*offset, "22.0");
        datapoints_5.put(start+2L*offset, "95.0");
        datapoints_5.put(start+3L*offset, "67.0");
        datapoints_5.put(start+4L*offset, "57.0");
        datapoints_5.put(start+5L*offset, "75.0");
        datapoints_5.put(start+6L*offset, "55.0");
        metric_5.setDatapoints(datapoints_5);
        metric_5.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
        metric_5.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode1.Last_1_Min_Avg");
     
        
        Metric metric_6 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_6 = new HashMap<Long, String>();
        datapoints_6.put(start+1L*offset, "4.0");
        datapoints_6.put(start+2L*offset, "5.0");
        datapoints_6.put(start+3L*offset, "6.0");
        datapoints_6.put(start+4L*offset, "7.0");
        datapoints_6.put(start+5L*offset, "8.0");
        datapoints_6.put(start+6L*offset, "4.0");
        metric_6.setDatapoints(datapoints_6);
        metric_6.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
        metric_6.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode1.Last_1_Min_Avg");
        
        metrics.add(metric_5);
        metrics.add(metric_6);  
        
        
        List<Metric> result = transform.transform(metrics,Arrays.asList("TOTAL"));

        
        List<Metric> expected = new ArrayList<Metric>();        
        Metric expected_metric2 = new Metric("SUM", "CHI.SP2.cs15.Rac1");
        Map<Long, String> expected_Datapoints2=new HashMap<Long, String>();
//        expected_Datapoints2.put(60000L, "4.0");
//        expected_Datapoints2.put(120000L, "5.0");
//        expected_Datapoints2.put(180000L, "6.0");
//        expected_Datapoints2.put(240000L, "7.0");
//        expected_Datapoints2.put(300000L, "8.0");
//        expected_Datapoints2.put(360000L, "4.0");
//        expected_metric2.setDatapoints(expected_Datapoints2);
//        expected.add(expected_metric2);

        result.forEach(r -> System.out.println(r.toString()));
        result.stream()
        	  .filter(m -> m.getMetric().equals("AvailableMin"))
        	  .forEach(m -> System.out.println(m.toString()));
//        assertEquals(expected.get(0),result.get(0)); 
//        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
	}
	
}
