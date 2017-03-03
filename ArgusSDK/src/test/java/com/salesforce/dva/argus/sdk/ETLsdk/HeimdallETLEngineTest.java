package com.salesforce.dva.argus.sdk.ETLsdk;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author ethan.wang
 *
 */
public class HeimdallETLEngineTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void dev() {
		HeimdallETLEngine instance = HeimdallETLEngine.instanceOf();
		instance.execute(new String[]{
				"-op","reduce-normal",
				"-start","1488208396704",
				"-end","1488388396704",
				"-source","http://adhoc-db1-1-crd.eng.sfdc.net:8080/argusws",
				"-target","https://argus-ws.data.sfdc.net/argusws",
				"-user","SVC_DB_WORKLOADS",
				"-password","dBw0ak1oads!$",
				"-properties","src/test/resources/etl2.properties"
		});
	}
	
//	@Test
	public void devInterval() {
		HeimdallETLEngine instance = HeimdallETLEngine.instanceOf();
		instance.execute(new String[]{
				"-op","reduce-interval",
				"-start","1488208396704",
				"-end","1488388396704",
				"-source","http://adhoc-db1-1-crd.eng.sfdc.net:8080/argusws",
				"-target","https://argus-ws.data.sfdc.net/argusws",
				"-user","SVC_DB_WORKLOADS",
				"-password","dBw0ak1oads!$",
				"-properties","src/test/resources/etl_engine.properties"
		});
	}


}
