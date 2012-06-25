package cz.cuni.mff.d3s.adapt.bookstore.store;

import cz.cuni.mff.d3s.adapt.bookstore.services.Book;

public class SimpleBook implements Book {
	private String title;
	private String isbn;
	private int price;
	
	public SimpleBook(String title, String isbn, int price) {
		this.title = title;
		this.isbn = isbn;
		this.price = price;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getIsbn() {
		return isbn;
	}

	@Override
	public int getPrice() {
		return price;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((isbn == null) ? 0 : isbn.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SimpleBook)) {
			return false;
		}
		SimpleBook other = (SimpleBook) obj;
		if (isbn == null) {
			if (other.isbn != null) {
				return false;
			}
		} else if (!isbn.equals(other.isbn)) {
			return false;
		}
		return true;
	}
	
	
}
