/*
 * Copyright (c) 2016, Salesforce.com, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of Salesforce.com nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.salesforce.dva.argus.service.metric.transform.plus.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.salesforce.dva.argus.entity.Metric;

/**
 * Salesforce DBCLoud RacServer Object 
 * <b>represents</b>
 * a Oracle Rac Server
 * 
 * @author aertoria ethan.wang@salesforce.com
 */
@SuppressWarnings("serial")
public class RacServer implements Serializable{
	@Inject
	private Provider<ComputationUtil> _computationUtil;
	
	private static transient ReportRange reportRange;

	private String racServerAddress;
	private List<MetricConsumer> listAPTTimeAppLevel;
	private List<MetricConsumer> listAPTTrafficAppLevel;
	private List<MetricConsumer> listACTRacLevel;
	private List<MetricConsumer> listCPUSysRacLevel;
	private List<MetricConsumer> listCPUUserRacLevel;
	
	private List<Metric> weightedAPT;
	private List<Metric> weightedTraffic;
	private List<Metric> weightedACT;
	private List<Metric> weightedCPU;
	
	@Inject
	private RacServer(){
	}
	
	public RacServer getRacServer(String racServerAddress, ReportRange reportRange, List<MetricConsumer> consumers){
		assert(racServerAddress!=null && racServerAddress.length()>10):"racServerAddress is not valid";
		this.racServerAddress=racServerAddress;
		RacServer.reportRange=reportRange;
		
		/*Load apt,traffic,act,cpusys,cpuuser*/
		load(consumers);
		
//		//CACULATE TRAFFIC AND APT NO MATTER WHAT, if empty, leave zero, but can not be both empty other wise crash
//		caculatedWeightedTraffic();
//		caculatedWeightedAPT();
		
		/*Caculate weightedAPT, weightedTraffic*/
		if (this.listAPTTrafficAppLevel!=null&&this.listAPTTrafficAppLevel.size()>0){
			caculatedWeightedTraffic();
		}

		if (this.listAPTTimeAppLevel!=null&&this.listAPTTimeAppLevel.size()>0){
			caculatedWeightedAPT();
		}
		
		if (this.listACTRacLevel!=null&&this.listACTRacLevel.size()==1){
			caculateACT();
		}
		
		if ((this.listCPUSysRacLevel!=null&&this.listCPUSysRacLevel.size()==1)
			||(this.listCPUUserRacLevel!=null&&this.listCPUUserRacLevel.size()==1)){
			caculateCPU();
		}
		return this;
	}
		
	public void load(List<MetricConsumer> consumers){
		assert(consumers!=null&&consumers.size()>0):"MetricConsumers not valid";
		loadAPTTimeAppLevelFromConsumers(consumers);
		loadAPTTrafficAppLevelFromConsumers(consumers);
		loadACTRacLevelFromConsumers(consumers);
		loadCPUSysRacLevelFromConsumers(consumers);
		loadCPUUserRacLevelFromConsumers(consumers);
	}
	
	private void loadAPTTimeAppLevelFromConsumers(List<MetricConsumer> consumers){
		this.listAPTTimeAppLevel=consumers.stream()
										.filter(c -> c.getRacServerAddress().equals(this.racServerAddress))
										.filter(c -> c.getConsumerType().equals(MetricConsumer.ConsumerTypes.APT_TIME_APPLEVEL))
										.collect(Collectors.toList());
	}

	private void loadAPTTrafficAppLevelFromConsumers(List<MetricConsumer> consumers){
		this.listAPTTrafficAppLevel=consumers.stream()
										.filter(c -> c.getRacServerAddress().equals(this.racServerAddress))
										.filter(c -> c.getConsumerType().equals(MetricConsumer.ConsumerTypes.APT_TRAFFIC_APPLEVEL))
										.collect(Collectors.toList());
	}

