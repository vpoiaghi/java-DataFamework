package data_source;

import connection.SourceConnection;

public interface IDataSource {

	SourceConnection open();
	void close(final SourceConnection connection) throws Exception;
	
}
