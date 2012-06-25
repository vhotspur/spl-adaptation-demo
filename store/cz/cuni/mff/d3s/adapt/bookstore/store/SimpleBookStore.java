package cz.cuni.mff.d3s.adapt.bookstore.store;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;

import cz.cuni.mff.d3s.adapt.bookstore.services.Book;
import cz.cuni.mff.d3s.adapt.bookstore.services.Database;
import cz.cuni.mff.d3s.adapt.bookstore.services.Store;
import cz.cuni.mff.d3s.adapt.bookstore.services.Wallet;

@Component
@Provides
public class SimpleBookStore implements Store {

	@Requires
	private Database database;
	
	public SimpleBookStore() {
	}
	
	@Override
	public Book[] fulltextSearch(String term) {
		return database.fulltextSearch(term);
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
