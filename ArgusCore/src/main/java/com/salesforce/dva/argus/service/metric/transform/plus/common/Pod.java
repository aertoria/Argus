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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.salesforce.dva.argus.entity.Metric;


/**
 * Salesforce DBCLoud dababase pod Object 
 * <b>represents</b>
 * a Oracle Rac Server
 * 
 * @author aertoria ethan.wang@salesforce.com
 */
@SuppressWarnings("serial")
public class Pod implements RenderableCORE, RenderableSCRT, Reportable, SFDCPod, Serializable{
	private List<RacServer> racServers;
	private String podAddress;
	private List<Metric> podAPT;
	private List<Metric> podACT;
	private List<Metric> podTraffic;
	
	@Inject
	private Provider<RacServer> _racServer;
	
	@Inject
	private Provider<ComputationUtil> _computationUtil;
	
	@Inject
	private Pod(){
	}
	
	/**instance factory**/
	public Pod getPod(List<MetricConsumer> metricConsumers,ReportRange reportRange){
		Set<RacServer> racServers = CreatAndloadCoreRacServer(metricConsumers, reportRange);
		return getPod(racServers);
	}
	
	/**
	 * Expecting rac server has both APT and TRAFFIC at minimum
	 * @param consumers
	 * @param reportRange
	 * @return
	 */
	private Set<RacServer> CreatAndloadCoreRacServer(List<MetricConsumer> consumers,ReportRange reportRange){
		Set<String> racServerAddresses=new HashSet<String>();
		
		if(! consumers.stream().anyMatch(
				  c -> c.getConsumerType().equals(MetricConsumer.ConsumerTypes.APT_TIME_APPLEVEL)
				  ||c.getConsumerType().equals(MetricConsumer.ConsumerTypes.APT_TRAFFIC_APPLEVEL))){
			throw new RuntimeException("Input matrics contains no APT Time or APT Traffic metric type");
		}
		
		consumers.stream()
				 .filter(c -> c.getConsumerType().equals(MetricConsumer.ConsumerTypes.APT_TIME_APPLEVEL)
						 ||c.getConsumerType().equals(MetricConsumer.ConsumerTypes.APT_TRAFFIC_APPLEVEL))
				 .forEach(c -> racServerAddresses.add(c.getRacServerAddress()));

		Set<RacServer> racServers = racServerAddresses.stream()
														.map(address -> _racServer.get().getRacServer(address,reportRange,consumers))
														.collect(Collectors.toSet());
		
		if (! racServers.stream().anyMatch(r -> r.hasAPT())){
			throw new RuntimeException("Input matrics contains no APT Time matric");
		}
		if (! racServers.stream().anyMatch(r -> r.hasTraffic())){
			throw new RuntimeException("Input matrics contains no APT Count matric");
		}

		return racServers.stream().filter(r -> r.hasAPT()&&r.hasTraffic()).collect(Collectors.toSet());
	}
	
	/**
	 * instance factory
	 * Core DB Pod, require both APT and TRAFFIC at minimum
	 */
	public Pod getPod(Set<RacServer> racServers){		
		assert(racServers!=null && racServers.size()>0):"racServers should be valid";
		List<RacServer> listOfRacServers =new ArrayList(racServers);
		listOfRacServers.sort(_racServer.get().compareByName());
		this.racServers=listOfRacServers;
		this.podAddress=generatePodAddress(listOfRacServers.get(0).getRacServerAddress());
				
		assert(hasAPT()&&hasTraffic()):"this pod should has both APT ans Traffic";
		this.podTraffic=caculateAndLoadPodTraffic();
		this.podAPT=caculateAndLoadPodAPT();
		
		if (hasACT()){
			this.podACT=caculateAndLoadPodACT();
		}
		return this;
	}
	
