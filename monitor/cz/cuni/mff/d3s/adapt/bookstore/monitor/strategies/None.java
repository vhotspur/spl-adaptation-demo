package cz.cuni.mff.d3s.adapt.bookstore.monitor.strategies;

import cz.cuni.mff.d3s.adapt.bookstore.agent.data.Measurement;
import cz.cuni.mff.d3s.adapt.bookstore.services.Replicable;

public class None implements Strategy {

	@Override
	public void init(Measurement measurement, String probeName,
			long slaTimeMicros, Replicable replicable) {
		/* Do nothing. */
	}

	@Override
	public void act() {
		/* Do nothing. */
	}

	@Override
	public String getName() {
		return "None";
	}

}
