package com.salesforce.dva.argus.sdk.transfer;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.salesforce.dva.argus.sdk.ArgusService;
import com.salesforce.dva.argus.sdk.entity.Metric;

public class TransferServiceTest {
	static ArgusService sourceSVC;
	static ArgusService targetSVC;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sourceSVC = ArgusService.getInstance("https://arguspm.ops.sfdc.net/argusws", 10);
		targetSVC = ArgusService.getInstance("https://argus-ws.data.sfdc.net/argusws", 10);
		sourceSVC.getAuthService().login("USERNAME", "PASSWORD_SAMPLE");
		targetSVC.getAuthService().login("USERNAME", "PASSWORD_SAMPLE");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		sourceSVC.close();
		targetSVC.close();
	}

	public void transfer() throws IOException {
		TransferService ts=TransferService.getTransferService(sourceSVC, targetSVC);
		String expression="-400d:production_splunk:apt_TopK{orgId=*,podId=*}:avg";
		try{
			ts.transfer(expression);
		}catch(Throwable t){}
	}
	
	
	@Test
	public void test() throws IOException {
		TransferService ts=TransferService.getTransferService(sourceSVC, targetSVC);
			
		List<String> workingRepo=Arrays.asList(
				/**P90 week resolution GROUP**/
				//"-700d:p90:podperc90{podId=*}:avg",
				//"-700d:p90_sandbox:podperc90{podId=*}:avg",
				
				/**P90 10min resolution SPLUNK GROUP**/
				//"-700d:-0d:production_splunk:podapt{podId=*}:avg",
				//"-700d:-0d:production_splunk:podcount{podId=*}:avg",
				//"-700d:-0d:production_splunk:podperc90{podId=*}:avg",
				//"-700d:-0d:sandbox_splunk:podapt{podId=*}:avg",
				//"-700d:-0d:sandbox_splunk:podcount{podId=*}:avg",
				//"-700d:-0d:sandbox_splunk:podperc90{podId=*}:avg",
				
				/**TopK group**/
				//"-700d:production_splunk:apt_TopK{orgId=*,podId=*}:avg",
				//"-700d:sandbox_splunk:count_TopK{orgId=*,podId=*}:avg"
				);
		
		String expression="sandbox_splunk:count_TopK{orgId=*,podId=*}:avg";
		
		schedule(expression,10).forEach(ex -> {
			System.out.println("EXECUTING EXPRESSION: "+ex);
			try {
				ts.transfer(ex);
				Thread.sleep(500);
			} catch (Exception e) {
				
				e.printStackTrace();
				try {
					Thread.sleep(2000);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
//		ts.readFromSource(Arrays.asList("-30d:-20d:sandbox_splunk:podcount{podId=*}:avg"));
//		ts.transfer("-30d:-20d:sandbox_splunk:podcount{podId=*}:avg");
	}
	
	
	private List<String> schedule(String expression,int intevalDays){
		List<String> expressions=new ArrayList<String>();
		for (int i=0;i<=700;i+=intevalDays){
			expressions.add("-"+(i+intevalDays)+"d:-"+(i)+"d:"+expression);
		}
		return expressions;
	}
	
}
