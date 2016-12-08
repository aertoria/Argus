 package com.salesforce.dva.argus.service.metric.transform.plus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.management.RuntimeErrorException;

import org.eclipse.persistence.internal.jpa.parsing.UnaryMinus;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.service.metric.transform.MetricDistiller;
import com.salesforce.dva.argus.service.metric.transform.Transform;
import com.salesforce.dva.argus.service.metric.transform.TransformFactory;
import com.salesforce.dva.argus.service.metric.transform.TransformFactory.Function;

/**
 * Immutable system configuration information.
 *
 * @author  aertoria (ethan.wang@salesforce.com)
 */
public class HeimdallMetricReducer implements Transform{
	@Inject
	private Provider<ComputationUtil> _computationUtil;

	@Inject
	private Provider<Pod> _pod;
	
	@Override
	public List<Metric> transform(List<Metric> metrics, List<String> constants) {
		assert(constants!=null&&constants.size()==1):"constants should has exactly one result";
		//align up
		List<Metric> metricsLineup=_computationUtil.get().downsample("1m-avg", metrics);
		List<Metric> metricsLineupInHour=_computationUtil.get().downsample("1h-avg", metrics);
		final ReportRange reportRange=ReportRange.getReportRange(metricsLineupInHour);
		
		List<MetricConsumer> listConsumer=consumeMetrics(metricsLineup);
		
		//listConsumer.forEach(c->c.inspect());
		Renderable pod=_pod.get().getPod(listConsumer,reportRange);
		((SFDCPod) pod).inspect();
		
		switch(constants.get(0)){
		case "POD":
			return render(()->((Reportable) pod).reportPOD());
		case "RAC":
			return render(()->((Reportable) pod).reportRAC());
		case "RACHOUR":
			return render(()->((Reportable) pod).reportRACHOUR());
		case "TOTAL":
			return render(()->((Reportable) pod).reportTOTAL());
		}
		
		switch(constants.get(0)){
		case "APT":
			return render(()->pod.renderAPT());
		case "APTPOD":
			return render(()->pod.renderAPTPOD());
		case "TRAFFIC":
			return render(()->pod.renderTRAFFIC());
		case "TRAFFICPOD":
			return render(()->pod.renderTRAFFICPOD());
		case "ACT":
			return render(()->pod.renderACT());
		case "CPU":
			return render(()->pod.renderCPU());
		case "IMPACT":
			return render(()->pod.renderIMPACT());
		case "IMPACTBYAPT":
			return render(()->pod.renderIMPACTBYAPT());
		case "IMPACTBYACT":
			return render(()->pod.renderIMPACTBYACT());
		case "IMPACTPOD":
			return render(()->pod.renderIMPACTPOD());
		case "IMPACTTOTAL":
			return render(()->pod.renderIMPACTTOTAL());
		case "AVA":
			return render(()->pod.renderAVA());
		case "AVAPOD":
			return render(()->pod.renderAVAPOD());
		case "AVATOTAL":
			return render(()->pod.renderAVATOTAL());
		case "TTMPOD":
			return render(()->pod.renderTTMPOD());
		case "TTMTOTAL":
			return render(()->pod.renderTTMTOTAL());
		}
		throw new RuntimeException("unsupported");
	}
	
	private List<Metric> render(java.util.function.Supplier<List<Metric>> renderable){
		System.out.println("\n**PRIVATE TRANSFORM**Heimdall Reducer");
		return renderable.get();
	}
	
	@Override
	public List<Metric> transform(List<Metric> metrics) {		
		throw new RuntimeException("Not supported");
	}
	
	private List<MetricConsumer> consumeMetrics(List<Metric> metrics){
		assert(metrics != null):"metrics input can not be null";
		List<MetricConsumer> listConsumers=new ArrayList<MetricConsumer>();
		listConsumers=metrics.stream()
				.map(m -> MetricConsumer.getMetricConsumer(m))
				.collect(Collectors.toList());
		return listConsumers;	
	}
		
	@Override
	public List<Metric> transform(List<Metric>... metrics) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getResultScopeName() {
		return TransformFactory.Function.HEIMDALL.name();
	}
}

/**Aspect Defined as Renderable by Transform**/
interface Renderable{
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
}

