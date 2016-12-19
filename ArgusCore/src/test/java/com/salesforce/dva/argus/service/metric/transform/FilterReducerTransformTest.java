package com.salesforce.dva.argus.service.metric.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.util.Providers;
import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.service.TSDBService;
import com.salesforce.dva.argus.service.broker.DefaultJSONService;
import com.salesforce.dva.argus.system.SystemConfiguration;

public class FilterReducerTransformTest{
	private static final String TEST_SCOPE = "test-scope";
	private static final String TEST_METRIC = "test-metric";
	private Injector injector;
	private SystemConfiguration configuration;

	@Before
	public void setUp() throws Exception {
		configuration=new SystemConfiguration(new Properties());
		configuration.setProperty("service.property.json.endpoint", "https://arguspm.ops.sfdc.net:443/argus/api");
		injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				new FactoryModuleBuilder().build(TransformFactory.class);
				bind(TSDBService.class).to(DefaultJSONService.class);
				bind(SystemConfiguration.class).toInstance(configuration);
			}
		});
	}

	@After
	public void tearDown() throws Exception {
		injector=null;
	}

	@Test
	public void Dev(){
		Transform transform=injector.getInstance(FilterReducerTransform.class);

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
        metric_1.setTag("podId", "cs12");
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
        metric_2.setTag("podId", "na11");
        metric_2.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");

        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(metric_1);
        metrics.add(metric_2);

        List<String> constants=new ArrayList<String>();
        constants.add("podId");
        constants.add("NOT.*na.*");
        List<Metric> result = transform.transform(metrics,constants);
        System.out.println("\n\nINPUT>>>\n"+metrics);
        System.out.println("\n\nOUTPUT>>>\n"+result);
	}
}
