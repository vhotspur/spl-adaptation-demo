package cz.cuni.mff.d3s.adapt.bookstore.database;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;

import cz.cuni.mff.d3s.adapt.bookstore.agent.data.EventLogger;
import cz.cuni.mff.d3s.adapt.bookstore.services.Book;
import cz.cuni.mff.d3s.adapt.bookstore.services.Database;
import cz.cuni.mff.d3s.adapt.bookstore.services.Replicable;

@Component
@Provides
public class MultithreadedDatabase implements Database, Replicable {
	
	private final int MAX_POOL_SIZE = 8;
	
	private Random random = new Random(0);
	private Set<FileBook> books = new HashSet<>();
	BlockingQueue<Runnable> tasks; 
	private ThreadPoolExecutor threadPool;
	
	public MultithreadedDatabase() {
		for (int i = 1; i < 9; i++) {
			addBook(String.format("Book #%d", i),
				String.format("book_%d.txt", i));
		}
		
		tasks = new ArrayBlockingQueue<>(100);
		threadPool = new ThreadPoolExecutor(1, 1, 100, TimeUnit.SECONDS, tasks);
		EventLogger.recordInstanceStart();
	}

	@Override
	public Book[] fulltextSearch(String term) {
		Searcher searcher = new Searcher(term);
		threadPool.execute(searcher);
		return searcher.getFoundBooks();
	}
	

	@Override
	public void startInstance() {
		addToPoolSize(1);
	}

	@Override
	public void stopInstance() {
		addToPoolSize(-1);
	}
	
	@Override
	public synchronized int getInstanceCount() {
		return threadPool.getCorePoolSize();
	}
	
	private synchronized void addToPoolSize(int amount) {
		int oldSize = threadPool.getCorePoolSize();
		int newSize = oldSize + amount;
		if (newSize < 1) {
			newSize = 1;
		}
		if (newSize > MAX_POOL_SIZE) {
			newSize = MAX_POOL_SIZE;
		}
		
		for (int i = oldSize; i < newSize; i++) {
			EventLogger.recordInstanceStart();
		}
		
		for (int i = oldSize; i > newSize; i--) {
			EventLogger.recordInstanceStop();
		}
		
		threadPool.setCorePoolSize(newSize);
		threadPool.setMaximumPoolSize(newSize);
	}
	
	private void addBook(String title, String filename) {
		String isbn = String.format("ISBN-%05d", books.size() + 1);
		int price = random.nextInt(20) + 5;
		
		FileBook book = new FileBook(title, isbn, price,
			"../books/" + filename);
		
		books.add(book);
	}
	
	private class Searcher implements Runnable {
		private String termLowered;
		private List<Book> found;
		/*
		 * Normal boolean does not have wait() and notify() and
		 * Boolean is immutable.
		 */
		private AtomicBoolean finished;
		
		public Searcher(String term) {
			termLowered = term.toLowerCase();
			found = new ArrayList<>();
			finished = new AtomicBoolean(false);
		}
		
		public Book[] getFoundBooks() {
			synchronized (finished) {
				while (!finished.get()) {
					try {
						finished.wait();
					} catch (InterruptedException e) {
						System.err.printf("WARNING: interrupted: %s.\n", e.getMessage());
					}
				}
			}
			return found.toArray(FileBook.EMPTY_ARRAY);
		}
		
		@Override
		public void run() {
			for (FileBook b : books) {
				if (b.contentMatches(termLowered)) {
					found.add(b);
				}
			}
			try {
				Thread.sleep(2 + random.nextInt(2), random.nextInt(10000));
			} catch (InterruptedException e) {
			}
			synchronized (finished) {
				finished.set(true);
				finished.notify();
			}
		}
	}
}
