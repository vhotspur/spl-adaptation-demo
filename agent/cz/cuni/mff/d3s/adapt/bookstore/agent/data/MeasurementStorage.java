package cz.cuni.mff.d3s.adapt.bookstore.agent.data;

public class MeasurementStorage {
	private static Measurement measurements = new InMemoryMeasurement();
	
	public static void recordMeasurement(String id, long ns) {
		measurements.add(id, ns, System.currentTimeMillis());
	}
	
	public static Measurement getBackend() {
		return measurements;
	}
}
