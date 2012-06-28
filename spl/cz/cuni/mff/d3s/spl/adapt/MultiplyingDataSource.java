package cz.cuni.mff.d3s.spl.adapt;

import java.util.ArrayList;
import java.util.Collection;

import cz.cuni.mff.d3s.spl.adapt.DataSource;

public class MultiplyingDataSource implements DataSource {
	private DataSource backend;
	private double multiplier;
	
	public MultiplyingDataSource(DataSource original, double constant) {
		backend = original;
		multiplier = constant;
	}

	@Override
	public Collection<Long> get() {
		Collection<Long> original = backend.get();
		Collection<Long> transformed = new ArrayList<>(original.size());
		for (Long l : original) {
			double newValue = ((double) l) * multiplier;
			transformed.add(Math.round(newValue));
		}
		return transformed;
	}

}
