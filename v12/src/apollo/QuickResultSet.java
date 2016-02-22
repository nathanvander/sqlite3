package apollo;

/** The only difference between this and a regular resultset is that calling
* close() on this also recycles the Connection.
*/
public class QuickResultSet extends ResultSet {
	private Connection conn;

	//called only by DataStore
	protected QuickResultSet(Connection conn,String sql) throws SQLiteException {
		super(conn.getHandle(),sql);
		this.conn=conn;
	}

	//close it when you are done
	public void close() {
		super.close();
		conn.recycle();
	}
}