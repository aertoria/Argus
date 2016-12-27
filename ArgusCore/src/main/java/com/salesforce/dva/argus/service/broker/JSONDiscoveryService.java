package com.salesforce.dva.argus.service.broker;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.google.inject.Inject;
import com.salesforce.dva.argus.entity.MetricSchemaRecord;
import com.salesforce.dva.argus.entity.MetricSchemaRecordQuery;
import com.salesforce.dva.argus.inject.SLF4JTypeListener;
import com.salesforce.dva.argus.service.DefaultService;
import com.salesforce.dva.argus.service.DiscoveryService;
import com.salesforce.dva.argus.service.SchemaService;
import com.salesforce.dva.argus.service.SchemaService.RecordType;
import com.salesforce.dva.argus.service.metric.transform.kepler.aertoria;
import com.salesforce.dva.argus.service.tsdb.MetricQuery;
import com.salesforce.dva.argus.system.SystemAssert;
import com.salesforce.dva.argus.system.SystemConfiguration;


/**
 * 
 * @author {@link aertoria} ethan.wang@salesforce.com
 *
 */
public class JSONDiscoveryService extends DefaultService implements DiscoveryService {
    //~ Static fields/initializers *******************************************************************************************************************
	private static final int HARD_LIMIT = 20;
    private static final char[] WILDCARD_CHARSET = new char[] { '*', '?', '[', ']', '|' };

    //~ Instance fields ******************************************************************************************************************************

    @SLF4JTypeListener.InjectLogger
    private Logger _logger;
    private final SchemaService _schemaService;

    //~ Constructors *********************************************************************************************************************************
    /**
     * Creates a new DefaultDiscoveryService object.
     *
     * @param  schemaService  The schema service to use.
     * @param config Service properties
     */
    @Inject
    public JSONDiscoveryService(SchemaService schemaService, SystemConfiguration config) {
    	super(config);
        this._schemaService = schemaService;
    }

    //~ Methods **************************************************************************************************************************************    
    /**
	 * call if type == null
	 */
    @Override
    public List<MetricSchemaRecord> filterRecords(String namespaceRegex, String scopeRegex, String metricRegex, String tagkRegex, String tagvRegex,
        int limit, int page) {
        requireNotDisposed();
        SystemAssert.requireArgument(scopeRegex != null && !scopeRegex.isEmpty(), "Scope regex cannot be null or empty.");
        SystemAssert.requireArgument(metricRegex != null && !metricRegex.isEmpty(), "Metric regex cannot be null or empty.");
        SystemAssert.requireArgument(limit > 0, "Limit must be a positive integer");
        SystemAssert.requireArgument(page > 0, "Page must be a positive integer");

        MetricSchemaRecordQuery query = new MetricSchemaRecordQuery(namespaceRegex, scopeRegex, metricRegex, tagkRegex, tagvRegex);

        _logger.debug(query.toString());
        long start = System.nanoTime();
        List<MetricSchemaRecord> result = _schemaService.get(query, limit, page);

        _logger.debug("Time to filter records in ms: " + (System.nanoTime() - start) / 1000000);
        return result;
    }
    
	/**
	 * call if type != null
	 */
    @Override
    public List<String> getUniqueRecords(String namespaceRegex, String scopeRegex, String metricRegex, String tagkRegex, String tagvRegex,
        RecordType type, int limit, int page) {
        requireNotDisposed();
        SystemAssert.requireArgument(scopeRegex != null && !scopeRegex.isEmpty(), "Scope regex cannot be null or empty.");
        SystemAssert.requireArgument(metricRegex != null && !metricRegex.isEmpty(), "Metric regex cannot be null or empty.");
        SystemAssert.requireArgument(limit > 0, "Limit must be a positive integer");
        SystemAssert.requireArgument(page > 0, "Page must be a positive integer");

        MetricSchemaRecordQuery query = new MetricSchemaRecordQuery(namespaceRegex, scopeRegex, metricRegex, tagkRegex, tagvRegex);
        _logger.debug(query.toString());
        long start = System.nanoTime();
        List<String> records = _schemaService.getUnique(query, limit, page, type);
        _logger.debug("Time to get Unique Records in ms: " + (System.nanoTime() - start) / 1000000);
        return records;
    }

    /**
	 * call by regular MetricReader. Line 729
	 * List<MetricQuery> queries = discoveryService.getMatchingQueries(query);
	 */
    @Override
    public List<MetricQuery> getMatchingQueries(MetricQuery query) {
		System.out.println("ArgusCore+ Deferred DiscoveryService acknowledged");
		List<MetricQuery> queryList = new ArrayList<MetricQuery>();
		queryList.add(query);
		return queryList;
    }

    @Override
    public boolean isWildcardQuery(MetricQuery query) {
        if (_containsWildcard(query.getScope()) || _containsWildcard(query.getMetric())) {
            return true;
        }
        if (_containsWildcard(query.getNamespace())) {
            return true;
        }
        if (query.getTags() != null) {
            for (String tagKey : query.getTags().keySet()) {
                if (_containsWildcard(tagKey) || (!"*".equals(query.getTag(tagKey)) && _containsWildcard(query.getTag(tagKey)))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void _printMatchedQueries(List<MetricQuery> queryList) {
        _logger.debug("Matched Queries:");

        int i = 1;

        for (MetricQuery q : queryList) {
            _logger.debug(MessageFormat.format("MetricQuery{0} = {1}", i++, q));
        }
    }

    private boolean _containsWildcard(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        char[] arr = str.toCharArray();

        for (char ch : arr) {
            if (_isWildcard(ch)) {
                return true;
            }
        }
        return false;
    }

    private boolean _isWildcard(char ch) {
        for (char c : WILDCARD_CHARSET) {
            if (c == ch) {
                return true;
            }
        }
        return false;
    }

    private void _copyRemainingProperties(MetricQuery dest, MetricQuery orig) {
        dest.setStartTimestamp(orig.getStartTimestamp());
        dest.setEndTimestamp(orig.getEndTimestamp());
        dest.setAggregator(orig.getAggregator());
        dest.setDownsampler(orig.getDownsampler());
        dest.setDownsamplingPeriod(orig.getDownsamplingPeriod());
    }

    /**
     * givien string -20d:-15d:REDUCEDTEST.core.*:IMPACTPOD{podId=*}:avg
     * return List<string> 
     */
    @Override
	public List<String> getMatchingExpressions(String expression){
    	SystemAssert.requireArgument(expression != null,"input expressioin can not be null");
    	
    	final String[] splitedExpression=expression.split(":");
    	assert(splitedExpression.length==5):"in valid expression length";
    	
    	
    	final String startTime=splitedExpression[0];
    	final String endTime=splitedExpression[1];
    	final String scopeRex=splitedExpression[2];
    	final String metricRexWithTag=splitedExpression[3];
    	final String aggregator=splitedExpression[4];
    	
    	List<MetricSchemaRecord> discoveredRecords=filterRecords(null,scopeRex,metricRexWithTag,null,null,10000,1);
    	
    	List<String> records=discoveredRecords.stream()
							    	.map(r -> {
							    		if(r.getTagKey()!=null){
							    			return startTime+":"+endTime+":"+r.getScope()+":"+r.getMetric()+"{"+r.getTagKey()+"="+r.getTagValue()+"}:"+aggregator;
							    		}
							    		return startTime+":"+endTime+":"+r.getScope()+":"+r.getMetric()+":"+aggregator;
							    	})
							    	.collect(Collectors.toList());
    	return records;
    }
    
    
}
