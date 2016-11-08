/* Generated By:JavaCC: Do not edit this line. MetricReader.java */
/* Copyright (c) 2014, Salesforce.com, Inc.
 * All rights reserved.
 *  
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *   
 *      Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 *      Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 *      Neither the name of Salesforce.com nor the names of its contributors may be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */
package com.salesforce.dva.argus.service.metric;

import java.io.*;
import java.util.*;
import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.service.DiscoveryService;
import com.salesforce.dva.argus.service.TSDBService;
import com.salesforce.dva.argus.service.metric.transform.Transform;
import com.salesforce.dva.argus.service.metric.transform.TransformFactory;
import com.salesforce.dva.argus.service.metric.transform.TransformIterator;
import com.salesforce.dva.argus.service.tsdb.MetricQuery;
import com.salesforce.dva.argus.service.tsdb.MetricQuery.Aggregator;
import com.salesforce.dva.argus.system.SystemAssert;
import com.salesforce.dva.argus.system.SystemException;
import com.google.inject.Inject;
import static com.salesforce.dva.argus.system.SystemAssert.*;


public class MetricReader<T> implements MetricReaderConstants {
        private TSDBService tsdbService;
        private DiscoveryService discoveryService;
        private TransformFactory factory;

        @Inject
    MetricReader(TSDBService tsdbService, DiscoveryService discoveryService, TransformFactory factory) {
        this((Reader)null);
        this.tsdbService = tsdbService;
        this.discoveryService = discoveryService;
        this.factory = factory;
    }

