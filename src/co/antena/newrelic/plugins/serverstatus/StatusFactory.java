package co.antena.newrelic.plugins.serverstatus;

import java.util.Map;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.AgentFactory;

public class StatusFactory extends AgentFactory {

	public StatusFactory() {
		super("co.antena.newrelic.plugins.serverstatus.json");
	}
	
	@Override
	public Agent createConfiguredAgent(Map<String, Object> properties) {
		String name = (String) properties.get("name");
		
		return new Status(name);
	}
}
