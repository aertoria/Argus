package com.salesforce.dva.argus.service.metric.transform.plus;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.salesforce.dva.argus.service.metric.transform.Transform;
import com.salesforce.dva.argus.service.metric.transform.TransformFactory;
import com.salesforce.dva.argus.service.metric.transform.TransformFactory.Function;


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
		final ReportRange reportRange=ReportRange.getReportRange(metricsLineupInHour.get(0));
		
		List<MetricConsumer> listConsumer=consumeMetrics(metricsLineup);

		Renderable pod=_pod.get().getPod(listConsumer,reportRange);
		((SFDCPod) pod).inspect();
		
		switch(constants.get(0)){
		case "APT":
			return render(()->pod.renderAPT());
		case "APTPOD":
			return render(()->pod.renderAPTPOD());
		case "ACT":
			return render(()->pod.renderACT());
		case "IMPACT":
			return render(()->pod.renderIMPACT());
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
	List<Metric> renderIMPACTPOD();
	List<Metric> renderIMPACTTOTAL();
	List<Metric> renderAPT();
	List<Metric> renderAPTPOD();
	List<Metric> renderACT();
	List<Metric> renderAVA();
	List<Metric> renderAVAPOD();
	List<Metric> renderAVATOTAL();
	List<Metric> renderTTMPOD();
}

/**Aspect Defined as SFDCPod**/
interface SFDCPod{
	String getPodAddress();
	void inspect();
	boolean hasACT();
}

/**Pod implementation**/
final class Pod implements Renderable, SFDCPod{
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
		consumers.forEach(c -> racServerAddresses.add(c.getRacServerAddress()));
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
			List<Metric> toBeDivided = Arrays.asList(constructedProduct.get(),this.podTraffic.get(0));
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
			List<Metric> toBeDivided = Arrays.asList(constructedProduct.get(),this.podTraffic.get(0));
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
				.map(r -> _computationUtil.get().scale(r.getAvaRateHourly(),r.getWeightedTrafficHourly())
												.get(0)
					)
				.reduce((m1,m2)-> _computationUtil.get().sumWithUnion(Arrays.asList(m1,m2)).get(0));

		Optional<Metric> constructedDivisor=this.racServers.stream()
						.map(r -> r.getWeightedTrafficHourly().get(0))
						.reduce((m1,m2) ->_computationUtil.get().sumWithUnion(Arrays.asList(m1,m2)).get(0));
		
