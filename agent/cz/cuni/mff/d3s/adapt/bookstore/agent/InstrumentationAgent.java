package cz.cuni.mff.d3s.adapt.bookstore.agent;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import ch.usi.dag.disl.annotation.GuardMethod;
import ch.usi.dag.disl.staticcontext.MethodStaticContext;

public class InstrumentationAgent implements Runnable {
	public static final boolean DEBUG = false;

	private static InstrumentationAgent instance;

	private Instrumentation instrumentation;
	private ClassTransformer transformer = new ClassTransformer(Snippets.class);
	private BlockingQueue<Class<?>> classesToReload = new ArrayBlockingQueue<>(
			100);

	private static Set<String> instrumentedMethods = new HashSet<>();

	private static Set<ClassLoader> knownClassLoaders = Collections
			.newSetFromMap(new WeakHashMap<ClassLoader, Boolean>());

	public static void premain(String agentArguments,
			Instrumentation instrumentation) {
		instance = new InstrumentationAgent(instrumentation);
		Thread thr = new Thread(instance);
		thr.start();
	}

	public static void registerClassLoader(ClassLoader loader) {
		if (loader == null) {
			return;
		}

		boolean newLoader = knownClassLoaders.add(loader);
		if (newLoader) {
			if (DEBUG) {
				System.err.printf(
						"Registered class loader %s (parent = %s).\n", loader,
						loader.getParent());
			}
		}
	}

	public static void instrumentMethod(String className, String methodName) {
		if (DEBUG) {
			System.err.printf(
					"InstrumentationAgent.instrumentMethod('%s', '%s')\n",
					className, methodName);
		}

		String fullName = className + "#" + methodName;
		synchronized (instrumentedMethods) {
			boolean alreadyInstrumented = !instrumentedMethods.add(fullName);
			if (alreadyInstrumented) {
				System.err.printf("WARNING: refusing to re-instrument %s.\n",
						fullName);
				return;
			}
		}

		/*
		 * Try to get this class from all known class loaders.
		 */
		int hits = 0;
		for (ClassLoader loader : knownClassLoaders) {
			boolean reloadOk = reloadClassInClassLoader(loader, className);
			if (reloadOk) {
				hits++;
			}
		}

		if (hits == 0) {
			System.err.printf("ERROR: failed to instrument %s.\n", className);
		}
	}

	private static boolean reloadClassInClassLoader(ClassLoader loader,
			String className) {
		Class<?> klass;
		try {
			klass = loader.loadClass(className);
		} catch (ClassNotFoundException e) {
			/*
			 * Not a problem, we expect that this could happen.
			 */
			return false;
		}

		assert (klass != null);

		try {
			instance.classesToReload.put(klass);
		} catch (InterruptedException e) {
			/*
			 * This shall not happen. We ought to report this.
			 */
			System.err.printf(
					"WARNING: blocking queue operation interrupted (%s).\n"
							+ "  %s (loader %s) will not be instrumented.\n",
					e.getMessage(), className, loader);
			return false;
		}

		return true;
	}

	@GuardMethod
	public static boolean shallInstrumentMethod(MethodStaticContext ctx) {
		String name = ctx.thisClassName().replace('/', '.') + "#"
				+ ctx.thisMethodName();

		return instrumentedMethods.contains(name);
	}

	private InstrumentationAgent(Instrumentation instr) {
		instrumentation = instr;
		instrumentation.addTransformer(transformer, true);
	}

	@Override
	public void run() {
		System.err.printf("InstrumentationAgent started...\n");

		while (true) {
			Class<?> classToTransform;
			try {
				classToTransform = classesToReload.take();
			} catch (InterruptedException e) {
				System.err.printf("Taking from blocking queue failed.\n");
				e.printStackTrace();
				continue;
			}
			try {
				if (DEBUG) {
					System.err.printf("About to retransform %s.\n",
							classToTransform.toString());
				}
				transformer.enableTransformation();
				instrumentation.retransformClasses(classToTransform);
			} catch (UnmodifiableClassException e) {
				System.err.printf("Retransformation failed.\n");
				e.printStackTrace();
			}
		}
	}
}
