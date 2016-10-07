package com.salesforce.dva.argus.service.metric.transform.kepler;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class keplerModule extends AbstractModule {
	@Override
	protected void configure(){
		bind(KeplerService.class).to(Kepler.class);
		install(new FactoryModuleBuilder().implement(EthanService.class,Ethan.class).build(EthanFactoryService.class));
	}

}
