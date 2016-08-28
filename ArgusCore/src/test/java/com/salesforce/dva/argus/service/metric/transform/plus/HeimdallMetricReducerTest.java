package com.salesforce.dva.argus.service.metric.transform.plus;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
	public void HeimdallTotalAvaTransform_dev(){
		Transform transform=injector.getInstance(HeimdallMetricReducer.class);
		int offset=1000*60/100;
        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
        datapoints_1.put(100L*offset, "900.0");
        datapoints_1.put(200L*offset, "1200.0");
        datapoints_1.put(300L*offset, "1000.0");
        datapoints_1.put(400L*offset, "1000.0");
        datapoints_1.put(500L*offset, "400.0");
        datapoints_1.put(600L*offset, "200.0");
        metric_1.setDatapoints(datapoints_1);
        metric_1.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
        metric_1.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
        
        Metric metric_2 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_2 = new HashMap<Long, String>();
        datapoints_2.put(100L*offset, "1400.0");
        datapoints_2.put(200L*offset, "1350.0");
        datapoints_2.put(300L*offset, "1350.0");
        datapoints_2.put(400L*offset, "950.0");
        datapoints_2.put(500L*offset, "950.0");
        datapoints_2.put(600L*offset, "50.0");
        metric_2.setDatapoints(datapoints_2);
        metric_2.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
        metric_2.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
        
        Metric metric_3 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_3 = new HashMap<Long, String>();
        datapoints_3.put(100L*offset, "2.0");
        datapoints_3.put(200L*offset, "3.0");
        datapoints_3.put(300L*offset, "3.0");
        datapoints_3.put(400L*offset, "2.0");
        datapoints_3.put(500L*offset, "3.0");
        datapoints_3.put(600L*offset, "2.0");
        metric_3.setDatapoints(datapoints_3);
        metric_3.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
        metric_3.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
        
        Metric metric_4 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_4 = new HashMap<Long, String>();
        datapoints_4.put(100L*offset, "1.0");
        datapoints_4.put(200L*offset, "9.0");
        datapoints_4.put(300L*offset, "2.0");
        datapoints_4.put(400L*offset, "2.0");
        datapoints_4.put(500L*offset, "1.0");
        datapoints_4.put(600L*offset, "1.0");
        metric_4.setDatapoints(datapoints_4);
        metric_4.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
        metric_4.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
        
        
        Metric metric_5 = new Metric("SUM", "result");
        Map<Long, String> datapoints_5 = new HashMap<Long, String>();
        datapoints_5.put(100L*offset, "0");
        datapoints_5.put(200L*offset, "0");
        datapoints_5.put(300L*offset, "69");
        datapoints_5.put(400L*offset, "50");
        datapoints_5.put(500L*offset, "32");
        datapoints_5.put(600L*offset, "0");
        metric_5.setDatapoints(datapoints_5);
        metric_5.setTag("device", "na11-db1-2-chi.ops.sfdc.net");
        metric_5.setMetric("result");
        
        
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(metric_1);
        metrics.add(metric_2);
        metrics.add(metric_3);
        metrics.add(metric_4);
        //metrics.add(metric_5);
        
        List<Metric> result = transform.transform(metrics);
        System.out.println("\n\nINPUT>>>\n"+metrics);
        System.out.println("\n\nOUTPUT>>>\n"+result);
	}
}
