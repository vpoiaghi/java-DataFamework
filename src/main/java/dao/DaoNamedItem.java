package dao;

import java.sql.ResultSet;

import bo.BoNamedItem;

public abstract class DaoNamedItem<T extends BoNamedItem> extends DaoItem<T> {

	@Override
	protected void initLinks(final DaoLinks daoLinks) {
	}

	@Override
	protected void buildBo(final ResultSet rs, final T bean) throws Exception {
	
		bean.setId(rs.getLong("Id"));
		bean.setTsp(dateToLocalDateTime(rs.getString("TSP")));
		bean.setName(rs.getString("Name")); 
	        
	}
}