	/**instance factory
	 * SCRT DB Pod, require only ACT at minimum
	 */
	public Pod getSCRTPod(List<MetricConsumer> metricConsumers,ReportRange reportRange){
		Set<String> racServerAddresses=new HashSet<String>();
		if(! metricConsumers.stream().anyMatch(c -> c.getConsumerType().equals(MetricConsumer.ConsumerTypes.ACT_RACLEVEL))){
			throw new RuntimeException("Input consumers contains no ACT consumer type");
		}
		
		metricConsumers.stream()
			.filter(c -> c.getConsumerType().equals(MetricConsumer.ConsumerTypes.ACT_RACLEVEL))
			.forEach(c -> racServerAddresses.add(c.getRacServerAddress()));
		
		Set<RacServer> racServers = racServerAddresses.stream()
				.map(address -> _racServer.get().getRacServer(address, reportRange, metricConsumers))
				.collect(Collectors.toSet());
				
		assert(racServers!=null && racServers.size()>0):"racServers should be valid";
		List<RacServer> listOfRacServers =new ArrayList(racServers);
		listOfRacServers.sort(_racServer.get().compareByName());
		this.racServers=listOfRacServers;
		this.podAddress=generatePodAddress(listOfRacServers.get(0).getRacServerAddress());
				
		assert(hasACT()):"this pod should has ACT";
//		this.podACT=caculateAndLoadPodACT();//podACT is not possible to caculate since no traffic can be used to weight 
		return this;	
	}
	
	private String generatePodAddress(String racAddress){
		assert(racAddress!=null && racAddress.length()>10):"racAddress have to be valid";
		return racAddress.substring(0,racAddress.lastIndexOf("."));
	}

	public void inspect(){
		System.out.println("\n\nINSPECT object Pod\nPodName:\t\t**"+this.podAddress+"**");
		System.out.print("RacServer Included:\t");
		racServers.forEach(r -> System.out.print(r.getRacServerAddress()+", "));
		System.out.println();
		this.racServers.forEach(r -> r.inspect());
	}
	
	private List<Metric> caculateAndLoadPodAPT(){
		Optional<Metric> constructedProduct=this.racServers.stream()
				.filter(r -> r.hasAPT())
				.map(r -> _computationUtil.get().scale(r.getRawAPTMinutely(),r.getWeightedTrafficMinutely())
												.get(0)
					)
				.reduce((m1,m2)-> _computationUtil.get().sumWithUnion(Arrays.asList(m1,m2)).get(0));
		
		assert(constructedProduct.isPresent()):"accumulation result is not valid";
		assert(this.podTraffic!=null&&this.podTraffic.size()==1):"podAPT requires podTraffic";
		if(constructedProduct.isPresent()){
			List<Metric> toBeDivided = Arrays.asList(constructedProduct.get(),_computationUtil.get().removeZeroMetric(this.podTraffic.get(0)));
			List<Metric> dividedResult=_computationUtil.get().divide(toBeDivided);
			Metric m=dividedResult.get(0);
			m.setMetric(podAddress);
			return Collections.unmodifiableList(Arrays.asList(m));
		}
		throw new RuntimeException("Can not caculate podAPT");
	}
	
	private List<Metric> caculateAndLoadPodACT(){
		assert(this.racServers.stream().anyMatch(r -> r.hasACT())):"to call this method, at least one rac server of this pod must have act";		
		Optional<Metric> constructedProduct=this.racServers.stream()
				.filter(r->r.hasACT())
				.map(r -> _computationUtil.get().scale(r.getRawACTMinutely(),r.getWeightedTrafficMinutely())
												.get(0)
				)
				.reduce((m1,m2)-> _computationUtil.get().sumWithUnion(Arrays.asList(m1,m2)).get(0));
		
		assert(constructedProduct.isPresent()):"accumulation result is not valid";
		assert(this.podTraffic!=null&&this.podTraffic.size()==1):"podAPT requires podTraffic";
		if(constructedProduct.isPresent()){
			List<Metric> toBeDivided = Arrays.asList(constructedProduct.get(),_computationUtil.get().removeZeroMetric(this.podTraffic.get(0)));
			List<Metric> dividedResult=_computationUtil.get().divide(toBeDivided);
			Metric m=dividedResult.get(0);
			m.setMetric(podAddress);
			return Collections.unmodifiableList(Arrays.asList(m));
		}
		throw new RuntimeException("Can not caculate podAPT");
	}
	
