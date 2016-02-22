package apollo;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.Pointer;

/**
* ResultSet
*
*/
public class ResultSet extends Statement {
	private boolean hasMore=false;

	public ResultSet(Connection.Handle ch,String sql) throws SQLiteException {
		super(ch,sql);
	}



	public void reset() throws SQLiteException {
		int rc=api.sqlite3_reset(stmtHandle.getPointer());
		if (rc!=0) {
			throw new SQLiteException(rc,"error in reset()");
		}
	}

	/**
	* Returns:
	*	true if there is data (SQLITE_ROW) was returned, false if statement has been completed (SQLITE_DONE)
	* Throws:
	*   SQLiteException - if result code from sqlite3_step was neither SQLITE_ROW nor SQLITE_DONE, or if any other problem occurs
	*/
	public boolean step() throws SQLiteException {
		int rc=api.sqlite3_step(stmtHandle.getPointer());
		if (rc==SQLITE_ROW) {return true;}
		else if (rc==SQLITE_DONE) {return false;}
		else if (rc==SQLITE_BUSY) {
			//wait a little
			System.out.println("the database is busy.  waiting a little bit and trying again");
			try {
				Thread.sleep(50);
			} catch (InterruptedException x) {
				throw new SQLiteException(SQLITE_BUSY,"interrupted sleep",x);
			}
			return step();
		} else {
			throw new SQLiteException(rc,"error in step()");
		}
	}

	//from jdbc
	//same as step, except it doesn't throw an exception
	public boolean next() {
		boolean b=false;
		try {
			b=step();
		} catch (Exception x) {
			x.printStackTrace();
		}
		hasMore=b;
		return b;
	}

	//from Iterator
	public boolean hasNext() {
		return hasMore;
	}

	/**
	* remove().  Not implemented.  Why is this here?
	*/
	public void remove() {}

	//returns true is the statement has stepped at least once
	//but not run until completion.
	public boolean isBusy() {
		int i=api.sqlite3_stmt_busy(stmtHandle.getPointer());
		return (i==1)?true:false;
	}

	//-----------------------------------------------
	public int getInt(int columnIndex) {
		return api.sqlite3_column_int(stmtHandle.getPointer(), columnIndex);
	}

	public long getLong(int iCol) {
		return api.sqlite3_column_int64(stmtHandle.getPointer(),iCol);
	}

	public double getDouble(int iCol) {
		return api.sqlite3_column_double(stmtHandle.getPointer(),iCol);
	}

	public String getString(int columnIndex) {
		Pointer p=api.sqlite3_column_text(stmtHandle.getPointer(),columnIndex);
		//int i=api.sqlite3_column_bytes(pstmt,columnIndex);
		return p.getString(0).trim();
	}

	public int getColumnCount() {
		return api.sqlite3_column_count(stmtHandle.getPointer());
	}

	public String getColumnName(int i) {
		Pointer p=api.sqlite3_column_name(stmtHandle.getPointer(), i);
		return p.getString(0);
	}

	public int getColumnType(int i) {
		return api.sqlite3_column_type(stmtHandle.getPointer(),i);
	}

	/**
	* I'm not sure this will work.  Test it.
	*/
	public byte[] getBytes(int columnIndex) {
		Pointer p=api.sqlite3_column_blob(stmtHandle.getPointer(),columnIndex);
		int i=api.sqlite3_column_bytes(stmtHandle.getPointer(),columnIndex);
		return p.getByteArray(0,i);
	}

	protected void finalize() {
		close();
	}

	//============================================
	public final static int SQLITE_BUSY=5;
	public final static int SQLITE_ROW=         100;  /* sqlite3_step() has another row ready */
	public final static int SQLITE_DONE=        101;  /* sqlite3_step() has finished executing */
	public static SQLITE_API api;
	static {
		api=(SQLITE_API)Native.loadLibrary("sqlite3",SQLITE_API.class,W32APIOptions.DEFAULT_OPTIONS);
	}

	public interface SQLITE_API extends Library {
		//also in Statement
		//int sqlite3_finalize(sqlite3_stmt *pStmt);
		//public int sqlite3_finalize(Pointer pStmt);

		//This interface can be used to retrieve a saved copy of the original SQL text used to create a prepared statement
		//if that statement was compiled using either sqlite3_prepare_v2() or sqlite3_prepare16_v2().
		//public Pointer sqlite3_sql(Pointer pStmt);

		public int sqlite3_reset(Pointer pStmt);

		public int sqlite3_step(Pointer pStmt);

		public int sqlite3_stmt_busy(Pointer psqlite3_stmt);

		//int sqlite3_column_int(sqlite3_stmt*, int iCol);
		public int sqlite3_column_int(Pointer pstmt, int iCol);

		//sqlite3_int64 sqlite3_column_int64(sqlite3_stmt*, int iCol);
		public long sqlite3_column_int64(Pointer pstmt, int iCol);

		public double sqlite3_column_double(Pointer pstmt, int iCol);

		//return the number of bytes in a text or blob
		//int sqlite3_column_bytes(sqlite3_stmt*, int iCol);
		public int sqlite3_column_bytes(Pointer pstmt, int iCol);

		//const unsigned char *sqlite3_column_text(sqlite3_stmt*, int iCol);
		//return a pointer to the text
		public Pointer sqlite3_column_text(Pointer pstmt, int iCol);

		//get the column_type
		//int sqlite3_column_type(sqlite3_stmt*, int iCol);
		public int sqlite3_column_type(Pointer pstmt, int iCol);

		//const void *sqlite3_column_blob(sqlite3_stmt*, int iCol);
		//returns a pointer to the blob object
		public Pointer sqlite3_column_blob(Pointer pstmt, int iCol);

		public int sqlite3_column_count(Pointer pStmt);

		public Pointer sqlite3_column_name(Pointer pstmt, int N);
	}
}