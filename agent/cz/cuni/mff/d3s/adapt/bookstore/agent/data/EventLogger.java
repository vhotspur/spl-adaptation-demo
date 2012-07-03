package cz.cuni.mff.d3s.adapt.bookstore.agent.data;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class EventLogger {
	private static final Boolean guard = new Boolean(true);
	private static Writer writer = null;
	private static long startTimeMillis = 0;
	private static Map<String, Collection<LoggedEventListener>> listeners = new HashMap<>();
	
	public static void addListener(String action, LoggedEventListener listener) {
		Collection<LoggedEventListener> existing = listeners.get(action);
		if (existing == null) {
			existing = new LinkedList<>();
			listeners.put(action, existing);
		}
		existing.add(listener);
	}
	
	public static void recordViolation(long howMuchMicro) {
		recordEvent("violation");
	}
	
	public static void recordClientEnter() {
		recordEvent("enter");
	}
	
	public static void recordClientLeave() {
		recordEvent("leave");
	}
	
	public static void recordInstanceStart() {
		recordEvent("start");
	}
	
	public static void recordInstanceStop() {
		recordEvent("stop");
	}
	
	public static void imageRequested() {
		recordEvent("image");
	}
	
	private static void recordEvent(String entry) {
		init();
		
		long now = System.currentTimeMillis();
		long offset = now - startTimeMillis;
		
		String event = String.format("%d %s\n", offset, entry);
		synchronized (writer) {
			try {
				writer.append(event);
				writer.flush();
			} catch (IOException e) {
				System.err.printf("Failed to write: %s\n", e.getMessage());
			}
		}
		
		Collection<LoggedEventListener> whoToNotify = listeners.get(entry);
		if (whoToNotify != null) {
			for (LoggedEventListener listener : whoToNotify) {
				listener.action(offset);
			}
		}
	}
	
	private static void init() {
		synchronized (guard) {
			if (writer != null) {
				return;
			}
			
			try {
				writer = new FileWriter(System.getProperty("events.file", "events.txt"));
				writer.write("");
			} catch (IOException e) {
				System.err.printf("Failed to open: %s\n", e.getMessage());
				writer = null;
				return;
			}
			startTimeMillis = System.currentTimeMillis();
		}
	}
}
