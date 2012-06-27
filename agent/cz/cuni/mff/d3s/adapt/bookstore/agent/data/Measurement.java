package cz.cuni.mff.d3s.adapt.bookstore.agent.data;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;


public interface Measurement {
	/** Add time measured by given probe.
	 * 
	 * @param probe Probe identification.
	 * @param time Measured time in nanoseconds.
	 */
	void add(String probe, long time);
	/** Add time measured by given probe at given time.
	 * 
	 * @param probe Probe identification.
	 * @param time Measured time in nanoseconds.
	 * @param clock Wall-clock time in milliseconds since 1970
	 *              (Unix-time, but in milliseconds).
	 */
	void add(String probe, long time, long clock);
	
	/** Get measured data for given probe.
	 * 
	 * This method shall return only data not bound with wall clock
	 * time (i.e. those added via add(String, long)).
	 * 
	 * @param probe Probe identification.
	 * @return All measured data.
	 */
	Collection<Long> get(String probe);
	
	/** Get measured data in given time interval.
	 * 
	 * @see add
	 * 
	 * @param probe Probe identification.
	 * @param startTime Start (wall clock) time in milliseconds (inclusive).
	 * @param endTime End (wall clock) time in milliseconds (exclusive).
	 * @return Measured data in given time interval.
	 */
	Collection<Long> get(String probe, long startTime, long endTime);
	
	/** Dump all known data.
	 * 
	 * The format of the dump is implementation-specific.

	 */
	void dump(Writer output) throws IOException;
}
