package com.salesforce.dva.argus.service.metric.transform.plus;

import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.service.metric.transform.Transform;
import com.salesforce.dva.argus.service.metric.transform.plus.common.ComputationUtil;
import com.salesforce.dva.argus.service.metric.transform.plus.common.MetricConsumer;
import com.salesforce.dva.argus.service.metric.transform.plus.common.Pod;
import com.salesforce.dva.argus.service.metric.transform.plus.common.RenderableCORE;
import com.salesforce.dva.argus.service.metric.transform.plus.common.RenderableSCRT;
import com.salesforce.dva.argus.service.metric.transform.plus.common.SFDCPod;
import com.salesforce.dva.argus.service.metric.transform.plus.common.ReportRange;

public class HeimdallSCRTReducer implements Transform{
	@Inject
	private Provider<ComputationUtil> _computationUtil;
	
	@Inject
	private Provider<Pod> _pod;
	
	@Override
	public List<Metric> transform(List<Metric> metrics) {
		throw new RuntimeException("Not supported");
	}

	@Override
	public List<Metric> transform(List<Metric> metrics, List<String> constants) {
		assert(constants!=null&&constants.size()==2):"constants should has exactly one result";
		
		List<Metric> metricsLineup=_computationUtil.get().downsample("1m-avg", metrics);
		List<MetricConsumer> listConsumer=MetricConsumer.consumeMetrics(metricsLineup);
		final ReportRange reportRange=ReportRange.getReportRange(metricsLineup);
		reportRange.setActThreshold(Integer.valueOf(constants.get(0)));
		
		RenderableSCRT pod=_pod.get().getSCRTPod(listConsumer,reportRange);
		((SFDCPod) pod).inspect();
		
		switch(constants.get(1)){
		case "IMPACT":
			return pod.renderIMPACT();
		case "ACT":
			return pod.renderACT();
		case "AVA":
			return pod.renderAVA();
		}
		throw new RuntimeException("unsupported");
	}

	@Override
	public List<Metric> transform(List<Metric>... metrics) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getResultScopeName() {
		// TODO Auto-generated method stub
		return null;
	}
}
