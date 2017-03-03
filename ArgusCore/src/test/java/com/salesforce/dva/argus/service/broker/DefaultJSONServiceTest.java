package com.salesforce.dva.argus.service.broker;

import java.util.Arrays;
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
import com.google.inject.matcher.Matchers;
import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.entity.MetricSchemaRecordQuery;
import com.salesforce.dva.argus.inject.SLF4JTypeListener;
import com.salesforce.dva.argus.service.DiscoveryService;
import com.salesforce.dva.argus.service.SchemaService;
import com.salesforce.dva.argus.service.TSDBService;
import com.salesforce.dva.argus.service.metric.transform.TransformFactory;
import com.salesforce.dva.argus.service.tsdb.MetricQuery;
import com.salesforce.dva.argus.service.tsdb.MetricQuery.Aggregator;
import com.salesforce.dva.argus.system.SystemConfiguration;

public class DefaultJSONServiceTest {
	private static Injector injector;
	private static SystemConfiguration configuration;
    SchemaService _schemaService;
    DiscoveryService _discoveryService;
    TSDBService _tsdbService;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception  {
    	configuration=new SystemConfiguration(new Properties());
		configuration.setProperty("service.property.json.endpoint", "XXX");
		configuration.setProperty("service.property.json.username", "XXX");
		configuration.setProperty("service.property.json.password", "XXX");
		
		injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				new FactoryModuleBuilder().build(TransformFactory.class);
				bind(TSDBService.class).to(DefaultJSONService.class);
				//bind(DiscoveryService.class).to(JSONDiscoveryService.class);
				bind(SchemaService.class).to(DeferredSchemaService.class);
				bind(SystemConfiguration.class).toInstance(configuration);
				bindListener(Matchers.any(), new SLF4JTypeListener());
			}
		});
    }
    
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		injector=null;
	}
	
	@Test
    public void dev() {
		MetricQuery query = new MetricQuery("REDUCED.db.PROD.CHI.SP2.na2", "CollectedMin", null, 1488208396704L, 1488388396704L);
		query.setAggregator(Aggregator.AVG);
		_tsdbService=injector.getInstance(DefaultJSONService.class);
		Map<MetricQuery, List<Metric>> ms = _tsdbService.getMetrics(Arrays.asList(query));
		ms.get(query).forEach(m -> System.out.println(m.toString()));
    }
}
