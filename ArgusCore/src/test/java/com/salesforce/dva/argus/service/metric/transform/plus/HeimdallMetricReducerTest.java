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

public class HeimdallMetricReducerTest{
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
	public void HeimdallTotalAvaTransform_APT(){
		Transform transform=injector.getInstance(HeimdallMetricReducer.class);
		int offset=1000*60;
		long start=0L;
        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
        datapoints_1.put(start+1L*offset, "900.0");
        datapoints_1.put(start+2L*offset, "1200.0");
        datapoints_1.put(start+3L*offset, "1000.0");
        datapoints_1.put(start+4L*offset, "1000.0");
        datapoints_1.put(start+5L*offset, "400.0");
        datapoints_1.put(start+6L*offset, "200.0");
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
        
        
        Metric metric_6 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_6 = new HashMap<Long, String>();
        datapoints_6.put(start+1L*offset, "0.0");
        datapoints_6.put(start+2L*offset, "0.0");
        datapoints_6.put(start+3L*offset, "0.0");
        datapoints_6.put(start+4L*offset, "0.0");
        datapoints_6.put(start+5L*offset, "0.0");
        datapoints_6.put(start+6L*offset, "0.0");
        metric_6.setDatapoints(datapoints_6);
        metric_6.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
        metric_6.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode1.Last_1_Min_Avg");
       
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(metric_1);
        metrics.add(metric_2);
        metrics.add(metric_3);
        metrics.add(metric_4);
        metrics.add(metric_6);
        
        List<Metric> result = transform.transform(metrics,Arrays.asList("APT"));

        List<Metric> expected = new ArrayList<Metric>();
        Metric expected_metric1 = new Metric("DIVIDE", "CHI.SP2.cs15.Rac1");
        expected.add(expected_metric1);
        
        Metric expected_metric2 = new Metric("DIVIDE", "CHI.SP2.cs15.Rac2");
        Map<Long, String> expected_Datapoints2=new HashMap<Long, String>();
        expected_Datapoints2.put(60000L, "1066.6666666666667");
        expected_Datapoints2.put(120000L, "1312.5");
        expected_Datapoints2.put(180000L, "1140.0");
        expected_Datapoints2.put(240000L, "975.0");
        expected_Datapoints2.put(300000L, "780.7692307692307");
        expected_Datapoints2.put(360000L, "150.0");
        expected_metric2.setDatapoints(expected_Datapoints2);
        expected.add(expected_metric2);
 
        assertEquals(expected,result); 
        assertEquals(result.get(1).getDatapoints(),expected.get(1).getDatapoints()); 
	}
	
	public void HeimdallTotalAvaTransform_TRAFFIC(){
		Transform transform=injector.getInstance(HeimdallMetricReducer.class);
		int offset=1000*60;
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
        datapoints_1.put(hourstart+1L*offset, "90.0");
        datapoints_1.put(hourstart+2L*offset, "100.0");
        datapoints_1.put(hourstart+3L*offset, "100.0");
        datapoints_1.put(hourstart+4L*offset, "100.0");
        datapoints_1.put(hourstart+5L*offset, "40.0");
        datapoints_1.put(hourstart+6L*offset, "20.0");
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
        datapoints_2.put(hourstart+1L*offset, "100.0");
        datapoints_2.put(hourstart+2L*offset, "150.0");
        datapoints_2.put(hourstart+3L*offset, "150.0");
        datapoints_2.put(hourstart+4L*offset, "90.0");
        datapoints_2.put(hourstart+5L*offset, "90.0");
        datapoints_2.put(hourstart+6L*offset, "5.0");
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
        
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(metric_1);
        metrics.add(metric_2);
        metrics.add(metric_3);
        metrics.add(metric_4);
        metrics.add(metric_5);
        metrics.add(metric_6);    
        
        
        Metric metric_act1 = new Metric(TEST_SCOPE_ACT, TEST_METRIC);
        Map<Long, String> datapoints_act1 = new HashMap<Long, String>();
        datapoints_act1.put(start+1L*offset, "20.0");
        datapoints_act1.put(start+2L*offset, "203.0");
        datapoints_act1.put(start+3L*offset, "19.0");
        datapoints_act1.put(start+4L*offset, "23.0");
        datapoints_act1.put(start+5L*offset, "12.0");
        datapoints_act1.put(start+6L*offset, "23.0");
        metric_act1.setDatapoints(datapoints_act1);
        metric_act1.setTag("device", "na11-db1-1-chi.ops.sfdc.net");
        metric_act1.setMetric("CNADB11.NADB11-1.active__sessions");
     
        metrics.add(metric_act1);    
        
        //CPUSYS
        Metric metric_cpuSys = new Metric(TEST_SCOPE_CPU, TEST_METRIC);
        Map<Long, String> datapoints_cpuSys = new HashMap<Long, String>();
        datapoints_cpuSys.put(start+1L*offset, "1.05");
        datapoints_cpuSys.put(start+2L*offset, "10.3");
        datapoints_cpuSys.put(start+3L*offset, "40.9");
        datapoints_cpuSys.put(start+4L*offset, "66.0");
        datapoints_cpuSys.put(start+5L*offset, "1.0");
        datapoints_cpuSys.put(start+6L*offset, "2.0");
        metric_cpuSys.setDatapoints(datapoints_cpuSys);
        metric_cpuSys.setTag("device", "na11-db1-1-chi.ops.sfdc.net");
        metric_cpuSys.setMetric("CpuPerc.cpu.system");
        metrics.add(metric_cpuSys);    
        
        //CPU-empty-doubledigit-no par APT
        Metric metric_cpuSys2 = new Metric(TEST_SCOPE_CPU, TEST_METRIC);
        metric_cpuSys2.setTag("device", "na11-db1-2-chi.ops.sfdc.net");
        Map<Long, String> datapoints_cpuSys2 = new HashMap<Long, String>();
        datapoints_cpuSys2.put(start+4L*offset, "800.0");
        metric_cpuSys2.setDatapoints(datapoints_cpuSys2);
        metric_cpuSys2.setMetric("CpuPerc.cpu.system");
        //metrics.add(metric_cpuSys2);    
        
        //CPUUSER
        Metric metric_cpuUser = new Metric(TEST_SCOPE_CPU, TEST_METRIC);
        Map<Long, String> datapoints_cpuUser = new HashMap<Long, String>();
        datapoints_cpuUser.put(start+1L*offset, "100.05");
        datapoints_cpuUser.put(start+2L*offset, "313.3");
        datapoints_cpuUser.put(start+3L*offset, "100");
        datapoints_cpuUser.put(start+4L*offset, "400.0");
        datapoints_cpuUser.put(start+5L*offset, "100.0");
        datapoints_cpuUser.put(start+6L*offset, "200.0");

        metric_cpuUser.setTag("device", "na11-db1-1-chi.ops.sfdc.net");
        metric_cpuUser.setMetric("CpuPerc.cpu.user");
        metrics.add(metric_cpuUser);    
        
        List<Metric> result = transform.transform(metrics,Arrays.asList("TRAFFIC"));

        
        List<Metric> expected = new ArrayList<Metric>();        
        Metric expected_metric2 = new Metric("SUM", "CHI.SP2.cs15.Rac1");
        Map<Long, String> expected_Datapoints2=new HashMap<Long, String>();
        expected_Datapoints2.put(60000L, "4.0");
        expected_Datapoints2.put(120000L, "5.0");
        expected_Datapoints2.put(180000L, "6.0");
        expected_Datapoints2.put(240000L, "7.0");
        expected_Datapoints2.put(300000L, "8.0");
        expected_Datapoints2.put(360000L, "4.0");
        expected_metric2.setDatapoints(expected_Datapoints2);
        expected.add(expected_metric2);

        System.out.println(result);
        assertEquals(expected.get(0),result.get(0)); 
        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
	}
	
