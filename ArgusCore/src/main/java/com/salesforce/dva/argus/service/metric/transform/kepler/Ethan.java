package com.salesforce.dva.argus.service.metric.transform.kepler;

import java.util.Properties;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.salesforce.dva.argus.service.DefaultService;
import com.salesforce.dva.argus.system.SystemConfiguration;

public class Ethan extends DefaultService implements EthanService{
	private final Provider<KeplerService> k;
	
	@Inject
	Ethan(SystemConfiguration config,Provider<KeplerService> k){
		super(config);
		this.k=k;
	}
	
	@aertoria(id = 140)
	public String getK(){
		return this.k.get().getName();
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Properties getServiceProperties() {
		// TODO Auto-generated method stub
		return null;
	}
}
