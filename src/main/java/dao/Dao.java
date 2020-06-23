package dao;

import java.sql.Date;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import bo.Bo;
import connection.SourceConnection;
import data_source.IDataSource;

public abstract class Dao<T extends Bo> implements IDao {

	private IDataSource connection;
	public abstract String getTableName(); 
	public abstract long save(final T bo);
	
	protected abstract T getNewBo(final ResultSet rs) throws Exception;
	protected abstract void buildBo(final ResultSet rs, final T bo) throws Exception;
	protected abstract void addToCache(final T bo);	
	protected abstract void boLoaded(final T bo) throws Exception;
	
	public void setDataSource(IDataSource dataSource) {
		connection = dataSource;		
	}
	
	public IDataSource getDataSource() {
		return connection;		
	}

	protected SourceConnection openConnection() {
		return connection.open();
	}
	
	protected void closeConnection(SourceConnection conn) throws Exception {
		connection.close(conn);
	}

	/*private void logColumnsNames(ResultSet rs) throws SQLException {
		
	    ResultSetMetaData rsmd = rs.getMetaData();
	    
		LogUtils.info(" ");
		
	    for (int c = 1; c <= rsmd.getColumnCount(); c++) {
	    	LogUtils.info("-------------------" + rsmd.getColumnName(c));
	    }
	
	    LogUtils.info(" ");
	}*/
	
	protected LocalDateTime getCurrentTsp() {
		return LocalDateTime.now();
	}
	
	protected String getSqlDate(final LocalDateTime ldt) {
	
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss", Locale.FRANCE);
		
		return "datetime('" + dateFormatter.format(ldt) + "')";
	}
	
	protected String getSearchString(final String initString) {
		
		return initString.trim().toUpperCase();
		
	}
	
	protected LocalDateTime dateToLocalDateTime(Date date) throws Exception {
		
		LocalDateTime result = null;
		
		if (date != null) {

			try {
				result = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			} catch (DateTimeParseException e) {
				throw new Exception(e);
			}
		}
		
		return result;
	}
	
	protected LocalDateTime dateToLocalDateTime(final String date) throws Exception {
		
		LocalDateTime result = null;
		
		if (date != null) {

			try {
				result = LocalDateTime.parse(date.replace(' ', 'T'));
			} catch (DateTimeParseException e) {
				throw new Exception(e);
			}
		}
		
		return result;
	}
	
	protected LocalDate dateToLocalDate(Date date) throws Exception {
		
		LocalDate result = null;
		
		if (date != null) {

			try {
				result = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			} catch (DateTimeParseException e) {
				throw new Exception(e);
			}
		}
		
		return result;
	}

	protected LocalDate dateToLocalDate(final String date) throws Exception {
		
		LocalDate result = null;
		
		if (date != null) {

			String dte = date.split(" ")[0];

			try {
				result = LocalDate.parse(dte);
			} catch (DateTimeParseException e) {
				throw new Exception(e);
			}
		}
		
		return result;
	}

	/**
	 * @return list of all bo of specific bo type
	 */
	protected List<T> getBoFromSource(final String rqt) throws Exception {

		final List<T> boList = new ArrayList<>();
		
		SourceConnection conn = openConnection();
		
		ResultSet rs = null;
		try {
			rs = conn.execute(rqt);
		} catch (Exception e) {
			throw e;
		}

		if (rs != null) {
        	
    		T bo;
			while (rs.next()) {
				bo = getNewBo(rs);
				boList.add(bo);
				addToCache(bo);
			}

			for(T newBo : boList) {
				boLoaded(newBo);					
			}
		}

    	closeConnection(conn);
		
		return boList;

	}
	
	protected int executeCountQuery(final String rqt) throws Exception {
		
		int result = 0;
		
		SourceConnection conn = openConnection();
		ResultSet rs = conn.execute(rqt);

		if (rs != null) {
        	result = rs.getInt(1);
		}

    	closeConnection(conn);
		
		return result;
	}
}
