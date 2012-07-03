package cz.cuni.mff.d3s.adapt.bookstore.services;

public interface Replicable {
	int getInstanceCount();
	void startInstance();
	void stopInstance();
}
