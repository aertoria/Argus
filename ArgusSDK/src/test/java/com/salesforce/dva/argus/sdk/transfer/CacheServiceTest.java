package com.salesforce.dva.argus.sdk.transfer;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.salesforce.dva.argus.sdk.AbstractTest;
import com.salesforce.dva.argus.sdk.ArgusService;
import com.salesforce.dva.argus.sdk.entity.Metric;


/**
 * TODO: 
 * 1,mutliThreading
 * 2,ProgressBar
 * 3,Error handling.Error detection for re-run
 * 
 * @author ethan.wang
 *
 */
public class CacheServiceTest {
	static ArgusService sourceSVC;
	static ArgusService targetSVC;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sourceSVC = ArgusService.getInstance("http://ewang-ltm.internal.salesforce.com:8080/argusws", 10);
		//sourceSVC = ArgusService.getInstance("https://argus-ws.data.sfdc.net/argusws", 10);
		targetSVC = ArgusService.getInstance("https://argus-ws.data.sfdc.net/argusws", 10);
		sourceSVC.getAuthService().login("**removed**", "****");
		targetSVC.getAuthService().login("**removed**", "****");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		sourceSVC.close();
		targetSVC.close();
	}

	
	@Test
	public void transfer() throws IOException {
		TransferService ts=TransferService.getTransferService(sourceSVC, targetSVC);
//		String expression="HEIMDALL("
//				+ "1477094400:1477180800:core.CHI.SP3.na5:SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode*.Last_1_Min_Avg{device=*-app*-*.ops.sfdc.net}:avg, "
//				+ "1477094400:1477180800:core.CHI.SP3.na5:SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode*.Last_1_Min_Avg{device=*-app*-*.ops.sfdc.net}:avg,"
//				+ "#IMPACTPOD#)";
		List<String> pods=getPods();
		int count=pods.size();
		getPods().forEach(exp -> {
			makeATransfer(ts,exp);
		});
	}
	
	private void makeATransfer(TransferService ts, String exp){
		String sourceExp=getSource(exp);
		String targetScope=getTarget(exp);
		System.out.println("Loading to "+targetScope+" from expression"+sourceExp);
		try {
			//List<Metric> ms=ts.readFromSource(Arrays.asList(sourceExp));
			//ms.forEach(m -> System.out.println(m.getMetric()+m.getDatapoints()));
			ts.transfer(sourceExp,targetScope,"IMPACTPOD");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	private List<String> getPods() throws IOException{
		InputStream is=getClass().getResourceAsStream("/pods.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		List<String> pods=new ArrayList<String>();
		while ((line = br.readLine()) != null) {
			pods.add(line);
		}
		return pods;
	}
	
	
	private String getSource(String t) {
		String expression="HEIMDALL("
				+ "1477094400:1477180800:core."+t+":SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode*.Last_1_Min_Avg{device=*-app*-*.ops.sfdc.net}:avg, "
				+ "1477094400:1477180800:core."+t+":SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode*.Last_1_Min_Avg{device=*-app*-*.ops.sfdc.net}:avg,"
				+ "#IMPACTPOD#)";
		return expression;
	}
	
	
	private String getTarget(String t) {
		String expression="REDUCEDTEST.core."+t;
		return expression;
	}
	
}

