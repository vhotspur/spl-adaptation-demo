package cz.cuni.mff.d3s.adapt.bookstore.monitor.strategies;

import cz.cuni.mff.d3s.adapt.bookstore.agent.data.Measurement;
import cz.cuni.mff.d3s.adapt.bookstore.services.Replicable;
import cz.cuni.mff.d3s.spl.adapt.DataSource;
import cz.cuni.mff.d3s.spl.adapt.SlaFormula;
import cz.cuni.mff.d3s.spl.adapt.SlaFormula.ContractCompliance;
import cz.cuni.mff.d3s.spl.adapt.SlidingTimeSlotDataSource;

public class Simple implements Strategy {
	
	private SlaFormula sla = null;
	private SlaFormula stopInstanceContract = null;
	private Replicable target = null;
	private int instanceCount = 1;

	@Override
	public void init(Measurement measurement, String probeName,
			long slaTimeMicros, Replicable replicable) {
		target = replicable;
		
		DataSource recentData = SlidingTimeSlotDataSource.createSlotSeconds(probeName, measurement, 0, 2);
		
		sla = new SlaFormula(slaTimeMicros * 1000);
		sla.bindDataSource(recentData);
		
		stopInstanceContract = new SlaFormula(slaTimeMicros * 1000 / 2);
		stopInstanceContract.bindDataSource(recentData);
	}
	
	@Override
	public void act() {
		ContractCompliance slaCompliance = sla.checkContract();
		if (slaCompliance == ContractCompliance.VIOLATES) {
			System.err.printf("SLA probably violated!\n");
			target.startInstance();
			instanceCount++;
		}
		
		if (instanceCount > 1) {
			ContractCompliance stopInstanceCompliance = stopInstanceContract.checkContract();
			if (stopInstanceCompliance == ContractCompliance.COMPLIES) {
				System.err.printf("Too many resources, freeing some.\n");
				target.stopInstance();
			}
		}
	}

}