	@Test
	public void HeimdallTotalAvaTransform_TRAFFICPOD(){
		Transform transform=injector.getInstance(HeimdallMetricReducer.class);
		int offset=1000*60;
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
        datapoints_1.put(hourstart+1L*offset, "90.0");
        datapoints_1.put(hourstart+2L*offset, "100.0");
        datapoints_1.put(hourstart+3L*offset, "100.0");
        datapoints_1.put(hourstart+4L*offset, "100.0");
        datapoints_1.put(hourstart+5L*offset, "40.0");
        datapoints_1.put(hourstart+6L*offset, "20.0");
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
        datapoints_2.put(hourstart+1L*offset, "100.0");
        datapoints_2.put(hourstart+2L*offset, "150.0");
        datapoints_2.put(hourstart+3L*offset, "150.0");
        datapoints_2.put(hourstart+4L*offset, "90.0");
        datapoints_2.put(hourstart+5L*offset, "90.0");
        datapoints_2.put(hourstart+6L*offset, "5.0");
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
        
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(metric_1);
        metrics.add(metric_2);
        metrics.add(metric_3);
        metrics.add(metric_4);
        metrics.add(metric_5);
        metrics.add(metric_6);    
        
        
        Metric metric_act1 = new Metric(TEST_SCOPE_ACT, TEST_METRIC);
        Map<Long, String> datapoints_act1 = new HashMap<Long, String>();
        datapoints_act1.put(start+1L*offset, "20.0");
        datapoints_act1.put(start+2L*offset, "203.0");
        datapoints_act1.put(start+3L*offset, "19.0");
        datapoints_act1.put(start+4L*offset, "23.0");
        datapoints_act1.put(start+5L*offset, "12.0");
        datapoints_act1.put(start+6L*offset, "23.0");
        metric_act1.setDatapoints(datapoints_act1);
        metric_act1.setTag("device", "na11-db1-1-chi.ops.sfdc.net");
        metric_act1.setMetric("CNADB11.NADB11-1.active__sessions");
     
        metrics.add(metric_act1);    
        
        //CPUSYS
        Metric metric_cpuSys = new Metric(TEST_SCOPE_CPU, TEST_METRIC);
        Map<Long, String> datapoints_cpuSys = new HashMap<Long, String>();
        datapoints_cpuSys.put(start+1L*offset, "1.05");
        datapoints_cpuSys.put(start+2L*offset, "10.3");
        datapoints_cpuSys.put(start+3L*offset, "40.9");
        datapoints_cpuSys.put(start+4L*offset, "66.0");
        datapoints_cpuSys.put(start+5L*offset, "1.0");
        datapoints_cpuSys.put(start+6L*offset, "2.0");
        metric_cpuSys.setDatapoints(datapoints_cpuSys);
        metric_cpuSys.setTag("device", "na11-db1-1-chi.ops.sfdc.net");
        metric_cpuSys.setMetric("CpuPerc.cpu.system");
        metrics.add(metric_cpuSys);    
        
        //CPU-empty-doubledigit-no par APT
        Metric metric_cpuSys2 = new Metric(TEST_SCOPE_CPU, TEST_METRIC);
        metric_cpuSys2.setTag("device", "na11-db1-2-chi.ops.sfdc.net");
        Map<Long, String> datapoints_cpuSys2 = new HashMap<Long, String>();
        datapoints_cpuSys2.put(start+4L*offset, "800.0");
        metric_cpuSys2.setDatapoints(datapoints_cpuSys2);
        metric_cpuSys2.setMetric("CpuPerc.cpu.system");
        //metrics.add(metric_cpuSys2);    
        
        //CPUUSER
        Metric metric_cpuUser = new Metric(TEST_SCOPE_CPU, TEST_METRIC);
        Map<Long, String> datapoints_cpuUser = new HashMap<Long, String>();
        datapoints_cpuUser.put(start+1L*offset, "100.05");
        datapoints_cpuUser.put(start+2L*offset, "313.3");
        datapoints_cpuUser.put(start+3L*offset, "100");
        datapoints_cpuUser.put(start+4L*offset, "400.0");
        datapoints_cpuUser.put(start+5L*offset, "100.0");
        datapoints_cpuUser.put(start+6L*offset, "200.0");

        metric_cpuUser.setTag("device", "na11-db1-1-chi.ops.sfdc.net");
        metric_cpuUser.setMetric("CpuPerc.cpu.user");
        metrics.add(metric_cpuUser);    
        
        List<Metric> result = transform.transform(metrics,Arrays.asList("TRAFFICPOD"));

        
        List<Metric> expected = new ArrayList<Metric>();        
        Metric expected_metric2 = new Metric("SUM", "CHI.SP2.cs15");
        Map<Long, String> expected_Datapoints2=new HashMap<Long, String>();
//        expected_Datapoints2.put(60000L, "7.0");
//        expected_Datapoints2.put(120000L, "17.0");
//        expected_Datapoints2.put(180000L, "11.0");
//        expected_Datapoints2.put(240000L, "11.0");
//        expected_Datapoints2.put(300000L, "21.0");
//        expected_Datapoints2.put(360000L, "7.0");
//        expected_Datapoints2.put(3660000L, "3.0");
//        expected_Datapoints2.put(3720000L, "12.0");
//        expected_Datapoints2.put(3780000L, "5.0");
//        expected_Datapoints2.put(3840000L, "4.0");
//        expected_Datapoints2.put(3900000L, "13.0");
//        expected_Datapoints2.put(3960000L, "3.0");
          expected_Datapoints2.put(0L, "74.0");
          expected_Datapoints2.put(3600000L, "40.0");
        expected_metric2.setDatapoints(expected_Datapoints2);
        expected.add(expected_metric2);

        System.out.println(result);
        assertEquals(expected.get(0),result.get(0)); 
        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
	}

	@Test
	public void HeimdallTotalAvaTransform_TRAFFIC_OFFBYONE(){
		Transform transform=injector.getInstance(HeimdallMetricReducer.class);
		int offset=1000*60;//1MIN
		long start=-offset;
		long hourstart=1000*3600L;
        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
        datapoints_1.put(start+1L*offset, "100.0");
        datapoints_1.put(start+2L*offset, "100.0");
        datapoints_1.put(start+3L*offset, "100.0");
        datapoints_1.put(start+4L*offset, "100.0");
        datapoints_1.put(start+5L*offset, "40.0");
        datapoints_1.put(start+6L*offset, "00.0");
//        datapoints_1.put(hourstart, "10.0");
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
//        datapoints_2.put(hourstart, "1.0");
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
        datapoints_3.put(hourstart, "1.0");
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
        datapoints_4.put(hourstart, "1.0");
        metric_4.setDatapoints(datapoints_4);
        metric_4.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
        metric_4.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
        
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(metric_1);
        metrics.add(metric_2);
        metrics.add(metric_3);
        metrics.add(metric_4);
        
        List<Metric> result = transform.transform(metrics,Arrays.asList("TOTAL"));
        List<Metric> expected = new ArrayList<Metric>();  
        Metric expected_metric1 = new Metric("DIVIDE", "ImpactedMin");
        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
        expected_Datapoints1.put(0L, "0.0");
        expected_metric1.setDatapoints(expected_Datapoints1);
        expected.add(expected_metric1);
        
        Metric expected_metric2 = new Metric("SUM", "AvailableMin");
        Map<Long, String> expected_Datapoints2=new HashMap<Long, String>();
        expected_Datapoints2.put(0L, "7.0");
        expected_metric2.setDatapoints(expected_Datapoints2);
        expected.add(expected_metric2);
        
        Metric expected_metric3 = new Metric("DIVIDE", "Availability");
        Map<Long, String> expected_Datapoints3=new HashMap<Long, String>();
        expected_Datapoints3.put(0L, "100.0");
        expected_metric3.setDatapoints(expected_Datapoints3);
        expected.add(expected_metric3);
        
        Metric expected_metric4 = new Metric("SUM", "TTM");
        Map<Long, String> expected_Datapoints4=new HashMap<Long, String>();
        expected_Datapoints4.put(0L, "0.0");
        expected_metric1.setDatapoints(expected_Datapoints4);
        expected.add(expected_metric4);
        

        result.forEach(r -> System.out.println(r.toString()));
        assertEquals(expected.get(0),result.get(0)); 
        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
	}
	
