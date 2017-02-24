package com.salesforce.dva.argus.service.metric.transform.plus.common;

import java.util.List;

import com.salesforce.dva.argus.entity.Metric;

/**
 * Aspect Defined as Renderable by Transform
 * **/
public interface RenderableCORE {
	List<Metric> renderIMPACT();
	List<Metric> renderIMPACTBYAPT();
	List<Metric> renderIMPACTBYACT();
	List<Metric> renderIMPACTPOD();
	List<Metric> renderIMPACTTOTAL();
	List<Metric> renderAPT();
	List<Metric> renderAPTPOD();
	List<Metric> renderTRAFFIC();
	List<Metric> renderTRAFFICPOD();
	List<Metric> renderACT();
	List<Metric> renderCPU();
	List<Metric> renderAVA();
	List<Metric> renderAVAPOD();
	List<Metric> renderAVATOTAL();
	List<Metric> renderTTMPOD();
	List<Metric> renderTTMTOTAL();
	List<Metric> renderCOLLECTED();
}
