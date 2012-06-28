package cz.cuni.mff.d3s.spl.adapt;

import java.util.Collection;

import cz.cuni.mff.d3s.adapt.bookstore.agent.data.Measurement;

public class SlidingTimeSlotDataSource implements DataSource {
	private boolean dumpOnGet = false;
	private String id;
	private Measurement datas;
	private long slotLengthMs;
	private long slotShiftToHistoryMs;
	
	public static SlidingTimeSlotDataSource createOneMinuteSlot(
			String name, Measurement dataStorage,
			long slotShiftMin) {
		return createSlotMinutes(name, dataStorage, slotShiftMin, 1);
	}
	
	public static SlidingTimeSlotDataSource createSlotMinutes(
			String name, Measurement dataStorage,
			long slotShiftMin, long slotSizeMin) {
		return new SlidingTimeSlotDataSource(name, dataStorage,
				60 * slotSizeMin, 60 * slotShiftMin);
	}
	
	public static SlidingTimeSlotDataSource createOneSecondSlot(
			String name, Measurement dataStorage,
			long slotShiftSec) {
		return createSlotSeconds(name, dataStorage, slotShiftSec, 1);
	}
	
	public static SlidingTimeSlotDataSource createSlotSeconds(
			String name, Measurement dataStorage,
			long slotShiftSec, long slotSizeSec) {
		return new SlidingTimeSlotDataSource(name, dataStorage,
				slotSizeSec, slotShiftSec);
	}
	
	private SlidingTimeSlotDataSource(String name, Measurement dataStorage,
			long slotLengthSec, long slotShiftSec) {
		id = name;
		datas = dataStorage;
		slotLengthMs = slotLengthSec * 1000;
		slotShiftToHistoryMs = slotShiftSec * 1000;
	}
	
	@Override
	public Collection<Long> get() {
		long endTime = System.currentTimeMillis() - slotShiftToHistoryMs;
		long startTime = endTime - slotLengthMs;
		Collection<Long> result = datas.get(id, startTime, endTime);
		if (dumpOnGet) {
			System.out.printf("Asking for data in span <%d, %d) = %d records.\n",
					startTime, endTime, result.size());
		}
		return result;
	}

}
