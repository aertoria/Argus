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

public class HeimdallMetricReducerTest {
	private static final String TEST_SCOPE = "core.CHI.SP2.cs15";
	private static final String TEST_SCOPE_ACT = "db.oracle.CHI.SP2.cs15";
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

//	@Test
//	public void HeimdallTotalAvaTransform_APT(){
//		Transform transform=injector.getInstance(HeimdallMetricReducer.class);
//		int offset=1000*60;
//		long start=0L;
//        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
//        datapoints_1.put(start+1L*offset, "900.0");
//        datapoints_1.put(start+2L*offset, "1200.0");
//        datapoints_1.put(start+3L*offset, "1000.0");
//        datapoints_1.put(start+4L*offset, "1000.0");
//        datapoints_1.put(start+5L*offset, "400.0");
//        datapoints_1.put(start+6L*offset, "200.0");
//        metric_1.setDatapoints(datapoints_1);
//        metric_1.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_1.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_2 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_2 = new HashMap<Long, String>();
//        datapoints_2.put(start+1L*offset, "1400.0");
//        datapoints_2.put(start+2L*offset, "1350.0");
//        datapoints_2.put(start+3L*offset, "1350.0");
//        datapoints_2.put(start+4L*offset, "950.0");
//        datapoints_2.put(start+5L*offset, "950.0");
//        datapoints_2.put(start+6L*offset, "50.0");
//        metric_2.setDatapoints(datapoints_2);
//        metric_2.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
//        metric_2.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_3 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_3 = new HashMap<Long, String>();
//        datapoints_3.put(start+1L*offset, "2.0");
//        datapoints_3.put(start+2L*offset, "3.0");
//        datapoints_3.put(start+3L*offset, "3.0");
//        datapoints_3.put(start+4L*offset, "2.0");
//        datapoints_3.put(start+5L*offset, "4.0");
//        datapoints_3.put(start+6L*offset, "2.0");
//        metric_3.setDatapoints(datapoints_3);
//        metric_3.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_3.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_4 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_4 = new HashMap<Long, String>();
//        datapoints_4.put(start+1L*offset, "1.0");
//        datapoints_4.put(start+2L*offset, "9.0");
//        datapoints_4.put(start+3L*offset, "2.0");
//        datapoints_4.put(start+4L*offset, "2.0");
//        datapoints_4.put(start+5L*offset, "9.0");
//        datapoints_4.put(start+6L*offset, "1.0");
//        metric_4.setDatapoints(datapoints_4);
//        metric_4.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
//        metric_4.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
//        
//        
//        Metric metric_6 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_6 = new HashMap<Long, String>();
//        datapoints_6.put(start+1L*offset, "0.0");
//        datapoints_6.put(start+2L*offset, "0.0");
//        datapoints_6.put(start+3L*offset, "0.0");
//        datapoints_6.put(start+4L*offset, "0.0");
//        datapoints_6.put(start+5L*offset, "0.0");
//        datapoints_6.put(start+6L*offset, "0.0");
//        metric_6.setDatapoints(datapoints_6);
//        metric_6.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_6.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode1.Last_1_Min_Avg");
//       
//        List<Metric> metrics = new ArrayList<Metric>();
//        metrics.add(metric_1);
//        metrics.add(metric_2);
//        metrics.add(metric_3);
//        metrics.add(metric_4);
//        metrics.add(metric_6);
//        
//        List<Metric> result = transform.transform(metrics,Arrays.asList("APT"));
//
//        List<Metric> expected = new ArrayList<Metric>();
//        Metric expected_metric1 = new Metric("DIVIDE", "CHI.SP2.cs15.Rac1");
//        expected.add(expected_metric1);
//        
//        Metric expected_metric2 = new Metric("DIVIDE", "CHI.SP2.cs15.Rac2");
//        Map<Long, String> expected_Datapoints2=new HashMap<Long, String>();
//        expected_Datapoints2.put(60000L, "1066.6666666666667");
//        expected_Datapoints2.put(120000L, "1312.5");
//        expected_Datapoints2.put(180000L, "1140.0");
//        expected_Datapoints2.put(240000L, "975.0");
//        expected_Datapoints2.put(300000L, "780.7692307692307");
//        expected_Datapoints2.put(360000L, "150.0");
//        expected_metric2.setDatapoints(expected_Datapoints2);
//        expected.add(expected_metric2);
// 
//        assertEquals(expected,result); 
//        assertEquals(result.get(1).getDatapoints(),expected.get(1).getDatapoints()); 
//	}
//	
//	@Test
//	public void HeimdallTotalAvaTransform_IMPACT(){
//		Transform transform=injector.getInstance(HeimdallMetricReducer.class);
//		int offset=1000*60;
//		long start=0L;
//        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
//        datapoints_1.put(start+1L*offset, "900.0");
//        datapoints_1.put(start+2L*offset, "1200.0");
//        datapoints_1.put(start+3L*offset, "1000.0");
//        datapoints_1.put(start+4L*offset, "1000.0");
//        datapoints_1.put(start+5L*offset, "400.0");
//        datapoints_1.put(start+6L*offset, "200.0");
//        metric_1.setDatapoints(datapoints_1);
//        metric_1.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_1.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_2 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_2 = new HashMap<Long, String>();
//        datapoints_2.put(start+1L*offset, "1400.0");
//        datapoints_2.put(start+2L*offset, "1350.0");
//        datapoints_2.put(start+3L*offset, "1350.0");
//        datapoints_2.put(start+4L*offset, "950.0");
//        datapoints_2.put(start+5L*offset, "950.0");
//        datapoints_2.put(start+6L*offset, "50.0");
//        metric_2.setDatapoints(datapoints_2);
//        metric_2.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
//        metric_2.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_3 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_3 = new HashMap<Long, String>();
//        datapoints_3.put(start+1L*offset, "2.0");
//        datapoints_3.put(start+2L*offset, "3.0");
//        datapoints_3.put(start+3L*offset, "3.0");
//        datapoints_3.put(start+4L*offset, "2.0");
//        datapoints_3.put(start+5L*offset, "4.0");
//        datapoints_3.put(start+6L*offset, "2.0");
//        metric_3.setDatapoints(datapoints_3);
//        metric_3.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_3.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_4 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_4 = new HashMap<Long, String>();
//        datapoints_4.put(start+1L*offset, "1.0");
//        datapoints_4.put(start+2L*offset, "9.0");
//        datapoints_4.put(start+3L*offset, "2.0");
//        datapoints_4.put(start+4L*offset, "2.0");
//        datapoints_4.put(start+5L*offset, "9.0");
//        datapoints_4.put(start+6L*offset, "1.0");
//        metric_4.setDatapoints(datapoints_4);
//        metric_4.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
//        metric_4.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
//        
//        
//        Metric metric_6 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_6 = new HashMap<Long, String>();
//        datapoints_6.put(start+1L*offset, "0.0");
//        datapoints_6.put(start+2L*offset, "0.0");
//        datapoints_6.put(start+3L*offset, "0.0");
//        datapoints_6.put(start+4L*offset, "0.0");
//        datapoints_6.put(start+5L*offset, "0.0");
//        datapoints_6.put(start+6L*offset, "0.0");
//        metric_6.setDatapoints(datapoints_6);
//        metric_6.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_6.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode1.Last_1_Min_Avg");
//        
//        Metric metric_7 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_7 = new HashMap<Long, String>();
//        datapoints_7.put(start+1L*offset, "1.0");
//        datapoints_7.put(start+2L*offset, "9.0");
//        datapoints_7.put(start+3L*offset, "2.0");
//        datapoints_7.put(start+4L*offset, "2.0");
//        datapoints_7.put(start+5L*offset, "9.0");
//        datapoints_7.put(start+6L*offset, "1.0");
//        metric_7.setDatapoints(datapoints_7);
//        metric_7.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
//        metric_7.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode1.Last_1_Min_Avg");
//        
//        
//        
//        Metric metric_5 = new Metric("SUM", "result");
//        Map<Long, String> datapoints_5 = new HashMap<Long, String>();
//        datapoints_5.put(start+1L*offset, "0");
//        datapoints_5.put(start+2L*offset, "0");
//        datapoints_5.put(start+3L*offset, "69");
//        datapoints_5.put(start+4L*offset, "50");
//        datapoints_5.put(start+5L*offset, "32");
//        datapoints_5.put(start+6L*offset, "0");
//        metric_5.setDatapoints(datapoints_5);
//        metric_5.setTag("device", "na11-db1-2-chi.ops.sfdc.net");
//        metric_5.setMetric("result");
//        
//        
//        List<Metric> metrics = new ArrayList<Metric>();
//        metrics.add(metric_1);
//        metrics.add(metric_2);
//        metrics.add(metric_3);
//        metrics.add(metric_4);
//        //metrics.add(metric_5);
//        metrics.add(metric_6);
//        //metrics.add(metric_7);        
//        List<Metric> result = transform.transform(metrics,Arrays.asList("IMPACT"));
//
//        
//        List<Metric> expected = new ArrayList<Metric>();
//        Metric expected_metric1 = new Metric("DIVIDE", "CHI.SP2.cs15.Rac1");
//        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
//        expected_Datapoints1.put(0L, "0");
//        expected_metric1.setDatapoints(expected_Datapoints1);
//        expected.add(expected_metric1);
//        
//        Metric expected_metric2 = new Metric("DIVIDE", "CHI.SP2.cs15.Rac2");
//        Map<Long, String> expected_Datapoints2=new HashMap<Long, String>();
//        expected_Datapoints2.put(0L, "5.0");
//        expected_metric2.setDatapoints(expected_Datapoints2);
//        expected.add(expected_metric2);
// 
//        assertEquals(expected,result); 
//        assertEquals(result.get(1).getDatapoints(),expected.get(1).getDatapoints()); 
//	}
//
//	@Test
//	public void HeimdallTotalAvaTransform_AVA(){
//		Transform transform=injector.getInstance(HeimdallMetricReducer.class);
//		int offset=1000*60;
//		long start=0L;
//        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
//        datapoints_1.put(start+1L*offset, "900.0");
//        datapoints_1.put(start+2L*offset, "1200.0");
//        datapoints_1.put(start+3L*offset, "1000.0");
//        datapoints_1.put(start+4L*offset, "1000.0");
//        datapoints_1.put(start+5L*offset, "400.0");
//        datapoints_1.put(start+6L*offset, "200.0");
//        metric_1.setDatapoints(datapoints_1);
//        metric_1.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_1.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_2 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_2 = new HashMap<Long, String>();
//        datapoints_2.put(start+1L*offset, "1400.0");
//        datapoints_2.put(start+2L*offset, "1350.0");
//        datapoints_2.put(start+3L*offset, "1350.0");
//        datapoints_2.put(start+4L*offset, "950.0");
//        datapoints_2.put(start+5L*offset, "950.0");
//        datapoints_2.put(start+6L*offset, "50.0");
//        metric_2.setDatapoints(datapoints_2);
//        metric_2.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
//        metric_2.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_3 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_3 = new HashMap<Long, String>();
//        datapoints_3.put(start+1L*offset, "2.0");
//        datapoints_3.put(start+2L*offset, "3.0");
//        datapoints_3.put(start+3L*offset, "3.0");
//        datapoints_3.put(start+4L*offset, "2.0");
//        datapoints_3.put(start+5L*offset, "4.0");
//        datapoints_3.put(start+6L*offset, "2.0");
//        metric_3.setDatapoints(datapoints_3);
//        metric_3.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_3.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_4 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_4 = new HashMap<Long, String>();
//        datapoints_4.put(start+1L*offset, "1.0");
//        datapoints_4.put(start+2L*offset, "9.0");
//        datapoints_4.put(start+3L*offset, "2.0");
//        datapoints_4.put(start+4L*offset, "2.0");
//        datapoints_4.put(start+5L*offset, "9.0");
//        datapoints_4.put(start+6L*offset, "1.0");
//        metric_4.setDatapoints(datapoints_4);
//        metric_4.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
//        metric_4.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
//        
//        
//        Metric metric_6 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_6 = new HashMap<Long, String>();
//        datapoints_6.put(start+1L*offset, "0.0");
//        datapoints_6.put(start+2L*offset, "0.0");
//        datapoints_6.put(start+3L*offset, "0.0");
//        datapoints_6.put(start+4L*offset, "0.0");
//        datapoints_6.put(start+5L*offset, "0.0");
//        datapoints_6.put(start+6L*offset, "0.0");
//        metric_6.setDatapoints(datapoints_6);
//        metric_6.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_6.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode1.Last_1_Min_Avg");
//     
//        
//        List<Metric> metrics = new ArrayList<Metric>();
//        metrics.add(metric_1);
//        metrics.add(metric_2);
//        metrics.add(metric_3);
//        metrics.add(metric_4);
//        metrics.add(metric_6);      
//        List<Metric> result = transform.transform(metrics,Arrays.asList("AVA"));
//
//        
//        List<Metric> expected = new ArrayList<Metric>();
//        Metric expected_metric1 = new Metric("DIVIDE", "CHI.SP2.cs15.Rac1");
//        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
//        expected_Datapoints1.put(0L, "100.0");
//        expected_metric1.setDatapoints(expected_Datapoints1);
//        expected.add(expected_metric1);
//        
//        Metric expected_metric2 = new Metric("DIVIDE", "CHI.SP2.cs15.Rac2");
//        Map<Long, String> expected_Datapoints2=new HashMap<Long, String>();
//        expected_Datapoints2.put(0L, "16.666668");
//        expected_metric2.setDatapoints(expected_Datapoints2);
//        expected.add(expected_metric2);
// 
//        System.out.println(result);
//        assertEquals(expected,result); 
//        assertEquals(result.get(1).getDatapoints(),expected.get(1).getDatapoints()); 
//	}
//
//	@Test
//	public void HeimdallTotalAvaTransform_IMPACTPOD(){
//		Transform transform=injector.getInstance(HeimdallMetricReducer.class);
//		int offset=1000*60;
//		long start=0L;
//        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
//        datapoints_1.put(start+1L*offset, "900.0");
//        datapoints_1.put(start+2L*offset, "1200.0");
//        datapoints_1.put(start+3L*offset, "1000.0");
//        datapoints_1.put(start+4L*offset, "1000.0");
//        datapoints_1.put(start+5L*offset, "400.0");
//        datapoints_1.put(start+6L*offset, "200.0");
//        metric_1.setDatapoints(datapoints_1);
//        metric_1.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_1.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_2 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_2 = new HashMap<Long, String>();
//        datapoints_2.put(start+1L*offset, "1400.0");
//        datapoints_2.put(start+2L*offset, "1350.0");
//        datapoints_2.put(start+3L*offset, "1350.0");
//        datapoints_2.put(start+4L*offset, "950.0");
//        datapoints_2.put(start+5L*offset, "950.0");
//        datapoints_2.put(start+6L*offset, "50.0");
//        metric_2.setDatapoints(datapoints_2);
//        metric_2.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
//        metric_2.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_3 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_3 = new HashMap<Long, String>();
//        datapoints_3.put(start+1L*offset, "2.0");
//        datapoints_3.put(start+2L*offset, "3.0");
//        datapoints_3.put(start+3L*offset, "3.0");
//        datapoints_3.put(start+4L*offset, "2.0");
//        datapoints_3.put(start+5L*offset, "4.0");
//        datapoints_3.put(start+6L*offset, "2.0");
//        metric_3.setDatapoints(datapoints_3);
//        metric_3.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_3.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_4 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_4 = new HashMap<Long, String>();
//        datapoints_4.put(start+1L*offset, "1.0");
//        datapoints_4.put(start+2L*offset, "9.0");
//        datapoints_4.put(start+3L*offset, "2.0");
//        datapoints_4.put(start+4L*offset, "2.0");
//        datapoints_4.put(start+5L*offset, "9.0");
//        datapoints_4.put(start+6L*offset, "1.0");
//        metric_4.setDatapoints(datapoints_4);
//        metric_4.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
//        metric_4.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
//        
//        
//        Metric metric_5 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_5 = new HashMap<Long, String>();
//        datapoints_5.put(start+1L*offset, "932.0");
//        datapoints_5.put(start+2L*offset, "995.0");
//        datapoints_5.put(start+3L*offset, "667.0");
//        datapoints_5.put(start+4L*offset, "567.0");
//        datapoints_5.put(start+5L*offset, "765.0");
//        datapoints_5.put(start+6L*offset, "485.0");
//        metric_5.setDatapoints(datapoints_5);
//        metric_5.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_5.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode1.Last_1_Min_Avg");
//     
//        
//        Metric metric_6 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_6 = new HashMap<Long, String>();
//        datapoints_6.put(start+1L*offset, "4.0");
//        datapoints_6.put(start+2L*offset, "5.0");
//        datapoints_6.put(start+3L*offset, "6.0");
//        datapoints_6.put(start+4L*offset, "7.0");
//        datapoints_6.put(start+5L*offset, "8.0");
//        datapoints_6.put(start+6L*offset, "4.0");
//        metric_6.setDatapoints(datapoints_6);
//        metric_6.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_6.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode1.Last_1_Min_Avg");
//     
//        
//        List<Metric> metrics = new ArrayList<Metric>();
//        metrics.add(metric_1);
//        metrics.add(metric_2);
//        metrics.add(metric_3);
//        metrics.add(metric_4);
//        metrics.add(metric_5);
//        metrics.add(metric_6);      
//        List<Metric> result = transform.transform(metrics,Arrays.asList("IMPACTPOD"));
//
//        
//        List<Metric> expected = new ArrayList<Metric>();
//        Metric expected_metric1 = new Metric("SUM", "CHI.SP2.cs15");
//        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
//        expected_Datapoints1.put(0L, "10.0");
//        expected_metric1.setDatapoints(expected_Datapoints1);
//        expected.add(expected_metric1);
//       
//        System.out.println(result);
//        
//        assertEquals(expected,result); 
//        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
//	}
//
//	@Test
//	public void HeimdallTotalAvaTransform_AVAPOD(){
//		Transform transform=injector.getInstance(HeimdallMetricReducer.class);
//		int offset=1000*60;
//		long start=0L;
//        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
//        datapoints_1.put(start+1L*offset, "900.0");
//        datapoints_1.put(start+2L*offset, "1200.0");
//        datapoints_1.put(start+3L*offset, "1000.0");
//        datapoints_1.put(start+4L*offset, "1000.0");
//        datapoints_1.put(start+5L*offset, "400.0");
//        datapoints_1.put(start+6L*offset, "200.0");
//        metric_1.setDatapoints(datapoints_1);
//        metric_1.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_1.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_2 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_2 = new HashMap<Long, String>();
//        datapoints_2.put(start+1L*offset, "1400.0");
//        datapoints_2.put(start+2L*offset, "1350.0");
//        datapoints_2.put(start+3L*offset, "1350.0");
//        datapoints_2.put(start+4L*offset, "950.0");
//        datapoints_2.put(start+5L*offset, "950.0");
//        datapoints_2.put(start+6L*offset, "50.0");
//        metric_2.setDatapoints(datapoints_2);
//        metric_2.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
//        metric_2.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_3 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_3 = new HashMap<Long, String>();
//        datapoints_3.put(start+1L*offset, "2.0");
//        datapoints_3.put(start+2L*offset, "3.0");
//        datapoints_3.put(start+3L*offset, "3.0");
//        datapoints_3.put(start+4L*offset, "2.0");
//        datapoints_3.put(start+5L*offset, "4.0");
//        datapoints_3.put(start+6L*offset, "2.0");
//        metric_3.setDatapoints(datapoints_3);
//        metric_3.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_3.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_4 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_4 = new HashMap<Long, String>();
//        datapoints_4.put(start+1L*offset, "1.0");
//        datapoints_4.put(start+2L*offset, "9.0");
//        datapoints_4.put(start+3L*offset, "2.0");
//        datapoints_4.put(start+4L*offset, "2.0");
//        datapoints_4.put(start+5L*offset, "9.0");
//        datapoints_4.put(start+6L*offset, "1.0");
//        metric_4.setDatapoints(datapoints_4);
//        metric_4.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
//        metric_4.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
//        
//        
//        Metric metric_5 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_5 = new HashMap<Long, String>();
//        datapoints_5.put(start+1L*offset, "932.0");
//        datapoints_5.put(start+2L*offset, "995.0");
//        datapoints_5.put(start+3L*offset, "667.0");
//        datapoints_5.put(start+4L*offset, "567.0");
//        datapoints_5.put(start+5L*offset, "765.0");
//        datapoints_5.put(start+6L*offset, "485.0");
//        metric_5.setDatapoints(datapoints_5);
//        metric_5.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_5.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode1.Last_1_Min_Avg");
//     
//        
//        Metric metric_6 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_6 = new HashMap<Long, String>();
//        datapoints_6.put(start+1L*offset, "4.0");
//        datapoints_6.put(start+2L*offset, "5.0");
//        datapoints_6.put(start+3L*offset, "6.0");
//        datapoints_6.put(start+4L*offset, "7.0");
//        datapoints_6.put(start+5L*offset, "8.0");
//        datapoints_6.put(start+6L*offset, "4.0");
//        metric_6.setDatapoints(datapoints_6);
//        metric_6.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_6.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode1.Last_1_Min_Avg");
//     
//        
//        List<Metric> metrics = new ArrayList<Metric>();
//        metrics.add(metric_1);
//        metrics.add(metric_2);
//        metrics.add(metric_3);
//        metrics.add(metric_4);
//        metrics.add(metric_5);
//        metrics.add(metric_6);      
//        List<Metric> result = transform.transform(metrics,Arrays.asList("AVAPOD"));
//
//        
//        List<Metric> expected = new ArrayList<Metric>();
//        Metric expected_metric1 = new Metric("DIVIDE", "CHI.SP2.cs15");
//        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
//        expected_Datapoints1.put(0L, "16.666668");
//        expected_metric1.setDatapoints(expected_Datapoints1);
//        expected.add(expected_metric1);
//       
//        System.out.println(result);
//        
//        assertEquals(expected,result); 
//        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
//	}
//	
//	@Test
//	public void HeimdallTotalAvaTransform_IMPACTTOTAL(){
//		Transform transform=injector.getInstance(HeimdallMetricReducer.class);
//		int offset=1000*60;
//		long start=0L;
//        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
//        datapoints_1.put(start+1L*offset, "900.0");
//        datapoints_1.put(start+2L*offset, "1200.0");
//        datapoints_1.put(start+3L*offset, "1000.0");
//        datapoints_1.put(start+4L*offset, "1000.0");
//        datapoints_1.put(start+5L*offset, "400.0");
//        datapoints_1.put(start+6L*offset, "200.0");
//        metric_1.setDatapoints(datapoints_1);
//        metric_1.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_1.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_2 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_2 = new HashMap<Long, String>();
//        datapoints_2.put(start+1L*offset, "1400.0");
//        datapoints_2.put(start+2L*offset, "1350.0");
//        datapoints_2.put(start+3L*offset, "1350.0");
//        datapoints_2.put(start+4L*offset, "950.0");
//        datapoints_2.put(start+5L*offset, "950.0");
//        datapoints_2.put(start+6L*offset, "50.0");
//        metric_2.setDatapoints(datapoints_2);
//        metric_2.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
//        metric_2.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_3 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_3 = new HashMap<Long, String>();
//        datapoints_3.put(start+1L*offset, "2.0");
//        datapoints_3.put(start+2L*offset, "3.0");
//        datapoints_3.put(start+3L*offset, "3.0");
//        datapoints_3.put(start+4L*offset, "2.0");
//        datapoints_3.put(start+5L*offset, "4.0");
//        datapoints_3.put(start+6L*offset, "2.0");
//        metric_3.setDatapoints(datapoints_3);
//        metric_3.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_3.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_4 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_4 = new HashMap<Long, String>();
//        datapoints_4.put(start+1L*offset, "1.0");
//        datapoints_4.put(start+2L*offset, "9.0");
//        datapoints_4.put(start+3L*offset, "2.0");
//        datapoints_4.put(start+4L*offset, "2.0");
//        datapoints_4.put(start+5L*offset, "9.0");
//        datapoints_4.put(start+6L*offset, "1.0");
//        metric_4.setDatapoints(datapoints_4);
//        metric_4.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
//        metric_4.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
//        
//        
//        Metric metric_5 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_5 = new HashMap<Long, String>();
//        datapoints_5.put(start+1L*offset, "932.0");
//        datapoints_5.put(start+2L*offset, "995.0");
//        datapoints_5.put(start+3L*offset, "667.0");
//        datapoints_5.put(start+4L*offset, "567.0");
//        datapoints_5.put(start+5L*offset, "765.0");
//        datapoints_5.put(start+6L*offset, "485.0");
//        metric_5.setDatapoints(datapoints_5);
//        metric_5.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_5.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode1.Last_1_Min_Avg");
//     
//        
//        Metric metric_6 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_6 = new HashMap<Long, String>();
//        datapoints_6.put(start+1L*offset, "4.0");
//        datapoints_6.put(start+2L*offset, "5.0");
//        datapoints_6.put(start+3L*offset, "6.0");
//        datapoints_6.put(start+4L*offset, "7.0");
//        datapoints_6.put(start+5L*offset, "8.0");
//        datapoints_6.put(start+6L*offset, "4.0");
//        metric_6.setDatapoints(datapoints_6);
//        metric_6.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_6.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode1.Last_1_Min_Avg");
//     
//        
//        List<Metric> metrics = new ArrayList<Metric>();
//        metrics.add(metric_1);
//        metrics.add(metric_2);
//        metrics.add(metric_3);
//        metrics.add(metric_4);
//        metrics.add(metric_5);
//        metrics.add(metric_6);      
//        List<Metric> result = transform.transform(metrics,Arrays.asList("IMPACTTOTAL"));
//
//        
//        List<Metric> expected = new ArrayList<Metric>();
//        Metric expected_metric1 = new Metric("DIVIDE", "CHI.SP2.cs15");
//        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
//        expected_Datapoints1.put(0L, "16.666668");
//        expected_metric1.setDatapoints(expected_Datapoints1);
//        expected.add(expected_metric1);
//       
//        System.out.println(result);
//        
//        assertEquals(expected,result); 
//        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
//	}
//
//	@Test
//	public void HeimdallTotalAvaTransform_APTPOD(){
//		Transform transform=injector.getInstance(HeimdallMetricReducer.class);
//		int offset=1000*60;
//		long start=0L;
//        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
//        datapoints_1.put(start+1L*offset, "900.0");
//        datapoints_1.put(start+2L*offset, "1200.0");
//        datapoints_1.put(start+3L*offset, "1000.0");
//        datapoints_1.put(start+4L*offset, "1000.0");
//        datapoints_1.put(start+5L*offset, "400.0");
//        datapoints_1.put(start+6L*offset, "200.0");
//        metric_1.setDatapoints(datapoints_1);
//        metric_1.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_1.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_2 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_2 = new HashMap<Long, String>();
//        datapoints_2.put(start+1L*offset, "1400.0");
//        datapoints_2.put(start+2L*offset, "1350.0");
//        datapoints_2.put(start+3L*offset, "1350.0");
//        datapoints_2.put(start+4L*offset, "950.0");
//        datapoints_2.put(start+5L*offset, "950.0");
//        datapoints_2.put(start+6L*offset, "50.0");
//        metric_2.setDatapoints(datapoints_2);
//        metric_2.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
//        metric_2.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_3 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_3 = new HashMap<Long, String>();
//        datapoints_3.put(start+1L*offset, "2.0");
//        datapoints_3.put(start+2L*offset, "3.0");
//        datapoints_3.put(start+3L*offset, "3.0");
//        datapoints_3.put(start+4L*offset, "2.0");
//        datapoints_3.put(start+5L*offset, "4.0");
//        datapoints_3.put(start+6L*offset, "2.0");
//        metric_3.setDatapoints(datapoints_3);
//        metric_3.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_3.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_4 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_4 = new HashMap<Long, String>();
//        datapoints_4.put(start+1L*offset, "1.0");
//        datapoints_4.put(start+2L*offset, "9.0");
//        datapoints_4.put(start+3L*offset, "2.0");
//        datapoints_4.put(start+4L*offset, "2.0");
//        datapoints_4.put(start+5L*offset, "9.0");
//        datapoints_4.put(start+6L*offset, "1.0");
//        metric_4.setDatapoints(datapoints_4);
//        metric_4.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
//        metric_4.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
//        
//        
//        Metric metric_5 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_5 = new HashMap<Long, String>();
//        datapoints_5.put(start+1L*offset, "932.0");
//        datapoints_5.put(start+2L*offset, "995.0");
//        datapoints_5.put(start+3L*offset, "667.0");
//        datapoints_5.put(start+4L*offset, "567.0");
//        datapoints_5.put(start+5L*offset, "765.0");
//        datapoints_5.put(start+6L*offset, "485.0");
//        metric_5.setDatapoints(datapoints_5);
//        metric_5.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_5.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode1.Last_1_Min_Avg");
//     
//        
//        Metric metric_6 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_6 = new HashMap<Long, String>();
//        datapoints_6.put(start+1L*offset, "4.0");
//        datapoints_6.put(start+2L*offset, "5.0");
//        datapoints_6.put(start+3L*offset, "6.0");
//        datapoints_6.put(start+4L*offset, "7.0");
//        datapoints_6.put(start+5L*offset, "8.0");
//        datapoints_6.put(start+6L*offset, "4.0");
//        metric_6.setDatapoints(datapoints_6);
//        metric_6.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_6.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode1.Last_1_Min_Avg");
//     
//        
//        List<Metric> metrics = new ArrayList<Metric>();
//        metrics.add(metric_1);
//        metrics.add(metric_2);
//        metrics.add(metric_3);
//        metrics.add(metric_4);
//        metrics.add(metric_5);
//        metrics.add(metric_6);      
//        List<Metric> result = transform.transform(metrics,Arrays.asList("APTPOD"));
//
//        
//        List<Metric> expected = new ArrayList<Metric>();
//        Metric expected_metric1 = new Metric("DIVIDE", "CHI.SP2.cs15");
//        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
//        expected_Datapoints1.put(60000L, "989.7142857142857");
//        expected_Datapoints1.put(120000L, "1219.1176470588234");
//        expected_Datapoints1.put(180000L, "882.0");
//        expected_Datapoints1.put(240000L, "715.3636363636364");
//        expected_Datapoints1.put(300000L, "774.7619047619048");
//        expected_Datapoints1.put(360000L, "341.42857142857144");
//        expected_metric1.setDatapoints(expected_Datapoints1);
//        expected.add(expected_metric1);
//       
//        System.out.println(result);
//        
//        assertEquals(expected,result); 
//        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
//	}
//	
	
//	@Test
//	public void HeimdallTotalAvaTransform_TTMPOD(){
//		Transform transform=injector.getInstance(HeimdallMetricReducer.class);
//		int offset=1000*60;
//		long start=0L;
//		long hourstart=1000*3600L;
//        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
//        datapoints_1.put(start+1L*offset, "900.0");
//        datapoints_1.put(start+2L*offset, "1200.0");
//        datapoints_1.put(start+3L*offset, "1000.0");
//        datapoints_1.put(start+4L*offset, "1000.0");
//        datapoints_1.put(start+5L*offset, "400.0");
//        datapoints_1.put(start+6L*offset, "200.0");
//        datapoints_1.put(hourstart+1L*offset, "900.0");
//        datapoints_1.put(hourstart+2L*offset, "1200.0");
//        datapoints_1.put(hourstart+3L*offset, "1000.0");
//        datapoints_1.put(hourstart+4L*offset, "1000.0");
//        datapoints_1.put(hourstart+5L*offset, "400.0");
//        datapoints_1.put(hourstart+6L*offset, "200.0");
//        metric_1.setDatapoints(datapoints_1);
//        metric_1.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_1.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_2 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_2 = new HashMap<Long, String>();
//        datapoints_2.put(start+1L*offset, "1400.0");
//        datapoints_2.put(start+2L*offset, "1350.0");
//        datapoints_2.put(start+3L*offset, "1350.0");
//        datapoints_2.put(start+4L*offset, "950.0");
//        datapoints_2.put(start+5L*offset, "950.0");
//        datapoints_2.put(start+6L*offset, "50.0");
//        datapoints_2.put(hourstart+1L*offset, "1400.0");
//        datapoints_2.put(hourstart+2L*offset, "1350.0");
//        datapoints_2.put(hourstart+3L*offset, "1350.0");
//        datapoints_2.put(hourstart+4L*offset, "950.0");
//        datapoints_2.put(hourstart+5L*offset, "950.0");
//        datapoints_2.put(hourstart+6L*offset, "50.0");
//        metric_2.setDatapoints(datapoints_2);
//        metric_2.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
//        metric_2.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_3 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_3 = new HashMap<Long, String>();
//        datapoints_3.put(start+1L*offset, "2.0");
//        datapoints_3.put(start+2L*offset, "3.0");
//        datapoints_3.put(start+3L*offset, "3.0");
//        datapoints_3.put(start+4L*offset, "2.0");
//        datapoints_3.put(start+5L*offset, "4.0");
//        datapoints_3.put(start+6L*offset, "2.0");
//        datapoints_3.put(hourstart+1L*offset, "2.0");
//        datapoints_3.put(hourstart+2L*offset, "3.0");
//        datapoints_3.put(hourstart+3L*offset, "3.0");
//        datapoints_3.put(hourstart+4L*offset, "2.0");
//        datapoints_3.put(hourstart+5L*offset, "4.0");
//        datapoints_3.put(hourstart+6L*offset, "2.0");
//        metric_3.setDatapoints(datapoints_3);
//        metric_3.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_3.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_4 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_4 = new HashMap<Long, String>();
//        datapoints_4.put(start+1L*offset, "1.0");
//        datapoints_4.put(start+2L*offset, "9.0");
//        datapoints_4.put(start+3L*offset, "2.0");
//        datapoints_4.put(start+4L*offset, "2.0");
//        datapoints_4.put(start+5L*offset, "9.0");
//        datapoints_4.put(start+6L*offset, "1.0");
//        datapoints_4.put(hourstart+1L*offset, "1.0");
//        datapoints_4.put(hourstart+2L*offset, "9.0");
//        datapoints_4.put(hourstart+3L*offset, "2.0");
//        datapoints_4.put(hourstart+4L*offset, "2.0");
//        datapoints_4.put(hourstart+5L*offset, "9.0");
//        datapoints_4.put(hourstart+6L*offset, "1.0");
//        metric_4.setDatapoints(datapoints_4);
//        metric_4.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
//        metric_4.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
//        
//        
//        Metric metric_5 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_5 = new HashMap<Long, String>();
//        datapoints_5.put(start+1L*offset, "200.0");
//        datapoints_5.put(start+2L*offset, "995.0");
//        datapoints_5.put(start+3L*offset, "667.0");
//        datapoints_5.put(start+4L*offset, "567.0");
//        datapoints_5.put(start+5L*offset, "765.0");
//        datapoints_5.put(start+6L*offset, "555.0");
//        metric_5.setDatapoints(datapoints_5);
//        metric_5.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_5.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode1.Last_1_Min_Avg");
//     
//        
//        Metric metric_6 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_6 = new HashMap<Long, String>();
//        datapoints_6.put(start+1L*offset, "4.0");
//        datapoints_6.put(start+2L*offset, "5.0");
//        datapoints_6.put(start+3L*offset, "6.0");
//        datapoints_6.put(start+4L*offset, "7.0");
//        datapoints_6.put(start+5L*offset, "8.0");
//        datapoints_6.put(start+6L*offset, "4.0");
//        metric_6.setDatapoints(datapoints_6);
//        metric_6.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_6.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode1.Last_1_Min_Avg");
//     
//        
//        List<Metric> metrics = new ArrayList<Metric>();
//        metrics.add(metric_1);
//        metrics.add(metric_2);
//        metrics.add(metric_3);
//        metrics.add(metric_4);
//        metrics.add(metric_5);
//        metrics.add(metric_6);      
//        List<Metric> result = transform.transform(metrics,Arrays.asList("TTMPOD"));
//
//        
//        List<Metric> expected = new ArrayList<Metric>();
//        Metric expected_metric1 = new Metric("SUM", "CHI.SP2.cs15");
//        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
//        expected_Datapoints1.put(0L, "6.0");
//        expected_Datapoints1.put(3600000L, "5.0");
//        expected_metric1.setDatapoints(expected_Datapoints1);
//        expected.add(expected_metric1);
//       
//        System.out.println(result);
//        
//        assertEquals(expected,result); 
//        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
//	}

	
//	@Test
//	public void HeimdallTotalAvaTransform_AVATOTAL(){
//		Transform transform=injector.getInstance(HeimdallMetricReducer.class);
//		int offset=1000*60;
//		long start=0L;
//		long hourstart=1000*3600L;
//        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
//        datapoints_1.put(start+1L*offset, "900.0");
//        datapoints_1.put(start+2L*offset, "1200.0");
//        datapoints_1.put(start+3L*offset, "1000.0");
//        datapoints_1.put(start+4L*offset, "1000.0");
//        datapoints_1.put(start+5L*offset, "400.0");
//        datapoints_1.put(start+6L*offset, "200.0");
//        datapoints_1.put(hourstart+1L*offset, "900.0");
//        datapoints_1.put(hourstart+2L*offset, "1200.0");
//        datapoints_1.put(hourstart+3L*offset, "1000.0");
//        datapoints_1.put(hourstart+4L*offset, "1000.0");
//        datapoints_1.put(hourstart+5L*offset, "400.0");
//        datapoints_1.put(hourstart+6L*offset, "200.0");
//        metric_1.setDatapoints(datapoints_1);
//        metric_1.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_1.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_2 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_2 = new HashMap<Long, String>();
//        datapoints_2.put(start+1L*offset, "1400.0");
//        datapoints_2.put(start+2L*offset, "1350.0");
//        datapoints_2.put(start+3L*offset, "1350.0");
//        datapoints_2.put(start+4L*offset, "950.0");
//        datapoints_2.put(start+5L*offset, "950.0");
//        datapoints_2.put(start+6L*offset, "50.0");
//        datapoints_2.put(hourstart+1L*offset, "1400.0");
//        datapoints_2.put(hourstart+2L*offset, "1350.0");
//        datapoints_2.put(hourstart+3L*offset, "1350.0");
//        datapoints_2.put(hourstart+4L*offset, "950.0");
//        datapoints_2.put(hourstart+5L*offset, "950.0");
//        datapoints_2.put(hourstart+6L*offset, "50.0");
//        metric_2.setDatapoints(datapoints_2);
//        metric_2.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
//        metric_2.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_3 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_3 = new HashMap<Long, String>();
//        datapoints_3.put(start+1L*offset, "2.0");
//        datapoints_3.put(start+2L*offset, "3.0");
//        datapoints_3.put(start+3L*offset, "3.0");
//        datapoints_3.put(start+4L*offset, "2.0");
//        datapoints_3.put(start+5L*offset, "4.0");
//        datapoints_3.put(start+6L*offset, "2.0");
//        datapoints_3.put(hourstart+1L*offset, "2.0");
//        datapoints_3.put(hourstart+2L*offset, "3.0");
//        datapoints_3.put(hourstart+3L*offset, "3.0");
//        datapoints_3.put(hourstart+4L*offset, "2.0");
//        datapoints_3.put(hourstart+5L*offset, "4.0");
//        datapoints_3.put(hourstart+6L*offset, "2.0");
//        metric_3.setDatapoints(datapoints_3);
//        metric_3.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_3.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_4 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_4 = new HashMap<Long, String>();
//        datapoints_4.put(start+1L*offset, "1.0");
//        datapoints_4.put(start+2L*offset, "9.0");
//        datapoints_4.put(start+3L*offset, "2.0");
//        datapoints_4.put(start+4L*offset, "2.0");
//        datapoints_4.put(start+5L*offset, "9.0");
//        datapoints_4.put(start+6L*offset, "1.0");
//        datapoints_4.put(hourstart+1L*offset, "1.0");
//        datapoints_4.put(hourstart+2L*offset, "9.0");
//        datapoints_4.put(hourstart+3L*offset, "2.0");
//        datapoints_4.put(hourstart+4L*offset, "2.0");
//        datapoints_4.put(hourstart+5L*offset, "9.0");
//        datapoints_4.put(hourstart+6L*offset, "1.0");
//        metric_4.setDatapoints(datapoints_4);
//        metric_4.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
//        metric_4.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
//        
//        
//        Metric metric_5 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_5 = new HashMap<Long, String>();
//        datapoints_5.put(start+1L*offset, "200.0");
//        datapoints_5.put(start+2L*offset, "995.0");
//        datapoints_5.put(start+3L*offset, "667.0");
//        datapoints_5.put(start+4L*offset, "567.0");
//        datapoints_5.put(start+5L*offset, "765.0");
//        datapoints_5.put(start+6L*offset, "555.0");
//        metric_5.setDatapoints(datapoints_5);
//        metric_5.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_5.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode1.Last_1_Min_Avg");
//     
//        
//        Metric metric_6 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_6 = new HashMap<Long, String>();
//        datapoints_6.put(start+1L*offset, "4.0");
//        datapoints_6.put(start+2L*offset, "5.0");
//        datapoints_6.put(start+3L*offset, "6.0");
//        datapoints_6.put(start+4L*offset, "7.0");
//        datapoints_6.put(start+5L*offset, "8.0");
//        datapoints_6.put(start+6L*offset, "4.0");
//        metric_6.setDatapoints(datapoints_6);
//        metric_6.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_6.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode1.Last_1_Min_Avg");
//     
//        
//        List<Metric> metrics = new ArrayList<Metric>();
//        metrics.add(metric_1);
//        metrics.add(metric_2);
//        metrics.add(metric_3);
//        metrics.add(metric_4);
//        metrics.add(metric_5);
//        metrics.add(metric_6);      
//        List<Metric> result = transform.transform(metrics,Arrays.asList("AVATOTAL"));
//
//        
//        List<Metric> expected = new ArrayList<Metric>();
//        Metric expected_metric1 = new Metric("DIVIDE", "CHI.SP2.cs15");
//        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
//        expected_Datapoints1.put(0L, "16.666668");
//        expected_metric1.setDatapoints(expected_Datapoints1);
//        expected.add(expected_metric1);
//
//        assertEquals(expected,result); 
//        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
//	}
	
	
//	@Test
//	public void HeimdallTotalAvaTransform_ACT(){
//		Transform transform=injector.getInstance(HeimdallMetricReducer.class);
//		int offset=1000*60;
//		long start=0L;
//		long hourstart=1000*3600L;
//        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
//        datapoints_1.put(start+1L*offset, "900.0");
//        datapoints_1.put(start+2L*offset, "1200.0");
//        datapoints_1.put(start+3L*offset, "1000.0");
//        datapoints_1.put(start+4L*offset, "1000.0");
//        datapoints_1.put(start+5L*offset, "400.0");
//        datapoints_1.put(start+6L*offset, "200.0");
//        datapoints_1.put(hourstart+1L*offset, "900.0");
//        datapoints_1.put(hourstart+2L*offset, "1200.0");
//        datapoints_1.put(hourstart+3L*offset, "1000.0");
//        datapoints_1.put(hourstart+4L*offset, "1000.0");
//        datapoints_1.put(hourstart+5L*offset, "400.0");
//        datapoints_1.put(hourstart+6L*offset, "200.0");
//        metric_1.setDatapoints(datapoints_1);
//        metric_1.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_1.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_2 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_2 = new HashMap<Long, String>();
//        datapoints_2.put(start+1L*offset, "1400.0");
//        datapoints_2.put(start+2L*offset, "1350.0");
//        datapoints_2.put(start+3L*offset, "1350.0");
//        datapoints_2.put(start+4L*offset, "950.0");
//        datapoints_2.put(start+5L*offset, "950.0");
//        datapoints_2.put(start+6L*offset, "50.0");
//        datapoints_2.put(hourstart+1L*offset, "1400.0");
//        datapoints_2.put(hourstart+2L*offset, "1350.0");
//        datapoints_2.put(hourstart+3L*offset, "1350.0");
//        datapoints_2.put(hourstart+4L*offset, "950.0");
//        datapoints_2.put(hourstart+5L*offset, "950.0");
//        datapoints_2.put(hourstart+6L*offset, "50.0");
//        metric_2.setDatapoints(datapoints_2);
//        metric_2.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
//        metric_2.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_3 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_3 = new HashMap<Long, String>();
//        datapoints_3.put(start+1L*offset, "2.0");
//        datapoints_3.put(start+2L*offset, "3.0");
//        datapoints_3.put(start+3L*offset, "3.0");
//        datapoints_3.put(start+4L*offset, "2.0");
//        datapoints_3.put(start+5L*offset, "4.0");
//        datapoints_3.put(start+6L*offset, "2.0");
//        datapoints_3.put(hourstart+1L*offset, "2.0");
//        datapoints_3.put(hourstart+2L*offset, "3.0");
//        datapoints_3.put(hourstart+3L*offset, "3.0");
//        datapoints_3.put(hourstart+4L*offset, "2.0");
//        datapoints_3.put(hourstart+5L*offset, "4.0");
//        datapoints_3.put(hourstart+6L*offset, "2.0");
//        metric_3.setDatapoints(datapoints_3);
//        metric_3.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_3.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_4 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_4 = new HashMap<Long, String>();
//        datapoints_4.put(start+1L*offset, "1.0");
//        datapoints_4.put(start+2L*offset, "9.0");
//        datapoints_4.put(start+3L*offset, "2.0");
//        datapoints_4.put(start+4L*offset, "2.0");
//        datapoints_4.put(start+5L*offset, "9.0");
//        datapoints_4.put(start+6L*offset, "1.0");
//        datapoints_4.put(hourstart+1L*offset, "1.0");
//        datapoints_4.put(hourstart+2L*offset, "9.0");
//        datapoints_4.put(hourstart+3L*offset, "2.0");
//        datapoints_4.put(hourstart+4L*offset, "2.0");
//        datapoints_4.put(hourstart+5L*offset, "9.0");
//        datapoints_4.put(hourstart+6L*offset, "1.0");
//        metric_4.setDatapoints(datapoints_4);
//        metric_4.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
//        metric_4.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
//        
//        
//        Metric metric_5 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_5 = new HashMap<Long, String>();
//        datapoints_5.put(start+1L*offset, "212.0");
//        datapoints_5.put(start+2L*offset, "995.0");
//        datapoints_5.put(start+3L*offset, "667.0");
//        datapoints_5.put(start+4L*offset, "567.0");
//        datapoints_5.put(start+5L*offset, "765.0");
//        datapoints_5.put(start+6L*offset, "555.0");
//        metric_5.setDatapoints(datapoints_5);
//        metric_5.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_5.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode1.Last_1_Min_Avg");
//     
//        
//        Metric metric_6 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_6 = new HashMap<Long, String>();
//        datapoints_6.put(start+1L*offset, "4.0");
//        datapoints_6.put(start+2L*offset, "5.0");
//        datapoints_6.put(start+3L*offset, "6.0");
//        datapoints_6.put(start+4L*offset, "7.0");
//        datapoints_6.put(start+5L*offset, "8.0");
//        datapoints_6.put(start+6L*offset, "4.0");
//        metric_6.setDatapoints(datapoints_6);
//        metric_6.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_6.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode1.Last_1_Min_Avg");
//        
//        List<Metric> metrics = new ArrayList<Metric>();
//        metrics.add(metric_1);
//        metrics.add(metric_2);
//        metrics.add(metric_3);
//        metrics.add(metric_4);
//        metrics.add(metric_5);
//        metrics.add(metric_6);    
//        
//        
//        Metric metric_act1 = new Metric(TEST_SCOPE_ACT, TEST_METRIC);
//        Map<Long, String> datapoints_act1 = new HashMap<Long, String>();
//        datapoints_act1.put(start+1L*offset, "40.0");
//        datapoints_act1.put(start+2L*offset, "103.0");
//        datapoints_act1.put(start+3L*offset, "159.0");
//        datapoints_act1.put(start+4L*offset, "23.0");
//        datapoints_act1.put(start+5L*offset, "49.0");
//        datapoints_act1.put(start+6L*offset, "23.0");
//        metric_act1.setDatapoints(datapoints_act1);
//        metric_act1.setTag("device", "na11-db1-1-chi.ops.sfdc.net");
//        metric_act1.setMetric("CNADB11.NADB11-1.active__sessions");
//     
//        metrics.add(metric_act1);    
//        
//        List<Metric> result = transform.transform(metrics,Arrays.asList("ACT"));
//
//        
//        List<Metric> expected = new ArrayList<Metric>();
//        Metric expected_metric1 = new Metric("CHI.SP2.cs15.Rac1", "CHI.SP2.cs15.Rac1");
//        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
//        expected_Datapoints1.put(60000L, "40.0");
//        expected_Datapoints1.put(120000L, "103.0");
//        expected_Datapoints1.put(180000L, "159.0");
//        expected_Datapoints1.put(240000L, "23.0");
//        expected_Datapoints1.put(300000L, "49.0");
//        expected_Datapoints1.put(360000L, "23.0");
//        expected_metric1.setDatapoints(expected_Datapoints1);
//        expected.add(expected_metric1);
//
//        System.out.println(result);
//        assertEquals(expected,result); 
//        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
//	}
//	
//	
//	@Test
//	public void HeimdallTotalAvaTransform_IMPACT_WITH_ACT(){
//		Transform transform=injector.getInstance(HeimdallMetricReducer.class);
//		int offset=1000*60;
//		long start=0L;
//		long hourstart=1000*3600L;
//        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
//        datapoints_1.put(start+1L*offset, "900.0");
//        datapoints_1.put(start+2L*offset, "1200.0");
//        datapoints_1.put(start+3L*offset, "1000.0");
//        datapoints_1.put(start+4L*offset, "1000.0");
//        datapoints_1.put(start+5L*offset, "400.0");
//        datapoints_1.put(start+6L*offset, "200.0");
//        datapoints_1.put(hourstart+1L*offset, "900.0");
//        datapoints_1.put(hourstart+2L*offset, "1200.0");
//        datapoints_1.put(hourstart+3L*offset, "1000.0");
//        datapoints_1.put(hourstart+4L*offset, "1000.0");
//        datapoints_1.put(hourstart+5L*offset, "400.0");
//        datapoints_1.put(hourstart+6L*offset, "200.0");
//        metric_1.setDatapoints(datapoints_1);
//        metric_1.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_1.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_2 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_2 = new HashMap<Long, String>();
//        datapoints_2.put(start+1L*offset, "1400.0");
//        datapoints_2.put(start+2L*offset, "1350.0");
//        datapoints_2.put(start+3L*offset, "1350.0");
//        datapoints_2.put(start+4L*offset, "950.0");
//        datapoints_2.put(start+5L*offset, "950.0");
//        datapoints_2.put(start+6L*offset, "50.0");
//        datapoints_2.put(hourstart+1L*offset, "1400.0");
//        datapoints_2.put(hourstart+2L*offset, "1350.0");
//        datapoints_2.put(hourstart+3L*offset, "1350.0");
//        datapoints_2.put(hourstart+4L*offset, "950.0");
//        datapoints_2.put(hourstart+5L*offset, "950.0");
//        datapoints_2.put(hourstart+6L*offset, "50.0");
//        metric_2.setDatapoints(datapoints_2);
//        metric_2.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
//        metric_2.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_3 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_3 = new HashMap<Long, String>();
//        datapoints_3.put(start+1L*offset, "2.0");
//        datapoints_3.put(start+2L*offset, "3.0");
//        datapoints_3.put(start+3L*offset, "3.0");
//        datapoints_3.put(start+4L*offset, "2.0");
//        datapoints_3.put(start+5L*offset, "4.0");
//        datapoints_3.put(start+6L*offset, "2.0");
//        datapoints_3.put(hourstart+1L*offset, "2.0");
//        datapoints_3.put(hourstart+2L*offset, "3.0");
//        datapoints_3.put(hourstart+3L*offset, "3.0");
//        datapoints_3.put(hourstart+4L*offset, "2.0");
//        datapoints_3.put(hourstart+5L*offset, "4.0");
//        datapoints_3.put(hourstart+6L*offset, "2.0");
//        metric_3.setDatapoints(datapoints_3);
//        metric_3.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_3.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
//        
//        Metric metric_4 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_4 = new HashMap<Long, String>();
//        datapoints_4.put(start+1L*offset, "1.0");
//        datapoints_4.put(start+2L*offset, "9.0");
//        datapoints_4.put(start+3L*offset, "2.0");
//        datapoints_4.put(start+4L*offset, "2.0");
//        datapoints_4.put(start+5L*offset, "9.0");
//        datapoints_4.put(start+6L*offset, "1.0");
//        datapoints_4.put(hourstart+1L*offset, "1.0");
//        datapoints_4.put(hourstart+2L*offset, "9.0");
//        datapoints_4.put(hourstart+3L*offset, "2.0");
//        datapoints_4.put(hourstart+4L*offset, "2.0");
//        datapoints_4.put(hourstart+5L*offset, "9.0");
//        datapoints_4.put(hourstart+6L*offset, "1.0");
//        metric_4.setDatapoints(datapoints_4);
//        metric_4.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
//        metric_4.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
//        
//        
//        Metric metric_5 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_5 = new HashMap<Long, String>();
//        datapoints_5.put(start+1L*offset, "212.0");
//        datapoints_5.put(start+2L*offset, "995.0");
//        datapoints_5.put(start+3L*offset, "667.0");
//        datapoints_5.put(start+4L*offset, "567.0");
//        datapoints_5.put(start+5L*offset, "765.0");
//        datapoints_5.put(start+6L*offset, "555.0");
//        metric_5.setDatapoints(datapoints_5);
//        metric_5.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_5.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode1.Last_1_Min_Avg");
//     
//        
//        Metric metric_6 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_6 = new HashMap<Long, String>();
//        datapoints_6.put(start+1L*offset, "4.0");
//        datapoints_6.put(start+2L*offset, "5.0");
//        datapoints_6.put(start+3L*offset, "6.0");
//        datapoints_6.put(start+4L*offset, "7.0");
//        datapoints_6.put(start+5L*offset, "8.0");
//        datapoints_6.put(start+6L*offset, "4.0");
//        metric_6.setDatapoints(datapoints_6);
//        metric_6.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_6.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode1.Last_1_Min_Avg");
//        
//        List<Metric> metrics = new ArrayList<Metric>();
//        metrics.add(metric_1);
//        metrics.add(metric_2);
//        metrics.add(metric_3);
//        metrics.add(metric_4);
//        metrics.add(metric_5);
//        metrics.add(metric_6);    
//        
//        
//        Metric metric_act1 = new Metric(TEST_SCOPE_ACT, TEST_METRIC);
//        Map<Long, String> datapoints_act1 = new HashMap<Long, String>();
//        datapoints_act1.put(start+1L*offset, "420.0");
//        datapoints_act1.put(start+2L*offset, "103.0");
//        datapoints_act1.put(start+3L*offset, "159.0");
//        datapoints_act1.put(start+4L*offset, "23.0");
//        datapoints_act1.put(start+5L*offset, "152.0");
//        datapoints_act1.put(start+6L*offset, "23.0");
//        metric_act1.setDatapoints(datapoints_act1);
//        metric_act1.setTag("device", "na11-db1-1-chi.ops.sfdc.net");
//        metric_act1.setMetric("CNADB11.NADB11-1.active__sessions");
//     
//        metrics.add(metric_act1);    
//        
//        List<Metric> result = transform.transform(metrics,Arrays.asList("IMPACT"));
//
//        
//        List<Metric> expected = new ArrayList<Metric>();
//        Metric expected_metric1 = new Metric("SUM", "CHI.SP2.cs15.Rac1");
//        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
//        expected_Datapoints1.put(0L, "6.0");
//        expected_Datapoints1.put(3600000L, "0");
//        expected_metric1.setDatapoints(expected_Datapoints1);
//        expected.add(expected_metric1);
//        
//        Metric expected_metric2 = new Metric("DIVIDE", "CHI.SP2.cs15.Rac2");
//        Map<Long, String> expected_Datapoints2=new HashMap<Long, String>();
//        expected_Datapoints2.put(0L, "5.0");
//        expected_Datapoints2.put(3600000L, "5.0");
//        expected_metric2.setDatapoints(expected_Datapoints2);
//        expected.add(expected_metric2);
//        
//
//        System.out.println(result);
//        assertEquals(expected,result); 
//        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
//	}
	
	
	@Test
	public void HeimdallTotalAvaTransform_TTMPOD_WITH_ACT(){
		Transform transform=injector.getInstance(HeimdallMetricReducer.class);
		int offset=1000*60;
		long start=0L;
		long hourstart=1000*3600L;
        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
        datapoints_1.put(start+1L*offset, "900.0");
        datapoints_1.put(start+2L*offset, "1200.0");
        datapoints_1.put(start+3L*offset, "1000.0");
        datapoints_1.put(start+4L*offset, "1000.0");
        datapoints_1.put(start+5L*offset, "400.0");
        datapoints_1.put(start+6L*offset, "200.0");
        datapoints_1.put(hourstart+1L*offset, "900.0");
        datapoints_1.put(hourstart+2L*offset, "1200.0");
        datapoints_1.put(hourstart+3L*offset, "1000.0");
        datapoints_1.put(hourstart+4L*offset, "1000.0");
        datapoints_1.put(hourstart+5L*offset, "400.0");
        datapoints_1.put(hourstart+6L*offset, "200.0");
        metric_1.setDatapoints(datapoints_1);
        metric_1.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
        metric_1.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
        
        Metric metric_2 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_2 = new HashMap<Long, String>();
        datapoints_2.put(start+1L*offset, "1400.0");
        datapoints_2.put(start+2L*offset, "1350.0");
        datapoints_2.put(start+3L*offset, "1350.0");
        datapoints_2.put(start+4L*offset, "950.0");
        datapoints_2.put(start+5L*offset, "950.0");
        datapoints_2.put(start+6L*offset, "50.0");
        datapoints_2.put(hourstart+1L*offset, "1400.0");
        datapoints_2.put(hourstart+2L*offset, "1350.0");
        datapoints_2.put(hourstart+3L*offset, "1350.0");
        datapoints_2.put(hourstart+4L*offset, "950.0");
        datapoints_2.put(hourstart+5L*offset, "950.0");
        datapoints_2.put(hourstart+6L*offset, "50.0");
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
        datapoints_3.put(hourstart+1L*offset, "2.0");
        datapoints_3.put(hourstart+2L*offset, "3.0");
        datapoints_3.put(hourstart+3L*offset, "3.0");
        datapoints_3.put(hourstart+4L*offset, "2.0");
        datapoints_3.put(hourstart+5L*offset, "4.0");
        datapoints_3.put(hourstart+6L*offset, "2.0");
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
        datapoints_4.put(hourstart+1L*offset, "1.0");
        datapoints_4.put(hourstart+2L*offset, "9.0");
        datapoints_4.put(hourstart+3L*offset, "2.0");
        datapoints_4.put(hourstart+4L*offset, "2.0");
        datapoints_4.put(hourstart+5L*offset, "9.0");
        datapoints_4.put(hourstart+6L*offset, "1.0");
        metric_4.setDatapoints(datapoints_4);
        metric_4.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
        metric_4.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
        
        
        Metric metric_5 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_5 = new HashMap<Long, String>();
        datapoints_5.put(start+1L*offset, "212.0");
        datapoints_5.put(start+2L*offset, "995.0");
        datapoints_5.put(start+3L*offset, "667.0");
        datapoints_5.put(start+4L*offset, "567.0");
        datapoints_5.put(start+5L*offset, "765.0");
        datapoints_5.put(start+6L*offset, "555.0");
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
        
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(metric_1);
        metrics.add(metric_2);
        metrics.add(metric_3);
        metrics.add(metric_4);
        metrics.add(metric_5);
        metrics.add(metric_6);    
        
        
        Metric metric_act1 = new Metric(TEST_SCOPE_ACT, TEST_METRIC);
        Map<Long, String> datapoints_act1 = new HashMap<Long, String>();
        datapoints_act1.put(start+1L*offset, "420.0");
        datapoints_act1.put(start+2L*offset, "103.0");
        datapoints_act1.put(start+3L*offset, "159.0");
        datapoints_act1.put(start+4L*offset, "23.0");
        datapoints_act1.put(start+5L*offset, "152.0");
        datapoints_act1.put(start+6L*offset, "23.0");
        metric_act1.setDatapoints(datapoints_act1);
        metric_act1.setTag("device", "na11-db1-1-chi.ops.sfdc.net");
        metric_act1.setMetric("CNADB11.NADB11-1.active__sessions");
     
        metrics.add(metric_act1);    
        
        List<Metric> result = transform.transform(metrics,Arrays.asList("TTMPOD"));

        
        List<Metric> expected = new ArrayList<Metric>();
        Metric expected_metric1 = new Metric("SUM", "CHI.SP2.cs15");
        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
        expected_Datapoints1.put(0L, "6.0");
        expected_Datapoints1.put(3600000L, "5.0");
        expected_metric1.setDatapoints(expected_Datapoints1);
        expected.add(expected_metric1);


        System.out.println(result);
        assertEquals(expected,result); 
        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
	}
}
