package com.salesforce.dva.argus.service.metric.transform.plus;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.salesforce.dva.argus.service.metric.transform.kepler.EthanFactoryService;
import com.salesforce.dva.argus.service.metric.transform.kepler.EthanService;
import com.salesforce.dva.argus.service.metric.transform.kepler.keplerModule;
import com.salesforce.dva.argus.system.SystemMain;
import com.salesforce.dva.argus.service.*;

public class EthanTest {
	@Inject
	Provider<ServiceFactory> f;
	
	@Test
	public void test() {
//		Injector injector= Guice.createInjector(new keplerModule());
//		EthanFactoryService f=injector.getInstance(EthanFactoryService.class);
//		EthanService e=f.create();
//		System.out.println(e.getK());
		
	}

}
