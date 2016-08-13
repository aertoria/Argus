package com.salesforce.dva.argus.service.metric.transform.plus;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.salesforce.dva.argus.entity.Metric;
import com.sun.tools.javac.util.List;

public final class TimeSeries{
	private Map<Long, String> ts;
	private Metric m;
	private String podName;
	private String hostType;//either app or rac
	private String hostName;//rac03  app04 
	
	public Map<Long, String> getTs(){
		return this.ts;
	}
	public Metric getMetric(){
		return this.m;
	}
	
	private TimeSeries(){
	}
	
	public static TimeSeries getTimeSeries(TimeSeries timeSeries){
		TimeSeries self=new TimeSeries();
		self.ts=timeSeries.getTs();
		self.m=timeSeries.getMetric();
		return self;
	} 
	
	public static TimeSeries getTimeSeries(){
		TimeSeries self=new TimeSeries();
		return self;
	} 
	
	public static TimeSeries getTimeSeries(Map<Long, String> ts){
		assert(ts!=null):"have to be a valid ts";
		TimeSeries self=new TimeSeries();
		self.ts=ts;
		return self;
	} 
	
	public static TimeSeries getTimeSeries(Metric m){
		assert(m!=null&&m.getMetric()!=null&m.getDatapoints().size()>0):"have to be a valid not null Metric";
		TimeSeries self=new TimeSeries();
		self.m=m;
		self.ts=m.getDatapoints();
		return self;
	}

	public static TimeSeries getTimeSeries(List<Metric> l){
		assert(l!=null&&l.size()>1);
		return getTimeSeries(l.get(0));
	}
	
	//RealMethod Implementation
	
	//Return a new Timeseries object, with the same keyset as template, but filled up missing with zero
	public TimeSeries fillZero(TimeSeries template){
		assert(template!=null && template.getTs().size()>0):"template has to be a vaid tempate";
		Map<Long, String> newTS=new HashMap<Long,String>(this.getTs());
		assert(newTS.entrySet().stream()
							   .filter(k -> !template.getTs().keySet().contains(k))
							   .collect(Collectors.toSet())
							   .size() ==0
				):"The newTS time series has to be a subset of template";
		template.getTs().entrySet().forEach(e -> newTS.putIfAbsent(e.getKey(), String.valueOf(0)));
		return getTimeSeries(newTS);
	}
	
	//Return a new Timeseries object, with the same keyset as itself, but remote all zeros valye
	public TimeSeries remoteZero(){
		Map<Long, String> newTS=new HashMap<Long,String>();
		this.getTs().entrySet().stream()
								.filter(e -> e.getValue()!=null && Float.valueOf(e.getValue())>0f)
								.forEach(e -> newTS.put(e.getKey(), e.getValue()));
		return getTimeSeries(newTS);
	}
	
	
}
