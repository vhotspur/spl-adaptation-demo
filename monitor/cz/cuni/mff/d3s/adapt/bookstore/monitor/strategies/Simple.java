package cz.cuni.mff.d3s.adapt.bookstore.monitor.strategies;

import cz.cuni.mff.d3s.adapt.bookstore.agent.data.Measurement;
import cz.cuni.mff.d3s.adapt.bookstore.services.Replicable;
import cz.cuni.mff.d3s.spl.adapt.DataSource;
import cz.cuni.mff.d3s.spl.adapt.SlaFormula;
import cz.cuni.mff.d3s.spl.adapt.SlaFormula.ContractCompliance;
import cz.cuni.mff.d3s.spl.adapt.SlidingTimeSlotDataSource;

public class Simple implements Strategy {
	
	private SlaFormula sla = null;
	private Replicable target = null;

	@Override
	public void init(Measurement measurement, String probeName,
			long slaTimeMicros, Replicable replicable) {
		target = replicable;
		
		DataSource recentData = SlidingTimeSlotDataSource.createSlotSeconds(probeName, measurement, 0, 2);
		
		sla = new SlaFormula(slaTimeMicros * 1000);
		sla.bindDataSource(recentData);
	}
	
	@Override
	public void act() {
		ContractCompliance slaCompliance = sla.checkContract();
		if (slaCompliance == ContractCompliance.VIOLATES) {
			System.err.printf("SLA probably violated!\n");
			target.startInstance();
		}
	}

}