	private List<Metric> caculateAndLoadPodTraffic(){
		Optional<Metric> podTraffic=this.racServers.stream()
				.map(r -> r.getWeightedTrafficMinutely().get(0))
				.reduce((m1,m2) ->_computationUtil.get().sumWithUnion(Arrays.asList(m1,m2)).get(0));
		assert(podTraffic.isPresent()):"accumulation result ise not valid";
		if (podTraffic.isPresent()){
			Metric m=podTraffic.get();
			m.setMetric(podAddress);
			return Collections.unmodifiableList(Arrays.asList(m));
		}
		return null;
	}
	
	private List<Metric> caculateAVAPOD(){
		Optional<Metric> constructedProduct=this.racServers.stream()
				.map(r -> _computationUtil.get().scale(r.getAvaRateHourly(),r.getWeightedTrafficCountHourly())
												.get(0)
					)
				.reduce((m1,m2)-> _computationUtil.get().sumWithUnion(Arrays.asList(m1,m2)).get(0));
		Optional<Metric> constructedDivisor=this.racServers.stream()
						.map(r -> r.getWeightedTrafficCountHourly().get(0))
						.reduce((m1,m2) ->_computationUtil.get().sumWithUnion(Arrays.asList(m1,m2)).get(0));
		assert(constructedProduct.isPresent()&&constructedDivisor.isPresent()):"accumulation result is not valid";
		if(constructedProduct.isPresent()&&constructedDivisor.isPresent()){
			List<Metric> toBeDivided = Arrays.asList(constructedProduct.get(),_computationUtil.get().removeZeroMetric(constructedDivisor.get()));			
			List<Metric> dividedResult=_computationUtil.get().divide(toBeDivided);
			Metric m=dividedResult.get(0);
			m.setMetric(podAddress);
			return Collections.unmodifiableList(Arrays.asList(m));
		}
		throw new RuntimeException("Not a single result");
	}
	
	/**Renderable**/	
	@Override
	public List<Metric> renderIMPACT() {		
		List<Metric> constructedResult=this.racServers.stream()
													  .map(r -> r.getImpactedMinHourly().get(0))
													  .collect(Collectors.toList());
		return Collections.unmodifiableList(constructedResult);
	}
	
	@Override
	public List<Metric> renderCOLLECTED(){
		List<Metric> constructedResult=this.racServers.stream()
						  .map(r -> r.getCollectedMinBasedOnImpactHourly().get(0))
						  .collect(Collectors.toList());
		return Collections.unmodifiableList(constructedResult);
	}
	
	@Override
	public List<Metric> renderIMPACTBYAPT() {
		List<Metric> constructedResult=this.racServers.stream()
													  .map(r -> r.getImpactedMinHourlyByAPT().get(0))
													  .collect(Collectors.toList());
		return Collections.unmodifiableList(constructedResult);
	}
	
	@Override
	public List<Metric> renderIMPACTBYACT() {
		List<Metric> constructedResult=this.racServers.stream()
													  .filter(r -> r.hasACT())
													  .map(r -> r.getImpactedMinHourlyByACT().get(0))
													  .collect(Collectors.toList());
		return Collections.unmodifiableList(constructedResult);
	}
	
	@Override
	public List<Metric> renderIMPACTPOD() {
		Optional<Metric> constructedResult=this.racServers.stream()
										  .map(r -> r.getImpactedMinHourly().get(0))
										  .reduce((m1,m2) -> _computationUtil.get().sumWithUnion(Arrays.asList(m1,m2)).get(0));
		if(constructedResult.isPresent()){
			Metric m=constructedResult.get();
			m.setMetric(podAddress);
			return Collections.unmodifiableList(Arrays.asList(m));
		}
		throw new RuntimeException("Not a single result");
	}
	
	/**
	 * reduce impact by apt across different rac
	 * @return
	 */
	private List<Metric> renderIMPACTBYAPTPOD(){
		Optional<Metric> constructedResult=this.racServers.stream()
				  						.map(r -> r.getImpactedMinHourlyByAPT().get(0))
				  						.reduce((m1,m2) -> _computationUtil.get().sumWithUnion(Arrays.asList(m1,m2)).get(0));
		if(constructedResult.isPresent()){
			Metric m=constructedResult.get();
			m.setMetric(podAddress);
			return Collections.unmodifiableList(Arrays.asList(m));
		}
		throw new RuntimeException("Not a single result");
	}
	
