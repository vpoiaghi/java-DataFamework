package dao;

import data_source.IDataSource;

public interface IDao {

	abstract String getTableName();
	void setDataSource(final IDataSource dataSource);
	IDataSource getDataSource();
	
}
