package co.antena.newrelic.plugins.serverstatus;

import java.util.logging.Level;

import com.newrelic.metrics.publish.Runner;
import com.newrelic.metrics.publish.binding.Context;
import com.newrelic.metrics.publish.configuration.ConfigurationException;


public class Main {	
    public static void main(String[] args) {
    	Runner runner = new Runner();
    	
    	runner.add(new StatusFactory());
    	
		try {
			Context.getLogger().log(Level.INFO,"Antena Server Status New Relic plugin Started!");
	    	runner.setupAndRun();
		} catch (ConfigurationException e) {
			e.printStackTrace();
    		System.err.println("Error configuring");
    		System.exit(-1);
		}
    	
    }
    
}
