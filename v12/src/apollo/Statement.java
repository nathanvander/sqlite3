package apollo;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;

/**
* I really dislike this class.  It is usually unnecessary because you just want a ResultSet.
* The only use is if you have a PreparedStatement, and that should be a different class.
* The only purpose here is to wrap the StatementHandle, which is shared between classes.
*
* See also https://www.sqlite.org/c3ref/stmt.html
*/
public class Statement {
	public static class Handle extends PointerType {
		public Handle(Pointer p) {
			super(p);
		}
	}
	protected Handle stmtHandle;
	protected boolean closed=false;

	/**
	* Create a new Statement, given the connection and the sql.
	*/
	public Statement(Connection.Handle ch,String sql) throws SQLiteException {
		PointerByReference ppStmt=new PointerByReference();
		PointerByReference pzTail=new PointerByReference();
		byte[] basql=DataStore.getByteArray(sql);

		int rc=api.sqlite3_prepare_v2(
			ch.getPointer(),
			basql,
			basql.length,
			ppStmt,
			pzTail
		);

		if (rc==0) {
			//System.out.println("success");
			Pointer pstmt=ppStmt.getValue();
			stmtHandle=new Handle(pstmt);
		} else {
			throw new SQLiteException(rc,"error in "+sql);
		}
	}

	/**
	* This closes the associated Statement.  Don't use this after closing it.
	*/
	public void close() {
		int rc=api.sqlite3_finalize(stmtHandle.getPointer());
		closed=true;
		if (rc!=0) {
			//don't throw an exception
			System.out.println("warning: Statement.close() produced the following error: "+rc);
		}
	}

	public boolean isClosed() {
		return closed;
	}

	protected Handle getHandle() {return stmtHandle;}

	//==============================================
	public static SQLITE_API api;
	static {
		api=(SQLITE_API)Native.loadLibrary("sqlite3",SQLITE_API.class,W32APIOptions.DEFAULT_OPTIONS);
	}

	public interface SQLITE_API extends Library {
		//sqlite3_prepare_v2
		//SQLITE_API int SQLITE_STDCALL sqlite3_prepare_v2(
		//  sqlite3 *db,            /* Database handle */
		//  const char *zSql,       /* SQL statement, UTF-8 encoded */
		//  int nByte,              /* Maximum length of zSql in bytes. */
		//  sqlite3_stmt **ppStmt,  /* OUT: Statement handle */
		//  const char **pzTail     /* OUT: Pointer to unused portion of zSql */
		//);
		//create an sqlite3_stmt
		public int sqlite3_prepare_v2(
			Pointer psqlite3,
			byte[] sql,
			int nByte,
			PointerByReference ppStmt,
			PointerByReference pzTail
		);

		//int sqlite3_finalize(sqlite3_stmt *pStmt);
		public int sqlite3_finalize(Pointer pStmt);
	}
}