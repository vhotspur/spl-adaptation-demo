package cz.cuni.mff.d3s.adapt.bookstore.monitor;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import cz.cuni.mff.d3s.adapt.bookstore.client.RandomClient;
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
	
	private Set<UserDrivenClient> clients = new HashSet<>();
	
	public Controller(Store store) {
		this.store = store;
	}
	
	public synchronized void addClient() {
		UserDrivenClient client = new UserDrivenClient(store);
		Thread thread = new Thread(client);
		clients.add(client);
		thread.start();
	}
	
	public synchronized void stopClient() {
		UserDrivenClient arr[] = clients.toArray(new UserDrivenClient[0]);
		int index = arr.length / 2;
		
		UserDrivenClient victim = arr[index];
		
		victim.stop();
		clients.remove(victim);
	}
}