	private void loadACTRacLevelFromConsumers(List<MetricConsumer> consumers){
		this.listACTRacLevel=consumers.stream()
									.filter(c -> c.getRacServerAddress().equals(this.racServerAddress))
									.filter(c -> c.getConsumerType().equals(MetricConsumer.ConsumerTypes.ACT_RACLEVEL))
									.collect(Collectors.toList());
	}
	
	private void loadCPUSysRacLevelFromConsumers(List<MetricConsumer> consumers){
		this.listCPUSysRacLevel=consumers.stream()
									.filter(c -> c.getRacServerAddress().equals(this.racServerAddress))
									.filter(c -> c.getConsumerType().equals(MetricConsumer.ConsumerTypes.CPU_SYS_RACLEVEL))
									.collect(Collectors.toList());
	}
	
	private void loadCPUUserRacLevelFromConsumers(List<MetricConsumer> consumers){
		this.listCPUUserRacLevel=consumers.stream()
									.filter(c -> c.getRacServerAddress().equals(this.racServerAddress))
									.filter(c -> c.getConsumerType().equals(MetricConsumer.ConsumerTypes.CPU_USER_RACLEVEL))
									.collect(Collectors.toList());
	}
	

	/**Caculate pod level Traffic**/
	public void caculatedWeightedTraffic(){
		this.weightedTraffic=loadWeightedTraffic();
		this.weightedTraffic.get(0).setMetric(this.racServerAddress);
		this.weightedTraffic.get(0).setTags(null);
	}
	
	/**Caculate pod level APT**/
	public void caculatedWeightedAPT(){
		List<Metric> scaledResult=caculateProduct();
		this.weightedAPT=loadProduct(scaledResult);
		this.weightedAPT.get(0).setMetric(this.racServerAddress);
		this.weightedAPT.get(0).setTags(null);
	}
	
	/**Caculate pod level ACT**/
	public void caculateACT(){
		assert(this.listACTRacLevel!=null&&this.listACTRacLevel.size()==1):"have be one and only one act per racnode";
		this.weightedACT=new ArrayList<Metric>();
		this.weightedACT.add(new Metric(this.listACTRacLevel.get(0).getSelfAsMetric()));
		this.weightedACT.get(0).setMetric(this.racServerAddress);
		this.weightedACT.get(0).setTags(null);
	}
	
	/**Caculate pod level CPU**/
	public void caculateCPU(){
		assert((this.listCPUSysRacLevel!=null&&this.listCPUSysRacLevel.size()==1)
				||(this.listCPUUserRacLevel!=null&&this.listCPUUserRacLevel.size()==1)):"have be one and only one CPU sys or user per racnode";
		this.weightedCPU=new ArrayList<Metric>();
		
		Metric CPUSys=null;
		if (this.listCPUSysRacLevel!=null&&this.listCPUSysRacLevel.size()==1){
			CPUSys=this.listCPUSysRacLevel.get(0).getSelfAsMetric();
		}
		
		Metric CPUUser=null;
		if (this.listCPUUserRacLevel!=null&&this.listCPUUserRacLevel.size()==1){
			CPUUser=this.listCPUUserRacLevel.get(0).getSelfAsMetric();
		}
		
		List<Metric> toBeSummed=Arrays.asList(CPUSys,CPUUser).stream()
															.filter(m -> m!=null&&m.getDatapoints().size()>0)
															.collect(Collectors.toList());
		this.weightedCPU=_computationUtil.get().sumWithUnion(toBeSummed);
		this.weightedCPU.get(0).setMetric(this.racServerAddress);
		this.weightedCPU.get(0).setTags(null);
	}
	
	private List<Metric> caculateProduct(){
		List<Metric> toBeMatchScaled = new ArrayList<Metric>();
		listAPTTimeAppLevel.forEach(c -> toBeMatchScaled.add(c.getSelfAsMetric()));
		listAPTTrafficAppLevel.forEach(c -> toBeMatchScaled.add(c.getSelfAsMetric()));
		
		if(toBeMatchScaled==null||toBeMatchScaled.size()==0){
			System.out.println("rac server:"+this.racServerAddress+listAPTTimeAppLevel+listAPTTrafficAppLevel);
			throw new RuntimeException("input of scale is empty!");
		}
		List<Metric> scaledResult=_computationUtil.get().scale_match(toBeMatchScaled);
		return scaledResult;
	}
	
