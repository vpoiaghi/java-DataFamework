package data_source;

import java.sql.Connection;
import java.sql.SQLException;

import connection.SourceConnection;

public abstract class DataSource implements IDataSource {

	private Connection conn = null;
	
	protected abstract Connection getConnection();	

	public final SourceConnection open() {
		
		SourceConnection srcConn = null;
		
		if (conn == null) {
			//LogUtils.warn("connection db");
			conn = getConnection();
			srcConn = new SourceConnection(conn, false);
		
		} else {
			srcConn = new SourceConnection(conn, true);
		}
		
		return srcConn;
	}	

	public void close(SourceConnection srcConn) throws Exception {

		try {
			if ((srcConn != null) && (! srcConn.isAlreadyOpened())) {
				//LogUtils.warn("close db");
				conn.close();
				conn = null;
			}

		} catch (SQLException e) {
			throw new Exception(e);
		}

	}

}
