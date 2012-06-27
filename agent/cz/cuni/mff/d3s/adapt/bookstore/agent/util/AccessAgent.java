package cz.cuni.mff.d3s.adapt.bookstore.agent.util;

import java.lang.reflect.Method;

public class AccessAgent {
	private static final String AGENT_CLASSNAME = "cz.cuni.mff.d3s.adapt.bookstore.agent.InstrumentationAgent";
	private static Method agentMethodForInstrumenting = null;

	public static void instrumentMethod(String className, String methodName) {
		initAgentMetodInstrument();

		try {
			agentMethodForInstrumenting.invoke(null, className, methodName);
		} catch (Exception e) {
			// TODO: log the problem properly
			System.err.printf("WARNING: failed to instrument (%s.%s): %s.\n",
					className, methodName, e.getMessage());
		}
	}
	
	private static synchronized void initAgentMetodInstrument() {
		if (agentMethodForInstrumenting != null) {
			return;
		}
		/*
		 * Agent was loaded by the system class loader because no other class
		 * loader was available at that time.
		 */
		ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
		Class<?> agentClass;
		try {
			agentClass = systemClassLoader.loadClass(AGENT_CLASSNAME);
		} catch (ClassNotFoundException e) {
			System.err.printf(
					"ERROR: agent class not found.\n"
							+ "  Have you loaded the agent?\n"
							+ "  Agent class: %s.\n", AGENT_CLASSNAME);
			return;
		}
		try {
			agentMethodForInstrumenting = agentClass.getMethod(
					"instrumentMethod", String.class, String.class);
		} catch (Exception e) {
			System.err.printf("ERROR: instrumenting method not found: %s.\n",
					e.getMessage());
		}
	}

}