	/**
	 * reduce impact by apt across different rac
	 * @return
	 */
	private List<Metric> renderIMPACTBYACTPOD(){
		Optional<Metric> constructedResult=this.racServers.stream()
										.filter(r -> r.hasACT())
				  						.map(r -> r.getImpactedMinHourlyByACT().get(0))
				  						.reduce((m1,m2) -> _computationUtil.get().sumWithUnion(Arrays.asList(m1,m2)).get(0));
		if(constructedResult.isPresent()){
			Metric m=constructedResult.get();
			m.setMetric(podAddress);
			return Collections.unmodifiableList(Arrays.asList(m));
		}
		throw new RuntimeException("Not a single result");
	}
	
	@Override
	public List<Metric> renderIMPACTTOTAL() {
		List<Metric> IMPACTPOD=renderIMPACTPOD();
		List<Metric> IMPACTTOTAL = _computationUtil.get().downsample("100d-sum", IMPACTPOD,RacServer.getReportRange().getStart());
		return IMPACTTOTAL;
	}
	
	public List<Metric> renderIMPACTBYAPTTOTAL() {
		List<Metric> IMPACTAPT=renderIMPACTBYAPTPOD();
		List<Metric> IMPACTTOTAL = _computationUtil.get().downsample("100d-sum", IMPACTAPT,RacServer.getReportRange().getStart());
		return IMPACTTOTAL;
	}
	
	public List<Metric> renderIMPACTBYACTTOTAL() {
		List<Metric> IMPACTACT=renderIMPACTBYACTPOD();
		List<Metric> IMPACTTOTAL = _computationUtil.get().downsample("100d-sum", IMPACTACT,RacServer.getReportRange().getStart());
		return IMPACTTOTAL;
	}
	
	public List<Metric> renderCollectedMinPOD(){
		Optional<Metric> constructedResult=this.racServers.stream()
										  .map(r -> r.getWeightedTrafficCountHourly().get(0))
										  .reduce((m1,m2) -> _computationUtil.get().sumWithUnion(Arrays.asList(m1,m2)).get(0));
		if(constructedResult.isPresent()){
			Metric m=constructedResult.get();
			m.setMetric(podAddress);
			return Collections.unmodifiableList(Arrays.asList(m));
		}
		throw new RuntimeException("Not a single result");
	} 
	
	@Override
	public List<Metric> renderAPT() {
		List<Metric> constructedResult=this.racServers.stream()
				  									  .map(r -> r.getRawAPTMinutely().get(0))
				  									  .collect(Collectors.toList());
		return Collections.unmodifiableList(constructedResult);
	}
	
	@Override
	public List<Metric> renderAPTPOD(){
		return Collections.unmodifiableList(this.podAPT);
	}
	
	@Override
	public List<Metric> renderTRAFFIC() {
		List<Metric> constructedResult=this.racServers.stream()
				  .map(r -> r.getWeightedTrafficMinutely().get(0))
				  .collect(Collectors.toList());
		return Collections.unmodifiableList(constructedResult);
	}
	
	@Override
	public List<Metric> renderTRAFFICPOD() {
		Optional<Metric> constructedResult=this.racServers.stream()
				  .map(r -> r.getWeightedTrafficSumHourly().get(0))
				  .reduce((m1,m2) -> _computationUtil.get().sumWithUnion(Arrays.asList(m1,m2)).get(0));
		if(constructedResult.isPresent()){
			Metric m=constructedResult.get();
			m.setMetric(podAddress);
			return Collections.unmodifiableList(Arrays.asList(m));
		}
		throw new RuntimeException("Not a single result");
	}
	
	@Override
	public List<Metric> renderACT() {
		List<Metric> constructedResult=this.racServers.stream()
										  .filter(r -> r.hasACT())
										  .map(r -> r.getRawACTMinutely().get(0))
										  .collect(Collectors.toList());
		return Collections.unmodifiableList(constructedResult);
	}
	
	@Override
	public List<Metric> renderCPU() {
		List<Metric> constructedResult=this.racServers.stream()
										  .filter(r -> r.hasCPU())
										  .map(r -> r.getRawCPUMinutely().get(0))
										  .collect(Collectors.toList());
		return Collections.unmodifiableList(constructedResult);
	}
	
