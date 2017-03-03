package com.salesforce.dva.argus.sdk.ETLsdk;


/**
 * Reduce metric from expression a to expressoin b, with mutliple threads
 * @author aertoria <ethan.wang@salesforce.com>
 */
public class ReduceNormalETL extends HeimdallETL{

	private String ETLFILE;
	private String PODFILE;
	
	@Override
	public void init() throws Exception {
		// TODO Auto-generated method stub
		ETLFILE = properties.getProperty("ETLFILE");
		PODFILE = properties.getProperty("PODFILE");
	}

	@Override
	public void execute() throws Exception {
		// TODO Auto-generated method stub
		System.out.println(ETLFILE+""+PODFILE);
		CachedETL.main(new String[]{ETLFILE,PODFILE});
	}

}
