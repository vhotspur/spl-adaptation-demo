package cz.cuni.mff.d3s.adapt.bookstore.monitor;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import cz.cuni.mff.d3s.adapt.bookstore.client.RandomClient;
import cz.cuni.mff.d3s.adapt.bookstore.monitor.strategies.None;
import cz.cuni.mff.d3s.adapt.bookstore.monitor.strategies.Strategy;
import cz.cuni.mff.d3s.adapt.bookstore.services.Constants;
import cz.cuni.mff.d3s.adapt.bookstore.services.Replicable;
import cz.cuni.mff.d3s.adapt.bookstore.services.Store;

public class Controller {
	private class UserDrivenClient extends RandomClient implements Runnable {
		private boolean stopSoon = false;
		private Random random = new Random();
		private Store store;
		
		public UserDrivenClient(Store store) {
			this.store = store;
		}
		
		public synchronized void stop() {
			stopSoon = true;
		}

		@Override
		protected synchronized boolean continueShopping() {
			return !stopSoon;
		}
		
		@Override
		protected synchronized boolean shallVisitAgain() {
			return !stopSoon;
		}

		@Override
		protected void beforeNextVisit() {
		}

		@Override
		protected void beforeNextAction() {
			int sleepLength = Constants.CLIENT_PAUSE_BETWEEN_ACTIONS_FIXED_MILLIS
					+ random.nextInt(Constants.CLIENT_PAUSE_BETWEEN_ACTIONS_VARIABLE_MILLIS);
			sleep(sleepLength);
		}

		@Override
		protected String getSearchTerm() {
			return generateRandomString(4, "abcdefghijklmnopqrstuvwxyz", random.nextInt());
		}

		@Override
		protected String getClientName() {
			return "UserDriven";
		}
		
		@Override
		public void run() {
			randomStoreUse(store);
		}
	}
	
	private Store store;
	private Replicable replicable;
	
	private Set<UserDrivenClient> clients = new HashSet<>();

	private Map<String, Strategy> strategies;
	private Strategy currentStrategy;
	
	public Controller(Store store, Replicable replicable, Map<String, Strategy> strategies) {
		this.store = store;
		this.replicable = replicable;
		this.strategies = strategies;
		currentStrategy = getDefaultStrategy();
	}
	
	public synchronized void addClient() {
		UserDrivenClient client = new UserDrivenClient(store);
		Thread thread = new Thread(client);
		clients.add(client);
		thread.start();
	}
	
	public synchronized void stopClient() {
		UserDrivenClient arr[] = clients.toArray(new UserDrivenClient[0]);
		if (arr.length == 0) {
			return;
		}
		
		int index = arr.length / 2;
		
		UserDrivenClient victim = arr[index];
		
		victim.stop();
		clients.remove(victim);
	}
	
	public synchronized int getClientCount() {
		return clients.size();
	}
	
	public int getInstanceCount() {
		return replicable.getInstanceCount();
	}

	public synchronized Strategy getStrategy() {
		assert currentStrategy != null;
		return currentStrategy;
	}
	
	public synchronized void changeStrategy(String newName) {
		Strategy tmp = strategies.get(newName);
		if (tmp == null) {
			return;
		}
		currentStrategy = tmp;
	}
	
	private Strategy getDefaultStrategy() {
		String strategyName = System.getProperty("strategy", "");
		Strategy strategy = strategies.get(strategyName);
		if (strategy == null) {
			strategy = strategies.get("none");
		}
		assert strategy != null;
		return strategy;
	}
}
