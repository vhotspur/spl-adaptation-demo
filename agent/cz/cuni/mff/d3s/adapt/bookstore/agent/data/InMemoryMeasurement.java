package cz.cuni.mff.d3s.adapt.bookstore.agent.data;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class InMemoryMeasurement implements Measurement {
	private class ProbeData {
		private SortedMap<Long, Long> clockData;
		private List<Long> data;
		public ProbeData() {
			clockData = new TreeMap<>();
			data = new LinkedList<>();
		}
		public void add(long time, long clock) {
			clockData.put(clock, time);
		}
		public void add(long time) {
			data.add(time);
		}
		public Collection<Long> getNonClockData() {
			return data;
		}
		public Collection<Long> getClockData() {
			return getClockData(Long.MIN_VALUE, Long.MAX_VALUE);
		}
		
		public Collection<Long> getClockData(long start, long end) {
			SortedMap<Long, Long> submap = clockData.subMap(start, end);
			return submap.values();
		}
	}
	private Map<String, ProbeData> data;
	
	public InMemoryMeasurement() {
		data = new HashMap<>();
	}
	
	@Override
	public void add(String probe, long time) {
		ProbeData pd = getProbeData(probe);
		pd.add(time);
	}

	@Override
	public void add(String probe, long time, long clock) {
		ProbeData pd = getProbeData(probe);
		pd.add(time, clock);
	}

	@Override
	public Collection<Long> get(String probe) {
		ProbeData pd = getProbeData(probe);
		return pd.getNonClockData();
	}

	@Override
	public Collection<Long> get(String probe, long startTime, long endTime) {
		ProbeData pd = getProbeData(probe);
		return pd.getClockData(startTime, endTime);
	}
	
	protected ProbeData getProbeData(String id) {
		ProbeData d = data.get(id);
		if (d == null) {
			d = new ProbeData();
			data.put(id, d);
		}
		return d;
	}

	@Override
	public void dump(Writer output) throws IOException {
		output.write("InMemoryMeasurements:\n");
		for (Map.Entry<String, ProbeData> entry : data.entrySet()) {
			ProbeData pd = entry.getValue();
			Collection<Long> nonClockData = pd.getNonClockData();
			Collection<Long> clockData = pd.getClockData();
			output.write(String.format("  %s: any=%d, clock=%d\n", entry.getKey(),
					nonClockData.size(), clockData.size()));
			output.write("   Non-clock: ");
			for (Long l : nonClockData) {
				output.write(String.format(" %d", l));
			}
			output.write("\n   Clock:    ");
			for (Long l : clockData) {
				output.write(String.format(" %d", l));
			}
			output.write("\n");
		}
		output.write("---------------------\n");
		output.flush();
	}

}
