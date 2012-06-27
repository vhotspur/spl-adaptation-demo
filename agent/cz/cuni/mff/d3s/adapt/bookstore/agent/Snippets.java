package cz.cuni.mff.d3s.adapt.bookstore.agent;

import ch.usi.dag.disl.annotation.After;
import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.annotation.SyntheticLocal;
import ch.usi.dag.disl.marker.BodyMarker;
import ch.usi.dag.disl.staticcontext.MethodStaticContext;

public class Snippets {
	@SyntheticLocal
	private static long startTime = 0;

	@Before(marker = BodyMarker.class, guard = InstrumentationAgent.class)
	public static void startMeasuring(MethodStaticContext sc) {
		startTime = System.nanoTime();
	}

	@After(marker = BodyMarker.class, guard = InstrumentationAgent.class)
	public static void endMeasureAnnounceResults(MethodStaticContext sc) {
		long now = System.nanoTime();
		long runLengthUs = (now - startTime) / 1000;
		
		String id = "class:" + sc.thisClassName() + "#" + sc.thisMethodName();
		
		System.err.printf(">>> %s: %dus\n", id, runLengthUs);		
	}
}