	@Override
	public List<Metric> renderAVA(){
		List<Metric> constructedResult=this.racServers.stream()
										   .map(r -> r.getAvaRateHourly().get(0))
										   .collect(Collectors.toList());
		return Collections.unmodifiableList(constructedResult);
	}
	
	@Override
	public List<Metric> renderAVAPOD(){
		return caculateAVAPOD();
	}
	
	@Override
	public List<Metric> renderAVATOTAL(){
		final List<Metric> dataRecievedCount=_computationUtil.get().downsample("1h-count", podTraffic);
		final List<Metric> dataRecievedCountFilled=_computationUtil.get().mergeZero(RacServer.getReportRange(),60,dataRecievedCount);
		
		final List<Metric> podLevelAVA=caculateAVAPOD();
		final List<Metric> product=_computationUtil.get().scale(Arrays.asList(podLevelAVA.get(0),dataRecievedCountFilled.get(0)));
		final List<Metric> productDownsampled=_computationUtil.get().downsample("100d-sum", product, RacServer.getReportRange().getStart());
		final List<Metric> weightDownsampled=_computationUtil.get().downsample("100d-sum", dataRecievedCountFilled, RacServer.getReportRange().getStart());
		assert(weightDownsampled.get(0).getDatapoints().size()==1):"downsampled to one result";
		final List<Metric> avaDownsampled=_computationUtil.get().divide(Arrays.asList(productDownsampled.get(0),weightDownsampled.get(0)));
		
		return Collections.unmodifiableList(avaDownsampled);
	}
	
	private List<Metric> renderAvailableTOTAL(){
		Metric m=new Metric("SUM", "SinglePoint");
		Map<Long,String> datapoints=new HashMap<Long,String>();
		datapoints.put(RacServer.getReportRange().getStart(), String.valueOf(countAvaiableMin()));
		m.setDatapoints(datapoints);
		return Collections.unmodifiableList(Arrays.asList(m));
	}
	
	private Long countAvaiableMin(){
		Long result=this.racServers.stream()
				.map(r -> r.getWeightedTrafficMinutely().get(0).getDatapoints().entrySet())
				.flatMap(sets -> sets.stream())
				.collect(Collectors.counting());
		return result;
	}
	
	/**Reportable**/
	@Override
	public List<Metric> reportPOD() {		
		List<Metric> reportPod=reportPODLevel();
		List<Metric> result=reportPod.stream()
						.map(m -> _computationUtil.get().downsample("1h-avg", Arrays.asList(m)).get(0))
						.collect(Collectors.toList());
		return result;
	}
	
	private List<Metric> reportPODLevel(){
		List<Metric> renderAPTPOD=renderAPTPOD();
		renderAPTPOD.get(0).setMetric("PodLevelAPT");
		List<Metric> renderIMPACTPOD=renderIMPACTPOD();
		renderIMPACTPOD.get(0).setMetric("ImpactedMin");
		List<Metric> renderAVAPOD=renderAVAPOD();
		renderAVAPOD.get(0).setMetric("Availability"); 
		List<Metric> renderTTMPOD=renderTTMPOD();
		renderTTMPOD.get(0).setMetric("TTM");
		List<Metric> renderCollectedMin=renderCollectedMinPOD();
		renderCollectedMin.get(0).setMetric("CollectedMin");
		List<Metric> renderTraffic=renderTRAFFICPOD();
		renderTraffic.get(0).setMetric("Traffic");
		
		List<Metric> reportPod=new ArrayList<Metric>();
		reportPod.addAll(renderAPTPOD);
		reportPod.addAll(renderIMPACTPOD);
		reportPod.addAll(renderAVAPOD);
		reportPod.addAll(renderTTMPOD);
		reportPod.addAll(renderCollectedMin);
		reportPod.addAll(renderTraffic);
		return Collections.unmodifiableList(reportPod);
	}
	
