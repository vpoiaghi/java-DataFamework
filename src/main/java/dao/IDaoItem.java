package dao;

import java.util.List;

import bo.BoItem;

public interface IDaoItem extends IDao {

	<T extends BoItem> Class<T> getBoClass();
	<T extends BoItem> T getById(final long id) throws Exception;
	List<? extends BoItem> getById(final List<Long> idList) throws Exception;
	
}
