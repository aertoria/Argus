package com.salesforce.dva.argus.service.broker.HTTP;
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

import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.entity.MetricSchemaRecord;
import com.salesforce.dva.argus.service.tsdb.MetricQuery;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.salesforce.dva.argus.entity.Annotation;
import com.salesforce.dva.argus.system.SystemAssert;

import java.io.IOException;
import java.util.*;

public class ArgusService {

    //~ Instance fields ******************************************************************************************************************************

    private final ArgusHttpClient client;

    //~ Constructors *********************************************************************************************************************************
    public ArgusService(String tsdReadEndpoint, int maxConn, int connTimeout, int connRequestTimeout) {
        client = new ArgusHttpClient(tsdReadEndpoint, maxConn, connTimeout, connRequestTimeout);
    }

    //~ Methods **************************************************************************************************************************************

    /**
     * Closes the service connections and prepares the service for garbage collection. This method may be invoked on a service which has already been
     * disposed.
     */
    public void dispose() {
        client.dispose();
    }

    /**
     * Writes a metric datum.
     *
     * @param  metric  The metric datum to write. May not be null.
     */
    public void put(Metric metric) {
    	SystemAssert.requireArgument(metric != null, "Data point cannot be null.");
        put(Arrays.asList(new Metric[] { metric }));
    }

    /**
     * Writes metric data.
     *
     * @param  data  The metric data to write.
     */
    public void put(List<Metric> data) {
    	SystemAssert.requireArgument(data != null && !data.isEmpty(), "Data cannot be null or empty.");
        client.putMetricData(data);
    }

    /**
     * Create or update an annotation.
     *
     * @param  annotation  The annotation to add. Cannot be null.
     */
    public void putAnnotation(Annotation annotation) {
    	SystemAssert.requireArgument(annotation != null, "Annotation cannot be null.");
        putAnnotations(Arrays.asList(new Annotation[] { annotation }));
    }

    /**
     * Create or update global annotations.
     *
     * @param  annotations  The annotations to add. Cannot be null.
     */
    public void putAnnotations(List<Annotation> annotations) {
    	SystemAssert.requireArgument(annotations != null && !annotations.isEmpty(), "Data cannot be null or empty.");
        client.putAnnotationData(annotations);
    }

    /**
     * Logs into the web services.
     *
     * @param  username  The username.
     * @param  password  The password.
     */
    public void login(String username, String password) {
        client.login(username, password);
    }

    /** Logs out of the web services. */
    public void logout() {
        client.logout();
    }

    /***/
    public Map<MetricQuery, List<Metric>> getMeticMap(MetricQuery query, String expression){
    	SystemAssert.requireArgument(expression != null && query!=null, "expression and query cannot be null.");
    	return client.getMeticMap(query, expression);
    }
    
    /***/
    public List<Metric> getMetric(MetricQuery query,String expression){
    	SystemAssert.requireArgument(expression != null && query!=null, "expression and query cannot be null.");
        return client.getMetric(query,expression);
    }
    
    /**
     * used by discovery service
     */
    public List<MetricSchemaRecord> getDiscoveredMetricSchemaRecord(String namespaceRegex, String scopeRegex, String metricRegex, String tagKeyRegex, String tagValueRegex, int limit) {
    	return client.getDiscoveredMetricSchemaRecord(namespaceRegex, scopeRegex, metricRegex, tagKeyRegex, tagValueRegex, limit);
    }
}
/* Copyright (c) 2014, Salesforce.com, Inc.  All rights reserved. */
