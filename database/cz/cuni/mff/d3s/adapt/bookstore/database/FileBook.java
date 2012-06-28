package cz.cuni.mff.d3s.adapt.bookstore.database;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import cz.cuni.mff.d3s.adapt.bookstore.services.Book;

public class FileBook implements Book {
	public static final Book[] EMPTY_ARRAY = new FileBook[0];
	
	private String title;
	private String isbn;
	private int price;
	private String filename;

	public FileBook(String title, String isbn, int price, String filename) {
		this.title = title;
		this.isbn = isbn;
		this.price = price;
		this.filename = filename;
	}

	public boolean contentMatches(String term) {
		String termLowered = term.toLowerCase();
		String contents = getTitle() + " " + readFile(filename);
		
		return contents.toLowerCase().contains(termLowered);
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
		if (!(obj instanceof FileBook)) {
			return false;
		}
		FileBook other = (FileBook) obj;
		if (isbn == null) {
			if (other.isbn != null) {
				return false;
			}
		} else if (!isbn.equals(other.isbn)) {
			return false;
		}
		return true;
	}

	private String readFile(String filename) {
		FileReader fileReader;
		try {
			fileReader = new FileReader(filename);
		} catch (FileNotFoundException e1) {
			return "";
		}
		BufferedReader reader = new BufferedReader(fileReader);
		StringBuilder contents = new StringBuilder();
		
		try {
			while (true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				contents.append(line);
				contents.append(" ");
			}
			return contents.toString();
		} catch (IOException e) {
			return "";
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				return contents.toString();
			}
		}
	}

}
