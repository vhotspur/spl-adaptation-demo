package cz.cuni.mff.d3s.adapt.bookstore.monitor.strategies;

import cz.cuni.mff.d3s.adapt.bookstore.agent.data.Measurement;
import cz.cuni.mff.d3s.adapt.bookstore.services.Replicable;

public interface Strategy {
	void init(Measurement measurement, String probeName, long slaTimeMicros, Replicable replicable);
	void act();
	String getName();
}
