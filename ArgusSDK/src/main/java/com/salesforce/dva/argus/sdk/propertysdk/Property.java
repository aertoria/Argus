package com.salesforce.dva.argus.sdk.propertysdk;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 
 * @author aertoria (ethan.wang@salesforce.com)
 *
 */
final public class Property {
	final private String address;
	final private Properties prop;
	private Map<String,String> propertyMap=new HashMap<String,String>();;
	
	private Property(String propertyAddress){
		address=propertyAddress;
		prop=new Properties();
	}
	
	/**
	 * Static factory method
	 * @param propertyAddress
	 * @return
	 * @throws IOException 
	 */
	public static Property of(String propertyAddress) throws IOException{
		Property self=new Property(propertyAddress);
		
		InputStream input = new FileInputStream(self.address);
		self.prop.load(input);
		
		Enumeration<?> e = self.prop.propertyNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String value = self.prop.getProperty(key);
			self.propertyMap.put(key, value);
		}
		return self;
	}
	
	/**
	 * 
	 * @return
	 */
	public Map get(){
		assert(propertyMap!=null && propertyMap.size()>0):"you are not suppose to call this method, because the map is empty";
		final Map<String,String> _map = Collections.unmodifiableMap(new HashMap<String,String>(this.propertyMap));
		return _map;
	}

}
