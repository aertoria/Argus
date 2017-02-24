package com.salesforce.dva.argus.service.metric.transform.plus.common;

import java.util.List;

import com.salesforce.dva.argus.entity.Metric;

/**
 * Aspect Defined as Renderable by Transform
 * **/
public interface RenderableSCRT {
	List<Metric> renderIMPACT();
	List<Metric> renderACT();
	List<Metric> renderAVA();
	List<Metric> renderCOLLECTED();
}
