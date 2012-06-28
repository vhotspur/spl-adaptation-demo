package cz.cuni.mff.d3s.adapt.bookstore.monitor;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;

import cz.cuni.mff.d3s.adapt.bookstore.agent.data.Measurement;
import cz.cuni.mff.d3s.adapt.bookstore.agent.data.MeasurementStorage;
import cz.cuni.mff.d3s.adapt.bookstore.agent.util.AccessAgent;
import cz.cuni.mff.d3s.adapt.bookstore.services.Replicable;
import cz.cuni.mff.d3s.spl.adapt.DataSource;
import cz.cuni.mff.d3s.spl.adapt.SlaFormula;
import cz.cuni.mff.d3s.spl.adapt.SlaFormula.ContractCompliance;
import cz.cuni.mff.d3s.spl.adapt.SlidingTimeSlotDataSource;

@Component
public class Monitor implements Runnable {

	private final String BOOK_STORE_CLASS = "cz.cuni.mff.d3s.adapt.bookstore.store.SimpleBookStore";
	private final String BOOK_STORE_INSTRUMENTED_METHOD = "fulltextSearch";
	private final String PROBE_NAME = "class:" + BOOK_STORE_CLASS.replace('.', '/') + "#" + BOOK_STORE_INSTRUMENTED_METHOD;
	
	private final double SLA_MAX_RESPONSE_TIME_MICROSEC = 4000.;
	
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
			DataSource recentData = SlidingTimeSlotDataSource.createSlotSeconds(PROBE_NAME, measurement, 0, 2);
			
			SlaFormula sla = new SlaFormula(SLA_MAX_RESPONSE_TIME_MICROSEC * 1000);
			sla.bindDataSource(recentData);
			
			ContractCompliance slaCompliance = sla.checkContract();
			if (slaCompliance == ContractCompliance.VIOLATES) {
				System.err.printf("SLA probably violated!\n");
				replicable.startInstance();
			}
			
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
