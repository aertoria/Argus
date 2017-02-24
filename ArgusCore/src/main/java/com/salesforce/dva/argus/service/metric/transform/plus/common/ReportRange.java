package com.salesforce.dva.argus.service.metric.transform.plus.common;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.salesforce.dva.argus.entity.Metric;

/**
 * 
 * @author ethan.wang
 *
 */
public class ReportRange {
	private final List<Long> range;
	
	private int actThreshold=150;
	
	/**
	 * 
	 * @param range
	 */
	private ReportRange(List<Long> range){
		this.range=range;
	}
	
	/**
	 * givien a list of metrics. set the report range to be the maximum and minimum of any metric inside
	 * @param metrics
	 * @return
	 */
	public static ReportRange getReportRange(List<Metric> metrics){
		assert(metrics!=null && metrics.size()>0):"start and end should not be null";
		
		Optional<Long> min=metrics.stream()
				   .flatMap(m -> m.getDatapoints().keySet().stream())
				   .collect(Collectors.minBy((k1,k2)->Long.compare(k1, k2)));
		Optional<Long> max=metrics.stream()
				   .flatMap(m -> m.getDatapoints().keySet().stream())
				   .collect(Collectors.maxBy((k1,k2)->Long.compare(k1, k2))); 
		assert(min.isPresent()&&max.isPresent()):"min value and max value should present";
		ReportRange self=new ReportRange(Arrays.asList(min.get(),max.get()));
		return self;		
	}
		
	public Long getStart(){
		return this.range.get(0);
	}
	
	public Long getEnd(){
		return this.range.get(1);
	}

	public int getActThreshold() {
		return actThreshold;
	}

	public void setActThreshold(int actThreshold) {
		this.actThreshold = actThreshold;
	}
}
