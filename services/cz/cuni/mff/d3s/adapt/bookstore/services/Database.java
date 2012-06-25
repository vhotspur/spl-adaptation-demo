package cz.cuni.mff.d3s.adapt.bookstore.services;

public interface Database {
	Book[] fulltextSearch(String term);
}
