package cz.cuni.mff.d3s.adapt.bookstore.monitor;

import java.util.Collection;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;

import cz.cuni.mff.d3s.adapt.bookstore.agent.data.Measurement;
import cz.cuni.mff.d3s.adapt.bookstore.agent.data.MeasurementStorage;
import cz.cuni.mff.d3s.adapt.bookstore.agent.util.AccessAgent;
import cz.cuni.mff.d3s.adapt.bookstore.services.Replicable;

@Component
public class Monitor implements Runnable {

	private final String BOOK_STORE_CLASS = "cz.cuni.mff.d3s.adapt.bookstore.store.SimpleBookStore";
	private final String BOOK_STORE_INSTRUMENTED_METHOD = "fulltextSearch";
	private final String PROBE_NAME = "class:" + BOOK_STORE_CLASS.replace('.', '/') + "#" + BOOK_STORE_INSTRUMENTED_METHOD;
	
	private final double SLA_MAX_RESPONSE_TIME = 4000.;
	
	@Requires
	private Replicable replicable;
	
	public Monitor() {

	}

	@Override
	public void run() {
		Measurement measurement = MeasurementStorage.getBackend();
		
		System.err.printf("MonitoringClient.run()\n");
		
		/*
		 * Give it some time to make sure other components would be fully
		 * initialized.
		 */
		sleep(2);

		/* Instrument the selected method. */
		AccessAgent.instrumentMethod(BOOK_STORE_CLASS,
				BOOK_STORE_INSTRUMENTED_METHOD);
		
		/* Give it some time to run smoothly... */
		sleep(2);
		/* ...and instrument the same again. That should fail. */
		AccessAgent.instrumentMethod(BOOK_STORE_CLASS,
				BOOK_STORE_INSTRUMENTED_METHOD);
		
		/*
		 * And now, act upon the data.
		 */
		while (true) {
			long now = System.currentTimeMillis();
			long interval = 1000 * 5;
			Collection<Long> recentData = measurement.get(PROBE_NAME, now - interval, now);
			
			double recentMean = computeMeanUs(recentData);
			
			System.err.printf("          now=%d (recent=%1.0fus [%d samples])\n",
					now,
					recentMean, recentData.size());
			
			if (recentMean * 1.05 > SLA_MAX_RESPONSE_TIME) {
				System.err.printf("WARNING: SLA probably violated (starting new instance)!\n");
				replicable.startInstance();
			}
			
			if (recentMean * 1.8 < SLA_MAX_RESPONSE_TIME) {
				System.err.print("Probably too many resources, stopping some.\n");
				replicable.stopInstance();
			}
			
			sleep(5);
		}
		
	}
	
	private double computeMeanUs(Collection<Long> input) {
		int size = input.size();
		if (size == 0) {
			return Double.NaN;
		}
		
		double sum = 0;
		for (Long l : input) {
			sum += l / 1000;
		}
		return sum / size;
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
