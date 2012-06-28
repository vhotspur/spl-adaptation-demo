package cz.cuni.mff.d3s.spl.adapt;

import java.util.Collection;

public class SlaFormula {
	public static final int MIN_SAMPLE_COUNT = 10;
	
	public enum ContractCompliance {
		CANNOT_COMPUTE,
		COMPLIES,
		VIOLATES
	};
	
	private double limit;
	private DataSource source;
	
	public SlaFormula(double limit) {
		this.limit = limit;
	}
	
	public void bindDataSource(DataSource src) {
		source = src;
	}
	
	public ContractCompliance checkContract() {
		Collection<Long> data = source.get();
		if (data.size() < MIN_SAMPLE_COUNT) {
			return ContractCompliance.CANNOT_COMPUTE;
		}
		
		double actualMean = getSampleMean(data);
		
		if (actualMean * 1.1 > limit) {
			return ContractCompliance.VIOLATES;
		}
		
		return ContractCompliance.COMPLIES;
	}
	
	protected double getSampleMean(Collection<Long> samples) {
		double sum = 0;
		for (Long l : samples) {
			sum += (double) l;
		}
		return sum / (double) samples.size();
	}
}
