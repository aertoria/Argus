/*
 * Copyright (c) 2016, Salesforce.com, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of Salesforce.com nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.salesforce.dva.argus.sdk.ETLsdk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.salesforce.dva.argus.sdk.ArgusService;

/**
 * Engine for running ETL processes.
 * 
 * <Comment> This class is wrap of HeimdallETL.-Ethan 
 * 
 * 
 * Command parameters look something like:
 * 
 * -op "reduce-interval"
 * -start 1480291200000
 * -end 1482105600000
 * -source http://adhoc-db1-1-crd.eng.sfdc.net:8080/argusws
 * -target https://argus-ws.data.sfdc.net/argusws
 * -user argus
 * -password foobar
 * -properties dataguard-prod-transport-lag.props
 *
 * @author Charles Kuo <ckuo@salesforce.com>
 * @revision aertoria <ethan.wang@salesforce.com>  Mar 1
 *
 */
public class HeimdallETLEngine implements AutoCloseable{
	/**
	 * Parsed command parameters.
	 */
	protected CommandParams commandParams;
	
	/**
	 * Source Argus service.
	 */
	private ArgusService sourceService;
	
	/**
	 * Target Argus serfvice.
	 */
	private ArgusService targetService;
	
	private static HeimdallETLEngine instance = null;
	
	/**
	 * Private constructor
	 * added by aertoria Mar1
	 */
	private HeimdallETLEngine(){}
	
	/**
	 * static factory method
	 * added by aertoria Mar1
	 * @return
	 */
	public static HeimdallETLEngine instanceOf(){
		if(HeimdallETLEngine.instance == null){
			HeimdallETLEngine.instance = new HeimdallETLEngine();
		}
		return HeimdallETLEngine.instance;
	}
	
	/**
	 * Main entry point.
	 * @param args
	 */
	public static void main(String[] args) {
		HeimdallETLEngine instance = HeimdallETLEngine.instanceOf();
		instance.execute(args);
	}
	
	@Override
	public void close() throws Exception {
		HeimdallETLEngine.instance = null;
	}
	
