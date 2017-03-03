package com.salesforce.dva.argus.service.metric.transform;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.system.SystemAssert;

import scala.Tuple2;

/**
 * @author aertoria (ethan.wang@salesforce.com)
 *  
 * DEDUCT(M1, M2) It returns M1 - M2 from time dimension. It remove all items from M1 whose keys are found in M2. This is the opposite of UNION.
 *
 * Note: it is not MIN, MIN only align both Metric up by key, and do the value minus.
 *
 * Usage: DEDUCT(List).
 *Example:
 *input:
 *M1 is 1L:10, 2L:10, 3L:20
 *M2 is 1L:10
 *
 *output as DEDUCT(M1,M2)
 *M is 2L:10, 3L:20
 *
 */
public class DeductReduceTransform implements Transform{
	@Override
	public List<Metric> transform(List<Metric> metrics) {
		SystemAssert.requireArgument(metrics!=null && !metrics.isEmpty(),"input has to be valid metrics");
		
		if (metrics.size()==1){
			return metrics;
		}
		
		
		Metric toBedeductedM = new Metric(metrics.get(0));
		for(int i=1;i<metrics.size();i++){
			toBedeductedM = DeductReduceTransform.deduct(toBedeductedM,metrics.get(i));
		}
		return Collections.unmodifiableList(Arrays.asList(toBedeductedM));
	}
	
	/**
	 * Deduct two direct metrics
	 * @param m1
	 * @param m2
	 * @return
	 */
	private static Metric deduct(final Metric m1,final Metric m2){
		Map<Long, String> datapoints = m1.getDatapoints()
				.entrySet()
				.stream()
				.map(e -> {
					Long timestamp = e.getKey();
					String v = m2.getDatapoints().get(timestamp);
					if (v==null){
						v="0";
					}
					String deducted = String.valueOf(Double.valueOf(e.getValue())-Double.valueOf(v));
					Tuple2<Long, String> deductedEntry=new Tuple2<Long, String>(timestamp,deducted);
					return deductedEntry;
				})
				.collect(Collectors.toMap(t -> t._1, t-> t._2));
		final Metric returningM = new Metric(m1.getScope(), m1.getMetric());
		returningM.setDatapoints(datapoints);
		return returningM;
	}
	
	@Override
	public List<Metric> transform(List<Metric> metrics, List<String> constants) {
		throw new IllegalArgumentException("Not supported");
	}

	@Override
	public List<Metric> transform(List<Metric>... metrics) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getResultScopeName() {
		// TODO Auto-generated method stub
		return TransformFactory.Function.DEDUCT.name();
	}

}