		assert(constructedProduct.isPresent()&&constructedDivisor.isPresent()):"accumulation result is not valid";
		if(constructedProduct.isPresent()&&constructedDivisor.isPresent()){
			List<Metric> toBeDivided = Arrays.asList(constructedProduct.get(),constructedDivisor.get());			
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
	public List<Metric> renderIMPACTPOD() {
		Optional<Metric> constructedResult=this.racServers.stream()
										  .map(r -> r.getImpactedMinHourly().get(0))
										  .reduce((m1,m2) -> _computationUtil.get().sum(Arrays.asList(m1,m2)).get(0));
		if(constructedResult.isPresent()){
			Metric m=constructedResult.get();
			m.setMetric(podAddress);
			return Collections.unmodifiableList(Arrays.asList(m));
		}
		throw new RuntimeException("Not a single result");
	}
	
	@Override
	public List<Metric> renderIMPACTTOTAL() {
		List<Metric> IMPACTPOD=renderAVAPOD();
		List<Metric> IMPACTTOTAL = _computationUtil.get().downsample("100d-sum", IMPACTPOD);
		return IMPACTTOTAL;
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
	public List<Metric> renderACT() {
		List<Metric> constructedResult=this.racServers.stream()
										  .filter(r -> r.hasACT())
										  .map(r -> r.getRawACTMinutely().get(0))
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
		final List<Metric> productDownsampled=_computationUtil.get().downsample("100d-sum", product);
		final List<Metric> weightDownsampled=_computationUtil.get().downsample("100d-sum", dataRecievedCountFilled);
		assert(weightDownsampled.get(0).getDatapoints().size()==1):"downsampled to one result";
		final List<Metric> avaDownsampled=_computationUtil.get().divide(Arrays.asList(productDownsampled.get(0),weightDownsampled.get(0)));
		return Collections.unmodifiableList(avaDownsampled);
	};
	
	/** TTM FORMULAR Following...
	 * 			Version: SFDC_HEIMDALL_SEP2.001239x09F
	 * 			Author: Decide on Meeting Jul 7th 2016
	 * F1:(UNIONED_RAC_LEVEL_APT or UNIONED_RAC_LEVEL_ACT)  AND  (RAC trigger1 OR RAC trigger2)
	 * F2:(POD_LEVEL_APT or POD_LEVEL_ACT)
	 * TTM counted iif:   F1 OR F2
	 * **/
	@Override
	public List<Metric> renderTTMPOD(){
		//F1-APT or ACT
		List<Metric> podLevelSLA=getPOD_LEVEL_SLA();
		
		//F2-APT or ACT
		List<Metric> listRacLevelSLA=getUNIONED_RACS_LEVEL_SLA();
		
		//F1 or F2 RELATIONSHIP
		List<Metric> TTMSLA=_computationUtil.get().unionOR(podLevelSLA,listRacLevelSLA);
		TTMSLA.get(0).setMetric(this.podAddress);
		List<Metric> TTM=_computationUtil.get().downsample("1h-count", TTMSLA);
		List<Metric> filledTTM=_computationUtil.get().mergeZero(RacServer.getReportRange(),60,TTM);
		return Collections.unmodifiableList(Arrays.asList(new Metric(filledTTM.get(0))));
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
		
//		System.out.println("racLevelAPT_SLAList"+racLevelAPT_SLAList);
//		System.out.println("racLevelACT_SLAList"+racLevelACT_SLAList);		
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

}



/**
 * Class RacServer
 **/
final class RacServer{
	@Inject
	private Provider<ComputationUtil> _computationUtil;
	
	private static ReportRange reportRange;

	private String racServerAddress;
	private List<MetricConsumer> listAPTTimeAppLevel;
	private List<MetricConsumer> listAPTTrafficAppLevel;
	private List<MetricConsumer> listACTRacLevel;
	private List<Metric> weightedAPT;
	private List<Metric> weightedTraffic;
	private List<Metric> weightedACT;
		
	@Inject
	private RacServer(){
	}
	
	public RacServer getRacServer(String racServerAddress, ReportRange reportRange, List<MetricConsumer> consumers){
		assert(racServerAddress!=null && racServerAddress.length()>10):"racServerAddress is not valid";
		this.racServerAddress=racServerAddress;
		RacServer.reportRange=reportRange;
		try{
			load(consumers);
		}catch(RuntimeException e){
			System.out.println("Error Durring lookup and loading metricConsumers for each rac server +"+e);
			throw new RuntimeException("Error Durring lookup and loading metricConsumers for each rac server +"+e);
		}
		
		try{
			caculatedWeightedAPT();
		}catch(Exception e){
			System.out.println("Error Durring caculating weighted apt for each rac server +"+e);
			throw new RuntimeException("Error Durring caculating weighted apt for each rac server +"+e);
		}
		
		try{
			//give the option that no ACT provided. after stream.filter, will have a empty list at least
			if (this.listACTRacLevel!=null&&this.listACTRacLevel.size()==1){
				caculateACT();
			}
		}catch(Exception e){
			System.out.println("Error Durring caculating direct act for each rac server +"+e);
			throw new RuntimeException("Error Durring caculating direct act for each rac server +"+e);
		}
		return this;
	}
		
	public void load(List<MetricConsumer> consumers){
		assert(consumers!=null&&consumers.size()>0):"MetricConsumers not valid";
		loadAPTTimeAppLevelFromConsumers(consumers);
		loadAPTTrafficAppLevelFromConsumers(consumers);
		loadACTRacLevelFromConsumers(consumers);
	}
			
	/**getImpactedMinHourly return a timeseries with count of impact min for each hour**/
	public List<Metric> getImpactedMinHourly(){
		//APT
		List<Metric> impactedMin=getImpactedMinHourlyAPT(weightedAPT,null);
		
		//ACT
		if (hasACT()){
			List<Metric> impactedMinACT = getImapctedMinHourlyACT();
			impactedMin=_computationUtil.get().unionOR(impactedMin,impactedMinACT);
		}
		
		impactedMin.get(0).setMetric(this.racServerAddress);		
		List<Metric> downsampledImpactedMin=_computationUtil.get().downsample("1h-count", impactedMin);
		//System.out.println("a"+downsampledImpactedMin);
		List<Metric> filledImpactedMin=_computationUtil.get().mergeZero(reportRange,60,downsampledImpactedMin);
		//System.out.println("b"+filledImpactedMin);
		return filledImpactedMin;
	}
	
	/**given RacServerLevel metric, return impactedMinmetric**/
	private List<Metric> getImpactedMinHourlyAPT(List<Metric> racLevelApt, List<Metric> racLevelCPU) {
		return _computationUtil.get().detectAPT(racLevelApt, this.racServerAddress);
	}
	
	/**getImpactedMin by detecting ACT**/
	private List<Metric> getImapctedMinHourlyACT(){
		System.out.println("ACT CACULATION...... Running per rac server");
		
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
		List<Metric> availability=_computationUtil.get().downsample("1h-count", weightedTraffic);
		List<Metric> availability_zeroRemoved=availability.stream().map(m->_computationUtil.get().removeZeroMetric(m)).collect(Collectors.toList());
		List<Metric> avaRateHourly=getImpactedMinHourly();
		
		List<Metric> toBeDivided = new ArrayList<Metric>();
		avaRateHourly.forEach(m -> toBeDivided.add(m));
		availability_zeroRemoved.forEach(m -> toBeDivided.add(m));
		
		//System.out.println("tobeDivided:"+toBeDivided);
		List<Metric> dividedResult=_computationUtil.get().divide(toBeDivided);
		//System.out.println("dividedResult"+dividedResult);
		
		assert(dividedResult!=null):"dividedResult should be valid"; 
		List<Metric> filleddividedResult=_computationUtil.get().mergeZero(reportRange,60,dividedResult);

		
		//System.out.println("filleddividedResult"+filleddividedResult);
		List<Metric> negatedAvaRate=_computationUtil.get().negate(filleddividedResult);
		
		return negatedAvaRate;
	}
	
	public void caculateACT(){
		assert(this.listACTRacLevel!=null&&this.listACTRacLevel.size()==1):"have be one and only one act per racnode";
		MetricConsumer c=this.listACTRacLevel.get(0);
		this.weightedACT=new ArrayList<Metric>();
		this.weightedACT.add(new Metric(this.listACTRacLevel.get(0).getSelfAsMetric()));
		this.weightedACT.get(0).setMetric(this.racServerAddress);
		this.weightedACT.get(0).setTags(null);
	}
	
	public void caculatedWeightedAPT(){
		List<Metric> scaledResult=caculateProduct();
		this.weightedAPT=loadProduct(scaledResult);
		this.weightedAPT.get(0).setMetric(this.racServerAddress);
		this.weightedAPT.get(0).setTags(null);
		this.weightedTraffic=loadWeightedTraffic(scaledResult);
		this.weightedTraffic.get(0).setMetric(this.racServerAddress);
		this.weightedTraffic.get(0).setTags(null);
	}
	
	private List<Metric> caculateProduct(){
		List<Metric> toBeMatchScaled = new ArrayList<Metric>();
		listAPTTimeAppLevel.forEach(c -> toBeMatchScaled.add(c.getSelfAsMetric()));
		listAPTTrafficAppLevel.forEach(c -> toBeMatchScaled.add(c.getSelfAsMetric()));
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
		if(this.weightedACT==null){
			System.out.println("weightedACT:\t\tNo ACT provided");
		}else{
			System.out.println("weightedACT:\t\t"+this.weightedACT.get(0).getDatapoints().size());
		}
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
	
	/**getters**/
	public String getRacServerAddress(){
		final String racServerAddressPass=this.racServerAddress;
		return racServerAddressPass;
	}
	
	public static ReportRange getReportRange(){
		final ReportRange getReportRangePass=reportRange;
		return getReportRangePass;
	} 
	
	public List<Metric> getRawAPTMinutely(){
		final Metric weightedAPTPass=new Metric(this.weightedAPT.get(0));
		return Collections.unmodifiableList(Arrays.asList(weightedAPTPass));
	}
	
	public List<Metric> getRawACTMinutely(){
		if (this.weightedACT==null || this.weightedACT.size()==0){
			throw new RuntimeException("No ACT provided, not legal to call this method");
		}
		assert(this.weightedACT!=null&&this.weightedACT.size()==1):"ACT has to be provided";
		final Metric weightedACTPass=new Metric(this.weightedACT.get(0));
		return Collections.unmodifiableList(Arrays.asList(weightedACTPass));
	}
	
	public boolean hasACT(){
		return (this.weightedACT==null || this.weightedACT.size()==0)?false:true;
	}
	
 	public List<Metric> getWeightedTrafficMinutely(){
		final Metric weightedTrafficPass=new Metric(this.weightedTraffic.get(0));
		List<Metric> weightedTrafficPassList=Collections.unmodifiableList(Arrays.asList(weightedTrafficPass));
		return weightedTrafficPassList;
	}
	
	public List<Metric> getWeightedTrafficHourly(){
		List<Metric> weightedTraffic=getWeightedTrafficMinutely();
		List<Metric> weightedTrafficDownsampled=_computationUtil.get().downsample("1h-count", weightedTraffic);
		return Collections.unmodifiableList(weightedTrafficDownsampled);
	}
	
	/**To Honor the sequence of RacServer**/
	public static Comparator<RacServer> compareByName(){
		return (rac1,rac2) -> rac1.getRacServerAddress().compareTo(rac2.getRacServerAddress());
	}
}




/*
 * Class MetricConsumer
 */
final class MetricConsumer{
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
			
			String racAddress=metricSource.substring(61, 62);
			String podAddress=scopeSource.substring(5);
			assert(racAddress!=null&&racAddress.length()>0&&podAddress!=null&&podAddress.length()>10):"Invalid pod or rac address+"+scopeSource+"."+metricSource;
			self.racServerAddress=podAddress+".Rac"+racAddress;
			
			String appAddress=tagSource.substring(5, 11);
			assert(appAddress!=null&&appAddress.length()>4):"Invalid app address+"+appAddress;
			self.appServerAddress=appAddress;
		}else if(Pattern.matches("SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode.*.Last_1_Min_Avg",metricSource)){
			//SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode2.Last_1_Min_Avg
			self.consmuerType=ConsumerTypes.APT_TRAFFIC_APPLEVEL;
			
			String racAddress=metricSource.substring(62, 63);
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
final class ReportRange{
	private final List<Long> range;
	private final Map<Long, String> zeroDatapoints;
	
	private ReportRange(List<Long> range,Map<Long, String> _templateDatapoints){
		this.zeroDatapoints=_templateDatapoints;
		this.range=range;
	}
	
	public static ReportRange getReportRange(Metric m){
		assert(m!=null && m.getDatapoints().size()>0):"start and end should not be null";
		ReportRange self=new ReportRange(Arrays.asList(
										Collections.min(m.getDatapoints().keySet()),
										Collections.max(m.getDatapoints().keySet())
										),
										generateTemplateDataPoints(m.getDatapoints()));
		return self;		
	}
	
	private static Map<Long,String> generateTemplateDataPoints(Map<Long, String> datapoints){
		Map<Long, String> zeroDatapoints=new HashMap<Long,String>();
		datapoints.entrySet().forEach(e -> zeroDatapoints.put(e.getKey(), String.valueOf("0")));
		return zeroDatapoints;
	} 
	
	public Long getStart(){
		return this.range.get(0);
	}
	
	public Long getEnd(){
		return this.range.get(1);
	}
	
	public Map<Long, String> getZeroDatapoints(){
		Map<Long, String> zeroDatapointsPass = Collections.unmodifiableMap(zeroDatapoints);
		return zeroDatapointsPass;
	}
}


/*
 * Class ComputationUtil
 */
final class ComputationUtil{
	@Inject
	private Provider<TransformFactory> _transformFactory;
	
	protected List<Metric> scale_match(List<Metric> metrics) {
		List<String> constants = new ArrayList<String>();
		constants.add("device");
		constants.add(".*");
		return _transformFactory.get().getTransform("SCALE_MATCH").transform(metrics, constants);
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
	
	protected List<Metric> divide(List<Metric> metrics) {
		try{
			return _transformFactory.get().getTransform("DIVIDE").transform(metrics);
		}catch(RuntimeException e){
			throw new RuntimeException("Error During ComputationUtil.Divide"+e);
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
	protected List<Metric> detectAPT(List<Metric> aptInput, String objectAddress){
		assert(aptInput != null && aptInput.get(0).getDatapoints() != null) : "input not valid";
		List<Metric> cull_below_filter = cull_below(aptInput, 500);
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
	protected List<Metric> detectACT(List<Metric> actInput, String objectAddress){
		assert(actInput != null && actInput.get(0).getDatapoints() != null) : "input not valid";
		List<Metric> cull_below_filter = cull_below(actInput, 150);
		if (cull_below_filter.get(0).getDatapoints().size() == 0) {
			System.out.println("cull_below_filter return. No data has been found that is above 500 @"+objectAddress);
		}
		return cull_below_filter;
	}
	
	protected List<Metric> unionOR(List<Metric>... listMetrics){
		assert(listMetrics!=null):"input metrics can not be null";
		ArrayList<List<Metric>> arrayMetrics=new ArrayList<List<Metric>>();
		arrayMetrics.addAll(Arrays.asList(listMetrics));
		List<Metric> flatArrayMetrics=arrayMetrics.stream()
												.flatMap(l -> l.stream())
												.collect(Collectors.toList());
		return unionOR(flatArrayMetrics);
	}
	
	protected List<Metric> unionOR(List<Metric> metrics){
		List<Metric> unionOR=sumWithUnion(metrics);
		assert (unionOR!=null&&unionOR.size()==1):"result of unionOr should be one metric boxed object";
		return Collections.unmodifiableList(Arrays.asList(new Metric(unionOR.get(0))));
	}
}
