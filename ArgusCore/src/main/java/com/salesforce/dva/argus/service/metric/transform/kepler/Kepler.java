package com.salesforce.dva.argus.service.metric.transform.kepler;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class Kepler implements KeplerService{
	private Provider<A> a;
	
	@Inject
	public Kepler(Provider<A> a){
		this.a=a;
		//System.out.println("Kepler class has been called"+a.getClass());
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return this.a.get().getA();
	}
}
