package com.salesforce.dva.argus.service.broker;

import java.util.Arrays;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.entity.MetricSchemaRecord;
import com.salesforce.dva.argus.entity.MetricSchemaRecordQuery;
import com.salesforce.dva.argus.service.DefaultService;
import com.salesforce.dva.argus.service.SchemaService;
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

    /**
     * Creates a new object via Guice
     * 
     * @param  config  The system configuration.  Cannot be null.
     */
	@Inject
	protected DeferredSchemaService(SystemConfiguration systemConfiguration) {
		super(systemConfiguration);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<MetricSchemaRecord> get(MetricSchemaRecordQuery query, int limit, int page) {
		System.out.println("\nArgusPlus+ SchemaService\n"+query.getScope()+":"+query.getMetric());
		// TODO Auto-generated method stub
		// Step1: go ask JSON service, get a JSON like this
		//			{"namespace":null,"scope":"REDUCEDTEST.core.CHI.SP4.cs1","metric":"IMPACTPOD","tagKey":null,"tagValue":null},
		//			{"namespace":null,"scope":"REDUCEDTEST.core.CHI.SP4.cs40","metric":"IMPACTPOD","tagKey":null,"tagValue":null}
		// Step2: convert this JSON into  List<MetricSchemaRecord and return it.
		
//		query.getMetric()
//		query.getScope()
		
		/**
		 * givien
		 * 		https://argus-ws.data.sfdc.net/argusws/discover/metrics/schemarecords?
				limit=104&metric=IMPACTPOD&scope=REDUCEDTEST.core.*
				
		   return
		   		[{"namespace":null,"scope":"REDUCEDTEST.core.CHI.SP1.cs14","metric":"IMPACTPOD","tagKey":null,"tagValue":null},
				 {"namespace":null,"scope":"REDUCEDTEST.core.CHI.SP1.cs7","metric":"IMPACTPOD","tagKey":null,"tagValue":null},
				 {"namespace":null,"scope":"REDUCEDTEST.core.CHI.SP1.cs8","metric":"IMPACTPOD","tagKey":null,"tagValue":null}]
		 */

		
				
		//1477094400:1477180800:REDUCEDTEST.core.CHI.SP3.cs24:IMPACTPOD:avg
		String scope="REDUCEDTEST.core.CHI.SP3.cs24";
		String metric="IMPACTPOD";
		//Also please set tag and tag values
		MetricSchemaRecord m=new MetricSchemaRecord(scope,metric);
		return Arrays.asList(m);
	}


	
	@Override
	public void put(Metric metric) {
		throw new NotImplementedError("not implmented yet.");
	}

	@Override
	public void put(List<Metric> metrics) {
		throw new NotImplementedError("not implmented yet.");
	}

	@Override
	public List<String> getUnique(MetricSchemaRecordQuery query, int limit, int page, RecordType type) {
		throw new NotImplementedError("not implmented yet.");
	}
	
}
