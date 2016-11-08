package com.salesforce.dva.argus.service.broker;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.salesforce.dva.argus.entity.MetricSchemaRecord;
import com.salesforce.dva.argus.service.DefaultService;
import com.salesforce.dva.argus.service.DiscoveryService;
import com.salesforce.dva.argus.service.SchemaService.RecordType;
import com.salesforce.dva.argus.service.tsdb.MetricQuery;
import com.salesforce.dva.argus.system.SystemConfiguration;

public class DeferredDiscoveryService extends DefaultService implements DiscoveryService {
	
	@Inject
	protected DeferredDiscoveryService(SystemConfiguration systemConfiguration) {
		super(systemConfiguration);
	}

	@Override
	public List<MetricSchemaRecord> filterRecords(String namespaceRegex, String scopeRegex, String metricRegex,
			String tagkRegex, String tagvRegex, int limit, int page) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getUniqueRecords(String namespaceRegex, String scopeRegex, String metricRegex, String tagkRegex,
			String tagvRegex, RecordType type, int limit, int page) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<MetricQuery> getMatchingQueries(MetricQuery query) {
		System.out.println("ArgusCore+ Deferred DiscoveryService acknowledged");
		List<MetricQuery> queryList = new ArrayList<MetricQuery>();
		queryList.add(query);
		return queryList;
	}

	@Override
	public boolean isWildcardQuery(MetricQuery query) {
		// TODO Auto-generated method stub
		return false;
	}
}