	/**
	 * to extract traffic from product 
	 * @param scaledResult
	 * @return
	 */
	private List<Metric> loadWeightedTraffic(){
		//As divisor, first summed up, then remove zero datapoints
		List<Metric> divisor=listAPTTrafficAppLevel.stream().map(c -> c.getSelfAsMetric()).collect(Collectors.toList());
		List<Metric> divisorSUMED=_computationUtil.get().sumWithUnion(divisor);
		assert(divisorSUMED!=null&&divisorSUMED.size()==1):"divisorSUMED should only have one metric inside";
		return divisorSUMED;
	}
	
	/**
	 * to extract apt from product
	 * @param scaledResult
	 * @return
	 */
	private List<Metric> loadProduct(List<Metric> scaledResult){
		List<Metric> toBeDivided = new ArrayList<Metric>();
		scaledResult.forEach(m -> toBeDivided.add(m));
		
		//As divisor, first summed up, then remove zero datapoints
		List<Metric> divisorSUMED=loadWeightedTraffic();
		
		List<Metric> divisorCleared=divisorSUMED.stream().map(m->_computationUtil.get().removeZeroMetric(m)).collect(Collectors.toList());
		divisorCleared.forEach(m -> toBeDivided.add(m));
		//System.out.println("tobeDivided:"+toBeDivided);
		List<Metric> dividedResult=_computationUtil.get().divide(toBeDivided);
		//System.out.println(this.racServerAddress+"+"+dividedResult);
		assert(dividedResult!=null && dividedResult.size()==1):"division should have only one result";
		return dividedResult;
	}
	
	public void inspect(){
		System.out.println("\nInspect object RacServer\nName:\t\t\t"+this.racServerAddress);
		if(this.hasTraffic()){
			System.out.println("weightedTraffic:\t"+this.weightedTraffic.get(0).getDatapoints().size());
		}else{
			System.out.println("Traffic:\t\tNo Traffic provided");
		}
		
		if(this.hasAPT()){
			System.out.println("APTTimeAppLevel:\t"+this.listAPTTimeAppLevel.size());
		}else{
			System.out.println("weightedAPT:\t\tNo APT provided");
		}
		
		if(this.hasACT()){
			System.out.println("weightedACT:\t\t"+this.weightedACT.get(0).getDatapoints().size());
		}else{
			System.out.println("weightedACT:\t\tNo ACT provided");
		}
		
		if(this.hasCPU()){
			System.out.println("weightedCPU:\t\t"+this.weightedCPU.get(0).getDatapoints().size());
		}else{
			System.out.println("weightedCPU:\t\tNo CPU provided");
		}
		
		
	}

	/**getters**/
	public String getRacServerAddress(){
		final String racServerAddressPass=this.racServerAddress;
		return racServerAddressPass;
	}
	
	public static ReportRange getReportRange(){
		final ReportRange getReportRangePass=reportRange;
		return getReportRangePass;
	} 
	
	/**getImpactedMinHourly return a timeseries with count of impact min for each hour
	 * called by all ava related renderables and renderings.
	 * **/
	@SuppressWarnings("unchecked")
	public List<Metric> getImpactedMinHourly(){
		//APT
		List<Metric> impactedMin = new ArrayList<Metric>();
		
		if (hasAPT()){
			List<Metric> impactedMinAPT=getImpactedMinHourlyAPT(weightedAPT,null);
			impactedMin=_computationUtil.get().unionOR(impactedMin,impactedMinAPT);
		}
		//ACT
		if (hasACT()){
			List<Metric> impactedMinACT = getImapctedMinHourlyACT();
			impactedMin=_computationUtil.get().unionOR(impactedMin,impactedMinACT);
		}
		
		List<Metric> postMetric = _computationUtil.get().downsampleAndFill(reportRange, 60, "1h-count",  impactedMin);
		return Collections.unmodifiableList(postMetric);
	}
	