/**Aspect Defined as Reported by Transform**/
interface Reportable{
	List<Metric> reportRAC();	//REPORT IMPACT. APT. ACT, CPU, TRAFFIC
	List<Metric> reportRACHOUR();//REPORT AVA,APT,IMPACT,Traffic,ACT,CPU
	List<Metric> reportPOD();	//REPORT PODLEVL APT. IMPACT. AVA. TTM
	List<Metric> reportTOTAL();	//REPORT AVATOTAL, AvailbleMin, ImpactedMin, TTM
}

/**Aspect Defined as SFDCPod**/
interface SFDCPod{
	String getPodAddress();
	void inspect();
	boolean hasACT();
	boolean hasCPU();
}

/**Pod implementation**/
@SuppressWarnings("serial")
final class Pod implements Renderable, Reportable, SFDCPod, Serializable{
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
		Set<RacServer> racServers = CreatAndloadRacServer(metricConsumers, reportRange);
		return getPod(racServers);
	}
	
	private Set<RacServer> CreatAndloadRacServer(List<MetricConsumer> consumers,ReportRange reportRange){
		Set<String> racServerAddresses=new HashSet<String>();
		consumers.stream()
				 .filter(c -> c.getConsumerType().equals(MetricConsumer.ConsumerTypes.APT_TIME_APPLEVEL)||c.getConsumerType().equals(MetricConsumer.ConsumerTypes.APT_TRAFFIC_APPLEVEL))
				 .forEach(c -> racServerAddresses.add(c.getRacServerAddress()));
		//System.out.println("creatAndloadRacServer"+racServerAddresses);
		Set<RacServer> racServers = racServerAddresses.stream()
														.map(address -> _racServer.get().getRacServer(address,reportRange,consumers))
														.collect(Collectors.toSet());
		return racServers;
	}
	
	/**instance factory**/
	public Pod getPod(Set<RacServer> racServers){
		assert(racServers!=null && racServers.size()>0):"racServers should be valid";
		List<RacServer> listOfRacServers =new ArrayList(racServers);
		listOfRacServers.sort(_racServer.get().compareByName());
		this.racServers=listOfRacServers;
		this.podAddress=generatePodAddress(listOfRacServers.get(0).getRacServerAddress());
		this.podTraffic=caculateAndLoadPodTraffic();
		this.podAPT=caculateAndLoadPodAPT();
		if (hasACT()){
			this.podACT=caculateAndLoadPodACT();
		}
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
	public boolean hasACT() {
		return this.racServers.stream().anyMatch(r -> r.hasACT());
	}
	
	@Override
	public boolean hasCPU() {
		return this.racServers.stream().anyMatch(r -> r.hasCPU());
	}

}


/**
 * Class RacServer
 **/
@SuppressWarnings("serial")
final class RacServer implements Serializable{
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
		
		/*Caculate weightedAPT, weightedTraffic,*/
		caculatedWeightedAPT();
		
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
	
