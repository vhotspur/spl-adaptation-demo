package cz.cuni.mff.d3s.adapt.bookstore.store;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;

import cz.cuni.mff.d3s.adapt.bookstore.services.Book;
import cz.cuni.mff.d3s.adapt.bookstore.services.Store;
import cz.cuni.mff.d3s.adapt.bookstore.services.Wallet;

@Component
@Provides
public class SimpleBookStore implements Store {

	private Set<Book> books;
	
	public SimpleBookStore() {
		books = new HashSet<>();
		books.add(new SimpleBook("Alpha", "00-00", 5));
		books.add(new SimpleBook("Bravo", "00-01", 6));
		books.add(new SimpleBook("Charlie", "00-02", 8));
		books.add(new SimpleBook("Delta", "00-03", 4));
		books.add(new SimpleBook("Echo", "00-04", 12));
	}
	
	@Override
	public Book[] fulltextSearch(String term) {
		// System.out.printf("Someone is looking for '%s'.\n", term);
		String termLowered = term.toLowerCase();
		List<Book> found = new ArrayList<>();
		for (Book b : books) {
			if (b.getTitle().toLowerCase().contains(termLowered)) {
				found.add(b);
			}
		}
		return found.toArray(new Book[0]);
	}

	@Override
	public boolean buy(Book book, Wallet wallet) {
		boolean success = wallet.pay(book.getPrice());
		if (!success) {
			return false;
		}
		System.out.printf("Someone just bought %s [%s, %d$].\n",
			book.getTitle(), book.getIsbn(), book.getPrice());
		return true;
	}

}
