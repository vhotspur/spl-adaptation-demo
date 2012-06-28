package cz.cuni.mff.d3s.adapt.bookstore.monitor;

import java.util.HashMap;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;

import cz.cuni.mff.d3s.adapt.bookstore.agent.data.Measurement;
import cz.cuni.mff.d3s.adapt.bookstore.agent.data.MeasurementStorage;
import cz.cuni.mff.d3s.adapt.bookstore.agent.util.AccessAgent;
import cz.cuni.mff.d3s.adapt.bookstore.monitor.strategies.None;
import cz.cuni.mff.d3s.adapt.bookstore.monitor.strategies.Simple;
import cz.cuni.mff.d3s.adapt.bookstore.monitor.strategies.Strategy;
import cz.cuni.mff.d3s.adapt.bookstore.services.Replicable;

@Component
public class Monitor implements Runnable {

	private final String BOOK_STORE_CLASS = "cz.cuni.mff.d3s.adapt.bookstore.store.SimpleBookStore";
	private final String BOOK_STORE_INSTRUMENTED_METHOD = "fulltextSearch";
	private final String PROBE_NAME = "class:" + BOOK_STORE_CLASS.replace('.', '/') + "#" + BOOK_STORE_INSTRUMENTED_METHOD;
	
	private final long SLA_MAX_RESPONSE_TIME_MICROSEC = 4000;
	
	@Requires
	private Replicable replicable;
	
	private Map<String, Strategy> strategies;
	
	public Monitor() {
		strategies = new HashMap<>();
		strategies.put("none", new None());
		strategies.put("simple", new Simple());
	}

	@Override
	public void run() {
		Measurement measurement = MeasurementStorage.getBackend();		
		System.err.printf("MonitoringClient.run(replicable=%s)\n", replicable);
		
		/*
		 * Give it some time to make sure other components would be fully
		 * initialized.
		 */
		sleep(2);

		/* Instrument the selected method. */
		AccessAgent.instrumentMethod(BOOK_STORE_CLASS,
				BOOK_STORE_INSTRUMENTED_METHOD);
		
		/*
		 * And now, act upon the measured data.
		 */
		Strategy strategy = getStrategy();
		strategy.init(measurement, PROBE_NAME, SLA_MAX_RESPONSE_TIME_MICROSEC, replicable);
		while (true) {
			strategy.act();
			
			sleep(1);
		}
	}
	
	private void sleep(int sec) {
		try {
			Thread.sleep(sec * 1000);
		} catch (InterruptedException e) {
			/* Never mind, this is not critical. */
		}
	}
	
	private Strategy getStrategy() {
		String strategyName = System.getProperty("strategy", "none");
		Strategy strategy = strategies.get(strategyName);
		if (strategy == null) {
			return new None();
		} else {
			return strategy;
		}
	}
	
	@Validate
	public void start() {
		Thread thread = new Thread(this);
		thread.start();
	}
}