	@Override
	public List<Metric> reportRAC() {
		List<Metric> reportRAC=new ArrayList<Metric>();	
		List<Metric> renderAPT=renderAPT();
		reportRAC.addAll(_computationUtil.get().reNameScope(renderAPT, "APT"));
		List<Metric> renderACT=renderACT();
		reportRAC.addAll(_computationUtil.get().reNameScope(renderACT, "ACT"));
		List<Metric> renderCPU=renderCPU();
		reportRAC.addAll(_computationUtil.get().reNameScope(renderCPU, "CPU"));
		List<Metric> renderTRAFFIC=renderTRAFFIC();
		reportRAC.addAll(_computationUtil.get().reNameScope(renderTRAFFIC, "Traffic"));
		
		return Collections.unmodifiableList(reportRAC);
	}
	
	@Override
	public List<Metric> reportRACHOUR() {
		List<Metric> reportRACHOUR=new ArrayList<Metric>();	
		reportRACHOUR.addAll(containRACHOUR(r -> r.getRawAPTHourly().get(0), r -> true, "APT"));
		reportRACHOUR.addAll(containRACHOUR(r -> r.getRawACTHourly().get(0), r -> r.hasACT(), "ACT"));
		reportRACHOUR.addAll(containRACHOUR(r -> r.getRawCPUHourly().get(0), r -> r.hasCPU(), "CPU"));
		reportRACHOUR.addAll(containRACHOUR(r -> r.getWeightedTrafficSumHourly().get(0), r -> true, "Traffic"));
		reportRACHOUR.addAll(containRACHOUR(r -> r.getAvaRateHourly().get(0), r -> true, "AVA"));
		reportRACHOUR.addAll(containRACHOUR(r -> r.getImpactedMinHourly().get(0), r -> true, "ImpactedMin"));
		reportRACHOUR.addAll(containRACHOUR(r -> r.getImpactedMinHourlyByAPT().get(0), r -> true, "ImpactedMinByAPT"));
		reportRACHOUR.addAll(containRACHOUR(r -> r.getImpactedMinHourlyByACT().get(0), r -> r.hasACT(), "ImpactedMinByACT"));
		reportRACHOUR.addAll(containRACHOUR(r -> r.getWeightedTrafficCountHourly().get(0), r -> true, "CollectedMin"));
		return Collections.unmodifiableList(reportRACHOUR);
	}
	
	private List<Metric> containRACHOUR(java.util.function.Function<RacServer, Metric> gettingMetric
										,java.util.function.Predicate<RacServer> filteringMetric
										,String metricName){
		List<Metric> constructedMetrics=this.racServers.stream()
									.filter(filteringMetric)
									.map(gettingMetric)
									.collect(Collectors.toList());
		List<Metric> returningMetric=_computationUtil.get().reNameScope(constructedMetrics, metricName);
		return Collections.unmodifiableList(returningMetric);
	}
	
	@Override
	public List<Metric> reportTOTAL() {
		List<Metric> reportTotal=new ArrayList<Metric>();
		
		List<Metric> renderIMPACTTOTAL=renderIMPACTTOTAL();
		renderIMPACTTOTAL.get(0).setMetric("ImpactedMin");
		reportTotal.addAll(renderIMPACTTOTAL);
		
		List<Metric> renderIMPACTTOTALBYAPT=renderIMPACTBYAPTTOTAL();
		renderIMPACTTOTALBYAPT.get(0).setMetric("ImpactedMinByAPT");
		reportTotal.addAll(renderIMPACTTOTALBYAPT);
		
		if(this.hasACT()){
			List<Metric> renderIMPACTTOTALBYACT=renderIMPACTBYACTTOTAL();
			renderIMPACTTOTALBYACT.get(0).setMetric("ImpactedMinByACT");
			reportTotal.addAll(renderIMPACTTOTALBYACT);
		}
		
		List<Metric> renderAVATOTAL=renderAVATOTAL();
		renderAVATOTAL.get(0).setMetric("Availability");
		reportTotal.addAll(renderAVATOTAL);
		
		List<Metric> renderAvailableTOTAL=renderAvailableTOTAL();
		renderAvailableTOTAL.get(0).setMetric("AvailableMin");
		reportTotal.addAll(renderAvailableTOTAL);
		
		List<Metric> renderTTMTOTAL=renderTTMTOTAL();
		renderTTMTOTAL.get(0).setMetric("TTM");
		reportTotal.addAll(renderTTMTOTAL);
		
		return Collections.unmodifiableList(reportTotal);
	}
	