	/**
	 * Execute ETL process.
	 * @param args
	 */
	protected void execute(String[] args) {
		try {
			// parse command parameters
			commandParams = parseCommandParams(args);
			
			// create etl
			HeimdallETL etl;
			if ("reduce-interval".equalsIgnoreCase(commandParams.op)) {
				etl = new ReduceIntervalETL();
			} else if("reduce-normal".equalsIgnoreCase(commandParams.op)){
				etl = new ReduceNormalETL();
			}else {
				throw new Exception("Unrecognized operation: " + commandParams.op);
			}
			
			// properties
			Properties props = null;
			if (commandParams.properties != null) {
				try {
					File file = new File(commandParams.properties);
					FileInputStream inputStream = new FileInputStream(file);
					try {
						props = new Properties();
						props.load(inputStream);
					}
					finally {
						inputStream.close();
					}
				}
				catch (Exception e) {
					throw new Exception("Error parsing parameters file: " + commandParams.properties);
				}
			}
			
			etl.setStartTime(commandParams.start);
			etl.setEndTime(commandParams.end);
			etl.setReadOnly(commandParams.readonly);
			etl.setProperties(props);
			
			// init ETL
			System.out.println("args"+commandParams.args);
			etl.init();
			
			// connect to services
			connectServices();
			
			etl.setSourceService(sourceService);
			etl.setTargetService(targetService);
			
			// execute ETL
			etl.execute();
		}
		catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
		}
		finally {
			disconnectServices();
		}
	}
	
	/**
	 * Connect to Argus services.
	 * @throws Exception
	 */
	protected void connectServices() throws Exception {
		// source
		try {
			System.out.println("Connecting to source: " + commandParams.source);
			sourceService = ArgusService.getInstance(commandParams.source, commandParams.concurrency);
		}
		catch (IOException e) {
			throw new Exception("Could not connect to: " + commandParams.source);
		}
		
		try {
			sourceService.getAuthService().login(commandParams.user, commandParams.password);
		}
		catch (Exception e) {
			throw new Exception("Could not login to: " + commandParams.source);
		}
		
		// target
		try {
			System.out.println("Connecting to target: " + commandParams.target);
			targetService = ArgusService.getInstance(commandParams.target, commandParams.concurrency);
		}
		catch (IOException e) {
			throw new Exception("Could not connect to: " + commandParams.target);
		}
		
		try {
			targetService.getAuthService().login(commandParams.user, commandParams.password);
		}
		catch (Exception e) {
			throw new Exception("Could not login to: " + commandParams.target);
		}
	}
	
	/**
	 * Disconnect from Argus services.
	 */
	protected void disconnectServices() {
		try {
			if (sourceService != null)
				sourceService.close();
		}
		catch (Exception e) {
			
		}
		
		try {
			if (targetService != null)
				targetService.close();
		}
		catch (Exception e) {
			
		}
	}
	
	/**
	 * Parse command parameters.
	 * @param args
	 * @return
	 * @throws Exception
	 */
	protected CommandParams parseCommandParams(String[] args) throws Exception {
		// setup command line options
		Option operationOption = Option.builder("op")
				.hasArg()
                .desc("operation to perform")
                .required()
                .build();
		
		Option startOption = Option.builder("start")
				.hasArg()
                .desc("start time")
                .required()
                .build();
		
		Option endOption = Option.builder("end")
				.hasArg()
                .desc("end time")
                .required()
                .build();
		
		Option sourceOption = Option.builder("source")
				.hasArg()
                .desc("source")
                .required()
                .build();
		
		Option targetOption = Option.builder("target")
				.hasArg()
                .desc("target")
                .required()
                .build();
		
		Option userOption = Option.builder("user")
				.hasArg()
                .desc("Argus user")
                .required()
                .build();
		
		Option passwordOption = Option.builder("password")
				.hasArg()
                .desc("Argus password")
                .required()
                .build();
		
		Option concurrencyOption = Option.builder("concurrency")
				.hasArg()
                .desc("Number of connections")
                .build();
		
		Option readOnlyOption = Option.builder("readonly")
                .desc("If true, ETL should not write to target")
                .build();
		
		Option propertiesOption = Option.builder("properties")
				.hasArg()
                .desc("properties file path")
                .build();
		
		Options options = new Options();
		options.addOption(operationOption);
		options.addOption(startOption);
		options.addOption(endOption);
		options.addOption(sourceOption);
		options.addOption(targetOption);
		options.addOption(userOption);
		options.addOption(passwordOption);
		options.addOption(concurrencyOption);
		options.addOption(readOnlyOption);
		options.addOption(propertiesOption);
		
		// parse command line
		CommandLineParser parser = new DefaultParser();
		
	    CommandLine commandLine;
	    try {
	    	commandLine = parser.parse(options, args, true);
	    }
	    catch (ParseException e) {
	    	throw new Exception("Command syntax error: " + e.getLocalizedMessage());
	    }
	    
	    CommandParams params = new CommandParams();
	    
	    // operation
	    params.op = commandLine.getOptionValue("op");
	    
	    // start time
	    try {
	    	params.start = Long.parseLong(commandLine.getOptionValue("start"), 10);
	    }
	    catch (NumberFormatException e) {
	    	throw new Exception("Start time is not a valid number.");
	    }
	    
	    // end time
	    try {
	    	params.end = Long.parseLong(commandLine.getOptionValue("end"), 10);
	    }
	    catch (NumberFormatException e) {
	    	throw new Exception("End time is not a valid number.");
	    }
	    
	    // source
	    params.source = commandLine.getOptionValue("source");
	    
	    // target
	    params.target = commandLine.getOptionValue("target");
	    
	    // user
	    params.user = commandLine.getOptionValue("user");
	    
	    // password
	    params.password = commandLine.getOptionValue("password");
	    
	    // concurrency
	    if (commandLine.hasOption("concurrency")) {
		    try {
		    	params.concurrency = Integer.parseInt(commandLine.getOptionValue("concurrency"), 10);
		    }
		    catch (NumberFormatException e) {
		    	throw new Exception("Concurrency is not a valid number.");
		    }
	    }
	    
	    // readonly
	    params.readonly = commandLine.hasOption("readonly");
	    
	    // properties
	    params.properties = commandLine.getOptionValue("properties");
	    
	    // rest of arguments
	    params.args = commandLine.getArgs();
	    
	    return params;
	}
	
	/**
	 * Parsed command paramters.
	 * @author Charles Kuo <ckuo@salesforce.com>
	 *
	 */
	protected static class CommandParams {
		/**
		 * ETL operation to execute.
		 */
		public String op;
		
		/**
		 * Start time for ETL process.
		 */
		public long start;
		
		/**
		 * End time for ETL process.
		 */
		public long end;
		
		/**
		 * Endpoint for source Argus service.
		 */
		public String source;
		
		/**
		 * Endpoint for target Argus service.
		 */
		public String target;
		
		/**
		 * Username to login to Argus.
		 */
		public String user;
		
		/**
		 * Password to login to Argus.
		 */
		public String password;
		
		/**
		 * How many concurrent Argus connections.
		 */
		public int concurrency = 1;
		
		/**
		 * ETL will not write to target Argus.
		 */
		public boolean readonly = false;
		
		/**
		 * Path to properties file for ETL process.
		 */
		public String properties;
		
		/**
		 * Additional command arguments to pass to ETL process.
		 */
		public String[] args;
	}
}