package cz.cuni.mff.d3s.adapt.bookstore.database;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;

import cz.cuni.mff.d3s.adapt.bookstore.services.Book;
import cz.cuni.mff.d3s.adapt.bookstore.services.Database;

@Component
@Provides
public class FileBasedDatabase implements Database {
	
	private Random random = new Random(0);
	private Set<FileBook> books = new HashSet<>();
	
	public FileBasedDatabase() {
		for (int i = 1; i < 9; i++) {
			addBook(String.format("Book #%d", i),
				String.format("book_%d.txt", i));
		}
	}

	@Override
	public Book[] fulltextSearch(String term) {
		String termLowered = term.toLowerCase();
		List<Book> found = new ArrayList<>();
		for (FileBook b : books) {
			if (b.contentMatches(termLowered)) {
				found.add(b);
			}
		}
		return found.toArray(new Book[0]);
	}
	
	private void addBook(String title, String filename) {
		String isbn = String.format("ISBN-%05d", books.size() + 1);
		int price = random.nextInt(20) + 5;
		
		FileBook book = new FileBook(title, isbn, price,
			"../books/" + filename);
		
		books.add(book);
	}
}
