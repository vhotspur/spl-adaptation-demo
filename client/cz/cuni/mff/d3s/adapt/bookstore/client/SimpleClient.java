package cz.cuni.mff.d3s.adapt.bookstore.client;

import java.util.Random;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;

import cz.cuni.mff.d3s.adapt.bookstore.services.Store;

@Component
public class SimpleClient extends RandomClient implements Runnable {

	@Requires
	private Store store;
	
	private Random random = new Random(0);
	
	public SimpleClient() {
		
	}

	@Override
	public void run() {
		System.err.printf("SimpleClient.run()\n");
		randomStoreUse(store);
	}	

	@Override
	protected boolean continueShopping() {
		return true;
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

	
	

}