	@Test
	public void HeimdallTotalAvaTransform_IMPACT(){
		Transform transform=injector.getInstance(HeimdallMetricReducer.class);
		int offset=1000*60;
		long start=0L;
        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
        datapoints_1.put(start+1L*offset, "900.0");
        datapoints_1.put(start+2L*offset, "1200.0");
        datapoints_1.put(start+3L*offset, "1000.0");
        datapoints_1.put(start+4L*offset, "1000.0");
        datapoints_1.put(start+5L*offset, "400.0");
        datapoints_1.put(start+6L*offset, "200.0");
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
        
        
        Metric metric_6 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_6 = new HashMap<Long, String>();
        datapoints_6.put(start+1L*offset, "0.0");
        datapoints_6.put(start+2L*offset, "0.0");
        datapoints_6.put(start+3L*offset, "0.0");
        datapoints_6.put(start+4L*offset, "0.0");
        datapoints_6.put(start+5L*offset, "0.0");
        datapoints_6.put(start+6L*offset, "0.0");
        metric_6.setDatapoints(datapoints_6);
        metric_6.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
        metric_6.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode1.Last_1_Min_Avg");
        
        Metric metric_7 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_7 = new HashMap<Long, String>();
        datapoints_7.put(start+1L*offset, "1.0");
        datapoints_7.put(start+2L*offset, "9.0");
        datapoints_7.put(start+3L*offset, "2.0");
        datapoints_7.put(start+4L*offset, "2.0");
        datapoints_7.put(start+5L*offset, "9.0");
        datapoints_7.put(start+6L*offset, "1.0");
        metric_7.setDatapoints(datapoints_7);
        metric_7.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
        metric_7.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode1.Last_1_Min_Avg");
        
        
        
        Metric metric_5 = new Metric("SUM", "result");
        Map<Long, String> datapoints_5 = new HashMap<Long, String>();
        datapoints_5.put(start+1L*offset, "0");
        datapoints_5.put(start+2L*offset, "0");
        datapoints_5.put(start+3L*offset, "69");
        datapoints_5.put(start+4L*offset, "50");
        datapoints_5.put(start+5L*offset, "32");
        datapoints_5.put(start+6L*offset, "0");
        metric_5.setDatapoints(datapoints_5);
        metric_5.setTag("device", "na11-db1-2-chi.ops.sfdc.net");
        metric_5.setMetric("result");
        
        
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(metric_1);
        metrics.add(metric_2);
        metrics.add(metric_3);
        metrics.add(metric_4);
        //metrics.add(metric_5);
        metrics.add(metric_6);
        //metrics.add(metric_7);        
        List<Metric> result = transform.transform(metrics,Arrays.asList("IMPACT"));

        
        List<Metric> expected = new ArrayList<Metric>();
        Metric expected_metric1 = new Metric("DIVIDE", "CHI.SP2.cs15.Rac1");
        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
        expected_Datapoints1.put(0L, "0");
        expected_metric1.setDatapoints(expected_Datapoints1);
        expected.add(expected_metric1);
        
        Metric expected_metric2 = new Metric("DIVIDE", "CHI.SP2.cs15.Rac2");
        Map<Long, String> expected_Datapoints2=new HashMap<Long, String>();
        expected_Datapoints2.put(0L, "5.0");
        expected_metric2.setDatapoints(expected_Datapoints2);
        expected.add(expected_metric2);
 
        assertEquals(expected,result); 
        assertEquals(result.get(1).getDatapoints(),expected.get(1).getDatapoints()); 
	}

	@Test
	public void HeimdallTotalAvaTransform_AVA(){
		Transform transform=injector.getInstance(HeimdallMetricReducer.class);
		int offset=1000*60;
		long start=0L;
        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
        datapoints_1.put(start+1L*offset, "900.0");
        datapoints_1.put(start+2L*offset, "1200.0");
        datapoints_1.put(start+3L*offset, "1000.0");
        datapoints_1.put(start+4L*offset, "1000.0");
        datapoints_1.put(start+5L*offset, "400.0");
        datapoints_1.put(start+6L*offset, "200.0");
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
        
        
        Metric metric_6 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_6 = new HashMap<Long, String>();
        datapoints_6.put(start+1L*offset, "0.0");
        datapoints_6.put(start+2L*offset, "0.0");
        datapoints_6.put(start+3L*offset, "0.0");
        datapoints_6.put(start+4L*offset, "0.0");
        datapoints_6.put(start+5L*offset, "0.0");
        datapoints_6.put(start+6L*offset, "0.0");
        metric_6.setDatapoints(datapoints_6);
        metric_6.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
        metric_6.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode1.Last_1_Min_Avg");
     
        
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(metric_1);
        metrics.add(metric_2);
        metrics.add(metric_3);
        metrics.add(metric_4);
        metrics.add(metric_6);      
        List<Metric> result = transform.transform(metrics,Arrays.asList("AVA"));

        
        List<Metric> expected = new ArrayList<Metric>();
        Metric expected_metric1 = new Metric("DIVIDE", "CHI.SP2.cs15.Rac1");
        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
        expected_Datapoints1.put(0L, "100.0");
        expected_metric1.setDatapoints(expected_Datapoints1);
        expected.add(expected_metric1);
        
        Metric expected_metric2 = new Metric("DIVIDE", "CHI.SP2.cs15.Rac2");
        Map<Long, String> expected_Datapoints2=new HashMap<Long, String>();
        expected_Datapoints2.put(0L, "16.666668");
        expected_metric2.setDatapoints(expected_Datapoints2);
        expected.add(expected_metric2);
 
        System.out.println(result);
        assertEquals(expected,result); 
        assertEquals(result.get(1).getDatapoints(),expected.get(1).getDatapoints()); 
	}

	@Test
	public void HeimdallTotalAvaTransform_IMPACTPOD(){
		Transform transform=injector.getInstance(HeimdallMetricReducer.class);
		int offset=1000*60;
		long start=0L;
        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
        datapoints_1.put(start+1L*offset, "900.0");
        datapoints_1.put(start+2L*offset, "1200.0");
        datapoints_1.put(start+3L*offset, "1000.0");
        datapoints_1.put(start+4L*offset, "1000.0");
        datapoints_1.put(start+5L*offset, "400.0");
        datapoints_1.put(start+6L*offset, "200.0");
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
        
        
        Metric metric_5 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_5 = new HashMap<Long, String>();
        datapoints_5.put(start+1L*offset, "932.0");
        datapoints_5.put(start+2L*offset, "995.0");
        datapoints_5.put(start+3L*offset, "667.0");
        datapoints_5.put(start+4L*offset, "567.0");
        datapoints_5.put(start+5L*offset, "765.0");
        datapoints_5.put(start+6L*offset, "485.0");
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
        List<Metric> result = transform.transform(metrics,Arrays.asList("IMPACTPOD"));

        
        List<Metric> expected = new ArrayList<Metric>();
        Metric expected_metric1 = new Metric("SUM", "CHI.SP2.cs15");
        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
        expected_Datapoints1.put(0L, "10.0");
        expected_metric1.setDatapoints(expected_Datapoints1);
        expected.add(expected_metric1);
       
        System.out.println(result);
        
        assertEquals(expected,result); 
        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
	}

