package com.salesforce.dva.argus.service.metric.transform.plus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.service.metric.transform.Transform;
import com.salesforce.dva.argus.service.metric.transform.TransformFactory;


public class HeimdallMetricReducer implements Transform{
	@Inject
	Provider<RacServer> _racServer;
	
	@Override
	public List<Metric> transform(List<Metric> metrics) {		
		System.out.println("\n**PRIVATE TRANSFORM**Heimdall Reducer");
		List<MetricConsumer> listConsumer=consumeMetrics(metrics);
		//listConsumer.forEach(c->c.inspect());
		
		Set<RacServer> racServers = loadRacServer(listConsumer);
		racServers.forEach(r -> r.inspect());
		
		return null;
	}
	
	private List<MetricConsumer> consumeMetrics(List<Metric> metrics){
		assert(metrics != null):"metrics input can not be null";
		List<MetricConsumer> listConsumers=new ArrayList<MetricConsumer>();
		listConsumers=metrics.stream()
				.map(m -> MetricConsumer.getMetricConsumer(m))
				.collect(Collectors.toList());
		return listConsumers;	
	}
	
	private Set<RacServer> loadRacServer(List<MetricConsumer> consumers){
		Set<String> racServerAddresses=new HashSet<String>();
		consumers.forEach(c -> racServerAddresses.add(c.getRacServerAddress()));
		Set<RacServer> racServers = racServerAddresses.stream()
														.map(address -> _racServer.get().getRacServer(address))
														.collect(Collectors.toSet());
		try{
			racServers.forEach(r -> r.load(consumers));
		}catch(RuntimeException e){
			System.out.println("Error Durring lookup and loading metricConsumers for each rac server +"+e);
			throw new RuntimeException("Error Durring lookup and loading metricConsumers for each rac server +"+e);
		}
		
		try{
			racServers.forEach(r -> r.caculatedWeightedAPT());
		}catch(Exception e){
			System.out.println("Error Durring caculating weighted apt for each rac server +"+e);
			throw new RuntimeException("Error Durring caculating weighted apt for each rac server +"+e);
		}
		
		
		return racServers;
	}
	
	@Override
	public List<Metric> transform(List<Metric> metrics, List<String> constants) {
		// TODO Auto-generated method stub
		return null;
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



/*
 * Class RacServer
 */
final class RacServer{
	@Inject
	Provider<ComputationUtil> _computationUtil;
	
	private String racServerAddress;
	private List<MetricConsumer> listAPTTimeAppLevel;
	private List<MetricConsumer> listAPTTrafficAppLevel;
	private Metric weightedAPT;
		
	@Inject
	private RacServer(){
	}
	
	public RacServer getRacServer(String racServerAddress){
		assert(racServerAddress!=null && racServerAddress.length()>10):"racServerAddress is not valid";
		this.racServerAddress=racServerAddress;
		return this;
	}
	
	public void load(List<MetricConsumer> consumers){
		assert(consumers!=null&&consumers.size()>0):"MetricConsumers not valid";
		loadAPTTimeAppLevelFromConsumers(consumers);
		loadAPTTrafficAppLevelFromConsumers(consumers);
	}
	
	public List<MetricConsumer> getAnomalyAPT(){
		return null;
	}
	
	
	public void caculatedWeightedAPT(){
		List<Metric> scaledResult=caculateProduct();
		List<Metric> dividedResult=divideProduct(scaledResult);
		this.weightedAPT=dividedResult.get(0);
	}
	
	private List<Metric> caculateProduct(){
		List<Metric> toBeMatchScaled = new ArrayList<Metric>();
		listAPTTimeAppLevel.forEach(c -> toBeMatchScaled.add(c.getSelfAsMetric()));
		listAPTTrafficAppLevel.forEach(c -> toBeMatchScaled.add(c.getSelfAsMetric()));
		List<Metric> scaledResult=_computationUtil.get().scale_match(toBeMatchScaled);
		return scaledResult;
	}
	
	private List<Metric> divideProduct(List<Metric> scaledResult){
		List<Metric> toBeDivided = new ArrayList<Metric>();
		scaledResult.forEach(m -> toBeDivided.add(m));
		
		//As divisor, first summed up, then remove zero datapoints
		List<Metric> divisor=listAPTTrafficAppLevel.stream().map(c -> c.getSelfAsMetric()).collect(Collectors.toList());
		List<Metric> divisorSUMED=_computationUtil.get().sumWithUnion(divisor);
		assert(divisorSUMED!=null&&divisorSUMED.size()==1):"divisorSUMED should only have one metric inside";
		List<Metric> divisorCleared=divisorSUMED.stream().map(m->_computationUtil.get().removeZeroMetric(m)).collect(Collectors.toList());
		divisorCleared.forEach(m -> toBeDivided.add(m));
		
		//System.out.println("tobeDivided:"+toBeDivided);
		List<Metric> dividedResult=_computationUtil.get().divide(toBeDivided);
		//System.out.println("dividedResult:"+dividedResult);
		return dividedResult;
	}
	
	public void inspect(){
		System.out.println("\nInspect object RacServer\nName:\t\t\t"+this.racServerAddress);
		System.out.println("APTTimeAppLevel:\t"+this.listAPTTimeAppLevel.size());
		System.out.println("APTTrafficAppLevel:\t"+this.listAPTTrafficAppLevel.size());
		System.out.println("weightedAPT:\t\t"+this.weightedAPT.getDatapoints().size());
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

}




/*
 * Class MetricConsumer
 */
final class MetricConsumer{
	private static List reportRange;
	public static void setReportRange(Long start,Long end){
		assert(reportRange!=null && reportRange.size()==2):"reportRange not valid";
		reportRange=Arrays.asList(start,end);
	}
	
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
}
