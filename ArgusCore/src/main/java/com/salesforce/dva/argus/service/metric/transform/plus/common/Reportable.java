package com.salesforce.dva.argus.service.metric.transform.plus.common;

import java.util.List;

import com.salesforce.dva.argus.entity.Metric;

/**
 * 
 * @author ethan.wang
 *
 */
public interface Reportable {
	//REPORT IMPACT. APT. ACT, CPU, TRAFFIC
	List<Metric> reportRAC();
	
	//REPORT AVA,APT,IMPACT,Traffic,ACT,CPU
	List<Metric> reportRACHOUR();
	
	//REPORT PODLEVL APT. IMPACT. AVA. TTM
	List<Metric> reportPOD();
	
	//REPORT AVATOTAL, AvailbleMin, ImpactedMin, TTM
	List<Metric> reportTOTAL();
}
