/*
 * Copyright (c) 2016, Salesforce.com, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of Salesforce.com nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
     
package com.salesforce.dva.argus.service.broker;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.salesforce.dva.argus.AbstractTest;
import com.salesforce.dva.argus.IntegrationTest;
import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.entity.MetricSchemaRecordQuery;
import com.salesforce.dva.argus.service.DiscoveryService;
import com.salesforce.dva.argus.service.SchemaService;
import com.salesforce.dva.argus.service.TSDBService;
import com.salesforce.dva.argus.service.SchemaService.RecordType;
import com.salesforce.dva.argus.service.metric.transform.TransformFactory;
import com.salesforce.dva.argus.service.tsdb.MetricQuery;
import com.salesforce.dva.argus.system.SystemConfiguration;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;
import com.salesforce.dva.argus.service.schema.DefaultDiscoveryService;

public class DeferredSchemaServiceTest{
	private static Injector injector;
	private static SystemConfiguration configuration;
    SchemaService _schemaService;
    DiscoveryService _discoveryService;
    @BeforeClass
    public static void setUpBeforeClass() throws Exception  {
    	configuration=new SystemConfiguration(new Properties());
		configuration.setProperty("service.property.json.endpoint", "https://localhost:443/argusws");
		configuration.setProperty("service.property.json.username", "sampleUserName");
		configuration.setProperty("service.property.json.password", "XXXXXXXX");
		
		injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				new FactoryModuleBuilder().build(TransformFactory.class);
				bind(TSDBService.class).to(DefaultJSONService.class);
				//bind(DiscoveryService.class).to(JSONDiscoveryService.class);
				bind(SchemaService.class).to(DeferredSchemaService.class);
				bind(SystemConfiguration.class).toInstance(configuration);
			}
		});
    }
    
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		injector=null;
	}
    
    //@Test
    public void testWildcardQueries() {
    	MetricSchemaRecordQuery msrq=new MetricSchemaRecordQuery(null,"S","M",null,null);
    	
    	_schemaService=injector.getInstance(DeferredSchemaService.class);
    	_schemaService.get(msrq, 10, 2);
    }
    
    @Test
    public void dd() {
    	MetricSchemaRecordQuery msrq=new MetricSchemaRecordQuery(null,"S","M",null,null);
    	_discoveryService=injector.getInstance(JSONDiscoveryService.class);
    	MetricQuery q=new MetricQuery("REDUCEDTEST.core.TYO.*", "IMPACTPOD", null, 1477094400L, 1477180800L);
    	_discoveryService.getMatchingQueries(q).forEach(i -> System.out.println(i.getScope()+":"+i.getMetric()));
    }
    
    
//    @Test
    public void loop() {
    	_discoveryService=injector.getInstance(JSONDiscoveryService.class);
    	Map<String, String> tags=new HashMap<String, String>();
    	tags.put("device","*-app*-*.ops.sfdc.net");
    	MetricQuery q=new MetricQuery("core.WAS.SP2.cs18", "SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode*.Last_1_Min_Avg", tags, 1477094400L, 1477180800L);
    	_discoveryService.getMatchingQueries(q).forEach(i -> System.out.println(i.getScope()+":"+i.getMetric()));
    }
    
//    @Test
    public void filterRecords() {
    	MetricSchemaRecordQuery msrq=new MetricSchemaRecordQuery(null,"S","M",null,null);
    	_discoveryService=injector.getInstance(JSONDiscoveryService.class);
    	MetricQuery q=new MetricQuery("REDUCEDTEST.core.TYO.*", "IMPACTPOD", null, 1477094400L, 1477180800L);
    	_discoveryService.filterRecords(null,"REDUCEDTEST.core.*","IMPACTPOD", null,null,100,1).forEach(i -> System.out.println(i.getScope()+":"+i.getMetric()));
    }
    
    @Test
    public void getUnique() {
    	_discoveryService=injector.getInstance(JSONDiscoveryService.class);
    	System.out.println(
    		_discoveryService.getUniqueRecords(null,"REDUCEDTEST.core.*", "IMPACTPOD", null,null,RecordType.SCOPE,100,10)
    	);
    }
}
/* Copyright (c) 2016, Salesforce.com, Inc.  All rights reserved. */
