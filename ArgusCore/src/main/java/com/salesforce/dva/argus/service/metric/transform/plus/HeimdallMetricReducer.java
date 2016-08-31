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
	Provider<ComputationUtil> _computationUtil;

	@Inject
	Provider<Pod> _pod;
	
	@Override
	public List<Metric> transform(List<Metric> metrics, List<String> constants) {
		assert(constants!=null&&constants.size()==1):"constants should has exactly one result";
		System.out.println("\n**PRIVATE TRANSFORM**Heimdall Reducer");
		//align up
		List<Metric> metricsLineup=_computationUtil.get().downsample("1m-avg", metrics);
		List<Metric> metricsLineupInHour=_computationUtil.get().downsample("1h-avg", metrics);
		final ReportRange reportRange=ReportRange.getReportRange(metricsLineupInHour.get(0));
		
		List<MetricConsumer> listConsumer=consumeMetrics(metrics);		
		Renderable pod=_pod.get().getPod(listConsumer,reportRange);
		((SFDCPod) pod).inspect();
		
		
		switch(constants.get(0)){
		case "APT":
			return render(()->pod.renderAPT());
		case "TTM":
			return render(()->pod.renderTTM());
		case "TTMPOD":
			return render(()->pod.renderTTMPOD());
		case "AVA":
			return render(()->pod.renderAVA());
		case "AVAPOD":
			return render(()->pod.renderAVAPOD());
		}
		throw new RuntimeException("unsupported");
	}
	
	private List<Metric> render(java.util.function.Supplier<List<Metric>> renderable){
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
	List<Metric> renderTTM();
	List<Metric> renderTTMPOD();
	List<Metric> renderAPT();
	List<Metric> renderAVA();
	List<Metric> renderAVAPOD();
}

/**Aspect Defined as SFDCPod**/
interface SFDCPod{
	String getPodAddress();
	void inspect();
}

/**Pod implementation**/
final class Pod implements Renderable, SFDCPod{
	private List<RacServer> racServers;
	private String podAddress;
	
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
	
	@Override
	public List<Metric> renderTTM() {		
		List<Metric> constructedResult=this.racServers.stream()
													  .map(r -> r.getImpactedMinHourly().get(0))
													  .collect(Collectors.toList());
		return Collections.unmodifiableList(constructedResult);
	}

