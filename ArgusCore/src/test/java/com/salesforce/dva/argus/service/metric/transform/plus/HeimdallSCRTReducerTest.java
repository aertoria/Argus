package com.salesforce.dva.argus.service.metric.transform.plus;

import static org.junit.Assert.*;

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

public class HeimdallSCRTReducerTest {
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
	public void HeimdallSCRTTransformDB(){
		Transform transform=injector.getInstance(HeimdallSCRTReducer.class);
		//Transform transform = new HeimdallTotalAvaTransform();
		int offset=1000*60/100;
        Metric metric_1 = new Metric("db.oracle.CHI.AGG.la20", "LADB20.LADB20-1.active__sessions");
        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
        datapoints_1.put(1487220991000L, "50.0");datapoints_1.put(1487221020000L, "170.0");datapoints_1.put(1487221050000L, "0.0");datapoints_1.put(1487221082000L, "151.0");datapoints_1.put(1487221111000L, "1.0");datapoints_1.put(1487221141000L, "2.0");datapoints_1.put(1487221171000L, "1.0");datapoints_1.put(1487221208000L, "1.0");datapoints_1.put(1487221231000L, "1.0");datapoints_1.put(1487221264000L, "1.0");datapoints_1.put(1487221291000L, "1.0");datapoints_1.put(1487221323000L, "2.0");datapoints_1.put(1487221352000L, "4.0");datapoints_1.put(1487221381000L, "2.0");datapoints_1.put(1487221411000L, "1.0");datapoints_1.put(1487221441000L, "1.0");datapoints_1.put(1487221473000L, "3.0");datapoints_1.put(1487221513000L, "2.0");datapoints_1.put(1487221532000L, "3.0");datapoints_1.put(1487221561000L, "2.0");datapoints_1.put(1487221591000L, "2.0");datapoints_1.put(1487221638000L, "2.0");datapoints_1.put(1487221656000L, "2.0");datapoints_1.put(1487221680000L, "2.0");datapoints_1.put(1487221711000L, "3.0");datapoints_1.put(1487221740000L, "2.0");datapoints_1.put(1487221771000L, "1.0");datapoints_1.put(1487221803000L, "2.0");datapoints_1.put(1487221831000L, "2.0");datapoints_1.put(1487221866000L, "2.0");datapoints_1.put(1487221891000L, "2.0");datapoints_1.put(1487221920000L, "2.0");datapoints_1.put(1487221951000L, "1.0");datapoints_1.put(1487221984000L, "1.0");datapoints_1.put(1487222012000L, "2.0");datapoints_1.put(1487222056000L, "1.0");datapoints_1.put(1487222071000L, "2.0");datapoints_1.put(1487222113000L, "1.0");datapoints_1.put(1487222131000L, "3.0");datapoints_1.put(1487222160000L, "2.0");datapoints_1.put(1487222191000L, "2.0");datapoints_1.put(1487222220000L, "2.0");datapoints_1.put(1487222253000L, "1.0");datapoints_1.put(1487222297000L, "1.0");datapoints_1.put(1487222311000L, "1.0");datapoints_1.put(1487222341000L, "3.0");datapoints_1.put(1487222371000L, "1.0");datapoints_1.put(1487222402000L, "1.0");datapoints_1.put(1487222431000L, "3.0");datapoints_1.put(1487222460000L, "2.0");datapoints_1.put(1487222492000L, "1.0");datapoints_1.put(1487222522000L, "2.0");datapoints_1.put(1487222551000L, "4.0");datapoints_1.put(1487222581000L, "2.0");datapoints_1.put(1487222611000L, "3.0");datapoints_1.put(1487222641000L, "2.0");datapoints_1.put(1487222671000L, "2.0");datapoints_1.put(1487222702000L, "2.0");datapoints_1.put(1487222730000L, "3.0");datapoints_1.put(1487222767000L, "1.0");datapoints_1.put(1487222791000L, "4.0");datapoints_1.put(1487222821000L, "3.0");datapoints_1.put(1487222850000L, "2.0");datapoints_1.put(1487222881000L, "1.0");datapoints_1.put(1487222912000L, "1.0");datapoints_1.put(1487222940000L, "2.0");datapoints_1.put(1487222972000L, "2.0");datapoints_1.put(1487223000000L, "2.0");datapoints_1.put(1487223031000L, "3.0");datapoints_1.put(1487223060000L, "2.0");datapoints_1.put(1487223091000L, "1.0");datapoints_1.put(1487223122000L, "1.0");datapoints_1.put(1487223151000L, "1.0");datapoints_1.put(1487223182000L, "1.0");datapoints_1.put(1487223211000L, "1.0");datapoints_1.put(1487223241000L, "1.0");datapoints_1.put(1487223272000L, "1.0");datapoints_1.put(1487223301000L, "1.0");datapoints_1.put(1487223331000L, "2.0");datapoints_1.put(1487223361000L, "2.0");datapoints_1.put(1487223392000L, "2.0");datapoints_1.put(1487223422000L, "1.0");datapoints_1.put(1487223451000L, "2.0");datapoints_1.put(1487223481000L, "1.0");datapoints_1.put(1487223512000L, "3.0");datapoints_1.put(1487223541000L, "1.0");datapoints_1.put(1487223571000L, "2.0");datapoints_1.put(1487223601000L, "3.0");datapoints_1.put(1487223631000L, "2.0");datapoints_1.put(1487223669000L, "2.0");datapoints_1.put(1487223692000L, "1.0");datapoints_1.put(1487223720000L, "2.0");datapoints_1.put(1487223752000L, "1.0");datapoints_1.put(1487223794000L, "2.0");datapoints_1.put(1487223811000L, "3.0");datapoints_1.put(1487223842000L, "1.0");datapoints_1.put(1487223871000L, "2.0");datapoints_1.put(1487223903000L, "1.0");datapoints_1.put(1487223931000L, "2.0");datapoints_1.put(1487223961000L, "2.0");datapoints_1.put(1487223990000L, "2.0");datapoints_1.put(1487224020000L, "2.0");datapoints_1.put(1487224050000L, "2.0");datapoints_1.put(1487224080000L, "2.0");datapoints_1.put(1487224111000L, "1.0");datapoints_1.put(1487224141000L, "2.0");datapoints_1.put(1487224171000L, "3.0");datapoints_1.put(1487224203000L, "2.0");datapoints_1.put(1487224231000L, "2.0");datapoints_1.put(1487224263000L, "1.0");datapoints_1.put(1487224290000L, "2.0");datapoints_1.put(1487224321000L, "2.0");datapoints_1.put(1487224351000L, "2.0");datapoints_1.put(1487224381000L, "1.0");datapoints_1.put(1487224411000L, "1.0");datapoints_1.put(1487224456000L, "2.0");datapoints_1.put(1487224471000L, "3.0");datapoints_1.put(1487224500000L, "2.0");datapoints_1.put(1487224532000L, "1.0");datapoints_1.put(1487224566000L, "2.0");datapoints_1.put(1487224594000L, "1.0");datapoints_1.put(1487224623000L, "3.0");datapoints_1.put(1487224652000L, "1.0");datapoints_1.put(1487224681000L, "2.0");datapoints_1.put(1487224711000L, "1.0");datapoints_1.put(1487224747000L, "1.0");datapoints_1.put(1487224771000L, "3.0");datapoints_1.put(1487224800000L, "2.0");datapoints_1.put(1487224830000L, "2.0");datapoints_1.put(1487224866000L, "1.0");datapoints_1.put(1487224891000L, "2.0");datapoints_1.put(1487224920000L, "2.0");datapoints_1.put(1487224951000L, "2.0");datapoints_1.put(1487224981000L, "1.0");datapoints_1.put(1487225011000L, "2.0");datapoints_1.put(1487225041000L, "1.0");datapoints_1.put(1487225071000L, "1.0");datapoints_1.put(1487225101000L, "3.0");datapoints_1.put(1487225132000L, "2.0");datapoints_1.put(1487225160000L, "2.0");datapoints_1.put(1487225191000L, "1.0");datapoints_1.put(1487225222000L, "2.0");datapoints_1.put(1487225250000L, "3.0");datapoints_1.put(1487225281000L, "3.0");datapoints_1.put(1487225311000L, "2.0");datapoints_1.put(1487225342000L, "1.0");datapoints_1.put(1487225371000L, "1.0");datapoints_1.put(1487225400000L, "2.0");datapoints_1.put(1487225431000L, "2.0");datapoints_1.put(1487225464000L, "1.0");datapoints_1.put(1487225491000L, "2.0");datapoints_1.put(1487225521000L, "2.0");datapoints_1.put(1487225553000L, "2.0");datapoints_1.put(1487225582000L, "3.0");datapoints_1.put(1487225611000L, "2.0");datapoints_1.put(1487225642000L, "2.0");datapoints_1.put(1487225671000L, "2.0");datapoints_1.put(1487225703000L, "3.0");datapoints_1.put(1487225731000L, "2.0");datapoints_1.put(1487225763000L, "2.0");datapoints_1.put(1487225791000L, "1.0");datapoints_1.put(1487225824000L, "1.0");datapoints_1.put(1487225851000L, "1.0");datapoints_1.put(1487225882000L, "1.0");datapoints_1.put(1487225911000L, "2.0");datapoints_1.put(1487225950000L, "3.0");datapoints_1.put(1487225971000L, "1.0");datapoints_1.put(1487226001000L, "1.0");datapoints_1.put(1487226032000L, "2.0");datapoints_1.put(1487226068000L, "1.0");datapoints_1.put(1487226091000L, "2.0");datapoints_1.put(1487226123000L, "2.0");datapoints_1.put(1487226152000L, "1.0");datapoints_1.put(1487226194000L, "1.0");datapoints_1.put(1487226211000L, "2.0");datapoints_1.put(1487226242000L, "1.0");datapoints_1.put(1487226273000L, "2.0");datapoints_1.put(1487226304000L, "1.0");datapoints_1.put(1487226331000L, "2.0");datapoints_1.put(1487226361000L, "1.0");datapoints_1.put(1487226391000L, "2.0");datapoints_1.put(1487226420000L, "2.0");datapoints_1.put(1487226450000L, "2.0");datapoints_1.put(1487226483000L, "2.0");datapoints_1.put(1487226511000L, "1.0");datapoints_1.put(1487226540000L, "2.0");datapoints_1.put(1487226571000L, "1.0");datapoints_1.put(1487226603000L, "1.0");datapoints_1.put(1487226630000L, "2.0");datapoints_1.put(1487226662000L, "2.0");datapoints_1.put(1487226691000L, "1.0");datapoints_1.put(1487226721000L, "2.0");datapoints_1.put(1487226753000L, "2.0");datapoints_1.put(1487226781000L, "1.0");datapoints_1.put(1487226811000L, "3.0");datapoints_1.put(1487226842000L, "2.0");datapoints_1.put(1487226871000L, "2.0");datapoints_1.put(1487226901000L, "1.0");datapoints_1.put(1487226932000L, "1.0");datapoints_1.put(1487226960000L, "2.0");datapoints_1.put(1487226991000L, "2.0");datapoints_1.put(1487227024000L, "1.0");datapoints_1.put(1487227051000L, "2.0");datapoints_1.put(1487227080000L, "2.0");datapoints_1.put(1487227112000L, "3.0");datapoints_1.put(1487227144000L, "3.0");datapoints_1.put(1487227171000L, "2.0");datapoints_1.put(1487227201000L, "1.0");datapoints_1.put(1487227232000L, "2.0");datapoints_1.put(1487227262000L, "2.0");datapoints_1.put(1487227291000L, "3.0");datapoints_1.put(1487227322000L, "1.0");datapoints_1.put(1487227351000L, "1.0");datapoints_1.put(1487227381000L, "2.0");datapoints_1.put(1487227411000L, "3.0");datapoints_1.put(1487227442000L, "3.0");datapoints_1.put(1487227470000L, "2.0");datapoints_1.put(1487227501000L, "2.0");datapoints_1.put(1487227531000L, "1.0");datapoints_1.put(1487227563000L, "1.0");datapoints_1.put(1487227590000L, "2.0");datapoints_1.put(1487227621000L, "2.0");datapoints_1.put(1487227651000L, "1.0");datapoints_1.put(1487227681000L, "2.0");datapoints_1.put(1487227713000L, "2.0");datapoints_1.put(1487227740000L, "2.0");datapoints_1.put(1487227771000L, "2.0");datapoints_1.put(1487227801000L, "3.0");datapoints_1.put(1487227832000L, "1.0");datapoints_1.put(1487227861000L, "2.0");datapoints_1.put(1487227891000L, "2.0");datapoints_1.put(1487227921000L, "1.0");datapoints_1.put(1487227951000L, "3.0");datapoints_1.put(1487227983000L, "1.0");datapoints_1.put(1487228012000L, "1.0");datapoints_1.put(1487228046000L, "2.0");datapoints_1.put(1487228071000L, "3.0");datapoints_1.put(1487228102000L, "2.0");datapoints_1.put(1487228132000L, "2.0");datapoints_1.put(1487228161000L, "4.0");
        metric_1.setDatapoints(datapoints_1);
        metric_1.setTag("device", "la20-db1-2-chi.ops.sfdc.net");
        
        
        Metric metric_2 = new Metric("db.oracle.CHI.AGG.la20", "LADB20.LADB20-2.active__sessions");
        Map<Long, String> datapoints_2 = new HashMap<Long, String>();
        datapoints_2.put(1487220991000L, "2.0");datapoints_2.put(1487221020000L, "1.0");datapoints_2.put(1487221050000L, "1.0");datapoints_2.put(1487221082000L, "2.0");datapoints_2.put(1487221111000L, "0.0");datapoints_2.put(1487221141000L, "1.0");datapoints_2.put(1487221171000L, "2.0");datapoints_2.put(1487221208000L, "3.0");datapoints_2.put(1487221231000L, "2.0");datapoints_2.put(1487221264000L, "2.0");datapoints_2.put(1487221291000L, "2.0");datapoints_2.put(1487221323000L, "2.0");datapoints_2.put(1487221352000L, "3.0");datapoints_2.put(1487221381000L, "2.0");datapoints_2.put(1487221411000L, "2.0");datapoints_2.put(1487221441000L, "2.0");datapoints_2.put(1487221473000L, "2.0");datapoints_2.put(1487221513000L, "2.0");datapoints_2.put(1487221532000L, "1.0");datapoints_2.put(1487221561000L, "1.0");datapoints_2.put(1487221591000L, "1.0");datapoints_2.put(1487221638000L, "1.0");datapoints_2.put(1487221656000L, "1.0");datapoints_2.put(1487221680000L, "1.0");datapoints_2.put(1487221711000L, "2.0");datapoints_2.put(1487221740000L, "1.0");datapoints_2.put(1487221771000L, "2.0");datapoints_2.put(1487221803000L, "2.0");datapoints_2.put(1487221831000L, "1.0");datapoints_2.put(1487221866000L, "2.0");datapoints_2.put(1487221891000L, "1.0");datapoints_2.put(1487221920000L, "1.0");datapoints_2.put(1487221951000L, "3.0");datapoints_2.put(1487221984000L, "2.0");datapoints_2.put(1487222012000L, "2.0");datapoints_2.put(1487222056000L, "2.0");datapoints_2.put(1487222071000L, "1.0");datapoints_2.put(1487222113000L, "2.0");datapoints_2.put(1487222131000L, "3.0");datapoints_2.put(1487222160000L, "1.0");datapoints_2.put(1487222191000L, "1.0");datapoints_2.put(1487222220000L, "1.0");datapoints_2.put(1487222253000L, "2.0");datapoints_2.put(1487222297000L, "2.0");datapoints_2.put(1487222311000L, "2.0");datapoints_2.put(1487222341000L, "2.0");datapoints_2.put(1487222371000L, "2.0");datapoints_2.put(1487222402000L, "2.0");datapoints_2.put(1487222431000L, "3.0");datapoints_2.put(1487222460000L, "1.0");datapoints_2.put(1487222492000L, "2.0");datapoints_2.put(1487222522000L, "2.0");datapoints_2.put(1487222551000L, "2.0");datapoints_2.put(1487222581000L, "1.0");datapoints_2.put(1487222611000L, "1.0");datapoints_2.put(1487222641000L, "1.0");datapoints_2.put(1487222671000L, "2.0");datapoints_2.put(1487222702000L, "2.0");datapoints_2.put(1487222730000L, "1.0");datapoints_2.put(1487222767000L, "2.0");datapoints_2.put(1487222791000L, "1.0");datapoints_2.put(1487222821000L, "1.0");datapoints_2.put(1487222850000L, "1.0");datapoints_2.put(1487222881000L, "2.0");datapoints_2.put(1487222912000L, "2.0");datapoints_2.put(1487222940000L, "1.0");datapoints_2.put(1487222972000L, "2.0");datapoints_2.put(1487223000000L, "1.0");datapoints_2.put(1487223031000L, "3.0");datapoints_2.put(1487223060000L, "1.0");datapoints_2.put(1487223091000L, "2.0");datapoints_2.put(1487223122000L, "2.0");datapoints_2.put(1487223151000L, "2.0");datapoints_2.put(1487223182000L, "2.0");datapoints_2.put(1487223211000L, "2.0");datapoints_2.put(1487223241000L, "2.0");datapoints_2.put(1487223272000L, "2.0");datapoints_2.put(1487223301000L, "2.0");datapoints_2.put(1487223331000L, "1.0");datapoints_2.put(1487223361000L, "1.0");datapoints_2.put(1487223392000L, "1.0");datapoints_2.put(1487223422000L, "3.0");datapoints_2.put(1487223451000L, "1.0");datapoints_2.put(1487223481000L, "2.0");datapoints_2.put(1487223512000L, "1.0");datapoints_2.put(1487223541000L, "2.0");datapoints_2.put(1487223571000L, "4.0");datapoints_2.put(1487223601000L, "1.0");datapoints_2.put(1487223631000L, "1.0");datapoints_2.put(1487223669000L, "2.0");datapoints_2.put(1487223692000L, "2.0");datapoints_2.put(1487223720000L, "1.0");datapoints_2.put(1487223752000L, "2.0");datapoints_2.put(1487223794000L, "1.0");datapoints_2.put(1487223811000L, "1.0");datapoints_2.put(1487223842000L, "2.0");datapoints_2.put(1487223871000L, "1.0");datapoints_2.put(1487223903000L, "2.0");datapoints_2.put(1487223931000L, "1.0");datapoints_2.put(1487223961000L, "1.0");datapoints_2.put(1487223990000L, "1.0");datapoints_2.put(1487224020000L, "1.0");datapoints_2.put(1487224050000L, "1.0");datapoints_2.put(1487224080000L, "1.0");datapoints_2.put(1487224111000L, "2.0");datapoints_2.put(1487224141000L, "1.0");datapoints_2.put(1487224171000L, "1.0");datapoints_2.put(1487224203000L, "2.0");datapoints_2.put(1487224231000L, "1.0");datapoints_2.put(1487224263000L, "2.0");datapoints_2.put(1487224290000L, "1.0");datapoints_2.put(1487224321000L, "1.0");datapoints_2.put(1487224351000L, "4.0");datapoints_2.put(1487224381000L, "2.0");datapoints_2.put(1487224411000L, "2.0");datapoints_2.put(1487224456000L, "1.0");datapoints_2.put(1487224471000L, "3.0");datapoints_2.put(1487224500000L, "1.0");datapoints_2.put(1487224532000L, "2.0");datapoints_2.put(1487224566000L, "1.0");datapoints_2.put(1487224594000L, "2.0");datapoints_2.put(1487224623000L, "2.0");datapoints_2.put(1487224652000L, "2.0");datapoints_2.put(1487224681000L, "1.0");datapoints_2.put(1487224711000L, "2.0");datapoints_2.put(1487224747000L, "2.0");datapoints_2.put(1487224771000L, "3.0");datapoints_2.put(1487224800000L, "1.0");datapoints_2.put(1487224830000L, "1.0");datapoints_2.put(1487224866000L, "2.0");datapoints_2.put(1487224891000L, "1.0");datapoints_2.put(1487224920000L, "1.0");datapoints_2.put(1487224951000L, "1.0");datapoints_2.put(1487224981000L, "2.0");datapoints_2.put(1487225011000L, "4.0");datapoints_2.put(1487225041000L, "2.0");datapoints_2.put(1487225071000L, "2.0");datapoints_2.put(1487225101000L, "3.0");datapoints_2.put(1487225132000L, "2.0");datapoints_2.put(1487225160000L, "1.0");datapoints_2.put(1487225191000L, "2.0");datapoints_2.put(1487225222000L, "2.0");datapoints_2.put(1487225250000L, "3.0");datapoints_2.put(1487225281000L, "1.0");datapoints_2.put(1487225311000L, "1.0");datapoints_2.put(1487225342000L, "2.0");datapoints_2.put(1487225371000L, "2.0");datapoints_2.put(1487225400000L, "1.0");datapoints_2.put(1487225431000L, "1.0");datapoints_2.put(1487225464000L, "2.0");datapoints_2.put(1487225491000L, "2.0");datapoints_2.put(1487225521000L, "1.0");datapoints_2.put(1487225553000L, "2.0");datapoints_2.put(1487225582000L, "1.0");datapoints_2.put(1487225611000L, "1.0");datapoints_2.put(1487225642000L, "1.0");datapoints_2.put(1487225671000L, "1.0");datapoints_2.put(1487225703000L, "1.0");datapoints_2.put(1487225731000L, "1.0");datapoints_2.put(1487225763000L, "2.0");datapoints_2.put(1487225791000L, "2.0");datapoints_2.put(1487225824000L, "2.0");datapoints_2.put(1487225851000L, "2.0");datapoints_2.put(1487225882000L, "2.0");datapoints_2.put(1487225911000L, "1.0");datapoints_2.put(1487225950000L, "1.0");datapoints_2.put(1487225971000L, "2.0");datapoints_2.put(1487226001000L, "2.0");datapoints_2.put(1487226032000L, "2.0");datapoints_2.put(1487226068000L, "2.0");datapoints_2.put(1487226091000L, "1.0");datapoints_2.put(1487226123000L, "2.0");datapoints_2.put(1487226152000L, "2.0");datapoints_2.put(1487226194000L, "2.0");datapoints_2.put(1487226211000L, "1.0");datapoints_2.put(1487226242000L, "2.0");datapoints_2.put(1487226273000L, "2.0");datapoints_2.put(1487226304000L, "2.0");datapoints_2.put(1487226331000L, "2.0");datapoints_2.put(1487226361000L, "2.0");datapoints_2.put(1487226391000L, "1.0");datapoints_2.put(1487226420000L, "1.0");datapoints_2.put(1487226450000L, "1.0");datapoints_2.put(1487226483000L, "2.0");datapoints_2.put(1487226511000L, "2.0");datapoints_2.put(1487226540000L, "1.0");datapoints_2.put(1487226571000L, "2.0");datapoints_2.put(1487226603000L, "2.0");datapoints_2.put(1487226630000L, "1.0");datapoints_2.put(1487226662000L, "2.0");datapoints_2.put(1487226691000L, "2.0");datapoints_2.put(1487226721000L, "1.0");datapoints_2.put(1487226753000L, "2.0");datapoints_2.put(1487226781000L, "2.0");datapoints_2.put(1487226811000L, "3.0");datapoints_2.put(1487226842000L, "2.0");datapoints_2.put(1487226871000L, "4.0");datapoints_2.put(1487226901000L, "2.0");datapoints_2.put(1487226932000L, "2.0");datapoints_2.put(1487226960000L, "1.0");datapoints_2.put(1487226991000L, "4.0");datapoints_2.put(1487227024000L, "2.0");datapoints_2.put(1487227051000L, "1.0");datapoints_2.put(1487227080000L, "1.0");datapoints_2.put(1487227112000L, "2.0");datapoints_2.put(1487227144000L, "1.0");datapoints_2.put(1487227171000L, "4.0");datapoints_2.put(1487227201000L, "2.0");datapoints_2.put(1487227232000L, "2.0");datapoints_2.put(1487227262000L, "1.0");datapoints_2.put(1487227291000L, "3.0");datapoints_2.put(1487227322000L, "2.0");datapoints_2.put(1487227351000L, "2.0");datapoints_2.put(1487227381000L, "1.0");datapoints_2.put(1487227411000L, "2.0");datapoints_2.put(1487227442000L, "1.0");datapoints_2.put(1487227470000L, "1.0");datapoints_2.put(1487227501000L, "2.0");datapoints_2.put(1487227531000L, "3.0");datapoints_2.put(1487227563000L, "2.0");datapoints_2.put(1487227590000L, "3.0");datapoints_2.put(1487227621000L, "1.0");datapoints_2.put(1487227651000L, "2.0");datapoints_2.put(1487227681000L, "1.0");datapoints_2.put(1487227713000L, "2.0");datapoints_2.put(1487227740000L, "1.0");datapoints_2.put(1487227771000L, "1.0");datapoints_2.put(1487227801000L, "1.0");datapoints_2.put(1487227832000L, "2.0");datapoints_2.put(1487227861000L, "1.0");datapoints_2.put(1487227891000L, "4.0");datapoints_2.put(1487227921000L, "2.0");datapoints_2.put(1487227951000L, "1.0");datapoints_2.put(1487227983000L, "2.0");datapoints_2.put(1487228012000L, "2.0");datapoints_2.put(1487228046000L, "1.0");datapoints_2.put(1487228071000L, "1.0");datapoints_2.put(1487228102000L, "2.0");datapoints_2.put(1487228132000L, "2.0");datapoints_2.put(1487228161000L, "2.0");
        metric_2.setDatapoints(datapoints_2);
        metric_2.setTag("device", "la20-db1-1-chi.ops.sfdc.net");
        
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(metric_1);
        metrics.add(metric_2);
        
        List<Metric> result = transform.transform(metrics,Arrays.asList("IMPACT"));
        System.out.println("output>>>"+result);
	}
	
