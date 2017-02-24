package com.salesforce.dva.argus.service.metric.transform.plus.common;

/**
 * A SFDC POD
 * @author aertoria ethan.wang@salesforce.com
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
