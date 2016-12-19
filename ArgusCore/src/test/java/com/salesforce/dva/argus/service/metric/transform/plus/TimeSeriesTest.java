package com.salesforce.dva.argus.service.metric.transform.plus;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

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
import com.salesforce.dva.argus.service.metric.transform.Transform;
import com.salesforce.dva.argus.service.metric.transform.TransformFactory;
import com.salesforce.dva.argus.service.metric.transform.plus.HeimdallTotalAvaTransform;
import com.salesforce.dva.argus.system.SystemConfiguration;

public class TimeSeriesTest {
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

//	@Test
//	public void fillZero(){
//		Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
//        datapoints_1.put(100L, "900.0");
//        datapoints_1.put(200L, "1200.0");
//        datapoints_1.put(300L, "1000.0");
//        metric_1.setDatapoints(datapoints_1);
//        metric_1.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_1.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
//        TimeSeries aptTimeSeries = TimeSeries.getTimeSeries(metric_1);
//
//        Metric metric_2 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_2 = new HashMap<Long, String>();
//        datapoints_2.put(100L, "900.0");
//        datapoints_2.put(200L, "1200.0");
//        datapoints_2.put(300L, "1000.0");
//        datapoints_2.put(400L, "1000.0");
//        metric_2.setDatapoints(datapoints_2);
//        metric_2.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_2.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
//        TimeSeries template = TimeSeries.getTimeSeries(metric_2);
//
//        TimeSeries result=aptTimeSeries.fillZero(template);
//        assertTrue(result.getTs().size()==4);
//        assertTrue(result.getTs().get(400L).equals(String.valueOf(0)));
//	}
//
//	@Test
//	public void remoteZero(){
//		Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
//        datapoints_1.put(100L, "900.0");
//        datapoints_1.put(200L, "0.0");
//        datapoints_1.put(300L, "1000.0");
//        datapoints_1.put(400L, "0");
//        metric_1.setDatapoints(datapoints_1);
//        metric_1.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_1.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
//        TimeSeries aptTimeSeries = TimeSeries.getTimeSeries(metric_1);
//
//        TimeSeries result=aptTimeSeries.remoteZero();
//        assertTrue(result.getTs().size()==2);
//	}
//

	@Test
	public void dev(){
//		System.out.println("start");
//
//		int a =-5;
//
//		System.out.println(Integer.toBinaryString(-33));
//
//		System.out.println(-33/32);
//		System.out.println(-33>>5);
//
//
//		float b=-5.0f;
//		System.out.println((byte)Float.floatToIntBits(b));
//
//
		//List<Integer> a=new ArrayList<Integer>(Arrays.asList(1,2,3));

//		Integer a1=Integer.valueOf(1000);
//		Integer a2=Integer.valueOf(1000);
//		int b=1000;
//		System.out.println(System.identityHashCode(a1));
//		System.out.println(System.identityHashCode(a2));
//		System.out.println(System.identityHashCode(1000));
//
//		System.out.println(System.identityHashCode(b));
//
//
//		Map a=new LinkedHashMap<>();



//		b=b+1;
//		System.out.println(System.identityHashCode(b));
//		System.out.println(System.identityHashCode(11));

//		int a=0;
//		System.out.println(System.identityHashCode(500));
//		System.out.println(System.identityHashCode(500));
//		System.out.println(System.identityHashCode(a));
//		System.out.println(System.identityHashCode(a));
		LinkedHashMap<Integer,String> lm=new LinkedHashMap<Integer,String>();

		lm.put(100,"Amit");
		lm.put(101,"Vijay");
		lm.put(102,"Rahul");

		System.out.println(lm);

		Set s=lm.keySet();
		System.out.println(s.getClass());

		Map<Long,String> m=new TreeMap<Long,String>();

	}


//	List doA(List input){
//		input=new ArrayList<String>(Arrays.asList("a","b"));
//		return input;
//	}

//	@Test
//	public void HeimdallTotalAvaTransform_dev(){
//		Transform transform=injector.getInstance(HeimdallTotalAvaTransform.class);
//		//Transform transform = new HeimdallTotalAvaTransform();
//		int offset=1000*60/100;
//        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
//        datapoints_1.put(100L*offset, "900.0");
//        datapoints_1.put(200L*offset, "1200.0");
//        datapoints_1.put(300L*offset, "1000.0");
//        datapoints_1.put(400L*offset, "1000.0");
//        datapoints_1.put(500L*offset, "400.0");
//        datapoints_1.put(600L*offset, "200.0");
//        metric_1.setDatapoints(datapoints_1);
//        metric_1.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_1.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
//
//        Metric metric_2 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_2 = new HashMap<Long, String>();
//        datapoints_2.put(100L*offset, "1400.0");
//        datapoints_2.put(200L*offset, "1350.0");
//        datapoints_2.put(300L*offset, "1350.0");
//        datapoints_2.put(400L*offset, "950.0");
//        datapoints_2.put(500L*offset, "950.0");
//        datapoints_2.put(600L*offset, "50.0");
//        metric_2.setDatapoints(datapoints_2);
//        metric_2.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
//        metric_2.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg");
//
//        Metric metric_3 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_3 = new HashMap<Long, String>();
//        datapoints_3.put(100L*offset, "2.0");
//        datapoints_3.put(200L*offset, "3.0");
//        datapoints_3.put(300L*offset, "3.0");
//        datapoints_3.put(400L*offset, "2.0");
//        datapoints_3.put(500L*offset, "3.0");
//        datapoints_3.put(600L*offset, "2.0");
//        metric_3.setDatapoints(datapoints_3);
//        metric_3.setTag("device", "na11-app1-1-chi.ops.sfdc.net");
//        metric_3.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
//
//        Metric metric_4 = new Metric(TEST_SCOPE, TEST_METRIC);
//        Map<Long, String> datapoints_4 = new HashMap<Long, String>();
//        datapoints_4.put(100L*offset, "1.0");
//        datapoints_4.put(200L*offset, "9.0");
//        datapoints_4.put(300L*offset, "2.0");
//        datapoints_4.put(400L*offset, "2.0");
//        datapoints_4.put(500L*offset, "1.0");
//        datapoints_4.put(600L*offset, "1.0");
//        metric_4.setDatapoints(datapoints_4);
//        metric_4.setTag("device", "na11-app1-2-chi.ops.sfdc.net");
//        metric_4.setMetric("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg");
//
//
//        List<Metric> metrics = new ArrayList<Metric>();
//        metrics.add(metric_1);
//        metrics.add(metric_2);
//        metrics.add(metric_3);
//        metrics.add(metric_4);
//
//        List<Metric> result = transform.transform(metrics);
//        System.out.println("\n\nINPUT>>>\n"+metrics);
//        System.out.println("\n\nOUTPUT>>>\n"+result);
//	}
}