	/**Caculate pod level APT TRAFFICE**/
	public void caculatedWeightedAPT(){
		List<Metric> scaledResult=caculateProduct();
		this.weightedAPT=loadProduct(scaledResult);
		this.weightedAPT.get(0).setMetric(this.racServerAddress);
		this.weightedAPT.get(0).setTags(null);
		this.weightedTraffic=loadWeightedTraffic(scaledResult);
		this.weightedTraffic.get(0).setMetric(this.racServerAddress);
		this.weightedTraffic.get(0).setTags(null);
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
	
	private List<Metric> loadWeightedTraffic(List<Metric> scaledResult){
		//As divisor, first summed up, then remove zero datapoints
		List<Metric> divisor=listAPTTrafficAppLevel.stream().map(c -> c.getSelfAsMetric()).collect(Collectors.toList());
		List<Metric> divisorSUMED=_computationUtil.get().sumWithUnion(divisor);
		assert(divisorSUMED!=null&&divisorSUMED.size()==1):"divisorSUMED should only have one metric inside";
		return divisorSUMED;
	}
	
	private List<Metric> loadProduct(List<Metric> scaledResult){
		List<Metric> toBeDivided = new ArrayList<Metric>();
		scaledResult.forEach(m -> toBeDivided.add(m));
		
		//As divisor, first summed up, then remove zero datapoints
		List<Metric> divisorSUMED=loadWeightedTraffic(scaledResult);
		
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
		System.out.println("APTTimeAppLevel:\t"+this.listAPTTimeAppLevel.size());
		System.out.println("APTTrafficAppLevel:\t"+this.listAPTTrafficAppLevel.size());
		System.out.println("weightedAPT:\t\t"+this.weightedAPT.get(0).getDatapoints().size());
		System.out.println("weightedTraffic:\t"+this.weightedTraffic.get(0).getDatapoints().size());
		
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
		List<Metric> impactedMin=getImpactedMinHourlyAPT(weightedAPT,null);
		
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
		return _computationUtil.get().detectAPT(racLevelApt, this.racServerAddress);
	}
	
	/**getImpactedMin by detecting ACT**/
	private List<Metric> getImapctedMinHourlyACT(){
		if (this.weightedACT==null || this.weightedACT.size()==0){
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




/**
 * Class MetricConsumer
 **/
@SuppressWarnings("serial")
final class MetricConsumer implements Serializable{
	private String racServerAddress;
	private String appServerAddress;
	private ConsumerTypes consmuerType;
	private Map<Long,String> datapoints;
	
	private MetricConsumer(){}
	/**static factory**/
	public static MetricConsumer getMetricConsumer(Metric m){
		//Differeciate m, make it consumeable
		MetricConsumer self=new MetricConsumer();
		String scopeSource=m.getScope();
		String metricSource=m.getMetric();
		String tagSource=m.getTag("device");
		
		if(Pattern.matches("SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode.*.Last_1_Min_Avg",metricSource)){
			//SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode2.Last_1_Min_Avg
			self.consmuerType=ConsumerTypes.APT_TIME_APPLEVEL;
			
			String[] racAddressSplit = metricSource.split("\\.");
			ArrayList<String> racAddressSplitList=new ArrayList<String>(Arrays.asList(racAddressSplit));
			assert(racAddressSplitList.size()==2):"should include one dot in the middle";
			String racAddress=racAddressSplitList.get(0).substring(61);
			
			String podAddress=scopeSource.substring(5);
			assert(racAddress!=null&&racAddress.length()>0&&podAddress!=null&&podAddress.length()>10):"Invalid pod or rac address+"+scopeSource+"."+metricSource;
			self.racServerAddress=podAddress+".Rac"+racAddress;
			
			String appAddress=tagSource.substring(5, 11);
			assert(appAddress!=null&&appAddress.length()>4):"Invalid app address+"+appAddress;
			self.appServerAddress=appAddress;
			
		}else if(Pattern.matches("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode.*.Last_1_Min_Avg",metricSource)){
			//SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg
			self.consmuerType=ConsumerTypes.APT_TRAFFIC_APPLEVEL;
			
			String[] racAddressSplit = metricSource.split("\\.");
			ArrayList<String> racAddressSplitList=new ArrayList<String>(Arrays.asList(racAddressSplit));
			assert(racAddressSplitList.size()==2):"should include one dot in the middle";
			String racAddress=racAddressSplitList.get(0).substring(62);
			
			String podAddress=scopeSource.substring(5);
			assert(racAddress!=null&&racAddress.length()>0&&podAddress!=null&&podAddress.length()>10):"Invalid pod or rac address+"+scopeSource+"."+metricSource;
			self.racServerAddress=podAddress+".Rac"+racAddress;
			
			String appAddress=tagSource.substring(5, 11);
			assert(appAddress!=null&&appAddress.length()>4):"Invalid app address+"+appAddress;
			self.appServerAddress=appAddress;
			
		}else if(Pattern.matches(".*.active__sessions",metricSource)){
			//CSDB15.CSDB15-3.active__sessions
			self.consmuerType=ConsumerTypes.ACT_RACLEVEL;
			
			String[] racAddressSplit = metricSource.split("\\.");
			ArrayList<String> racAddressSplitList=new ArrayList<String>(Arrays.asList(racAddressSplit));
			assert(racAddressSplitList.size()==3):"act should be in format such as CNADB11, NADB11-1, active__sessions";
			ArrayList<String> racAddressLocalList=new ArrayList<String>(Arrays.asList(racAddressSplitList.get(1).split("-")));
			assert(racAddressLocalList!=null && racAddressLocalList.size()==2):"local act should be format as NADB11-1";
			String racAddress=racAddressLocalList.get(1);
			
			String podAddress=scopeSource.substring(10);		
			assert(racAddress!=null&&racAddress.length()>0&&podAddress!=null&&podAddress.length()>10):"Invalid pod or rac address+"+scopeSource+"."+metricSource;
			self.racServerAddress=podAddress+".Rac"+racAddress;
			self.appServerAddress="RACLEVEL";
		}else if(Pattern.matches("CpuPerc.cpu.system",metricSource)){
			//CpuPerc.cpu.system
			self.consmuerType=ConsumerTypes.CPU_SYS_RACLEVEL;			
			String device=m.getTag("device");
			String[] racAddressSplit = device.split("-");
			assert(racAddressSplit!=null && racAddressSplit.length>=4):"format should be cs15-db1-1-chi.ops.sfdc.net however get:"+device;
			String racAddress=racAddressSplit[2];
			
			String podAddress=scopeSource.substring(7);
			assert(podAddress!=null&&podAddress.length()>9&&podAddress.split("\\.").length==3):"podAddress should have format such as CHI.SP2.cs15, however is"+podAddress;
			self.racServerAddress=podAddress+".Rac"+racAddress;
			self.appServerAddress="RACLEVEL";
			
		}else if(Pattern.matches("CpuPerc.cpu.user",metricSource)){
			//CpuPerc.cpu.system
			self.consmuerType=ConsumerTypes.CPU_USER_RACLEVEL;			
			String device=m.getTag("device");
			String[] racAddressSplit = device.split("-");
			assert(racAddressSplit!=null && racAddressSplit.length>=4):"format should be cs15-db1-1-chi.ops.sfdc.net however get:"+device;
			String racAddress=racAddressSplit[2];
			
			String podAddress=scopeSource.substring(7);
			assert(podAddress!=null&&podAddress.length()>9&&podAddress.split("\\.").length==3):"podAddress should have format such as CHI.SP2.cs15, however is"+podAddress;
			self.racServerAddress=podAddress+".Rac"+racAddress;
			self.appServerAddress="RACLEVEL";
			
		}else{
			throw new RuntimeException("This type of input is not supported: metric name "+metricSource);
		}
		
		self.datapoints=m.getDatapoints();
		self.cleanup();
		return self;
	}
	
	/**return SCOPE:METRIC{tags}            :avg
	 *		  rac :ConsumerType{device=app}:avg
	 ***/
	public Metric getSelfAsMetric(){
		Metric cMetric=new Metric(racServerAddress,this.consmuerType.toString());
		cMetric.setTag("device", this.appServerAddress);
		cMetric.setDatapoints(this.datapoints);
		return cMetric;
	}
	
	/**getters**/
	public String getRacServerAddress(){
		return this.racServerAddress;
	}
	
	public String getAppServerAddress(){
		return this.appServerAddress;
	}
	
	public ConsumerTypes getConsumerType(){
		return this.consmuerType;
	}

	private void cleanup(){
		//if datapoints are missing, fill it with zero.
		//if datapoints are short than expected, fill it with zero.
	}
	
	public void inspect(){
		System.out.println("\nInspect object MetricConsumer\nMetricType:\t\t"+this.consmuerType);
		System.out.println("racServerAddress:\t"+this.racServerAddress);
		System.out.println("appServerAddress:\t"+this.appServerAddress);
		//System.out.println("original details:\t\tMetric: "+this.metric.getMetric()+" tags:"+this.metric.getTag("device"));
		System.out.println("dataLength:\t\t"+this.datapoints.size());
		//System.out.println("sample datapoint:\t"+this.metric.getDatapoints());
	}
	
	public static enum ConsumerTypes {
		APT_TIME_APPLEVEL,
		APT_TRAFFIC_APPLEVEL,
		ACT_RACLEVEL,
		CPU_SYS_RACLEVEL,
		CPU_USER_RACLEVEL;
	}
}



/**ReportRange**/
@SuppressWarnings("serial")
final class ReportRange implements Serializable{
	private final List<Long> range;
	
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
}



/*
 * Class ComputationUtil
 */
final class ComputationUtil{
	@Inject
	private Provider<TransformFactory> _transformFactory;
	
	protected List<Metric> scale_match(List<Metric> metrics) {
		try{
			List<String> constants = new ArrayList<String>();
			constants.add("device");
			constants.add(".*");
			return _transformFactory.get().getTransform("SCALE_MATCH").transform(metrics, constants);
		}catch(RuntimeException e){
			throw new RuntimeException("Error During ComputationUtil.scale_match"+e+metrics);
		}
	}
	
	protected List<Metric> scale(List<Metric> metrics1,List<Metric> metrics2) {
		assert(metrics1!=null&&metrics1.size()==1&&metrics2!=null&&metrics2.size()==1):"metric inputs has to be single inputs each";
		List<Metric> toBeScaled = new ArrayList<Metric>();
		toBeScaled.addAll(metrics1);
		toBeScaled.addAll(metrics2);
		return scale(toBeScaled);
	}
	
	protected List<Metric> scale(List<Metric> metrics) {
		return _transformFactory.get().getTransform("SCALE").transform(metrics);
	}
	
	protected List<Metric> divide(List<Metric> metrics) throws RuntimeException {
		assert(metrics!=null && metrics.size()==2):"metrics should have two metric inside. No and divisor";
		try{
			Metric divisor=metrics.get(1);
			divisor.getDatapoints().entrySet().forEach(e->{
				if(Float.valueOf(e.getValue()).equals(0f)){
					throw new RuntimeException(" A divisor is empty in this divisor metircs: "+divisor.getMetric());
				}
			});
			return _transformFactory.get().getTransform("DIVIDE").transform(metrics);
		}catch(RuntimeException e){
			throw new RuntimeException("Error During ComputationUtil.Divide"+e+metrics);
		}	
	}
	
	protected Metric removeZeroMetric(Metric m) {
		Metric output = new Metric(m);
		output.setDatapoints(removeZero(m.getDatapoints()));
		return output;
	}
	
	private Map<Long, String> removeZero(Map<Long, String> input) {
		Map<Long, String> output = new HashMap<Long, String>();
		input.entrySet().stream().filter(e -> Float.valueOf(e.getValue()) > 0)
				.forEach(e -> output.put(e.getKey(), e.getValue()));
		return output;
	}
	
	protected List<Metric> sum(List<Metric> metrics) {
		return _transformFactory.get().getTransform("SUM").transform(metrics);
	}
	
	protected List<Metric> sumWithUnion(List<Metric> metrics) {
		List<String> constants = new ArrayList<String>();
		constants.add("union");
		return _transformFactory.get().getTransform("SUM").transform(metrics, constants);
	}

	protected List<Metric> consecutive(List<Metric> metrics) {
		List<String> constants = new ArrayList<String>();
		constants.add("4m");// Define as connect distance
		constants.add("1m");
		return _transformFactory.get().getTransform("CONSECUTIVE").transform(metrics, constants);
	}

	protected List<Metric> cull_below(List<Metric> metrics, int threshold) {
		List<Metric> metricsCopy=Collections.unmodifiableList(metrics.stream()
																	 .map(m -> new Metric(m))
																	 .collect(Collectors.toList()));
		List<String> constants = new ArrayList<String>();
		constants.add(String.valueOf(threshold));
		constants.add("value");
		return _transformFactory.get().getTransform("CULL_BELOW").transform(metricsCopy, constants);
	}

	protected List<Metric> downsample(String distance, List<Metric> metrics) {
		List<Metric> mutable = new ArrayList<Metric>();
		metrics.forEach(m -> mutable.add(new Metric(m)));
		List<String> constants = new ArrayList<String>();
		constants.add(distance);// Define as connect distance
		return _transformFactory.get().getTransform("DOWNSAMPLE").transform(mutable, constants);
	}
	
	protected List<Metric> downsample(String distance, List<Metric> metrics, Long timeStamp){
		assert (metrics!=null && metrics.size()==1):"only take single metric to downsample";
		List<Metric> downsampledResult=downsample(distance,metrics);
		assert(downsampledResult!=null&&downsampledResult.size()==1&&downsampledResult.get(0).getDatapoints().size()==1):"downsampled result should be single data points";
		Metric m=new Metric(downsampledResult.get(0));
		Map<Long,String> datapoints=new HashMap<Long,String>();
		datapoints.put(timeStamp, downsampledResult.get(0).getDatapoints().entrySet().iterator().next().getValue());
		m.setDatapoints(datapoints);
		return Collections.unmodifiableList(Arrays.asList(m));
	}

	protected List<Metric> downsampleAndFill(ReportRange range, int resolutionInMin, String distance, List<Metric> metrics){
		List<Metric> downsampledImpactedMin=downsample(distance, metrics);
		List<Metric> filledImpactedMin=mergeZero(range,resolutionInMin,downsampledImpactedMin);
		return Collections.unmodifiableList(filledImpactedMin);
	}
	protected List<Metric> zeroFill(List<Metric> m) {
		assert (m != null && m.size() > 1) : "list of metric has to be valid";
		Metric output = new Metric(m.get(0));
		List<Metric> outputlist = new LinkedList<Metric>();
		Map<Long, String> outdatapoints = new HashMap<Long, String>();
		m.get(0).getDatapoints().entrySet().stream().forEach(e -> outdatapoints.put(e.getKey(), "0"));
		output.setDatapoints(outdatapoints);
		outputlist.add(0, output);
		return outputlist;
	}

	protected List<Metric> fill(String distance, List<Metric> metrics) {
		if (metrics.get(0).getDatapoints() == null || metrics.get(0).getDatapoints().size() == 0) {
			return null;// if the incoming metrics are all zeros, then return
						// null
		}
		List<String> constants = new ArrayList<String>();
		constants.add(distance);// Define as connect distance
		constants.add("0m");
		constants.add("0");
		return _transformFactory.get().getTransform("FILL").transform(metrics, constants);
	}
	
	protected List<Metric> mergeZero(ReportRange range, int resolutionInMin, List<Metric> listMetric) {
		assert(listMetric!=null && listMetric.size()==1):"Only one item is allowed in the boxing";
		Metric m = new Metric(listMetric.get(0));
		Map<Long, String> resultDatapoints=fillZero(range.getStart(),range.getEnd(),Long.valueOf(resolutionInMin*1000*60),listMetric.get(0).getDatapoints());
		m.setDatapoints(resultDatapoints);
		return Arrays.asList(m);
	}
	
	protected List<Metric> negate(List<Metric> metrics){
		assert (metrics != null && metrics.size() > 0) : "list of metric has to be valid";
		Metric m=new Metric(metrics.get(0));
		m.setDatapoints(negate(m.getDatapoints()));
		List<Metric> returnList=Collections.unmodifiableList(Arrays.asList(m));
		return returnList;
	}
	
	private Map<Long, String> negate(Map<Long, String> input) {
		Map<Long, String> output = new HashMap<Long, String>();
		input.entrySet().forEach(e -> output.put(e.getKey(), String.valueOf((1f - Float.valueOf(e.getValue())) * 100)));
		return output;
	}

	protected Map<Long, String> fillZero(Long start,Long end, Long resolution, Map<Long, String> datapoints){
		Map<Long, String> resultDatapoints=new HashMap<Long, String>(datapoints);
		for(Long timestamp=start;timestamp<=end;timestamp+=resolution){
			if (!resultDatapoints.keySet().contains(timestamp)){
				resultDatapoints.put(timestamp, String.valueOf("0"));
			}
		}
		return resultDatapoints;
	}

	/**Givin a time series, return any datapoints that is above 500 unit for 5 consecutive timestamp, could be null**/
	protected List<Metric> detectAPT(List<Metric> input, String objectAddress){
		assert(input != null && input.get(0).getDatapoints() != null) : "input not valid";
		List<Metric> cull_below_filter = cull_below(input, 500);
		
		if (cull_below_filter.get(0).getDatapoints().size() == 0) {
			System.out.println("cull_below_filter return. No data has been found that is above 500 @"+objectAddress);
			return cull_below_filter;
		}
	
		List<Metric> consecutive_filter = consecutive(cull_below_filter);
		if (consecutive_filter.get(0).getDatapoints().size() == 0) {
			System.out.println("consecutive return. No data has been found that is consecutive more than 5 @"+objectAddress);
			return consecutive_filter;
		}
		assert(consecutive_filter.get(0).getDatapoints().size() > 0) : "till now, some data should be detected";
		
		return consecutive_filter;
		
	}
	
	/**Givin a time series, return any datapoints that is above 150, could be null**/
	protected List<Metric> detectACT(List<Metric> input, String objectAddress){
		assert(input != null && input.get(0).getDatapoints() != null) : "input not valid";
		return detectAbove(input,150,objectAddress);
	}
	
	/**Givin a time series, return any datapoints that is above 65, could be null**/
	protected List<Metric> detectCPU(List<Metric> input, String objectAddress){
		assert(input != null && input.get(0).getDatapoints() != null) : "input not valid";
		return detectAbove(input,65,objectAddress);
	}
	
	/**Givin a time series, return any datapoints that is above a threshold, could be null**/
	protected List<Metric> detectAbove(List<Metric> input, int threashold, String objectAddress){
		assert(input != null && input.get(0).getDatapoints() != null) : "input not valid";
		List<Metric> cull_below_filter = cull_below(input, threashold);
		if (cull_below_filter.get(0).getDatapoints().size() == 0) {
			System.out.println("cull_below_filter return. No data has been found that is above "+threashold+" @"+ objectAddress);
		}
		return cull_below_filter;
	}
	
	private List<Metric> flattArray(List<Metric>... listMetrics){
		assert(listMetrics!=null):"input metrics can not be null";
		ArrayList<List<Metric>> arrayMetrics=new ArrayList<List<Metric>>();
		arrayMetrics.addAll(Arrays.asList(listMetrics));
		List<Metric> flatArrayMetrics=arrayMetrics.stream()
												.flatMap(l -> l.stream())
												.collect(Collectors.toList());
		return flatArrayMetrics;
	}
	
	protected List<Metric> unionOR(List<Metric>... listMetrics){
		return unionOR(flattArray(listMetrics));
	}
	
	protected List<Metric> unionOR(List<Metric> metrics){
		List<Metric> unionOR=sumWithUnion(metrics);
		assert (unionOR!=null&&unionOR.size()==1):"result of unionOr should be one metric boxed object";
		return Collections.unmodifiableList(Arrays.asList(new Metric(unionOR.get(0))));
	}
	
	protected List<Metric> unionAND(List<Metric>... listMetrics){
		return unionAND(flattArray(listMetrics));
	}
	
	protected List<Metric> unionAND(List<Metric> metrics){
		List<Metric> unionAND=sum(metrics);
		assert (unionAND!=null&&unionAND.size()==1):"result of unionAnd should be one metric boxed object";
		return Collections.unmodifiableList(Arrays.asList(new Metric(unionAND.get(0))));
	}
	
	protected List<Metric> reNameScope(List<Metric> metrics, String scopeName){
		List<Metric> resultMetrics=new ArrayList<Metric>();
		metrics.forEach(m -> {
			Metric newMetric = new Metric(scopeName, m.getMetric());
			newMetric.setTags(m.getTags());
			newMetric.setNamespace(m.getNamespace());
			newMetric.setDatapoints(m.getDatapoints());
			resultMetrics.add(newMetric);
		});
		return Collections.unmodifiableList(resultMetrics);
	}

	protected List<Metric> weightedByTraffic(List<Metric> metrics, List<Metric> traffic, String downsampleDistance, ReportRange reportRange){
		assert (metrics!=null && metrics.size()==1 && traffic!=null && traffic.size()==1):"input not valid";
		List<Metric> product=scale(metrics,traffic);
		List<Metric> productDownsampled=downsample(downsampleDistance, product);
		
		List<Metric> divisor=Arrays.asList(removeZeroMetric(traffic.get(0)));
		List<Metric> divisorDownsampled=downsample(downsampleDistance, divisor);
		
		List<Metric> toBeDivided = new ArrayList<Metric>();
		toBeDivided.addAll(productDownsampled);
		toBeDivided.addAll(divisorDownsampled);
		List<Metric> dividedResult=divide(toBeDivided);
		
		assert(dividedResult!=null):"dividedResult should be valid"; 
		List<Metric> filleddividedResult=mergeZero(reportRange,60,dividedResult);

		return filleddividedResult;
	}
}
