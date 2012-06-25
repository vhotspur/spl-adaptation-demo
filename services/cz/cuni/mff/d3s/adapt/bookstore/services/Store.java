package cz.cuni.mff.d3s.adapt.bookstore.services;

public interface Store {
	Book[] fulltextSearch(String term);
	boolean buy(Book book, Wallet wallet);
}
