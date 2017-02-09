package com.salesforce.dva.argus.service.metric.transform.plus;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
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

public class HeimdallDataGuardMaxLagTest{
	private static final String TEST_SCOPE = "db.FRF.SP1.eu2";
	private static final String TEST_METRIC = "EU2DB1FRF.remote_dg_transport_lag";
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
	
	//@Test
	public void podFilterTest(){
		Transform transform=injector.getInstance(HeimdallDataGuardMaxLag.class);
		
		//1482105600000:1482710400000:db.FRF.SP1.eu2:*.remote_dg_transport_lag:avg
		int offset=1000*60;//every minute
		long start=0L;
        Metric metric_1 = new Metric(TEST_SCOPE, TEST_METRIC);
        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
        datapoints_1.put(1483860605000L,"0.0");
        datapoints_1.put(1483860662000L,"0.0");
        datapoints_1.put(1483860722000L,"0.0");
        datapoints_1.put(1483860782000L,"0.0");
        datapoints_1.put(1483860842000L,"0.0");
        datapoints_1.put(1483860903000L,"1.0");
        datapoints_1.put(1483861022000L,"106.0");//
        datapoints_1.put(1483861082000L,"0.0");
        datapoints_1.put(1483861142000L,"62.0");
        datapoints_1.put(1483861202000L,"122.0");//
        datapoints_1.put(1483861262000L,"0.0");
        datapoints_1.put(1483861329000L,"87.0");
        datapoints_1.put(1483861382000L,"148.0");//
        datapoints_1.put(1483861442000L,"112.0");//
        datapoints_1.put(1483861502000L,"0.0");
        datapoints_1.put(1483861562000L,"95.0");
        datapoints_1.put(1483861622000L,"156.0");//
        datapoints_1.put(1483861682000L,"118.0");//
        datapoints_1.put(1483861742000L,"80.0");
        datapoints_1.put(1483861922000L,"125.0");//
        datapoints_1.put(1483861982000L,"95.0");
        datapoints_1.put(1483862042000L,"56.0");
        datapoints_1.put(1483862102000L,"116.0");//
        datapoints_1.put(1483862163000L,"0.0");
        datapoints_1.put(1483862225000L,"81.0");
        datapoints_1.put(1483862282000L,"141.0");//
        datapoints_1.put(1483862403000L,"105.0");//
        datapoints_1.put(1483862462000L,"68.0");
        datapoints_1.put(1483862645000L,"0.0");
        datapoints_1.put(1483862762000L,"0.0");
        datapoints_1.put(1483862822000L,"0.0");
       

        metric_1.setDatapoints(datapoints_1);
        metric_1.setTag("device", "dataguard");
        
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(metric_1);
        List<Metric> result = transform.transform(metrics,Arrays.asList("20", "120.0"));
        
        
        System.out.println(result);
	}
		
	//@Test
	public void podFilterTestMutliple(){
		Transform transform=injector.getInstance(HeimdallDataGuardMaxLag.class);
		
		//1482105600000:1482710400000:db.FRF.SP1.eu2:*.remote_dg_transport_lag:avg
		int offset=1000*60;//every minute
		long start=0L;
		
		
		Metric metric_1 = new Metric("db.FRF.SP1.eu1", "db.FRF.SP1.eu1");
        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
        datapoints_1.put(start+1L*offset, "0.0");
        datapoints_1.put(start+2L*offset, "0.0");
        datapoints_1.put(start+3L*offset, "120.0");
        datapoints_1.put(start+4L*offset, "125.0");
        datapoints_1.put(start+5L*offset, "140.0");
        datapoints_1.put(start+6L*offset, "0.0");
        datapoints_1.put(start+7L*offset, "400.0");
        datapoints_1.put(start+8L*offset, "0.0");
        datapoints_1.put(start+9L*offset, "0.0");
        datapoints_1.put(start+10L*offset, "0.0");
        datapoints_1.put(start+11L*offset, "520.0");
        datapoints_1.put(start+12L*offset, "100.0");
        datapoints_1.put(start+13L*offset, "0.0");
        datapoints_1.put(start+14L*offset, "0.0");
        metric_1.setDatapoints(datapoints_1);
        metric_1.setTag("device", "dataguard");
               

        Metric metric_2 = new Metric("db.FRF.SP1.eu2", "db.FRF.SP1.eu2");
        Map<Long, String> datapoints_2 = new HashMap<Long, String>();
        datapoints_2.put(1482105600000L, "0.0");
        datapoints_2.put(1482105660000L, "0.0");
        datapoints_2.put(1482105720000L, "18.0");
        datapoints_2.put(1482105780000L, "91.0");
        datapoints_2.put(1482105840000L, "0");
        datapoints_2.put(1482105900000L, "0");
        datapoints_2.put(1482105960000L, "0.0");
       
        metric_2.setDatapoints(datapoints_2);
        metric_2.setTag("device", "dataguard");
        
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(metric_1);
        metrics.add(metric_2);
        
        List<Metric> result = transform.transform(metrics,Arrays.asList("4", "120.0"));
//        System.out.println(result);
	}
	