	//	@Test
	public void HeimdallSCRTTransformCSDB(){
		Transform transform=injector.getInstance(HeimdallSCRTReducer.class);
		//Transform transform = new HeimdallTotalAvaTransform();
		int offset=1000*60/100;
        Metric metric_1 = new Metric("db.oracle.CHI.AGG.la20", "LACSDB20.LACSDB20-1.active__sessions");
        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
        datapoints_1.put(1487220991000L, "160.0");datapoints_1.put(1487221020000L, "3.0");
        metric_1.setDatapoints(datapoints_1);
        metric_1.setTag("device", "la20-db1-2-chi.ops.sfdc.net");
        
        
        Metric metric_2 = new Metric("db.oracle.CHI.AGG.la20", "LACSDB20.LACSDB20-2.active__sessions");
        Map<Long, String> datapoints_2 = new HashMap<Long, String>();
        datapoints_2.put(1487220991000L, "5.0");datapoints_2.put(1487221020000L, "1.0");
        metric_2.setDatapoints(datapoints_2);
        metric_2.setTag("device", "la20-db1-1-chi.ops.sfdc.net");
        
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(metric_1);
        metrics.add(metric_2);
        
        List<Metric> result = transform.transform(metrics);
        System.out.println("\n\nINPUT2>>>\n"+metrics);
        System.out.println("\n\nOUTPUT2>>>\n"+result);
	}


	@Test
	public void HeimdallSCRTTransformDBSingleLarge(){
		Transform transform=injector.getInstance(HeimdallSCRTReducer.class);
		//Transform transform = new HeimdallTotalAvaTransform();
		int offset=1000*60/100;
        Metric metric_1 = new Metric("db.oracle.CHI.AGG.la90", "LADB20.LADB20-1.active__sessions");
        Map<Long, String> datapoints_1 = new HashMap<Long, String>();
        datapoints_1.put(1487220960000L+1L*60*1000, "50.0");
        datapoints_1.put(1487220960000L+2L*60*1000, "170.0");
        datapoints_1.put(1487220960000L+3L*60*1000, "14.0");
        datapoints_1.put(1487220960000L+4L*60*1000, "151.0");
        datapoints_1.put(1487220960000L+5L*60*1000, "12.0");
        
        metric_1.setDatapoints(datapoints_1);
        metric_1.setTag("device", "la20-db1-2-chi.ops.sfdc.net");
        
        
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(metric_1);
        
        List<Metric> result = transform.transform(metrics,Arrays.asList("-90000","AVA"));
        System.out.println("output>>>"+result);
	}
}