	public List<Metric> getImpactedMinHourlyByAPT(){
		//APT
		List<Metric> impactedMin=getImpactedMinHourlyAPT(weightedAPT,null);
		
		List<Metric> postMetric = _computationUtil.get().downsampleAndFill(reportRange, 60, "1h-count",  impactedMin);
		return Collections.unmodifiableList(postMetric);
	}
	
	public List<Metric> getImpactedMinHourlyByACT(){
		if(!hasACT()){
			throw new RuntimeException("No Act provided, you are not supposed to call this function");
		}
		List<Metric> impactedMinACT = getImapctedMinHourlyACT();
		List<Metric> postMetric = _computationUtil.get().downsampleAndFill(reportRange, 60, "1h-count",  impactedMinACT);
		return Collections.unmodifiableList(postMetric);
	}
	
	/**given RacServerLevel metric, return impactedMinmetric**/
	private List<Metric> getImpactedMinHourlyAPT(List<Metric> racLevelApt, List<Metric> racLevelCPU) {
		if (! hasAPT()){
			throw new RuntimeException("No APT provided, not legal to call this method");
		}
		return _computationUtil.get().detectAPT(racLevelApt, this.racServerAddress);
	}
	
	/**getImpactedMin by detecting ACT**/
	private List<Metric> getImapctedMinHourlyACT(){
		if (! hasACT()){
			throw new RuntimeException("No ACT provided, not legal to call this method");
		}
		assert(this.weightedACT!=null&&this.weightedACT.size()==1):"ACT has to be provided";
		List<Metric> ACT=Collections.unmodifiableList(Arrays.asList(new Metric(this.weightedACT.get(0))));
		List<Metric> ACTSLA=_computationUtil.get().detectACT(ACT, this.racServerAddress);
		return ACTSLA;
	}
	
	/**getAvaRateHourly return a timeseries reporting avaRate each hour**/
	public List<Metric> getAvaRateHourly(){
		List<Metric> availability=getWeightedTrafficCountHourly();
		List<Metric> availability_zeroRemoved=availability.stream().map(m->_computationUtil.get().removeZeroMetric(m)).collect(Collectors.toList());
		List<Metric> avaRateHourly=getImpactedMinHourly();
				
		List<Metric> toBeDivided = new ArrayList<Metric>();
		avaRateHourly.forEach(m -> toBeDivided.add(m));
		availability_zeroRemoved.forEach(m -> toBeDivided.add(m));
		
		List<Metric> dividedResult=_computationUtil.get().divide(toBeDivided);
		
		assert(dividedResult!=null):"dividedResult should be valid"; 
		List<Metric> filleddividedResult=_computationUtil.get().mergeZero(reportRange,60,dividedResult);

		List<Metric> negatedAvaRate=_computationUtil.get().negate(filleddividedResult);
		return Collections.unmodifiableList(negatedAvaRate);
	}
	
	public List<Metric> getRawAPTMinutely(){
		final Metric weightedAPTPass=new Metric(this.weightedAPT.get(0));
		return Collections.unmodifiableList(Arrays.asList(weightedAPTPass));
	}
	
	public List<Metric> getRawAPTHourly(){
		final List<Metric> weightedAPTPass=Collections.unmodifiableList(Arrays.asList(new Metric(this.weightedAPT.get(0))));
		final List<Metric> weightedTrafficPass=Collections.unmodifiableList(Arrays.asList(new Metric(this.weightedTraffic.get(0))));
		return _computationUtil.get().weightedByTraffic(weightedAPTPass, weightedTrafficPass, "1h-sum", this.reportRange);
	}
	
