package com.salesforce.dva.argus.service.broker.HTTP;
import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.entity.MetricSchemaRecord;
import com.salesforce.dva.argus.service.tsdb.MetricQuery;
import com.salesforce.dva.argus.system.SystemAssert;
import com.salesforce.dva.argus.system.SystemException;
import com.salesforce.dva.argus.entity.Annotation;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.salesforce.dva.argus.system.SystemAssert.requireArgument;

import java.io.*;
import java.net.*;
import java.util.*;



/**
 * HTTP based API client for Argus.
 */
@SuppressWarnings("deprecation")
public class ArgusHttpClient{

    //~ Static fields/initializers *******************************************************************************************************************

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(ArgusHttpClient.class);
    private static final int CHUNK_SIZE = 50;

    static {
        MAPPER.setVisibility(PropertyAccessor.GETTER, Visibility.ANY);
        MAPPER.setVisibility(PropertyAccessor.SETTER, Visibility.ANY);
    }

    //~ Instance fields ******************************************************************************************************************************

    int maxConn = 100;
    int connTimeout = 10000;
    int connRequestTimeout = 10000;
    private boolean disposed = false;
    String endpoint;
    CloseableHttpClient httpClient;
    PoolingHttpClientConnectionManager connMgr;
    private BasicCookieStore cookieStore;
    private BasicHttpContext httpContext;

    //~ Constructors *********************************************************************************************************************************

    /**
     * Creates a new Argus HTTP client.
     *
     * @param   endpoint    The URL of the read endpoint including the port number. Must not be null.
     * @param   maxConn     The maximum number of concurrent connections. Must be greater than 0.
     * @param   timeout     The connection timeout in milliseconds. Must be greater than 0.
     * @param   reqTimeout  The connection request timeout in milliseconds. Must be greater than 0.
     *
     * @throws  OrchestraException  If an error occurs.
     */
    public ArgusHttpClient(String endpoint,int maxConn,int timeout,int reqTimeout) {
    	SystemAssert.requireArgument((endpoint != null) && (!endpoint.isEmpty()), "Illegal endpoint URL.");
    	SystemAssert.requireArgument(maxConn >= 2, "At least two connections are required.");
    	SystemAssert.requireArgument(timeout >= 1, "Timeout must be greater than 0.");
    	SystemAssert.requireArgument(reqTimeout >= 1, "Request timeout must be greater than 0.");
        try {
            URL url = new URL(endpoint);
            int port = url.getPort();

            SystemAssert.requireArgument(port != -1, "Endpoint must include explicit port.");
            connMgr = new PoolingHttpClientConnectionManager();
            connMgr.setMaxTotal(maxConn);
            connMgr.setDefaultMaxPerRoute(maxConn);

            String routePath = endpoint.substring(0, endpoint.lastIndexOf(":"));
            HttpHost host = new HttpHost(routePath, port);
            RequestConfig defaultRequestConfig = RequestConfig.custom().setConnectionRequestTimeout(reqTimeout).setConnectTimeout(timeout).build();

            connMgr.setMaxPerRoute(new HttpRoute(host), maxConn / 2);
            httpClient = HttpClients.custom().setConnectionManager(connMgr).setDefaultRequestConfig(defaultRequestConfig).build();
            cookieStore = new BasicCookieStore();
            httpContext = new BasicHttpContext();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        } catch (MalformedURLException ex) {
            throw new OrchestraException("Error initializing the Argus HTTP Client.", ex);
        }
        LOGGER.info("Argus HTTP Client initialized using " + endpoint);
        this.endpoint = endpoint;
    }

    //~ Methods **************************************************************************************************************************************

    /**
     * Closes the client connections and prepares the client for garbage collection. This method may be invoked on a client which has already been
     * disposed.
     */
    public void dispose() {
        try {
            logout();
            httpClient.close();
        } catch (IOException ex) {
            LOGGER.warn("The HTTP client failed to shutdown properly.", ex);
        } finally {
            disposed = true;
        }
    }

    void login(String username, String password) {
        String requestUrl = endpoint + "/auth/login";
        Credentials creds = new Credentials();

        creds.setPassword(password);
        creds.setUsername(username);

        HttpResponse response = null;

        try {
            StringEntity entity = new StringEntity(toJson(creds));
            response = executeHttpRequest(RequestType.POST, requestUrl, entity);
            EntityUtils.consume(response.getEntity());
        } catch (Exception ex) {
            throw new OrchestraException(ex);
        }
        if (response.getStatusLine().getStatusCode() != 200) {
            String message = response.getStatusLine().getReasonPhrase();

            throw new OrchestraException(message);
        }
        LOGGER.info("Logged in as " + username);
    }

