package connection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import utils.LogUtils;

public class SourceConnection {
	
	private Connection conn;
	private boolean alreadyOpened;
	
	public SourceConnection(final Connection conn, final boolean alreadyOpened) {
		this.conn = conn;
		this.alreadyOpened = alreadyOpened;
	}
	
	public boolean isAlreadyOpened() {
		return alreadyOpened;
	}
	
	public ResultSet execute(final String rqt) throws Exception {

		LogUtils.warn("execute : " + rqt);

		ResultSet result = null;

		if (conn != null) {
			PreparedStatement pstmt  = conn.prepareStatement(rqt);
			result = pstmt.executeQuery();
		}
			
		return result;
	}
}
