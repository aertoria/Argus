package com.salesforce.dva.argus.service.metric.transform.plus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.service.metric.transform.Transform;
import com.salesforce.dva.argus.system.SystemAssert;


/*
*
* Transform for DataGuard lag SLA compliance.
* 
* For each pod and type of lag(remote Tr lag, remote Apply lag) calculate how many points are below threshold
* 
* @author Amey Ruikar(aruikar@salesforce.com)
*/

public class HeimdallDataGuardTransform implements Transform {
	
	private final String defaultMetricName = "DataGuard-SLAcompliance";
	private enum lags {local_dataguard_lag, 
						local_dg_transport_lag,
						remote_dataguard_lag,
						remote_dg_transport_lag};

	@Override
	public List<Metric> transform(List<Metric> metrics) {
		throw new UnsupportedOperationException("Have to have one constant");
	}

	@Override
	public List<Metric> transform(List<Metric> metrics, List<String> constants) {
		
		SystemAssert.requireArgument(constants.size()==1, "This transform needs constants - threshold");
        Float threshold = Float.parseFloat(constants.get(0));
        
        //store individual results for each pod in this list
        List<Metric> newMetricsList = new ArrayList<Metric>();
        
        for(Metric metric : metrics){
        	
    		try{
        		//scope: db.CHI.SP2.na12
        		String scope = metric.getScope();
        		String[] parts = scope.split("\\.");
        		String pod = parts[3];
        		
        		//metric: NADB13.local_dataguard_lag
        		if(!checkMetricName(metric.getMetric())){
        			continue;
        		}
        		
        		//NADB13.local_dataguard_lag -> local_dataguard_lag
        		String dataGuardMetric = metric.getMetric().split("\\.")[1];
        		
        		if(checkIfEmpty(metric)){
        			//create empty metric object
        			// will be used for missing info in dashboard
        			Map<Long, String> noDataMap = new HashMap<Long, String>();
        			newMetricsList.add(generateMetric(pod+"-"+defaultMetricName, pod+"."+dataGuardMetric, noDataMap));
        			continue;
        		}
        		
        		//all check complete, process the data-points now
        		Map<String, Integer> result = processDataPoints(metric.getDatapoints(), threshold);
        		
            	Float confidence = (float) ((result.get("totalPoints") - result.get("nullPoints")) * 100.00/result.get("totalPoints"));
            	Float SLAcompliance = (float) (result.get("compliedPoints") * 100.0 / (result.get("compliedPoints") + result.get("failedPoints")));
            	
            	//System.out.println("POD: "+ pod +" -> "+ result.get("compliedPoints") +" / "+ (result.get("compliedPoints") + result.get("failedPoints")));
            	
            	Map<Long, String> complianceData = new HashMap<Long, String>();
            	/*
            	 * round off to 4 digits 
            	 * 
            	 * index 0 - compliance value
            	 * index 1 - confidence value
            	 */
            	complianceData.put((long) 0, String.valueOf(Math.round(SLAcompliance*10.0)/10.0));
            	complianceData.put((long) 1, String.valueOf(Math.round(confidence*10.0)/10.0));
            	
            	//push into new metrics array
            	newMetricsList.add(generateMetric(pod+"-"+defaultMetricName, pod+"."+dataGuardMetric, complianceData));	
        		
    		}
    		catch(ArrayIndexOutOfBoundsException accessException){
    			
    			System.out.println("Could not get pod name from metric string. Skipping this metric..");
    			continue;
    		}
    		catch(Exception e){
    			System.out.println(e);
    			continue;
    		}
    		
        }
        
        return	newMetricsList;
	}

	
	
	/*
	 * Build a map containing counts of points that complied to SLA, null points and total number of points
	 * 
	 * @input: map of datapoints(long, string)
	 * 		   Threshold for the SLA compliance 
	 * 
	 * @return:	map with count of null points, complied points, total points 
	 * 
	 * @author Amey Ruikar(aruikar@salesforce.com)
	 */
	private	Map<String, Integer> processDataPoints(Map<Long, String> dataPoints, Float threshold){
		
		Map<String, Integer> countOfPoints = new HashMap<String, Integer>();
		Integer totalPoints = new Integer(0);
		Integer pointsComplied = new Integer(0);
		Integer pointsFailed = new Integer(0);
		Integer nullPoints = new Integer(0);
        Float minusOne = -1.00f;
		
    	for(Map.Entry<Long, String> point : dataPoints.entrySet()){
    		
    		Float lag = Float.parseFloat(point.getValue());
    		totalPoints++;
    		
    		int	retval = Float.compare(lag, minusOne);
    		if(retval == 0){
    			nullPoints++;
    		}
    		else{
    			int	compareValue = Float.compare(lag, threshold);
    			
    			if(compareValue > 0){
    				pointsFailed++;
    			}
    			else{
    				pointsComplied++;
    			}
    		}
    		
    	}
		
		//place all the counts in a Map
		countOfPoints.put("totalPoints", totalPoints);
		countOfPoints.put("compliedPoints", pointsComplied);
		countOfPoints.put("failedPoints", pointsFailed);
		countOfPoints.put("nullPoints", nullPoints);
		
		return countOfPoints;
	}
	

	/*
	 * Check if the metric name consists of one of the four Data Guard lags
	 * 
	 * @input: metric name
	 * 
	 * @author Amey Ruikar(aruikar@salesforce.com)
	 */
	private	boolean checkMetricName(String metricName){
		
		try{
			
			String[] dataGuardMetric = metricName.split("\\.");			
			lags specificLag;
			specificLag = lags.valueOf(dataGuardMetric[1]);
			
			return	true;
		}
		catch(IllegalArgumentException e){
			
			System.out.println("Lag specification in the metric Name does not match expected value: "+metricName);
			return false;
		}
		catch(Exception e){
			System.out.println("Metric Name cannot be split");
			return	false;
		}

	}
	
	
	/*
	 * Check if the data-points returned from JSON stream is Empty
	 * 
	 * @input: metric - object contains data points(map)
	 *
	 * @author Amey Ruikar(aruikar@salesforce.com)
	 */
	private boolean checkIfEmpty(Metric metric){
		
		if(metric.getDatapoints().isEmpty()){
			return true;
		}
		return false;
	}
	
	
	/*
	 * create new metric object
	 * 
	 * @input: scope - pod Name
	 * 		   metric - name for the metric 
	 * 		   targetDataPoints - map object to store data-points
	 *
	 * @author Amey Ruikar(aruikar@salesforce.com)
	 */
	private Metric generateMetric(String scope, String metric, Map<Long, String> targetDataPoints){
		
		Metric newMetric = new Metric(scope, metric);
		newMetric.setDisplayName("DataGuardLag SLA compliance");
        newMetric.setUnits(null);
        newMetric.setTags(null);
        newMetric.setDatapoints(targetDataPoints);
		return newMetric;
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
