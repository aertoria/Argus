package com.salesforce.dva.argus.service.metric.transform.plus.common;

/**
 * 
 * @author ethan.wang
 *
 */
public interface SFDCPod {
	String getPodAddress();
	void inspect();
	boolean hasAPT();
	boolean hasTraffic();
	boolean hasACT();
	boolean hasCPU();
}
