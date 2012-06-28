package cz.cuni.mff.d3s.spl.adapt;

import java.util.Collection;

import cz.cuni.mff.d3s.adapt.bookstore.agent.data.Measurement;


public class SimpleDataSource implements DataSource {
	private String id;
	private cz.cuni.mff.d3s.adapt.bookstore.agent.data.Measurement datas;
	
	public SimpleDataSource(String name, Measurement dataStorage) {
		id = name;
		datas = dataStorage;
	}
	
	@Override
	public Collection<Long> get() {
		return datas.get(id);
	}

}