	@Test
	public void HeimdallTotalAvaTransform_AVAPOD(){
		Transform transform=injector.getInstance(HeimdallMetricReducer.class);
		int offset=1000*60;
		long start=0L;
        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
        datapoints_1.put(start+1L*offset, "900.0");
        datapoints_1.put(start+2L*offset, "1200.0");
        datapoints_1.put(start+3L*offset, "1000.0");
        datapoints_1.put(start+4L*offset, "1000.0");
        datapoints_1.put(start+5L*offset, "400.0");
        datapoints_1.put(start+6L*offset, "200.0");
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
        
        
        Metric metric_5 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_5 = new HashMap<Long, String>();
        datapoints_5.put(start+1L*offset, "932.0");
        datapoints_5.put(start+2L*offset, "995.0");
        datapoints_5.put(start+3L*offset, "667.0");
        datapoints_5.put(start+4L*offset, "567.0");
        datapoints_5.put(start+5L*offset, "765.0");
        datapoints_5.put(start+6L*offset, "485.0");
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
        List<Metric> result = transform.transform(metrics,Arrays.asList("AVAPOD"));

        
        List<Metric> expected = new ArrayList<Metric>();
        Metric expected_metric1 = new Metric("DIVIDE", "CHI.SP2.cs15");
        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
        expected_Datapoints1.put(0L, "16.666668");
        expected_metric1.setDatapoints(expected_Datapoints1);
        expected.add(expected_metric1);
       
        System.out.println(result);
        
        assertEquals(expected,result); 
        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
	}
	
	@Test
	public void HeimdallTotalAvaTransform_IMPACTTOTAL(){
		Transform transform=injector.getInstance(HeimdallMetricReducer.class);
		int offset=1000*60;
		long start=0L;
        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
        datapoints_1.put(start+1L*offset, "900.0");
        datapoints_1.put(start+2L*offset, "1200.0");
        datapoints_1.put(start+3L*offset, "1000.0");
        datapoints_1.put(start+4L*offset, "1000.0");
        datapoints_1.put(start+5L*offset, "400.0");
        datapoints_1.put(start+6L*offset, "200.0");
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
        
        
        Metric metric_5 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_5 = new HashMap<Long, String>();
        datapoints_5.put(start+1L*offset, "932.0");
        datapoints_5.put(start+2L*offset, "995.0");
        datapoints_5.put(start+3L*offset, "667.0");
        datapoints_5.put(start+4L*offset, "567.0");
        datapoints_5.put(start+5L*offset, "765.0");
        datapoints_5.put(start+6L*offset, "485.0");
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
        List<Metric> result = transform.transform(metrics,Arrays.asList("IMPACTTOTAL"));

        
        List<Metric> expected = new ArrayList<Metric>();
        Metric expected_metric1 = new Metric("SUM", "CHI.SP2.cs15");
        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
        expected_Datapoints1.put(0L, "10.0");
        expected_metric1.setDatapoints(expected_Datapoints1);
        expected.add(expected_metric1);
       
        System.out.println(result);
        
        assertEquals(expected,result); 
        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
	}

	@Test
	public void HeimdallTotalAvaTransform_APTPOD(){
		Transform transform=injector.getInstance(HeimdallMetricReducer.class);
		int offset=1000*60;
		long start=0L;
        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
        datapoints_1.put(start+1L*offset, "900.0");
        datapoints_1.put(start+2L*offset, "1200.0");
        datapoints_1.put(start+3L*offset, "1000.0");
        datapoints_1.put(start+4L*offset, "1000.0");
        datapoints_1.put(start+5L*offset, "400.0");
        datapoints_1.put(start+6L*offset, "200.0");
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
        
        
        Metric metric_5 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_5 = new HashMap<Long, String>();
        datapoints_5.put(start+1L*offset, "932.0");
        datapoints_5.put(start+2L*offset, "995.0");
        datapoints_5.put(start+3L*offset, "667.0");
        datapoints_5.put(start+4L*offset, "567.0");
        datapoints_5.put(start+5L*offset, "765.0");
        datapoints_5.put(start+6L*offset, "485.0");
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
        List<Metric> result = transform.transform(metrics,Arrays.asList("APTPOD"));

        
        List<Metric> expected = new ArrayList<Metric>();
        Metric expected_metric1 = new Metric("DIVIDE", "CHI.SP2.cs15");
        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
        expected_Datapoints1.put(60000L, "989.7142857142857");
        expected_Datapoints1.put(120000L, "1219.1176470588234");
        expected_Datapoints1.put(180000L, "882.0");
        expected_Datapoints1.put(240000L, "715.3636363636364");
        expected_Datapoints1.put(300000L, "774.7619047619048");
        expected_Datapoints1.put(360000L, "341.42857142857144");
        expected_metric1.setDatapoints(expected_Datapoints1);
        expected.add(expected_metric1);
       
        System.out.println(result);
        
        assertEquals(expected,result); 
        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
	}
	
