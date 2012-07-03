package cz.cuni.mff.d3s.adapt.bookstore.monitor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;

import cz.cuni.mff.d3s.adapt.bookstore.agent.data.Measurement;
import cz.cuni.mff.d3s.adapt.bookstore.agent.data.MeasurementStorage;
import cz.cuni.mff.d3s.adapt.bookstore.agent.util.AccessAgent;
import cz.cuni.mff.d3s.adapt.bookstore.monitor.strategies.None;
import cz.cuni.mff.d3s.adapt.bookstore.monitor.strategies.Predict;
import cz.cuni.mff.d3s.adapt.bookstore.monitor.strategies.Simple;
import cz.cuni.mff.d3s.adapt.bookstore.monitor.strategies.Strategy;
import cz.cuni.mff.d3s.adapt.bookstore.services.Constants;
import cz.cuni.mff.d3s.adapt.bookstore.services.Replicable;
import cz.cuni.mff.d3s.adapt.bookstore.services.Store;

@Component
public class Monitor implements Runnable {

	private final String BOOK_STORE_CLASS = "cz.cuni.mff.d3s.adapt.bookstore.store.SimpleBookStore";
	private final String BOOK_STORE_INSTRUMENTED_METHOD = "fulltextSearch";
	private final String PROBE_NAME = "class:" + BOOK_STORE_CLASS.replace('.', '/') + "#" + BOOK_STORE_INSTRUMENTED_METHOD;
	
	@Requires
	private Replicable replicable;
	
	@Requires
	private Store store;
	
	private NanoHTTPD webserver;
	private Controller controller;
	private Graphs graphs;
	
	public Monitor() {
		graphs = new Graphs();
		
		Map<String, Strategy> strategies = new HashMap<>();
		strategies.put("none", new None());
		strategies.put("simple", new Simple());
		
		Measurement measurement = MeasurementStorage.getBackend();
		for (Strategy s : strategies.values()) {
			s.init(measurement, PROBE_NAME, Constants.SLA_REQUEST_LENGTH_SERVER_SIDE_MILLIS * 1000, replicable);
		}
		
		controller = new Controller(store, replicable, strategies);
		try {
			webserver = new NanoHTTPD(8888, new File("../wwwroot/").getAbsoluteFile(), graphs, controller);
		} catch (IOException e) {
			System.err.printf("Failed to start web server.\n");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		
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
		while (true) {
			Strategy strategy = controller.getStrategy();
			System.err.printf("Using strategy `%s'.\n", strategy.getName());
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
	
	@Validate
	public void start() {
		Thread thread = new Thread(this);
		thread.start();
	}
}
