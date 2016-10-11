package com.salesforce.dva.argus.service.metric.transform.plus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.salesforce.dva.argus.entity.Metric;

public final class DBRacNode {
	private String racNodeName;//The lable of this racnode: chi.na11.rac01
	private TimeSeries tsAPT;
	private TimeSeries tsACT;
	private TimeSeries tsCPU;
	
	private DBRacNode(){
	}
	
	public static DBRacNode getRac(String racNodeName){
		DBRacNode self=new DBRacNode();
		self.racNodeName=racNodeName;
		return self;
	}
	
	//Take app level apt and count, give a weighted APT for this RAC Node
	public void encapsulateAPT_APP(List<Metric> metricsAPTTime, List<Metric> metricsAPTCount){	
	}
}