	@Test
	public void HeimdallTotalAvaTransform_TTMPOD(){
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
        datapoints_5.put(start+1L*offset, "200.0");
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

	@Test
	public void HeimdallTotalAvaTransform_AVATOTAL(){
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
        datapoints_5.put(start+1L*offset, "200.0");
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
        List<Metric> result = transform.transform(metrics,Arrays.asList("AVATOTAL"));

        
        List<Metric> expected = new ArrayList<Metric>();
        Metric expected_metric1 = new Metric("DIVIDE", "CHI.SP2.cs15");
        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
        expected_Datapoints1.put(0L, "16.666668");
        expected_metric1.setDatapoints(expected_Datapoints1);
        expected.add(expected_metric1);

        assertEquals(expected,result); 
        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
	}

	@Test
	public void HeimdallTotalAvaTransform_ACT(){
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
        datapoints_act1.put(start+1L*offset, "40.0");
        datapoints_act1.put(start+2L*offset, "103.0");
        datapoints_act1.put(start+3L*offset, "159.0");
        datapoints_act1.put(start+4L*offset, "23.0");
        datapoints_act1.put(start+5L*offset, "49.0");
        datapoints_act1.put(start+6L*offset, "23.0");
        metric_act1.setDatapoints(datapoints_act1);
        metric_act1.setTag("device", "na11-db1-1-chi.ops.sfdc.net");
        metric_act1.setMetric("CNADB11.NADB11-1.active__sessions");
     
        metrics.add(metric_act1);    
        
        List<Metric> result = transform.transform(metrics,Arrays.asList("ACT"));

        
        List<Metric> expected = new ArrayList<Metric>();
        Metric expected_metric1 = new Metric("CHI.SP2.cs15.Rac1", "CHI.SP2.cs15.Rac1");
        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
        expected_Datapoints1.put(60000L, "40.0");
        expected_Datapoints1.put(120000L, "103.0");
        expected_Datapoints1.put(180000L, "159.0");
        expected_Datapoints1.put(240000L, "23.0");
        expected_Datapoints1.put(300000L, "49.0");
        expected_Datapoints1.put(360000L, "23.0");
        expected_metric1.setDatapoints(expected_Datapoints1);
        expected.add(expected_metric1);

        System.out.println(result);
        assertEquals(expected,result); 
        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
	}

	@Test
	public void HeimdallTotalAvaTransform_CPU(){
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
        
        //ACT
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
        
        //CPUSYS
        Metric metric_cpuSys = new Metric(TEST_SCOPE_CPU, TEST_METRIC);
        Map<Long, String> datapoints_cpuSys = new HashMap<Long, String>();
        datapoints_cpuSys.put(start+1L*offset, "1.05");
        datapoints_cpuSys.put(start+2L*offset, "10.3");
        datapoints_cpuSys.put(start+3L*offset, "40.9");
        datapoints_cpuSys.put(start+4L*offset, "66.0");
        datapoints_cpuSys.put(start+5L*offset, "1.0");
        datapoints_cpuSys.put(start+6L*offset, "2.0");
        metric_cpuSys.setDatapoints(datapoints_cpuSys);
        metric_cpuSys.setTag("device", "na11-db1-1-chi.ops.sfdc.net");
        metric_cpuSys.setMetric("CpuPerc.cpu.system");
        metrics.add(metric_cpuSys);    
        
        //CPU-empty-doubledigit-no par APT
        Metric metric_cpuSys2 = new Metric(TEST_SCOPE_CPU, TEST_METRIC);
        metric_cpuSys2.setTag("device", "na11-db1-12-chi.ops.sfdc.net");
        metric_cpuSys2.setMetric("CpuPerc.cpu.system");
        metrics.add(metric_cpuSys2);    
        
        //CPUUSER
        Metric metric_cpuUser = new Metric(TEST_SCOPE_CPU, TEST_METRIC);
        Map<Long, String> datapoints_cpuUser = new HashMap<Long, String>();
        datapoints_cpuUser.put(start+1L*offset, "1.05");
        datapoints_cpuUser.put(start+2L*offset, "33.3");
        datapoints_cpuUser.put(start+3L*offset, "10");
        datapoints_cpuUser.put(start+4L*offset, "4.0");
        datapoints_cpuUser.put(start+5L*offset, "1.0");
        datapoints_cpuUser.put(start+6L*offset, "2.0");
        metric_cpuUser.setDatapoints(datapoints_cpuUser);
        metric_cpuUser.setTag("device", "na11-db1-1-chi.ops.sfdc.net");
        metric_cpuUser.setMetric("CpuPerc.cpu.user");
        metrics.add(metric_cpuUser);    
        
        
        
        List<Metric> result = transform.transform(metrics,Arrays.asList("CPU"));

        
        List<Metric> expected = new ArrayList<Metric>();
        Metric expected_metric1 = new Metric("SUM", "CHI.SP2.cs15.Rac1");
        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
        expected_Datapoints1.put(60000L, "2.1");
        expected_Datapoints1.put(120000L, "43.599999999999994");
        expected_Datapoints1.put(180000L, "50.9");
        expected_Datapoints1.put(240000L, "70.0");
        expected_Datapoints1.put(300000L, "2.0");
        expected_Datapoints1.put(360000L, "4.0");
        expected_metric1.setDatapoints(expected_Datapoints1);
        expected.add(expected_metric1);


        System.out.println(result);
        assertEquals(expected,result); 
        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
	}

	@Test
	public void HeimdallTotalAvaTransform_IMPACT_WITH_ACT(){
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
        
        List<Metric> result = transform.transform(metrics,Arrays.asList("IMPACT"));

        
        List<Metric> expected = new ArrayList<Metric>();
        Metric expected_metric1 = new Metric("SUM", "CHI.SP2.cs15.Rac1");
        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
        expected_Datapoints1.put(0L, "6.0");
        expected_Datapoints1.put(3600000L, "0");
        expected_metric1.setDatapoints(expected_Datapoints1);
        expected.add(expected_metric1);
        
        Metric expected_metric2 = new Metric("DIVIDE", "CHI.SP2.cs15.Rac2");
        Map<Long, String> expected_Datapoints2=new HashMap<Long, String>();
        expected_Datapoints2.put(0L, "5.0");
        expected_Datapoints2.put(3600000L, "5.0");
        expected_metric2.setDatapoints(expected_Datapoints2);
        expected.add(expected_metric2);
        

        System.out.println(result);
        assertEquals(expected,result); 
        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
	}

	@Test
	public void HeimdallTotalAvaTransform_IMPACTBYAPT_WITH_ACT(){
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
        
        List<Metric> result = transform.transform(metrics,Arrays.asList("IMPACTBYAPT"));

        
        List<Metric> expected = new ArrayList<Metric>();
        Metric expected_metric1 = new Metric("DIVIDE", "CHI.SP2.cs15.Rac1");
        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
        expected_Datapoints1.put(0L, "5.0");
        expected_Datapoints1.put(3600000L, "0");
        expected_metric1.setDatapoints(expected_Datapoints1);
        expected.add(expected_metric1);
        
        Metric expected_metric2 = new Metric("DIVIDE", "CHI.SP2.cs15.Rac2");
        Map<Long, String> expected_Datapoints2=new HashMap<Long, String>();
        expected_Datapoints2.put(0L, "5.0");
        expected_Datapoints2.put(3600000L, "5.0");
        expected_metric2.setDatapoints(expected_Datapoints2);
        expected.add(expected_metric2);
        
        assertEquals(expected,result); 
        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
	}
	
	@Test
	public void HeimdallTotalAvaTransform_IMPACTBYACT_WITH_ACT(){
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
        
        List<Metric> result = transform.transform(metrics,Arrays.asList("IMPACTBYACT"));

        
        List<Metric> expected = new ArrayList<Metric>();
        Metric expected_metric1 = new Metric("CHI.SP2.cs15.Rac1", "CHI.SP2.cs15.Rac1");
        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
        expected_Datapoints1.put(0L, "3.0");
        expected_Datapoints1.put(3600000L, "0");
        expected_metric1.setDatapoints(expected_Datapoints1);
        expected.add(expected_metric1);
        
        assertEquals(expected,result); 
        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
	}
	
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

	@Test
	public void HeimdallTotalAvaTransform_TTMTOTAL_WITH_ACT(){
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
        
        List<Metric> result = transform.transform(metrics,Arrays.asList("TTMTOTAL"));

        
        List<Metric> expected = new ArrayList<Metric>();
        Metric expected_metric1 = new Metric("SUM", "CHI.SP2.cs15");
        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
        expected_Datapoints1.put(0L, "11.0");
        expected_metric1.setDatapoints(expected_Datapoints1);
        expected.add(expected_metric1);


        System.out.println(result);
        assertEquals(expected,result); 
        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
	}
	
	@Test
	public void HeimdallTotalAvaTransform_TTMPOD_WITH_ACT_CPU(){
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
        
        //ACT
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
        
        //CPUSYS
        Metric metric_cpuSys = new Metric(TEST_SCOPE_CPU, TEST_METRIC);
        Map<Long, String> datapoints_cpuSys = new HashMap<Long, String>();
        datapoints_cpuSys.put(start+1L*offset, "1.05");
        datapoints_cpuSys.put(start+2L*offset, "10.3");
        datapoints_cpuSys.put(start+3L*offset, "40.9");
        datapoints_cpuSys.put(start+4L*offset, "66.0");
        datapoints_cpuSys.put(start+5L*offset, "1.0");
        datapoints_cpuSys.put(start+6L*offset, "2.0");
        metric_cpuSys.setDatapoints(datapoints_cpuSys);
        metric_cpuSys.setTag("device", "na11-db1-1-chi.ops.sfdc.net");
        metric_cpuSys.setMetric("CpuPerc.cpu.system");
        metrics.add(metric_cpuSys);    
        
        //CPU-empty-doubledigit-no par APT
        Metric metric_cpuSys2 = new Metric(TEST_SCOPE_CPU, TEST_METRIC);
        metric_cpuSys2.setTag("device", "na11-db1-2-chi.ops.sfdc.net");
        Map<Long, String> datapoints_cpuSys2 = new HashMap<Long, String>();
        datapoints_cpuSys2.put(start+4L*offset, "800.0");
        metric_cpuSys2.setDatapoints(datapoints_cpuSys2);
        metric_cpuSys2.setMetric("CpuPerc.cpu.system");
        metrics.add(metric_cpuSys2);    
        
        //CPUUSER
        Metric metric_cpuUser = new Metric(TEST_SCOPE_CPU, TEST_METRIC);
        Map<Long, String> datapoints_cpuUser = new HashMap<Long, String>();
        datapoints_cpuUser.put(start+1L*offset, "1.05");
        datapoints_cpuUser.put(start+2L*offset, "33.3");
        datapoints_cpuUser.put(start+3L*offset, "10");
        datapoints_cpuUser.put(start+4L*offset, "4.0");
        datapoints_cpuUser.put(start+5L*offset, "1.0");
        datapoints_cpuUser.put(start+6L*offset, "2.0");
        metric_cpuUser.setDatapoints(datapoints_cpuUser);
        metric_cpuUser.setTag("device", "na11-db1-1-chi.ops.sfdc.net");
        metric_cpuUser.setMetric("CpuPerc.cpu.user");
        metrics.add(metric_cpuUser);    
        
        
        
        List<Metric> result = transform.transform(metrics,Arrays.asList("TTMPOD"));

        
        List<Metric> expected = new ArrayList<Metric>();
        Metric expected_metric1 = new Metric("SUM", "CHI.SP2.cs15");
        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
        expected_Datapoints1.put(0L, "6.0");
        expected_Datapoints1.put(3600000L, "5.0");
        expected_metric1.setDatapoints(expected_Datapoints1);
        expected.add(expected_metric1);


        System.out.println(result);
        assertEquals(expected.get(0),result.get(0)); 
        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
	}
	
	@Test
	public void HeimdallTotalAvaTransform_REPORT_RAC(){
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
        
        //ACT
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
        
        //CPUSYS
        Metric metric_cpuSys = new Metric(TEST_SCOPE_CPU, TEST_METRIC);
        Map<Long, String> datapoints_cpuSys = new HashMap<Long, String>();
        datapoints_cpuSys.put(start+1L*offset, "1.05");
        datapoints_cpuSys.put(start+2L*offset, "10.3");
        datapoints_cpuSys.put(start+3L*offset, "40.9");
        datapoints_cpuSys.put(start+4L*offset, "66.0");
        datapoints_cpuSys.put(start+5L*offset, "1.0");
        datapoints_cpuSys.put(start+6L*offset, "2.0");
        metric_cpuSys.setDatapoints(datapoints_cpuSys);
        metric_cpuSys.setTag("device", "na11-db1-1-chi.ops.sfdc.net");
        metric_cpuSys.setMetric("CpuPerc.cpu.system");
        metrics.add(metric_cpuSys);    
        
        //CPU-empty-doubledigit-no par APT
        Metric metric_cpuSys2 = new Metric(TEST_SCOPE_CPU, TEST_METRIC);
        metric_cpuSys2.setTag("device", "na11-db1-12-chi.ops.sfdc.net");
        metric_cpuSys2.setMetric("CpuPerc.cpu.system");
        metrics.add(metric_cpuSys2);    
        
        //CPUUSER
        Metric metric_cpuUser = new Metric(TEST_SCOPE_CPU, TEST_METRIC);
        Map<Long, String> datapoints_cpuUser = new HashMap<Long, String>();
        datapoints_cpuUser.put(start+1L*offset, "1.05");
        datapoints_cpuUser.put(start+2L*offset, "33.3");
        datapoints_cpuUser.put(start+3L*offset, "10");
        datapoints_cpuUser.put(start+4L*offset, "4.0");
        datapoints_cpuUser.put(start+5L*offset, "1.0");
        datapoints_cpuUser.put(start+6L*offset, "2.0");
        metric_cpuUser.setDatapoints(datapoints_cpuUser);
        metric_cpuUser.setTag("device", "na11-db1-1-chi.ops.sfdc.net");
        metric_cpuUser.setMetric("CpuPerc.cpu.user");
        metrics.add(metric_cpuUser);    
        
        
        
        List<Metric> result = transform.transform(metrics,Arrays.asList("RAC"));

        
        List<Metric> expected = new ArrayList<Metric>();
        Metric expected_metric1 = new Metric("APT", "CHI.SP2.cs15.Rac1");
        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
        expected_Datapoints1.put(60000L, "212.0");
        expected_Datapoints1.put(120000L, "995.0");
        expected_Datapoints1.put(180000L, "667.0");
        expected_Datapoints1.put(240000L, "567.0");
        expected_Datapoints1.put(300000L, "765.0");
        expected_Datapoints1.put(360000L, "555.0");
        expected_metric1.setDatapoints(expected_Datapoints1);
        expected.add(expected_metric1);


        System.out.println(result);
        assertEquals(expected.get(0),result.get(0)); 
        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
	}

	@Test
	public void HeimdallTotalAvaTransform_REPORT_POD(){
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
        
        List<Metric> result = transform.transform(metrics,Arrays.asList("POD"));

        
        List<Metric> expected = new ArrayList<Metric>();
        Metric expected_metric1 = new Metric("DIVIDE", "PodLevelAPT");
        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
        expected_Datapoints1.put(0L, "758.4929123164418");
        expected_Datapoints1.put(3600000L, "904.1559829059829");
        expected_metric1.setDatapoints(expected_Datapoints1);
        expected.add(expected_metric1);
        
        Metric expected_metric2 = new Metric("SUM", "ImpactedMin");
        Map<Long, String> expected_Datapoints2=new HashMap<Long, String>();
        expected_Datapoints2.put(0L, "11.0");
        expected_Datapoints2.put(3600000L, "5.0");
        expected_metric2.setDatapoints(expected_Datapoints2);
        expected.add(expected_metric2);

        Metric expected_metric3 = new Metric("DIVIDE", "Availability");
        Map<Long, String> expected_Datapoints3=new HashMap<Long, String>();
        expected_Datapoints3.put(0L, "8.333334");
        expected_Datapoints3.put(3600000L, "16.666668");
        expected_metric3.setDatapoints(expected_Datapoints3);
        expected.add(expected_metric3);
        
        Metric expected_metric4 = new Metric("SUM", "TTM");
        Map<Long, String> expected_Datapoints4=new HashMap<Long, String>();
        expected_Datapoints4.put(0L, "6.0");
        expected_Datapoints4.put(3600000L, "5.0");
        expected_metric4.setDatapoints(expected_Datapoints4);
        expected.add(expected_metric4);
        
        Metric expected_metric5 = new Metric("SUM", "CollectedMin");
        Map<Long, String> expected_Datapoints5=new HashMap<Long, String>();
        expected_Datapoints5.put(0L, "12.0");
        expected_Datapoints5.put(3600000L, "6.0");
        expected_metric5.setDatapoints(expected_Datapoints5);
        expected.add(expected_metric5);
        
        Metric expected_metric6 = new Metric("SUM", "Traffic");
        Map<Long, String> expected_Datapoints6=new HashMap<Long, String>();
        expected_Datapoints6.put(0L, "74.0");
        expected_Datapoints6.put(3600000L, "40.0");
        expected_metric6.setDatapoints(expected_Datapoints6);
        expected.add(expected_metric6);
        
        assertEquals(expected,result); 
        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
	}

	@Test
	public void HeimdallTotalAvaTransform_REPORT_TOTAL(){
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
        
        List<Metric> result = transform.transform(metrics,Arrays.asList("TOTAL"));
     
        List<Metric> expected = new ArrayList<Metric>();        
        Metric expected_metric2 = new Metric("SUM", "ImpactedMin");
        Map<Long, String> expected_Datapoints2=new HashMap<Long, String>();
        expected_Datapoints2.put(0L, "16.0");
        expected_metric2.setDatapoints(expected_Datapoints2);
        expected.add(expected_metric2);

        Metric expected_metric3 = new Metric("DIVIDE", "Availability");
        Map<Long, String> expected_Datapoints3=new HashMap<Long, String>();
        expected_Datapoints3.put(0L, "12.500001000000003");
        expected_metric3.setDatapoints(expected_Datapoints3);
        expected.add(expected_metric3);
        
        Metric expected_metric5 = new Metric("SUM", "AvailableMin");
        Map<Long, String> expected_Datapoints5=new HashMap<Long, String>();
        expected_Datapoints5.put(0L, "18");
        expected_metric5.setDatapoints(expected_Datapoints5);
        expected.add(expected_metric5);
        
        Metric expected_metric4 = new Metric("SUM", "TTM");
        Map<Long, String> expected_Datapoints4=new HashMap<Long, String>();
        expected_Datapoints4.put(0L, "11.0");
        expected_metric4.setDatapoints(expected_Datapoints4);
        expected.add(expected_metric4);

        System.out.println(result);
        assertEquals(expected,result); 
        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
	}
	
	@Test
	public void HeimdallTotalAvaTransform_REPORT_RACHOUR(){
		Transform transform=injector.getInstance(HeimdallMetricReducer.class);
		int offset=1000*60;
		long start=0L;
		long hourstart=1000*3600L;
        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
        datapoints_1.put(start+1L*offset, "00.0");
        datapoints_1.put(start+2L*offset, "100.0");
        datapoints_1.put(start+3L*offset, "100.0");
        datapoints_1.put(start+4L*offset, "100.0");
        datapoints_1.put(start+5L*offset, "40.0");
        datapoints_1.put(start+6L*offset, "20.0");
        datapoints_1.put(hourstart+1L*offset, "90.0");
        datapoints_1.put(hourstart+2L*offset, "120.0");
        datapoints_1.put(hourstart+3L*offset, "100.0");
        datapoints_1.put(hourstart+4L*offset, "10.0");
        datapoints_1.put(hourstart+5L*offset, "40.0");
        datapoints_1.put(hourstart+6L*offset, "20.0");
        metric_1.setDatapoints(datapoints_1);
        metric_1.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
        metric_1.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
        
        Metric metric_2 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_2 = new HashMap<Long, String>();
        datapoints_2.put(start+1L*offset, "100.0");
        datapoints_2.put(start+2L*offset, "150.0");
        datapoints_2.put(start+3L*offset, "130.0");
        datapoints_2.put(start+4L*offset, "95.0");
        datapoints_2.put(start+5L*offset, "90.0");
        datapoints_2.put(start+6L*offset, "5.0");
        datapoints_2.put(hourstart+1L*offset, "100.0");
        datapoints_2.put(hourstart+2L*offset, "10.0");
        datapoints_2.put(hourstart+3L*offset, "50.0");
        datapoints_2.put(hourstart+4L*offset, "90.0");
        datapoints_2.put(hourstart+5L*offset, "90.0");
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
        
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(metric_1);
        metrics.add(metric_2);
        metrics.add(metric_3);
        metrics.add(metric_4);
        metrics.add(metric_5);
        metrics.add(metric_6);    
        
        //ACT
        Metric metric_act1 = new Metric(TEST_SCOPE_ACT, TEST_METRIC);
        Map<Long, String> datapoints_act1 = new HashMap<Long, String>();
        datapoints_act1.put(start+1L*offset, "42.0");
        datapoints_act1.put(start+2L*offset, "1.0");
        datapoints_act1.put(start+3L*offset, "19.0");
        datapoints_act1.put(start+4L*offset, "2.0");
        datapoints_act1.put(start+5L*offset, "12.0");
        datapoints_act1.put(start+6L*offset, "23.0");
        metric_act1.setDatapoints(datapoints_act1);
        metric_act1.setTag("device", "na11-db1-1-chi.ops.sfdc.net");
        metric_act1.setMetric("CNADB11.NADB11-1.active__sessions");
     
        metrics.add(metric_act1);    
        
        //CPUSYS
        Metric metric_cpuSys = new Metric(TEST_SCOPE_CPU, TEST_METRIC);
        Map<Long, String> datapoints_cpuSys = new HashMap<Long, String>();
        datapoints_cpuSys.put(start+1L*offset, "1.05");
        datapoints_cpuSys.put(start+2L*offset, "10.3");
        datapoints_cpuSys.put(start+3L*offset, "40.9");
        datapoints_cpuSys.put(start+4L*offset, "66.0");
        datapoints_cpuSys.put(start+5L*offset, "1.0");
        datapoints_cpuSys.put(start+6L*offset, "2.0");
        metric_cpuSys.setDatapoints(datapoints_cpuSys);
        metric_cpuSys.setTag("device", "na11-db1-1-chi.ops.sfdc.net");
        metric_cpuSys.setMetric("CpuPerc.cpu.system");
        metrics.add(metric_cpuSys);    
        
        //CPU-empty-doubledigit-no par APT
        Metric metric_cpuSys2 = new Metric(TEST_SCOPE_CPU, TEST_METRIC);
        metric_cpuSys2.setTag("device", "na11-db1-12-chi.ops.sfdc.net");
        metric_cpuSys2.setMetric("CpuPerc.cpu.system");
        metrics.add(metric_cpuSys2);    
        
        //CPUUSER
        Metric metric_cpuUser = new Metric(TEST_SCOPE_CPU, TEST_METRIC);
        Map<Long, String> datapoints_cpuUser = new HashMap<Long, String>();
        datapoints_cpuUser.put(start+1L*offset, "1.05");
        datapoints_cpuUser.put(start+2L*offset, "33.3");
        datapoints_cpuUser.put(start+3L*offset, "10");
        datapoints_cpuUser.put(start+4L*offset, "4.0");
        datapoints_cpuUser.put(start+5L*offset, "1.0");
        datapoints_cpuUser.put(start+6L*offset, "2.0");
        metric_cpuUser.setDatapoints(datapoints_cpuUser);
        metric_cpuUser.setTag("device", "na11-db1-1-chi.ops.sfdc.net");
        metric_cpuUser.setMetric("CpuPerc.cpu.user");
        metrics.add(metric_cpuUser);    
        
        
        
        List<Metric> result = transform.transform(metrics,Arrays.asList("RACHOUR"));

        
        List<Metric> expected = new ArrayList<Metric>();
        Metric expected_metric1 = new Metric("APT", "CHI.SP2.cs15.Rac1");
        Map<Long, String> expected_Datapoints1=new HashMap<Long, String>();
        expected_Datapoints1.put(0L, "64.23529411764706");
        expected_Datapoints1.put(3600000L, "0");
        expected_metric1.setDatapoints(expected_Datapoints1);
        expected.add(expected_metric1);

        System.out.println(result);
        assertEquals(expected.get(0),result.get(0)); 
        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
	}
	
	@Test
	public void HeimdallTotalAvaTransform_APT_WITH_EMPTY_ACT(){
		Transform transform=injector.getInstance(HeimdallMetricReducer.class);
		int offset=1000*60;
		long start=0L;
        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
        datapoints_1.put(start+1L*offset, "900.0");
        datapoints_1.put(start+2L*offset, "1200.0");
        datapoints_1.put(start+3L*offset, "1000.0");
        datapoints_1.put(start+4L*offset, "1000.0");
        datapoints_1.put(start+5L*offset, "0");
        datapoints_1.put(start+6L*offset, "200.0");
        metric_1.setDatapoints(datapoints_1);
        metric_1.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
        metric_1.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
        
        Metric metric_2 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_2 = new HashMap<Long, String>();
        datapoints_2.put(start+1L*offset, "1400.0");
        datapoints_2.put(start+2L*offset, "1350.0");
        datapoints_2.put(start+3L*offset, "1350.0");
        datapoints_2.put(start+4L*offset, "950.0");
        datapoints_2.put(start+5L*offset, "0");
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
        datapoints_3.put(start+5L*offset, "0.0");
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
        datapoints_4.put(start+5L*offset, "0.0");
        datapoints_4.put(start+6L*offset, "1.0");
        metric_4.setDatapoints(datapoints_4);
        metric_4.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
        metric_4.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
        
        
        Metric metric_6 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_6 = new HashMap<Long, String>();
        datapoints_6.put(start+1L*offset, "0.0");
        datapoints_6.put(start+2L*offset, "0.0");
        datapoints_6.put(start+3L*offset, "0.0");
        datapoints_6.put(start+4L*offset, "0.0");
        datapoints_6.put(start+5L*offset, "0.0");
        datapoints_6.put(start+6L*offset, "0.0");
        metric_6.setDatapoints(datapoints_6);
        metric_6.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
        metric_6.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode1.Last_1_Min_Avg");
       
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(metric_1);
        metrics.add(metric_2);
        metrics.add(metric_3);
        metrics.add(metric_4);
        metrics.add(metric_6);
        
        
        
        
        
        Metric metric_act1 = new Metric(TEST_SCOPE_ACT, TEST_METRIC);
        Map<Long, String> datapoints_act1 = new HashMap<Long, String>();
        datapoints_act1.put(start+1L*offset, "420.0");
        datapoints_act1.put(start+2L*offset, "103.0");
        datapoints_act1.put(start+3L*offset, "159.0");
        datapoints_act1.put(start+4L*offset, "23.0");
        datapoints_act1.put(start+5L*offset, "50");
        datapoints_act1.put(start+6L*offset, "23.0");
        metric_act1.setDatapoints(datapoints_act1);
        metric_act1.setTag("device", "na11-db1-1-chi.ops.sfdc.net");
        metric_act1.setMetric("CNADB11.NADB11-1.active__sessions");
   
        metrics.add(metric_act1);   
        
        
        List<Metric> result = transform.transform(metrics,Arrays.asList("APT"));

        List<Metric> expected = new ArrayList<Metric>();
        Metric expected_metric1 = new Metric("DIVIDE", "CHI.SP2.cs15.Rac1");
        expected.add(expected_metric1);
        
        Metric expected_metric2 = new Metric("DIVIDE", "CHI.SP2.cs15.Rac2");
        Map<Long, String> expected_Datapoints2=new HashMap<Long, String>();
        expected_Datapoints2.put(60000L, "1066.6666666666667");
        expected_Datapoints2.put(120000L, "1312.5");
        expected_Datapoints2.put(180000L, "1140.0");
        expected_Datapoints2.put(240000L, "975.0");
        expected_Datapoints2.put(360000L, "150.0");
        expected_metric2.setDatapoints(expected_Datapoints2);
        expected.add(expected_metric2);
        
        System.out.println(result);
        assertEquals(expected,result); 
        assertEquals(result.get(1).getDatapoints(),expected.get(1).getDatapoints()); 
	}
	
	@Test
	public void HeimdallTotalAvaTransform_ZEROCASE(){
		Transform transform=injector.getInstance(HeimdallMetricReducer.class);
		int offset=1000*60;
		long start=0L;
		long hourstart=1000*3600L;
        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
        datapoints_1.put(start+1L*offset, "0");
        datapoints_1.put(start+2L*offset, "0");
        datapoints_1.put(start+3L*offset, "0");
        datapoints_1.put(start+4L*offset, "0");
        datapoints_1.put(start+5L*offset, "0");
        datapoints_1.put(start+6L*offset, "0");
        metric_1.setDatapoints(datapoints_1);
        metric_1.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
        metric_1.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode10.Last_1_Min_Avg");
        
        Metric metric_2 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_2 = new HashMap<Long, String>();
        datapoints_2.put(start+1L*offset, "0");
        datapoints_2.put(start+2L*offset, "0");
        datapoints_2.put(start+3L*offset, "0");
        datapoints_2.put(start+4L*offset, "0");
        datapoints_2.put(start+5L*offset, "0");
        datapoints_2.put(start+6L*offset, "0");
        metric_2.setDatapoints(datapoints_2);
        metric_2.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
        metric_2.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode10.Last_1_Min_Avg");
        
        Metric metric_3 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_3 = new HashMap<Long, String>();
        datapoints_3.put(start+1L*offset, "0");
        datapoints_3.put(start+2L*offset, "0");
        datapoints_3.put(start+3L*offset, "0");
        datapoints_3.put(start+4L*offset, "0");
        datapoints_3.put(start+5L*offset, "0");
        datapoints_3.put(start+6L*offset, "0");
        metric_3.setDatapoints(datapoints_3);
        metric_3.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
        metric_3.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode10.Last_1_Min_Avg");
        
        Metric metric_4 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_4 = new HashMap<Long, String>();
        datapoints_4.put(start+1L*offset, "0");
        datapoints_4.put(start+2L*offset, "0");
        datapoints_4.put(start+3L*offset, "0");
        datapoints_4.put(start+4L*offset, "0");
        datapoints_4.put(start+5L*offset, "0");
        datapoints_4.put(start+6L*offset, "0");
        metric_4.setDatapoints(datapoints_4);
        metric_4.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
        metric_4.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode10.Last_1_Min_Avg");
        

        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(metric_1);
        metrics.add(metric_2);
        metrics.add(metric_3);
        metrics.add(metric_4);
    
        
        Metric metric_act1 = new Metric(TEST_SCOPE_ACT, TEST_METRIC);
        Map<Long, String> datapoints_act1 = new HashMap<Long, String>();
        datapoints_act1.put(start+1L*offset, "420.0");
        datapoints_act1.put(start+2L*offset, "103.0");
        datapoints_act1.put(start+3L*offset, "159.0");
        datapoints_act1.put(start+4L*offset, "23.0");
        datapoints_act1.put(start+5L*offset, "152.0");
        datapoints_act1.put(start+6L*offset, "23.0");
        metric_act1.setDatapoints(datapoints_act1);
        metric_act1.setTag("device", "na11-db1-10-chi.ops.sfdc.net");
        metric_act1.setMetric("CNADB11.NADB11-10.active__sessions");
     
        metrics.add(metric_act1);    
        
        List<Metric> result = transform.transform(metrics,Arrays.asList("APT"));

        List<Metric> expected = new ArrayList<Metric>();        
        Metric expected_metric2 = new Metric("DIVIDE", "CHI.SP2.cs15.Rac10");
        Map<Long, String> expected_Datapoints2=new HashMap<Long, String>();
        expected_metric2.setDatapoints(expected_Datapoints2);
        expected.add(expected_metric2);

        System.out.println(result);
        assertEquals(expected,result); 
        assertEquals(result.get(0).getDatapoints(),expected.get(0).getDatapoints()); 
	}
}
