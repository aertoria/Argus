package com.salesforce.dva.argus.service.metric.transform.plus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.service.metric.transform.Transform;
import com.salesforce.dva.argus.system.SystemAssert;

/**
 * 
 * @author pull request. Author is Amey
 *
 */
public class HeimdallPodFilter implements Transform {
	
	//DGoWAN or SRDF
	private	float mode = (float) -1.0f;
	private Float minusOne = -1.00f;
	final private String typeName = "replication_type";
	
	//Map will save names of pod which are of 'mode' type
	private Map<String, Integer> filteredMap = new HashMap<String, Integer>();
	  List<Metric> filteredMetricsList;
	
	

	@Override
	public List<Metric> transform(List<Metric> metrics) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Metric> transform(List<Metric> metrics, List<String> constants) {
		
		SystemAssert.requireArgument(constants.size()==1, "This transform needs constants - threshold");
        Float podType = Float.parseFloat(constants.get(0));
        SystemAssert.requireArgument( (Float.compare(podType, (float)1.0) == 0) || (Float.compare(podType, (float)0.0) == 0), "PodType Invalid, use 1.0 - DGoWAN and  0.0 - SRDF");
        
        setPodType(podType);
        
        /* 
         * Scan through all the metrics and check replication flag 
         * for each pod, add to filteredMap based on 'mode'
         */
        for(Metric metric: metrics){
        	
    		
    		try {
    			
    			String scope = metric.getScope();
        		String[] parts = scope.split("\\.");
        		String pod = parts[3];
        	
	        	if(!checkReplicationFlag(metric.getMetric()) || filteredMap.containsKey(pod)){
	        		continue;
	        	}
	        	
	        	if(!checkPodType(metric.getDatapoints())){
	        		continue;
	        	}
	        	else{
	        			//put in our map
	            		filteredMap.put(pod, new Integer(1));	
	        	}
        	
			}catch(PatternSyntaxException e){
				System.out.println("Invalid Regex Expression");
	
			}catch(Exception e){
				System.out.println("Broken scope: " + metric.getScope());
			}
        }
        
        //Initialize the metrics list to be returned
        filteredMetricsList = new ArrayList<Metric>();
        
        //select metrics to be returned
        filterMetrics(metrics);
        
        //return only the metrics which are related to pods from filterMap
		return filteredMetricsList;
	}


	/*
	 * insert metrics related to pods in the filteredMap only in the filteredMetrics list
	 */
	private void filterMetrics(List<Metric> metrics) {
		
		for(Metric metric: metrics){
			
    		try {
    			
        		String scope = metric.getScope();
        		String[] parts = scope.split("\\.");
        		String pod = parts[3];
        		String[] pieces = metric.getMetric().split("\\.");	
        		
        		if(filteredMap.containsKey(pod) && !typeName.equals(pieces[1])){
        			this.filteredMetricsList.add(metric);
        		}
    			
    		}catch(PatternSyntaxException e){
    			System.out.println("Invalid Regex Expression");

    		}catch(Exception e){
    			System.out.println("Broken scope: " + metric.getScope());
    		}
			
		}
		
		
		
	}

	private boolean checkPodType(Map<Long, String> datapoints) {
		
		try{
		
	    	for(Map.Entry<Long, String> point : datapoints.entrySet()){
	    		
	    		Float value = Float.parseFloat(point.getValue());
	    		
	    		int	cmpVal = Float.compare(value, minusOne);
	    		if(cmpVal == 0){
	    			continue;
	    		}
	    		else {
	    			
	    			cmpVal = Float.compare(value, this.mode);
	    			if(cmpVal == 0){
	    				return true;
	    			}
	    			else{
	    				return false;
	    			}
	    		}
	    	}
	    	
		}
		catch(NumberFormatException e){
			System.out.println("string does not contain a parsable float");
		}
		catch(Exception e){
			System.out.println(e.toString());
		}
		
		//This return statement will execute if there are no datapoints to make a decision from.
		return false;
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
	
	private boolean checkReplicationFlag(String metricName) {
		
		try{
		
			String[] pieces = metricName.split("\\.");	
			
			if(typeName.equals(pieces[1])){
			
				return true;
			}
			else{
				return false;
			}
			
		}
		catch(PatternSyntaxException e){
			System.out.println("Invalid Regex Expression");
			return	false;
		}
		catch(Exception e){
			System.out.println("Broken metric name: "+ metricName);
			return	false;
		}
		
		
	}	
	
	//Filter DGoWAN pods or SRDF pods
	public void setPodType(Float podType){
		this.mode = podType;
	}

}