	/*
	 * test case for W-3647544 enhancements
	 * 
	 */
	@Test
	public void durationFilterTest() {
		Transform transform=injector.getInstance(HeimdallDataGuardMaxLag.class);
		
		//1482105600000:1482710400000:db.FRF.SP1.eu2:*.remote_dg_transport_lag:avg
		int offset=1000*60;//every minute
		long start=0L;
		
		
		Metric metric_1 = new Metric("db.FRF.SP1.eu1", "db.FRF.SP1.eu1");
        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
        datapoints_1.put(start+1L*offset, "0.0");
        datapoints_1.put(start+2L*offset, "0.0");
        datapoints_1.put(start+3L*offset, "120.0");
        datapoints_1.put(start+4L*offset, "125.0");
        datapoints_1.put(start+5L*offset, "140.0");
        datapoints_1.put(start+6L*offset, "0.0");
        datapoints_1.put(start+7L*offset, "400.0");
        datapoints_1.put(start+8L*offset, "0.0");
        datapoints_1.put(start+9L*offset, "0.0");
        datapoints_1.put(start+10L*offset, "0.0");
        datapoints_1.put(start+11L*offset, "520.0");
        datapoints_1.put(start+12L*offset, "100.0");
        datapoints_1.put(start+13L*offset, "0.0");
        datapoints_1.put(start+14L*offset, "0.0");
        metric_1.setDatapoints(datapoints_1);
        metric_1.setTag("device", "dataguard");
               

        Metric metric_2 = new Metric("db.FRF.SP1.eu2", "db.FRF.SP1.eu2");
        Map<Long, String> datapoints_2 = new HashMap<Long, String>();
        datapoints_2.put(1482105600000L, "0.0");
        datapoints_2.put(1482105660000L, "0.0");
        datapoints_2.put(1482105720000L, "18.0");
        datapoints_2.put(1482105780000L, "91.0");
        datapoints_2.put(1482105840000L, "0");
        datapoints_2.put(1482105900000L, "0");
        datapoints_2.put(1482105960000L, "0.0");
       
        metric_2.setDatapoints(datapoints_2);
        metric_2.setTag("device", "dataguard");
        
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(metric_1);
        metrics.add(metric_2);
        
        
        Metric metric_3 = new Metric("db.FRF.SP1.eu3", "db.FRF.SP1.eu3");
        Map<Long, String> datapoints_3 = new HashMap<Long, String>();
        datapoints_3.put(1482105600000L, "0.0");
        datapoints_3.put(1482105660000L, "0.0");
        datapoints_3.put(1482105720000L, "0.0");
        datapoints_3.put(1482105780000L, "0.0");
        datapoints_3.put(1482105840000L, "120");
        datapoints_3.put(1482105900000L, "0");
        datapoints_3.put(1482105960000L, "0.0");
       
        metric_3.setDatapoints(datapoints_3);
        metric_3.setTag("device", "dataguard");

        metrics.add(metric_3);

        
        List<Metric> result = transform.transform(metrics,Arrays.asList("4", "120.0"));
        //System.out.println("Duration filter test");
        //System.out.println(result);
        
        assertEquals(2*2, result.size());
        
        
	}

}