    void logout() {
        String requestUrl = endpoint + "/auth/logout";
        HttpResponse response = null;

        try {
            response = executeHttpRequest(RequestType.GET, requestUrl, null);
            EntityUtils.consume(response.getEntity());
        } catch (Exception ex) {
            throw new OrchestraException(ex);
        }
        if (response.getStatusLine().getStatusCode() != 200) {
            String message = response.getStatusLine().getReasonPhrase();

            throw new OrchestraException(message);
        }
        LOGGER.info("Logout succeeded");
    }

    /**
     * DOCUMENT ME!
     *
     * @param   data  DOCUMENT ME!
     *
     * @throws  OrchestraException  DOCUMENT ME!
     */
    public void putMetricData(List<Metric> data) {
        String requestUrl = endpoint + "/collection/metrics";
        HttpResponse response = null;

        try {
            String json = toJson(data.toArray());

            response = executeHttpRequest(RequestType.POST, requestUrl, new StringEntity(json));
            EntityUtils.consume(response.getEntity());
        } catch (Exception ex) {
            throw new OrchestraException(ex);
        }
        if (response.getStatusLine().getStatusCode() != 200) {
            String message = response.getStatusLine().getReasonPhrase();

            throw new OrchestraException(message);
        }
        LOGGER.info("Posted {} metrics.", data.size());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   annotations  DOCUMENT ME!
     *
     * @throws  OrchestraException  DOCUMENT ME!
     */
    public void putAnnotationData(List<Annotation> annotations) {
        String requestUrl = endpoint + "/collection/annotations";
        HttpResponse response = null;

        try {
            response = executeHttpRequest(RequestType.POST, requestUrl, new StringEntity(toJson(annotations)));
            EntityUtils.consume(response.getEntity());
        } catch (Exception ex) {
            throw new OrchestraException(ex);
        }
        if (response.getStatusLine().getStatusCode() != 200) {
            String message = response.getStatusLine().getReasonPhrase();

            throw new OrchestraException(message);
        }
        LOGGER.info("Posted {} annotations.", annotations.size());
    }

            	
    /* Execute a request given by type requestType. */
    private HttpResponse executeHttpRequest(RequestType requestType, String url, StringEntity entity) throws IOException {
        HttpResponse httpResponse = null;

        if (entity != null) {
            entity.setContentType("application/json");
        }
        switch (requestType) {
            case POST:

                HttpPost post = new HttpPost(url);

                post.setEntity(entity);
                httpResponse = httpClient.execute(post, httpContext);
                break;
            case GET:

                HttpGet httpGet = new HttpGet(url);

                httpResponse = httpClient.execute(httpGet, httpContext);
                break;
            case DELETE:

                HttpDelete httpDelete = new HttpDelete(url);

                httpResponse = httpClient.execute(httpDelete, httpContext);
                break;
            case PUT:

                HttpPut httpput = new HttpPut(url);

                httpput.setEntity(entity);
                httpResponse = httpClient.execute(httpput, httpContext);
                break;
            default:
                throw new IllegalArgumentException(" Request Type " + requestType + " not a valid request type. ");
        }
        return httpResponse;
    }

    private <T> String toJson(T type) {
        try {
            return MAPPER.writeValueAsString(type);
        } catch (IOException ex) {
            throw new OrchestraException(ex);
        }
    }

    //~ Enums ****************************************************************************************************************************************

    /**
     * The request type to use.
     *
     * @author  Tom Valine (tvaline@salesforce.com)
     */
    public static enum RequestType {
        POST("post"),
        GET("get"),
        DELETE("delete"),
        PUT("put");

        private final String requestType;

        private RequestType(String requestType) {
            this.requestType = requestType;
        }

        /**
         * Returns the request type as a string.
         *
         * @return  The request type.
         */
        public String getRequestType() {
            return requestType;
        }
    }

    public Map<MetricQuery, List<Metric>> getMeticMap(MetricQuery query, String expression){
    	Map<MetricQuery, List<Metric>> complexMap=new HashMap<MetricQuery, List<Metric>>();
    	complexMap.put(query, getMetric(query,expression));
    	return complexMap;
    }
        
    public List<Metric> getMetric(MetricQuery query, String expression) {
        HttpResponse response = null;
        String responseAsString = null;
        try {
        	String payload=this.endpoint+"/metrics?expression="+URLEncoder.encode(expression, "UTF-8");
        	response = executeHttpRequest(RequestType.GET, payload, null);
            responseAsString = EntityUtils.toString(response.getEntity());
            EntityUtils.consume(response.getEntity());
        } catch (Exception ex) {
            throw new OrchestraException(ex);
        }
        return deSerializer(responseAsString);
    }
    
    //provided a string response,, return Metric template
    private List<Metric> deSerializer(String response){
    	List<Metric> metrics=new ArrayList<Metric>();
    	response="{\"object\": "+response+"}";
    	JSONObject obj = new JSONObject(response);
    	try{
    		JSONArray objectArray=obj.getJSONArray("object");
	    	for (int i = 0; i < objectArray.length(); i++){
	    		JSONObject current=objectArray.getJSONObject(i);
	    		Metric m=deSerializer(current);
	    		metrics.add(m);
	    	}
    	}catch(Exception ex){
        	if(obj.getJSONObject("object").has("message")){
        		throw new OrchestraException("DefaultJSONService Exception+"+obj.getJSONObject("object").getString("message")+"\n"+ex);
        	}
        	throw new OrchestraException(ex);
    	}
    	return metrics;
    }
    
    //provided one signal json object, return a signal metric
    private Metric deSerializer(JSONObject obj){
		String scope=obj.get("scope").toString();
		String namespace=obj.get("namespace").toString();
		String metricname=obj.get("metric").toString();

		Map<String,String> _tags=new HashMap<String,String>();
		JSONObject tags=obj.getJSONObject("tags");
		tags.keySet().stream().forEach(k -> _tags.put(k, tags.getString(k)));
		
		Map<Long, String> _datapoints=new HashMap<Long, String>();	
		JSONObject datapoints=obj.getJSONObject("datapoints");
		datapoints.keySet().stream().forEach(k -> _datapoints.put(Long.valueOf(k), datapoints.getString(k)));
		
		Metric metric=new Metric(scope,metricname);
		metric.setNamespace(namespace);
		metric.setDatapoints(_datapoints);
		metric.setTags(_tags);
    	return metric;
    }   
    
    /**
     * provided one metricQuery, return a Metric template
     * @param query
     * @return
     */
    private Metric getMetricByQuery(MetricQuery query){
    	String scope=query.getScope();
    	String namespace=query.getNamespace();
    	String metricname=query.getMetric();
    	Map<String,String> tags=query.getTags();
    	Metric metric=new Metric(scope,metricname);
    	metric.setNamespace(namespace);
    	metric.setTags(tags);
    	return metric;
    }
    
    /**
     * used for DiscoveryService
     * @param namespaceRegex
     * @param scopeRegex
     * @param metricRegex
     * @param tagKeyRegex
     * @param tagValueRegex
     * @param limit
     * @return
     */
    public List<MetricSchemaRecord> getDiscoveredMetricSchemaRecord(String namespaceRegex, String scopeRegex, String metricRegex, String tagKeyRegex, String tagValueRegex, int limit) {
    	StringBuilder urlBuilder = _buildBaseUrl(namespaceRegex, scopeRegex, metricRegex, tagKeyRegex, tagValueRegex, limit);
        String requestUrl = this.endpoint+urlBuilder.toString();
        System.out.println(requestUrl); 
        HttpResponse response;
        List<MetricSchemaRecord> listOfResult=null;
		try {
			response = executeHttpRequest(ArgusHttpClient.RequestType.GET, requestUrl, null);        
	        HttpEntity entity = response.getEntity();
	        String result = EntityUtils.toString(entity);
	        listOfResult=fromJson(result, new TypeReference<List<MetricSchemaRecord>>(){});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return listOfResult;
    }
       
    protected <T> T fromJson(String json, TypeReference typeRef) throws IOException {
        return (T) MAPPER.readValue(json, typeRef);
    }

    
    
    private StringBuilder _buildBaseUrl(String namespaceRegex, String scopeRegex, String metricRegex, String tagKeyRegex, String tagValueRegex,
            int limit) {
    		final String RESOURCE = "/discover/metrics/schemarecords";
            StringBuilder urlBuilder = new StringBuilder(RESOURCE).append("?");

            if (namespaceRegex != null) {
                urlBuilder.append("namespace=").append(namespaceRegex).append("&");
            }
            if (scopeRegex != null) {
                urlBuilder.append("scope=").append(scopeRegex).append("&");
            }
            if (metricRegex != null) {
                urlBuilder.append("metric=").append(metricRegex).append("&");
            }
            if (tagKeyRegex != null) {
                urlBuilder.append("tagk=").append(tagKeyRegex).append("&");
            }
            if (tagValueRegex != null) {
                urlBuilder.append("tagv=").append(tagValueRegex).append("&");
            }
            urlBuilder.append("limit=").append(limit);
            return urlBuilder;
        }
    
}
/* Copyright (c) 2014, Salesforce.com, Inc.  All rights reserved. */