	public List<Metric> getRawACTMinutely(){
		if (!this.hasACT()){
			throw new RuntimeException("No ACT provided, not legal to call this method");
		}
		assert(this.hasACT()):"ACT has to be provided";
		final Metric weightedACTPass=new Metric(this.weightedACT.get(0));
		return Collections.unmodifiableList(Arrays.asList(weightedACTPass));
	}
	
	public List<Metric> getRawACTHourly(){
		if (!this.hasACT()){
			throw new RuntimeException("No ACT provided, not legal to call this method");
		}
		assert(this.hasACT()):"ACT has to be provided";
		assert(this.hasTraffic()):"Traffic has to be provided to get raw CPU hourly, because we use traffic to weight accorss hours";
		final List<Metric> weightedACTPass=Collections.unmodifiableList(Arrays.asList(new Metric(this.weightedACT.get(0))));
		final List<Metric> weightedTrafficPass=Collections.unmodifiableList(Arrays.asList(new Metric(this.weightedTraffic.get(0))));
		return _computationUtil.get().weightedByTraffic(weightedACTPass, weightedTrafficPass, "1h-sum", this.reportRange);
	}
	
	public List<Metric> getRawCPUMinutely(){
		if (!this.hasCPU()){
			throw new RuntimeException("No CPU provided, not legal to call this method");
		}
		assert(this.hasCPU()):"CPU has to be provided";
		final Metric weightedCPUPass=new Metric(this.weightedCPU.get(0));
		return Collections.unmodifiableList(Arrays.asList(weightedCPUPass));
	}
	
	public List<Metric> getRawCPUHourly(){
		if (!this.hasCPU()){
			throw new RuntimeException("No CPU provided, not legal to call this method");
		}
		assert(this.hasCPU()):"CPU has to be provided";
		assert(this.hasTraffic()):"Traffic has to be provided to get raw CPU hourly, because we use traffic to weight";
		final List<Metric> weightedCPUPass=Collections.unmodifiableList(Arrays.asList(new Metric(this.weightedCPU.get(0))));
		final List<Metric> weightedTrafficPass=Collections.unmodifiableList(Arrays.asList(new Metric(this.weightedTraffic.get(0))));
		return _computationUtil.get().weightedByTraffic(weightedCPUPass, weightedTrafficPass, "1h-sum", this.reportRange);
	}
	
	public List<Metric> getWeightedTrafficMinutely(){
		final Metric weightedTrafficPass=new Metric(this.weightedTraffic.get(0));
		List<Metric> weightedTrafficPassList=Collections.unmodifiableList(Arrays.asList(weightedTrafficPass));
		return weightedTrafficPassList;
	}
	
	public List<Metric> getWeightedTrafficCountHourly(){
		List<Metric> weightedTraffic=getWeightedTrafficMinutely();
		List<Metric> weightedTrafficDownsampled=_computationUtil.get().downsample("1h-count", weightedTraffic);
		return Collections.unmodifiableList(weightedTrafficDownsampled);
	}
	
	public List<Metric> getWeightedTrafficSumHourly(){
		List<Metric> weightedTraffic=getWeightedTrafficMinutely();
		List<Metric> weightedTrafficDownsampled=_computationUtil.get().downsample("1h-sum", weightedTraffic);
		return Collections.unmodifiableList(weightedTrafficDownsampled);
	}
	
	public boolean hasAPT(){
		return (this.weightedAPT==null || this.weightedAPT.size()==0)?false:true;
	}
	
	public boolean hasTraffic(){
		return (this.weightedTraffic==null || this.weightedTraffic.size()==0)?false:true;
	}
	
	public boolean hasACT(){
		return (this.weightedACT==null || this.weightedACT.size()==0)?false:true;
	}
	
	public boolean hasCPU(){
		return (this.weightedCPU==null || this.weightedCPU.size()==0)?false:true;
	}
	
	/**To honor the sequence of RacServer**/
	public static Comparator<RacServer> compareByName(){
		return (rac1,rac2) -> rac1.getRacServerAddress().compareTo(rac2.getRacServerAddress());
	}

}
