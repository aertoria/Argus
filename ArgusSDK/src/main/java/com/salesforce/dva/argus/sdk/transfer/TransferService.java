package com.salesforce.dva.argus.sdk.transfer;

import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.salesforce.dva.argus.sdk.ArgusService;
import com.salesforce.dva.argus.sdk.ArgusService.PutResult;
import com.salesforce.dva.argus.sdk.entity.Metric;


/**
 * Provides methods to transfer metrics.
 *
 * @author  aertoria (ethan.wang@salesforce.com)
 */
@SuppressWarnings("serial")
public class TransferService implements AutoCloseable, Serializable{
	private transient ArgusService _sourceSVC;
	private transient ArgusService _targetSVC;
	
	private TransferService(){}
	
	
	/**
     * Static factory method.
     *
     * @param  _sourceSVC. ArgusService. a dependecy needs to be injected.
     * 
     * @param  _targetSVC. ArgusService. a dependecy needs to be injected.
     */
	public static TransferService getTransferService(ArgusService _sourceSVC,ArgusService _targetSVC){
		TransferService self=new TransferService();
		self._sourceSVC=_sourceSVC;
		self._targetSVC=_targetSVC;
		return self;
	}
	
    /**
     * transfer metrics
     *
     * @param   expression  A expression of metrics
     *
     * @return  The JSON representation of the object.
     *
     * @throws  IOException  If a serialization error occurs.
     */
	public PutResult transfer(String expression) throws IOException{
		final List<Metric> metrics=readFromSource(Arrays.asList(expression));
		PutResult p=writeToTarget(metrics);
		System.out.println("\n\nRequest return:"+metrics.size()+" Transfer succeed: "+p.getSuccessCount()+" Failed:"+p.getFailCount()+p.getErrorMessages());
		return p;
	}
	
	public PutResult transfer(String src_expression, String tgt_scope, String tgt_metric) throws IOException {
		final List<Metric> metrics=readFromSource(Arrays.asList(src_expression));
		assert(metrics.size()==1):"result is not valid";
		
		Metric metric=new Metric();
		metric.setScope(tgt_scope);
		metric.setMetric(tgt_metric);
		metric.setDatapoints(metrics.get(0).getDatapoints());
		
		PutResult p=writeToTarget(Arrays.asList(metric));
		System.out.println("\n\nRequest return:"+metrics.size()+" Transfer succeed: "+p.getSuccessCount()+" Failed:"+p.getFailCount()+p.getErrorMessages());
		return p;
	}
	
	/**
	 * 
	 * @param src_expression
	 * @param tgt_scope
	 * @return
	 * @throws IOException
	 */
	public PutResult transfer(String src_expression, String tgt_scope) throws IOException {
		System.out.println("executing..."+src_expression);
		final List<Metric> metrics=readFromSource(Arrays.asList(src_expression));
		assert(metrics.size()>0):"result is not valid";
		List<Metric> metricsToLoad= metrics.stream()
										.map(m -> {
														Metric metric=new Metric();
														metric.setScope(tgt_scope);
														metric.setMetric(m.getMetric());
														metric.setDatapoints(m.getDatapoints());
														return metric;
													})					
										.collect(Collectors.toList());
		
		PutResult p=writeToTarget(metricsToLoad);
		System.out.println("\n\nRequest return:"+metrics.size()+" Transfer succeed: "+p.getSuccessCount()+" Failed:"+p.getFailCount()+p.getErrorMessages());
		return p;
	}

	public List<Metric> readFromSource(List<String> expressions) throws IOException{
		assert(expressions!=null && expressions.size()>0):"input not valid";
		final List<String> cleanExpressions=TransferService.expressionCleanUp(expressions);
		return this._sourceSVC.getMetricService().getMetrics(cleanExpressions);
	}
	
	public PutResult writeToTarget(List<Metric> metrics) throws IOException{
		assert(metrics!=null && metrics.size()>0):"input not valid";
		return this._targetSVC.getMetricService().putMetrics(metrics);
	}
	
	@SuppressWarnings("deprecation")
	private static List<String> expressionCleanUp(List<String> expressions){
		assert(expressions!=null && expressions.size()>0):"input not valid";
		List<String> r= expressions
				.stream().sequential()
				.map(e -> URLEncoder.encode(e))
				.collect(Collectors.toList());
		return r;
	}
	
	@Override
	public void close() throws IOException{
		_sourceSVC.close();
		_targetSVC.close();
	}
	
}
