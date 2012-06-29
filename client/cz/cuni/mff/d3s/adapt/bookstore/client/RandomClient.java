package cz.cuni.mff.d3s.adapt.bookstore.client;

import java.util.Random;

import cz.cuni.mff.d3s.adapt.bookstore.agent.data.EventLogger;
import cz.cuni.mff.d3s.adapt.bookstore.services.Book;
import cz.cuni.mff.d3s.adapt.bookstore.services.Store;
import cz.cuni.mff.d3s.adapt.bookstore.services.Wallet;

public abstract class RandomClient {
	private final int SLA_REQUEST_COMPLETED_MICROSEC = 10000;

	protected final void randomStoreUse(Store store) {
		Random random = new Random(0);
		Book[] lastSearchResults = new Book[0];
		Wallet wallet = new MillionareWallet();
		
		while (true) {
			System.err.printf("Client %s enters the shop.\n", getClientName());
			
			EventLogger.recordClientEnter();
		
			while (continueShopping()) {
				long start = System.nanoTime();
				int action = random.nextInt(10000);
				if (action == 0) {
					if (lastSearchResults.length > 0) {
						int index = random.nextInt(lastSearchResults.length);
						store.buy(lastSearchResults[index], wallet);
					}
				} else {
					lastSearchResults = store.fulltextSearch(getSearchTerm());
				}
				long end = System.nanoTime();
				long diffMicro = (end - start) / 1000;
				if (diffMicro > SLA_REQUEST_COMPLETED_MICROSEC) {
					EventLogger.recordViolation(diffMicro);
				}
				
				beforeNextAction();
			}
		
			EventLogger.recordClientLeave();
			
			System.err.printf("Client %s leaves the shop.\n", getClientName());
			
			beforeNextVisit();
		}
	}
	
	abstract protected boolean continueShopping();
	abstract protected void beforeNextVisit();
	abstract protected void beforeNextAction();
	abstract protected String getSearchTerm();
	abstract protected String getClientName();
	
	protected void sleepSec(int sec) {
		sleep(sec * 1000);
	}
	
	protected void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			/* Never mind, this is not critical. */
		}
	}
	
	protected String generateRandomString(int length, String allowedCharacters, int seed) {
		Random rnd = new Random(seed);
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < length; i++) {
			int randomIndex = rnd.nextInt(allowedCharacters.length());
			char nextCharacter = allowedCharacters.charAt(randomIndex);
			result.append(nextCharacter);
		}
		return result.toString();
	}
	
	private class MillionareWallet implements Wallet {
		@Override
		public boolean pay(int amount) {
			return true;
		}
	}
}