	public List<Metric> renderTTMPOD() {
		Optional<Metric> constructedResult=this.racServers.stream()
										  .map(r -> r.getImpactedMinHourly().get(0))
										  .reduce((m1,m2) -> _computationUtil.get().sum(Arrays.asList(m1,m2)).get(0));
		if(constructedResult.isPresent()){
			return Collections.unmodifiableList(Arrays.asList(constructedResult.get()));
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
	
	public List<Metric> renderAVA(){
		List<Metric> constructedResult=this.racServers.stream()
										   .map(r -> r.getAvaRateHourly().get(0))
										   .collect(Collectors.toList());
		return Collections.unmodifiableList(constructedResult);
	}
	
	public List<Metric> renderAVAPOD(){
		throw new RuntimeException("unsupported");
	}
	
	/**getters**/
	public String getPodAddress(){
		final String podAddressPass = this.podAddress;
		return podAddressPass;
	}

}




/**
 * Class RacServer
 **/
final class RacServer{
	@Inject
	Provider<ComputationUtil> _computationUtil;
	
	private ReportRange reportRange;
	private String racServerAddress;
	private List<MetricConsumer> listAPTTimeAppLevel;
	private List<MetricConsumer> listAPTTrafficAppLevel;
	private List<Metric> weightedAPT;
	private List<Metric> weightedTraffic;
		
	@Inject
	private RacServer(){
	}
	
	public RacServer getRacServer(String racServerAddress, ReportRange reportRange, List<MetricConsumer> consumers){
		assert(racServerAddress!=null && racServerAddress.length()>10):"racServerAddress is not valid";
		this.racServerAddress=racServerAddress;
		this.reportRange=reportRange;
		
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
		return this;
	}
		
	public void load(List<MetricConsumer> consumers){
		assert(consumers!=null&&consumers.size()>0):"MetricConsumers not valid";
		loadAPTTimeAppLevelFromConsumers(consumers);
		loadAPTTrafficAppLevelFromConsumers(consumers);
	}
		
	/**getImpactedMinHourly return a timeseries with count of impact min for each hour**/
	public List<Metric> getImpactedMinHourly(){
		List<Metric> impactedMin=getImpactedMinHourly(weightedAPT,null);
		impactedMin.get(0).setMetric(this.racServerAddress);
		
		List<Metric> downsampledImpactedMin=_computationUtil.get().downsample("1h-count", impactedMin);
		
		//System.out.println("a"+downsampledImpactedMin);
		List<Metric> filledImpactedMin=_computationUtil.get().mergeZero(reportRange,60,downsampledImpactedMin);
		//System.out.println("b"+filledImpactedMin);
		return filledImpactedMin;
	}
	
	/**given RacServerLevel metric, return impactedMinmetric**/
	private  List<Metric> getImpactedMinHourly(List<Metric> racLevelApt, List<Metric> racLevelCPU) {
		assert(racLevelApt != null && racLevelApt.get(0).getDatapoints() != null) : "input not valid";
		List<Metric> cull_below_filter = _computationUtil.get().cull_below(racLevelApt, 500);

		if (cull_below_filter.get(0).getDatapoints().size() == 0) {
			System.out.println("cull_below_filter return. No data has been found that is above 500");
			return cull_below_filter;
		}

		List<Metric> consecutive_filter = _computationUtil.get().consecutive(cull_below_filter);
		if (consecutive_filter.get(0).getDatapoints().size() == 0) {
			System.out.println("consecutive return. No data has been found that is consecutive more than 5");
			return consecutive_filter;
		}
		
		assert(consecutive_filter.get(0).getDatapoints().size() > 0) : "till now, some data should be detected";
		//List<Metric> impactedMin = _computationUtil.get().fill("1h", _computationUtil.get().downsample("1h-count", consecutive_filter));
		return consecutive_filter;
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
		//System.out.println(dividedResult);
		
		assert(dividedResult!=null):"dividedResult should be valid"; 
		List<Metric> filleddividedResult=_computationUtil.get().mergeZero(reportRange,60,dividedResult);
		List<Metric> negatedAvaRate=_computationUtil.get().negate(filleddividedResult);
		//System.out.println(filleddividedResult);
		
		return negatedAvaRate;
	}
	
	public void caculatedWeightedAPT(){
		List<Metric> scaledResult=caculateProduct();
		this.weightedAPT=loadProduct(scaledResult);
		this.weightedAPT.get(0).setMetric(this.racServerAddress);
		this.weightedTraffic=loadWeightedTraffic(scaledResult);
		this.weightedTraffic.get(0).setMetric(this.racServerAddress);
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
		assert(dividedResult!=null && dividedResult.size()==1):"division should have only one result";
		return dividedResult;
	}
	
	public void inspect(){
		System.out.println("\nInspect object RacServer\nName:\t\t\t"+this.racServerAddress);
		System.out.println("APTTimeAppLevel:\t"+this.listAPTTimeAppLevel.size());
		System.out.println("APTTrafficAppLevel:\t"+this.listAPTTrafficAppLevel.size());
		System.out.println("weightedAPT:\t\t"+this.weightedAPT.get(0).getDatapoints().size());
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

	/**getters**/
	public String getRacServerAddress(){
		final String racServerAddressPass=this.racServerAddress;
		return racServerAddressPass;
	}
	
	public ReportRange getReportRange(){
		final ReportRange getReportRangePass=this.reportRange;
		return getReportRangePass;
	} 
	
	public List<Metric> getRawAPTMinutely(){
		List<Metric> weightedTrafficPass=Collections.unmodifiableList(weightedAPT);
		return weightedTrafficPass;
	}
	
	public List<Metric> getWeightedTraffic(){
		List<Metric> weightedTrafficPass=Collections.unmodifiableList(weightedTraffic);
		return weightedTraffic;
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
		List<String> constants = new ArrayList<String>();
		constants.add(String.valueOf(threshold));
		constants.add("value");
		return _transformFactory.get().getTransform("CULL_BELOW").transform(metrics, constants);
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
		assert (metrics != null && metrics.size() > 1) : "list of metric has to be valid";
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
}
