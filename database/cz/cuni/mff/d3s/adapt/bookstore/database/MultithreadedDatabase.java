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

import cz.cuni.mff.d3s.adapt.bookstore.services.Book;
import cz.cuni.mff.d3s.adapt.bookstore.services.Database;

@Component
@Provides
public class MultithreadedDatabase implements Database {
	
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
	}

	@Override
	public Book[] fulltextSearch(String term) {
		Searcher searcher = new Searcher(term);
		threadPool.execute(searcher);
		return searcher.getFoundBooks();
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
