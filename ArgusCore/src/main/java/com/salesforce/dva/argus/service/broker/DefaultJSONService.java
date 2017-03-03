package com.salesforce.dva.argus.service.broker;
import java.util.HashMap;
/**
 * Provides methods fetch data.
 *
 * @author aertoria (ethan.wang@salesforce.com)
 */
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import com.salesforce.dva.argus.entity.Annotation;
import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.service.DefaultService;
import com.salesforce.dva.argus.service.TSDBService;
import com.salesforce.dva.argus.service.broker.HTTP.ArgusService;
import com.salesforce.dva.argus.service.tsdb.AnnotationQuery;
import com.salesforce.dva.argus.service.tsdb.MetricQuery;
import com.salesforce.dva.argus.system.SystemAssert;
import com.salesforce.dva.argus.system.SystemConfiguration;

/**
 * 
 * @author aertoria <ethan.wang@salesforce.com>
 *
 */
public class DefaultJSONService extends DefaultService implements TSDBService {
	private	ArgusService service;
	private final String endpoint;
	private final String username;
	private final String password;
	
	@Inject
	protected DefaultJSONService(SystemConfiguration systemConfiguration) {
		super(systemConfiguration);
		endpoint=systemConfiguration.getValue(Property.JSON_ENDPOINT.getName(), Property.JSON_ENDPOINT.getDefaultValue());
		username=systemConfiguration.getValue(Property.JSON_USERNAME.getName(), Property.JSON_USERNAME.getDefaultValue());
		password=systemConfiguration.getValue(Property.JSON_PASSWORD.getName(), Property.JSON_PASSWORD.getDefaultValue());
		service = new ArgusService(endpoint, 10,10000,10000);
	}

	@Override
	public Map<MetricQuery, List<Metric>> getMetrics(List<MetricQuery> queries) {
		service.login(username, password);
		Map<MetricQuery, List<Metric>> resultMap=new HashMap<MetricQuery, List<Metric>>();
		queries.forEach(query -> {		
			getMetrics(query).entrySet().forEach(e -> resultMap.put(e.getKey(), e.getValue()));
		});
        return resultMap;
	}
	
	public Map<MetricQuery, List<Metric>> getMetrics(MetricQuery query) {
		//service.login(username, password);
        String expression=getExpression(query);
        System.out.println("\n\nArgusCore+ service...expression acknowledged+ "+expression);
        return service.getMeticMap(query, expression);
	}
	
		
	private String getExpression(MetricQuery query){
		String namespace=query.getNamespace();
		String expression=query.getStartTimestamp()+":"+query.getEndTimestamp()+":";
		if (namespace!=null&&namespace.length()!=0){
			expression+=namespace+".";
		}
		Map<String,String> tags=query.getTags();
		expression+=query.getScope()+":"+query.getMetric();
		if (tags!=null&&tags.size()!=0){			
			Set<String> resultset=tags.entrySet().stream().map(i->i.getKey()+"="+i.getValue()).collect(Collectors.toSet());
			expression+="{"+String.join(",", resultset)+"}";
		}
		expression+=":"+query.getAggregator().toString().toLowerCase();
		return expression;
	}
	
	public enum Property {
	    JSON_ENDPOINT("service.property.json.endpoint","http://localhost"),
	    JSON_USERNAME("service.property.json.username","defaultUser"),
	    JSON_PASSWORD("service.property.json.password","password");
		private final String _name;
        private final String _defaultValue;

        private Property(String name, String defaultValue) {
            _name = name;
            _defaultValue = defaultValue;
        }
        
        public String getDefaultValue() {
            return _defaultValue;
        }
        
        public String getName() {
            return _name;
        }
	}
	
    @Override
    public void dispose() {
    	service.dispose();
    }
    
	@Override
	public void putMetrics(List<Metric> metrics) {
	}

	@Override
	public void putAnnotations(List<Annotation> annotations) {
		// TODO Auto-generated method stub	
	}

	@Override
	public List<Annotation> getAnnotations(List<AnnotationQuery> queries) {
		// TODO Auto-generated method stub
		return null;
	}

}
