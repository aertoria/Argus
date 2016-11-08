package com.salesforce.dva.argus.service.broker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.entity.MetricSchemaRecord;
import com.salesforce.dva.argus.entity.MetricSchemaRecordQuery;
import com.salesforce.dva.argus.service.DefaultService;
import com.salesforce.dva.argus.service.SchemaService;
import com.salesforce.dva.argus.service.SchemaService.RecordType;
import com.salesforce.dva.argus.service.broker.DefaultJSONService.Property;
import com.salesforce.dva.argus.service.broker.HTTP.ArgusService;
import com.salesforce.dva.argus.system.SystemConfiguration;

import scala.NotImplementedError;
import scala.util.control.Exception;

/**
 * Mircroservices implementation of the schema service.
 *
 * @author  Tom Valine (tvaline@salesforce.com)
 * @author  aertoria   (ethan.wang@salesforce.com)
 */
@Singleton
public class DeferredSchemaService extends DefaultService implements SchemaService{
	private	ArgusService service;
	private final String endpoint;
	private final String username;
	private final String password;
    /**
     * Creates a new object via Guice
     * 
     * @param  config  The system configuration.  Cannot be null.
     */
	@Inject
	protected DeferredSchemaService(SystemConfiguration systemConfiguration) {
		super(systemConfiguration);
		endpoint=systemConfiguration.getValue(Property.JSON_ENDPOINT.getName(), Property.JSON_ENDPOINT.getDefaultValue());
		username=systemConfiguration.getValue(Property.JSON_USERNAME.getName(), Property.JSON_USERNAME.getDefaultValue());
		password=systemConfiguration.getValue(Property.JSON_PASSWORD.getName(), Property.JSON_PASSWORD.getDefaultValue());
		service = new ArgusService(endpoint, 10,10000,10000);
		
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
	/**
	 *TODO Auto-generated method stub
	 *Step1: go ask JSON service, get a JSON like this
	 *			{"namespace":null,"scope":"REDUCEDTEST.core.CHI.SP4.cs1","metric":"IMPACTPOD","tagKey":null,"tagValue":null},
	 *			{"namespace":null,"scope":"REDUCEDTEST.core.CHI.SP4.cs40","metric":"IMPACTPOD","tagKey":null,"tagValue":null}
	 *Step2: convert this JSON into  List<MetricSchemaRecord and return it.
	 *
	 * givien
	 * 		https://argus-ws.data.sfdc.net/argusws/discover/metrics/schemarecords?
			limit=104&metric=IMPACTPOD&scope=REDUCEDTEST.core.*
			
	   return
	   		[{"namespace":null,"scope":"REDUCEDTEST.core.CHI.SP1.cs14","metric":"IMPACTPOD","tagKey":null,"tagValue":null},
			 {"namespace":null,"scope":"REDUCEDTEST.core.CHI.SP1.cs7","metric":"IMPACTPOD","tagKey":null,"tagValue":null},
			 {"namespace":null,"scope":"REDUCEDTEST.core.CHI.SP1.cs8","metric":"IMPACTPOD","tagKey":null,"tagValue":null}]
	 */
	@Override
	public List<MetricSchemaRecord> get(MetricSchemaRecordQuery query, int limit, int page) {
		System.out.println("\nArgusPlus+ SchemaService "+query.toString());


		service.login(username, password);
		return service.getDiscoveredMetricSchemaRecord(query.getNamespace(),query.getScope(),query.getMetric(),query.getTagKey(),query.getTagValue(),200);
	
	}

	/**
	 * Used by UI discovery service
	 */
	@Override
	public List<String> getUnique(MetricSchemaRecordQuery query, int limit, int page, RecordType type) {
		System.out.println("\nArgusPlus+ SchemaService "+query.toString());
		service.login(username, password);
		List<MetricSchemaRecord> discoveredList=service.getDiscoveredMetricSchemaRecord(query.getNamespace(),query.getScope(),query.getMetric(),query.getTagKey(),query.getTagValue(),200);
		return discoveredList.stream()
							 .map(r -> _getValueForType(r, type))
							 .collect(Collectors.toList());
	}

	@Override
	public void put(Metric metric) {
		throw new NotImplementedError("not implmented yet.");
	}

	@Override
	public void put(List<Metric> metrics) {
		throw new NotImplementedError("not implmented yet.");
	}
	
	private String _getValueForType(MetricSchemaRecord record, RecordType type) {
        switch (type) {
            case NAMESPACE:
                return record.getNamespace();
            case SCOPE:
                return record.getScope();
            case METRIC:
                return record.getMetric();
            case TAGK:
                return record.getTagKey();
            case TAGV:
                return record.getTagValue();
            default:
                return null;
        }
    }
	
}
