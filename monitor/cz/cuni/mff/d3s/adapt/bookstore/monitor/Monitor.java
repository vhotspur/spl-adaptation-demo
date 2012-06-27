package cz.cuni.mff.d3s.adapt.bookstore.monitor;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Validate;

import cz.cuni.mff.d3s.adapt.bookstore.agent.util.AccessAgent;

@Component
public class Monitor implements Runnable {

	private final String BOOK_STORE_CLASS = "cz.cuni.mff.d3s.adapt.bookstore.store.SimpleBookStore";
	private final String BOOK_STORE_INSTRUMENTED_METHOD = "buy";

	public Monitor() {

	}

	@Override
	public void run() {
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
