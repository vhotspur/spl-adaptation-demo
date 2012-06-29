package cz.cuni.mff.d3s.adapt.bookstore.client;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;

import cz.cuni.mff.d3s.adapt.bookstore.services.Store;

@Component
public class DelayedClient extends RandomClient implements Runnable {

	@Requires
	private Store store;
	
	private String name;
	
	private Random random;
	
	private int remainingActionsBeforeLogout;
	
	private static AtomicInteger counter = new AtomicInteger(0);
	
	public DelayedClient() {
		int seed = getRandomSeed();
		random = new Random(seed);
		name = String.format("Delayed%d", seed);
		
		/* Exercise the random generator a bit. */
		int rnd = random.nextInt(1000);
		while (rnd > 0) {
			random.nextInt();
			rnd--;
		}
		
		remainingActionsBeforeLogout = random.nextInt(500);
	}

	@Override
	public void run() {
		int delay = 15 + random.nextInt(240);
		System.err.printf("DelayedClient.run(delay=%d)\n", delay);
		sleepSec(delay);
		randomStoreUse(store);
	}
	
	@Override
	public String getClientName() {
		return name;
	}

	@Override
	protected boolean continueShopping() {
		return remainingActionsBeforeLogout > 0;
	}
	
	@Override
	protected void beforeNextVisit() {
		sleepSec(random.nextInt(30));
		remainingActionsBeforeLogout = random.nextInt(500);
	}

	@Override
	protected void beforeNextAction() {
		sleep(random.nextInt(1000));
		remainingActionsBeforeLogout--;
	}
	
	@Override
	protected String getSearchTerm() {
		return generateRandomString(4, "abcdefghijklmnopqrstuvwxyz", random.nextInt());
	}
	
	@Validate
	public void start() {
		Thread thread = new Thread(this);
		thread.start();
	}
	
	private int getRandomSeed() {
		long randomFromTime = System.currentTimeMillis() % 997;
		return counter.addAndGet((int) randomFromTime);
	}

}