	/** TTM FORMULAR Following...
	 * 			Version: SFDC_HEIMDALL_SEP2.001239x09F
	 * 			Author: Decided on Meeting Sep 14th 2016
	 * F1:(UNIONED_RAC_LEVEL_APT or UNIONED_RAC_LEVEL_ACT)
	 * F2:(WEIGHTED POD_LEVEL_APT or POD_LEVEL_ACT)
	 * F3:(RAC trigger1 OR RAC trigger2)
	 * TTM counted iif:   F1 OR F2 OR (F1 AND F3)  therefore dropping f3
	 * **/
	@Override
	public List<Metric> renderTTMPOD(){
		/*F1-UNIONED RAC LEVEL APT or ACT*/
		List<Metric> f1SLA=getUNIONED_RACS_LEVEL_SLA();
		
		/*F2-WEIGHTED POD LEVEL APT or ACT*/
		List<Metric> f2SLA=getPOD_LEVEL_SLA();
		
//		/*F3-TRGGER1 CPU*/
//		List<Metric> listRacLevelSLA_CPU=getUNIONED_RACS_LEVEL_SLA_CPU();
//		List<Metric> f3SLA=_computationUtil.get().unionAND(f1SLA,listRacLevelSLA_CPU);
//		
		/*F1 or F2 RELATIONSHIP*/
		List<Metric> TTMSLA=_computationUtil.get().unionOR(f1SLA,f2SLA);
		
		TTMSLA.get(0).setMetric(this.podAddress);
		List<Metric> TTM=_computationUtil.get().downsample("1h-count", TTMSLA);
		List<Metric> filledTTM=_computationUtil.get().mergeZero(RacServer.getReportRange(),60,TTM);
		return Collections.unmodifiableList(Arrays.asList(new Metric(filledTTM.get(0))));
	}
	
	@Override
	public List<Metric> renderTTMTOTAL() {
		List<Metric> TTMPOD=renderTTMPOD();
		List<Metric> TTMPODTOTAL = _computationUtil.get().downsample("100d-sum", TTMPOD, RacServer.getReportRange().getStart());
		return TTMPODTOTAL;
	}
	
	private List<Metric> getPOD_LEVEL_SLA(){
		List<Metric> podLevelAPT_SLA=_computationUtil.get().detectAPT(this.podAPT, this.podAddress);
		if (hasACT()){
			List<Metric> podLevelACT_SLA=_computationUtil.get().detectACT(this.podACT, this.podAddress);
			List<Metric> podLevelSLA=_computationUtil.get().unionOR(podLevelAPT_SLA,podLevelACT_SLA);
			return Collections.unmodifiableList(podLevelSLA);
		}
		List<Metric> podLevelSLA=_computationUtil.get().unionOR(podLevelAPT_SLA);
		return Collections.unmodifiableList(podLevelSLA);
	}
	
	private List<Metric> getUNIONED_RACS_LEVEL_SLA(){
		List<Metric> racLevelAPT_SLAList=this.racServers.stream()
				.map(r -> _computationUtil.get().detectAPT(r.getRawAPTMinutely(), this.podAddress).get(0))
				.collect(Collectors.toList());

		List<Metric> racLevelACT_SLAList=this.racServers.stream()
				.filter(r -> r.hasACT())
				.map(r -> _computationUtil.get().detectACT(r.getRawACTMinutely(), this.podAddress).get(0))
				.collect(Collectors.toList());	
		List<Metric> listRacLevelSLA=_computationUtil.get().unionOR(racLevelAPT_SLAList,racLevelACT_SLAList);
		return Collections.unmodifiableList(listRacLevelSLA);
	}
	
	/**getters**/
	@Override
	public String getPodAddress(){
		return this.podAddress;
	}
	
	@Override
	public boolean hasAPT(){
		return this.racServers.stream().anyMatch(r -> r.hasAPT());
	}
	
	@Override
	public boolean hasTraffic(){
		return this.racServers.stream().anyMatch(r -> r.hasTraffic());
	}
	
	@Override
	public boolean hasACT() {
		return this.racServers.stream().anyMatch(r -> r.hasACT());
	}
	
	@Override
	public boolean hasCPU() {
		return this.racServers.stream().anyMatch(r -> r.hasCPU());
	}

}