    public static boolean isValid(Collection<String> expressions) {
        boolean result = true;
        if(expressions != null) {
            MetricReader reader = new MetricReader((TSDBService)null, (DiscoveryService)null, (TransformFactory)null);
            for(String expression : expressions) {
                try {
                    if(!reader.isValidExpression(expression)) {
                        result = false;
                        break;
                    }
                } catch (Exception ex) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    public static boolean isValid(String expression) {
        return isValid(Arrays.asList(new String[] {expression}));
    }

        public enum TimeUnit {

        SECOND("s", 1000),
        MINUTE("m", 60 * SECOND.getValue()),
        HOUR("h", 60 * MINUTE.getValue()),
        DAY("d", 24 * HOUR.getValue());

        private final String _unit;
        private final long _value;

        private TimeUnit(String unit, long value) {
            _unit = unit;
            _value = value;
        }

        public String getUnit() {
            return _unit;
        }

        public long getValue() {
            return _value;
        }

        public static TimeUnit fromString(String text) {
                        if (text != null) {
                                for (TimeUnit unit : TimeUnit.values()) {
                                        if (text.equalsIgnoreCase(unit.getUnit())) {
                                                return unit;
                                        }
                                }
                        }
                        throw new SystemException(text + ": This time unit is not supported.", new UnsupportedOperationException());
        }
    }

  final public boolean isValidExpression(String expression) throws ParseException {
                {if (true) return start(expression, 0, true, (Class<T>) Metric.class) != null;}
    throw new Error("Missing return statement in function");
  }

  final public List<T> parse(String expression, long offsetInMillis, Class<T> clazz) throws ParseException {
                {if (true) return start(expression, offsetInMillis, false, clazz);}
    throw new Error("Missing return statement in function");
  }

  final private List<T> start(String expression, long offsetInMillis, boolean syntaxOnly, Class<T> clazz) throws ParseException {
        List<T> result = new ArrayList<T>();
        requireState(syntaxOnly || tsdbService != null, "TSDB service can only be null when syntax validation is being performed.");
        ReInit(new StringReader(expression));
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case TIME:
      result = expression(offsetInMillis, syntaxOnly, clazz);
      break;
    case IDENTITY:
    case HEIMDALL_TOTALAVA:
    case HEIMDALL:
    case SCALE_MATCH:
    case FILTER:
    case P90:
    case HeimdallDataGuardTransform:
    case SUM:
    case SUM_V:
    case DIVIDE:
    case DIVIDE_V:
    case DIFF:
    case DIFF_V:
    case MULTIPLY:
    case SCALE:
    case SCALE_V:
    case AVERAGE:
    case INTEGRAL:
    case DERIVATIVE:
    case MIN:
    case MAX:
    case AVERAGEBELOW:
    case PERCENTILE:
    case MOVINGAVERAGE:
    case ZEROIFMISSINGSUM:
    case ABSOLUTE:
    case ALIAS:
    case NORMALIZE:
    case NORMALIZE_V:
    case UNION:
    case COUNT:
    case GROUP:
    case ABOVE:
    case BELOW:
    case PROPAGATE:
    case MOVING:
    case EXCLUDE:
    case INCLUDE:
    case HIGHEST:
    case LOWEST:
    case LIMIT:
    case RANGE:
    case FILL:
    case FILL_CALCULATE:
    case LOG:
    case CULL_ABOVE:
    case CULL_BELOW:
    case SORT:
    case SHIFT:
    case DOWNSAMPLE:
    case DEVIATION:
    case JOIN:
    case CONSECUTIVE:
    case foreach:
    case HW_FORECAST:
    case HW_DEVIATION:
    case ANOMALY_DENSITY:
    case ANOMALY_ZSCORE:
    case ANOMALY_KMEANS:
    case ANOMALY_RPCA:
    case GROUPBY:
      result = function(offsetInMillis, syntaxOnly, clazz);
      break;
    default:
      jj_la1[0] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    jj_consume_token(0);
        {if (true) return result;}
    throw new Error("Missing return statement in function");
  }

  final private List<T> function(long offsetInMillis, boolean syntaxOnly, Class<T> clazz) throws ParseException {
        List<T> totalResult = new ArrayList<T>();
        List<T> result = new ArrayList<T>();
        List<String> constants = new ArrayList<String>();
        String functionName, constant = "";
        Token t = null;
    functionName = functionName();
    jj_consume_token(LEFT_PARENTHESIS);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case TIME:
      result = expression(offsetInMillis, syntaxOnly, clazz);
                  totalResult.addAll(result);
      break;
    case IDENTITY:
    case HEIMDALL_TOTALAVA:
    case HEIMDALL:
    case SCALE_MATCH:
    case FILTER:
    case P90:
    case HeimdallDataGuardTransform:
    case SUM:
    case SUM_V:
    case DIVIDE:
    case DIVIDE_V:
    case DIFF:
    case DIFF_V:
    case MULTIPLY:
    case SCALE:
    case SCALE_V:
    case AVERAGE:
    case INTEGRAL:
    case DERIVATIVE:
    case MIN:
    case MAX:
    case AVERAGEBELOW:
    case PERCENTILE:
    case MOVINGAVERAGE:
    case ZEROIFMISSINGSUM:
    case ABSOLUTE:
    case ALIAS:
    case NORMALIZE:
    case NORMALIZE_V:
    case UNION:
    case COUNT:
    case GROUP:
    case ABOVE:
    case BELOW:
    case PROPAGATE:
    case MOVING:
    case EXCLUDE:
    case INCLUDE:
    case HIGHEST:
    case LOWEST:
    case LIMIT:
    case RANGE:
    case FILL:
    case FILL_CALCULATE:
    case LOG:
    case CULL_ABOVE:
    case CULL_BELOW:
    case SORT:
    case SHIFT:
    case DOWNSAMPLE:
    case DEVIATION:
    case JOIN:
    case CONSECUTIVE:
    case foreach:
    case HW_FORECAST:
    case HW_DEVIATION:
    case ANOMALY_DENSITY:
    case ANOMALY_ZSCORE:
    case ANOMALY_KMEANS:
    case ANOMALY_RPCA:
    case GROUPBY:
      result = function(offsetInMillis, syntaxOnly, clazz);
                  totalResult.addAll(result);
      break;
    case CONSTANT:
      t = jj_consume_token(CONSTANT);
                                constant = t.image;
                                constant = constant.substring(1, constant.length() - 1);
                                constants.add(constant);
      break;
    default:
      jj_la1[1] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case COMMA:
        ;
        break;
      default:
        jj_la1[2] = jj_gen;
        break label_1;
      }
      jj_consume_token(COMMA);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case TIME:
        result = expression(offsetInMillis, syntaxOnly, clazz);
              totalResult.addAll(result);
        break;
      case IDENTITY:
      case HEIMDALL_TOTALAVA:
      case HEIMDALL:
      case SCALE_MATCH:
      case FILTER:
      case P90:
      case HeimdallDataGuardTransform:
      case SUM:
      case SUM_V:
      case DIVIDE:
      case DIVIDE_V:
      case DIFF:
      case DIFF_V:
      case MULTIPLY:
      case SCALE:
      case SCALE_V:
      case AVERAGE:
      case INTEGRAL:
      case DERIVATIVE:
      case MIN:
      case MAX:
      case AVERAGEBELOW:
      case PERCENTILE:
      case MOVINGAVERAGE:
      case ZEROIFMISSINGSUM:
      case ABSOLUTE:
      case ALIAS:
      case NORMALIZE:
      case NORMALIZE_V:
      case UNION:
      case COUNT:
      case GROUP:
      case ABOVE:
      case BELOW:
      case PROPAGATE:
      case MOVING:
      case EXCLUDE:
      case INCLUDE:
      case HIGHEST:
      case LOWEST:
      case LIMIT:
      case RANGE:
      case FILL:
      case FILL_CALCULATE:
      case LOG:
      case CULL_ABOVE:
      case CULL_BELOW:
      case SORT:
      case SHIFT:
      case DOWNSAMPLE:
      case DEVIATION:
      case JOIN:
      case CONSECUTIVE:
      case foreach:
      case HW_FORECAST:
      case HW_DEVIATION:
      case ANOMALY_DENSITY:
      case ANOMALY_ZSCORE:
      case ANOMALY_KMEANS:
      case ANOMALY_RPCA:
      case GROUPBY:
        result = function(offsetInMillis, syntaxOnly, clazz);
              totalResult.addAll(result);
        break;
      case CONSTANT:
        t = jj_consume_token(CONSTANT);
                                constant = t.image;
                                constant = constant.substring(1, constant.length() - 1);
                                constants.add(constant);
        break;
      default:
        jj_la1[3] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    jj_consume_token(RIGHT_PARENTHESIS);
                {if (true) return evaluateFunction(functionName, totalResult, constants, syntaxOnly, clazz);}
    throw new Error("Missing return statement in function");
  }

  final private String functionName() throws ParseException {
        Token t = null;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case IDENTITY:
      t = jj_consume_token(IDENTITY);
          {if (true) return t.image;}
      break;
    case HEIMDALL_TOTALAVA:
      t = jj_consume_token(HEIMDALL_TOTALAVA);
          {if (true) return t.image;}
      break;
    case HEIMDALL:
      t = jj_consume_token(HEIMDALL);
          {if (true) return t.image;}
      break;
    case SCALE_MATCH:
      t = jj_consume_token(SCALE_MATCH);
          {if (true) return t.image;}
      break;
    case FILTER:
      t = jj_consume_token(FILTER);
          {if (true) return t.image;}
      break;
    case P90:
      t = jj_consume_token(P90);
          {if (true) return t.image;}
      break;
    case HeimdallDataGuardTransform:
      t = jj_consume_token(HeimdallDataGuardTransform);
          {if (true) return t.image;}
      break;
    case MULTIPLY:
      t = jj_consume_token(MULTIPLY);
          {if (true) return t.image;}
      break;
    case AVERAGE:
      t = jj_consume_token(AVERAGE);
          {if (true) return t.image;}
      break;
    case INTEGRAL:
      t = jj_consume_token(INTEGRAL);
          {if (true) return t.image;}
      break;
    case DERIVATIVE:
      t = jj_consume_token(DERIVATIVE);
          {if (true) return t.image;}
      break;
    case MIN:
      t = jj_consume_token(MIN);
          {if (true) return t.image;}
      break;
    case MAX:
      t = jj_consume_token(MAX);
          {if (true) return t.image;}
      break;
    case ZEROIFMISSINGSUM:
      t = jj_consume_token(ZEROIFMISSINGSUM);
          {if (true) return t.image;}
      break;
    case ABSOLUTE:
      t = jj_consume_token(ABSOLUTE);
          {if (true) return t.image;}
      break;
    case UNION:
      t = jj_consume_token(UNION);
          {if (true) return t.image;}
      break;
    case COUNT:
      t = jj_consume_token(COUNT);
          {if (true) return t.image;}
      break;
    case GROUP:
      t = jj_consume_token(GROUP);
          {if (true) return t.image;}
      break;
    case AVERAGEBELOW:
      t = jj_consume_token(AVERAGEBELOW);
          {if (true) return t.image;}
      break;
    case PERCENTILE:
      t = jj_consume_token(PERCENTILE);
          {if (true) return t.image;}
      break;
    case MOVINGAVERAGE:
      t = jj_consume_token(MOVINGAVERAGE);
          {if (true) return t.image;}
      break;
    case MOVING:
      t = jj_consume_token(MOVING);
          {if (true) return t.image;}
      break;
    case SCALE:
      t = jj_consume_token(SCALE);
          {if (true) return t.image;}
      break;
    case SCALE_V:
      t = jj_consume_token(SCALE_V);
          {if (true) return t.image;}
      break;
    case DIFF:
      t = jj_consume_token(DIFF);
          {if (true) return t.image;}
      break;
    case DIFF_V:
      t = jj_consume_token(DIFF_V);
          {if (true) return t.image;}
      break;
    case SUM:
      t = jj_consume_token(SUM);
          {if (true) return t.image;}
      break;
    case SUM_V:
      t = jj_consume_token(SUM_V);
          {if (true) return t.image;}
      break;
    case DIVIDE:
      t = jj_consume_token(DIVIDE);
          {if (true) return t.image;}
      break;
    case DIVIDE_V:
      t = jj_consume_token(DIVIDE_V);
          {if (true) return t.image;}
      break;
    case ALIAS:
      t = jj_consume_token(ALIAS);
          {if (true) return t.image;}
      break;
    case PROPAGATE:
      t = jj_consume_token(PROPAGATE);
          {if (true) return t.image;}
      break;
    case NORMALIZE:
      t = jj_consume_token(NORMALIZE);
          {if (true) return t.image;}
      break;
    case NORMALIZE_V:
      t = jj_consume_token(NORMALIZE_V);
          {if (true) return t.image;}
      break;
    case ABOVE:
      t = jj_consume_token(ABOVE);
          {if (true) return t.image;}
      break;
    case BELOW:
      t = jj_consume_token(BELOW);
          {if (true) return t.image;}
      break;
    case INCLUDE:
      t = jj_consume_token(INCLUDE);
          {if (true) return t.image;}
      break;
    case EXCLUDE:
      t = jj_consume_token(EXCLUDE);
          {if (true) return t.image;}
      break;
    case HIGHEST:
      t = jj_consume_token(HIGHEST);
          {if (true) return t.image;}
      break;
    case LOWEST:
      t = jj_consume_token(LOWEST);
          {if (true) return t.image;}
      break;
    case LIMIT:
      t = jj_consume_token(LIMIT);
          {if (true) return t.image;}
      break;
    case RANGE:
      t = jj_consume_token(RANGE);
          {if (true) return t.image;}
      break;
    case FILL:
      t = jj_consume_token(FILL);
          {if (true) return t.image;}
      break;
    case FILL_CALCULATE:
      t = jj_consume_token(FILL_CALCULATE);
          {if (true) return t.image;}
      break;
    case LOG:
      t = jj_consume_token(LOG);
          {if (true) return t.image;}
      break;
    case CULL_ABOVE:
      t = jj_consume_token(CULL_ABOVE);
          {if (true) return t.image;}
      break;
    case CULL_BELOW:
      t = jj_consume_token(CULL_BELOW);
          {if (true) return t.image;}
      break;
    case SORT:
      t = jj_consume_token(SORT);
          {if (true) return t.image;}
      break;
    case SHIFT:
      t = jj_consume_token(SHIFT);
          {if (true) return t.image;}
      break;
    case DOWNSAMPLE:
      t = jj_consume_token(DOWNSAMPLE);
          {if (true) return t.image;}
      break;
    case DEVIATION:
      t = jj_consume_token(DEVIATION);
          {if (true) return t.image;}
      break;
    case JOIN:
      t = jj_consume_token(JOIN);
          {if (true) return t.image;}
      break;
    case CONSECUTIVE:
      t = jj_consume_token(CONSECUTIVE);
      {if (true) return t.image;}
      break;
    case foreach:
      t = jj_consume_token(foreach);
      {if (true) return t.image;}
      break;
    case HW_FORECAST:
      t = jj_consume_token(HW_FORECAST);
          {if (true) return t.image;}
      break;
    case HW_DEVIATION:
      t = jj_consume_token(HW_DEVIATION);
          {if (true) return t.image;}
      break;
    case ANOMALY_DENSITY:
      t = jj_consume_token(ANOMALY_DENSITY);
          {if (true) return t.image;}
      break;
    case ANOMALY_ZSCORE:
      t = jj_consume_token(ANOMALY_ZSCORE);
          {if (true) return t.image;}
      break;
    case ANOMALY_KMEANS:
      t = jj_consume_token(ANOMALY_KMEANS);
      {if (true) return t.image;}
      break;
    case ANOMALY_RPCA:
      t = jj_consume_token(ANOMALY_RPCA);
      {if (true) return t.image;}
      break;
    case GROUPBY:
      t = jj_consume_token(GROUPBY);
      {if (true) return t.image;}
      break;
    default:
      jj_la1[4] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final private List<T> evaluateFunction(String functionName, List<T> result, List<String> constants, boolean syntaxOnly, Class<T> clazz) throws ParseException {
                if(MetricQuery.class.equals(clazz)) {
                        {if (true) return (List<T>) result;}
                } else if(Metric.class.equals(clazz)) {
                        if(syntaxOnly) {
                                {if (true) return (List<T>) Arrays.asList( new Metric[] { new Metric("test","metric") });}
                        } else {
                                Transform transform = factory.getTransform(functionName);
                                if (functionName.equals("foreach")){
                        SystemAssert.requireArgument(constants!=null&&constants.size()>=2, "map function requires at least two constants. e.g., $device, $SCALE");
                        Transform _mapper = factory.getTransform(constants.get(1));
                        List<Metric> mapped=new ArrayList<Metric>();
                        for(List<Metric> m:((TransformIterator) transform).iterate((List<Metric>) result,constants.subList(0, 1))){
                                mapped.addAll((constants.size()==2)?_mapper.transform(m):_mapper.transform(m,constants.subList(2, constants.size())));
                        }
                        {if (true) return (List<T>)mapped;}
                    }

                                {if (true) return (List<T>) ((constants == null || constants.isEmpty()) ? transform.transform((List<Metric>) result) : transform.transform((List<Metric>) result, constants));}
                        }
                } else {
                        {if (true) throw new IllegalArgumentException("Invalid class type: " + clazz);}
                }
    throw new Error("Missing return statement in function");
  }

  final private List<T> expression(long offsetInMillis, boolean syntaxOnly, Class<T> clazz) throws ParseException {
        Long startTimestamp = null;
        Long endTimestamp = null;
        String namespace = null;
        String scope = null;
        String metric = null;
        Map<String, String> tags = new HashMap<String, String>();
        Aggregator aggregator = null;
        Aggregator downsampler = null;
        Long downsamplingPeriod = null;
        String downsampleTokenStr = null;
    startTimestamp = getTime();
    jj_consume_token(COLON);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case TIME:
      endTimestamp = getTime();
      jj_consume_token(COLON);
      break;
    default:
      jj_la1[5] = jj_gen;
      ;
    }
    scope = getString();
    jj_consume_token(COLON);
    metric = getString();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case LEFT_CURLY:
      jj_consume_token(LEFT_CURLY);
      tags = getTags();
      jj_consume_token(RIGHT_CURLY);
      break;
    default:
      jj_la1[6] = jj_gen;
      ;
    }
    jj_consume_token(COLON);
    aggregator = getAggregator();
    if (jj_2_1(2)) {
      jj_consume_token(COLON);
      downsampleTokenStr = getDownsampleToken();
    } else {
      ;
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case COLON:
      jj_consume_token(COLON);
      namespace = getString();
      break;
    default:
      jj_la1[7] = jj_gen;
      ;
    }
                if(MetricQuery.class.equals(clazz)) {
                        downsampler = downsampleTokenStr != null ? getDownsampler(downsampleTokenStr) : null;
                        downsamplingPeriod = downsampleTokenStr != null ? getDownsamplingPeriod(downsampleTokenStr): null;
                        startTimestamp += offsetInMillis;
                        endTimestamp = endTimestamp == null ? System.currentTimeMillis() + offsetInMillis : endTimestamp + offsetInMillis;
                MetricQuery query = new MetricQuery(scope, metric, tags, startTimestamp, endTimestamp);
                query.setNamespace(namespace);
                query.setAggregator(aggregator);
                query.setDownsampler(downsampler);
                query.setDownsamplingPeriod(downsamplingPeriod);
                List<MetricQuery> queries = discoveryService.getMatchingQueries(query);
                {if (true) return (List<T>) queries;}
                } else if(Metric.class.equals(clazz)) {
                        if(syntaxOnly) {
                    {if (true) return (List<T>) Arrays.asList( new Metric[] { new Metric("test","metric") });}
                } else {
                        downsampler = downsampleTokenStr != null ? getDownsampler(downsampleTokenStr) : null;
                                downsamplingPeriod = downsampleTokenStr != null ? getDownsamplingPeriod(downsampleTokenStr): null;
                                startTimestamp += offsetInMillis;
                                endTimestamp = endTimestamp == null ? System.currentTimeMillis() + offsetInMillis : endTimestamp + offsetInMillis;
                        MetricQuery query = new MetricQuery(scope, metric, tags, startTimestamp, endTimestamp);
                        query.setNamespace(namespace);
                        query.setAggregator(aggregator);
                        query.setDownsampler(downsampler);
                        query.setDownsamplingPeriod(downsamplingPeriod);
                        List<MetricQuery> queries = discoveryService.getMatchingQueries(query);
                                List<Metric> metrics = new ArrayList<Metric>();
                    Map<MetricQuery, List<Metric>> metricsMap = tsdbService.getMetrics(queries);
                    for(List<Metric> m : metricsMap.values()) {
                                        metrics.addAll(m);
                    }
                    {if (true) return (List<T>) metrics;}
                }
                } else {
                        {if (true) throw new IllegalArgumentException("Invalid class type: " + clazz);}
                }
    throw new Error("Missing return statement in function");
  }

  final private Long getTime() throws ParseException {
        Token t = null;
        Token t1 = null;
    t = jj_consume_token(TIME);
                try
                {
                        String timeStr = t.image;
                        if(timeStr.charAt(0) == '-') {
                                String timeDigits = timeStr.substring(1, timeStr.length() - 1);
                                String timeUnit = timeStr.substring(timeStr.length() - 1);
                                Long time = Long.parseLong(timeDigits);
                                TimeUnit unit = TimeUnit.fromString(timeUnit);
                                {if (true) return (System.currentTimeMillis() - (time * unit.getValue())) / 1000 * 1000;}
                        }
                        {if (true) return Long.parseLong(timeStr);}
                } catch(NumberFormatException nfe) {
                        {if (true) throw new SystemException("Could not parse time.", nfe);}
                }
    throw new Error("Missing return statement in function");
  }

  final private String getString() throws ParseException {
        Token t = null;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case SCOPE:
      t = jj_consume_token(SCOPE);
          {if (true) return t.image;}
      break;
    case METRIC:
      t = jj_consume_token(METRIC);
          {if (true) return t.image;}
      break;
    case NAMESPACE:
      t = jj_consume_token(NAMESPACE);
          {if (true) return t.image;}
      break;
    case AGGREGATOR:
      t = jj_consume_token(AGGREGATOR);
          {if (true) return t.image;}
      break;
    case DOWNSAMPLER:
      t = jj_consume_token(DOWNSAMPLER);
          {if (true) return t.image;}
      break;
    default:
      jj_la1[8] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final private String getNamespace() throws ParseException {
        Token t = null;
    t = jj_consume_token(NAMESPACE);
          {if (true) return t.image;}
    throw new Error("Missing return statement in function");
  }

  final private Map<String, String> getTags() throws ParseException {
        Token t = null;
    t = jj_consume_token(TAGS);
                Map<String, String> tagsMap = new HashMap<String, String>();
                String tagsStr = t.image;
                String[] tags = tagsStr.split(",");
                for(String tag : tags) {
                        String[] tagKVPair = tag.split("=");
                        String tagK = tagKVPair[0];
                        String tagV = tagKVPair[1];
                        tagsMap.put(tagK, tagV);
                }
                {if (true) return tagsMap;}
    throw new Error("Missing return statement in function");
  }

  final private Aggregator getAggregator() throws ParseException {
        Token t;
    t = jj_consume_token(AGGREGATOR);
          {if (true) return Aggregator.fromString(t.image);}
    throw new Error("Missing return statement in function");
  }

  final private String getDownsampleToken() throws ParseException {
        Token t = null;
    t = jj_consume_token(DOWNSAMPLER);
                {if (true) return t.image;}
    throw new Error("Missing return statement in function");
  }

  final private Aggregator getDownsampler(String token) throws ParseException {
                {if (true) return Aggregator.fromString(token.split("-")[1]);}
    throw new Error("Missing return statement in function");
  }

  final private Long getDownsamplingPeriod(String token) throws ParseException {
                String[] parts = token.split("-");
                String timeDigits = parts[0].substring(0, parts[0].length() - 1);
                String timeUnit = parts[0].substring(parts[0].length() - 1);
                Long time = Long.parseLong(timeDigits);
                TimeUnit unit = TimeUnit.fromString(timeUnit);
                {if (true) return time * unit.getValue();}
    throw new Error("Missing return statement in function");
  }

  private boolean jj_2_1(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_1(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(0, xla); }
  }

  private boolean jj_3_1() {
    if (jj_scan_token(COLON)) return true;
    if (jj_3R_2()) return true;
    return false;
  }

  private boolean jj_3R_2() {
    if (jj_scan_token(DOWNSAMPLER)) return true;
    return false;
  }

  /** Generated Token Manager. */
  public MetricReaderTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private Token jj_scanpos, jj_lastpos;
  private int jj_la;
  private int jj_gen;
  final private int[] jj_la1 = new int[9];
  static private int[] jj_la1_0;
  static private int[] jj_la1_1;
  static private int[] jj_la1_2;
  static private int[] jj_la1_3;
  static {
      jj_la1_init_0();
      jj_la1_init_1();
      jj_la1_init_2();
      jj_la1_init_3();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0xfe000000,0xfe000000,0x0,0xfe000000,0xfe000000,0x0,0x0,0x0,0x0,};
   }
   private static void jj_la1_init_1() {
      jj_la1_1 = new int[] {0xffffffff,0xffffffff,0x0,0xffffffff,0xffffffff,0x0,0x0,0x0,0x0,};
   }
   private static void jj_la1_init_2() {
      jj_la1_2 = new int[] {0x203fffff,0x203fffff,0x800000,0x203fffff,0x3fffff,0x20000000,0x1000000,0x400000,0xd0000000,};
   }
   private static void jj_la1_init_3() {
      jj_la1_3 = new int[] {0x0,0x8,0x0,0x8,0x0,0x0,0x0,0x0,0x3,};
   }
  final private JJCalls[] jj_2_rtns = new JJCalls[1];
  private boolean jj_rescan = false;
  private int jj_gc = 0;

  /** Constructor with InputStream. */
  public MetricReader(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public MetricReader(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new MetricReaderTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 9; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 9; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor. */
  public MetricReader(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new MetricReaderTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 9; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 9; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor with generated Token Manager. */
  public MetricReader(MetricReaderTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 9; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(MetricReaderTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 9; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static private final class LookaheadSuccess extends java.lang.Error { }
  final private LookaheadSuccess jj_ls = new LookaheadSuccess();
  private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0; Token tok = token;
      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
      if (tok != null) jj_add_error_token(kind, i);
    }
    if (jj_scanpos.kind != kind) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
    return false;
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;
  private int[] jj_lasttokens = new int[100];
  private int jj_endpos;

  private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) return;
    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];
      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }
      jj_entries_loop: for (java.util.Iterator<?> it = jj_expentries.iterator(); it.hasNext();) {
        int[] oldentry = (int[])(it.next());
        if (oldentry.length == jj_expentry.length) {
          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              continue jj_entries_loop;
            }
          }
          jj_expentries.add(jj_expentry);
          break jj_entries_loop;
        }
      }
      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
    }
  }

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[100];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 9; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1<<j)) != 0) {
            la1tokens[32+j] = true;
          }
          if ((jj_la1_2[i] & (1<<j)) != 0) {
            la1tokens[64+j] = true;
          }
          if ((jj_la1_3[i] & (1<<j)) != 0) {
            la1tokens[96+j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 100; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

  private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 1; i++) {
    try {
      JJCalls p = jj_2_rtns[i];
      do {
        if (p.gen > jj_gen) {
          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
          switch (i) {
            case 0: jj_3_1(); break;
          }
        }
        p = p.next;
      } while (p != null);
      } catch(LookaheadSuccess ls) { }
    }
    jj_rescan = false;
  }

  private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) { p = p.next = new JJCalls(); break; }
      p = p.next;
    }
    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
  }

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }

}