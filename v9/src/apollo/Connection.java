package apollo;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class Connection {
	private Handle.sqlite3_handle db;
	private boolean closed=false;

	public Connection(Handle.sqlite3_handle h) {
		if (h==null) {throw new IllegalArgumentException("sqlite3 handle is null");}
		this.db=h;
	}

	//for now, just use the simple slite3_close method
	public void close() {
		int rc=api.sqlite3_close(db.getPointer());
		closed=true;
		if (rc!=0) {
			System.out.println("warning: error "+rc+" on close()");
		}
	}

	public boolean isClosed() {
		return closed;
	}

	/**
	* Used for all sql commands except for SELECT, including DDL statements, inserts, updates and deletes.
	* Returns the number of rows affected.
	*/
	public int exec(String sql) throws SQLiteException {
		PointerByReference pbr=new PointerByReference();
		byte[] basql=NTBA.get(sql);
		int rc=api.sqlite3_exec(db.getPointer(),basql,null,null,pbr);
		if (rc==0) {
			//System.out.println("success");
			return getChanges();
		} else {
			Pointer perr=pbr.getValue();
			String err=perr.getString(0);
			throw new SQLiteException(rc,err);
		}
	}

	public String getDbFileName() {
		byte[] zDbName=NTBA.get("main");
		Pointer pName=api.sqlite3_db_filename(db.getPointer(),zDbName);
		if (pName==null) {return null;}
		else {return pName.getString(0);}
	}

	public Statement prepare(String sql) throws SQLiteException {
		PointerByReference ppStmt=new PointerByReference();
		PointerByReference pzTail=new PointerByReference();
		byte[] basql=NTBA.get(sql);

		int rc=api.sqlite3_prepare_v2(
			db.getPointer(),
			basql,
			basql.length,
			ppStmt,
			pzTail
		);

		if (rc==0) {
			//System.out.println("success");
			Pointer p=ppStmt.getValue();
			Handle.sqlite3_stmt_handle h=new Handle.sqlite3_stmt_handle(p);
			return new Statement(h);
		} else {
			throw new SQLiteException(rc,"error in "+sql);
		}
	}

	/**
	* You can use this to get a resultset without first obtaining a statement.
	* The JDBC way is:
	*	Statement st=conn.createStatement();
	*	ResultSet rs=st.executeQuery(sql);
	* This saves a step.
	*/
	public ResultSet query(String sql) throws SQLiteException {
		Statement st=prepare(sql);
		return new ResultSet(st.getHandle());
	}

	public long getLastInsertId() {
		return api.sqlite3_last_insert_rowid(db.getPointer());
	}

	/**
	* This function causes any pending database operation to abort and return at its earliest opportunity. This
	* routine is typically called in response to a user action such as pressing "Cancel" or Ctrl-C where the user
	* wants a long query operation to halt immediately.
	*/
	public void interrupt() {
		api.sqlite3_interrupt(db.getPointer());
	}

	/**
	* Get the last error code that the connection returned.
	*/
	public int getErrorCode() {
		return api.sqlite3_errcode(db.getPointer());
	}
	public int getExtendedErrorCode() {
		return api.sqlite3_extended_errcode(db.getPointer());
	}

	/**
	* Return true if the connection is read-only
	*/
	public boolean isReadOnly() {
		byte[] zDbName=NTBA.get("main");
		int i=api.sqlite3_db_readonly(db.getPointer(),zDbName);
		return (i==1)?true:false;
	}

	public boolean getAutoCommit() {
		int i=api.sqlite3_get_autocommit(db.getPointer());
		return (i==1)?true:false;
	}

	/**
	* This method returns the number of database rows that were changed or inserted or deleted by the most recently
	* completed SQL statement in this connection.
	*/
	public int getChanges() {
		return api.sqlite3_changes(db.getPointer());
	}

	//--------------------------
	//experimental transaction control
	public void begin() throws SQLiteException {
		//an immediate transaction sets a reserved lock on the file
		//which is what we want.
		exec("BEGIN TRANSACTION IMMEDIATE");
	}

	public void commit() throws SQLiteException {
		exec("COMMIT TRANSACTION");
	}

	public void rollback() throws SQLiteException {
		//this doesn't use savepoints
		exec("ROLLBACK TRANSACTION");
	}



	//==============================================
	public static SQLITE_API api;
	static {
		api=(SQLITE_API)Native.loadLibrary("sqlite3",SQLITE_API.class,W32APIOptions.DEFAULT_OPTIONS);
	}

	public interface SQLITE_API extends Library {
		//SQLITE_API int SQLITE_STDCALL sqlite3_close(sqlite3*);
		public int sqlite3_close(Pointer pSqlite3);

		//SQLITE_API int SQLITE_STDCALL sqlite3_close_v2(sqlite3*);
		public int sqlite3_close_v2(Pointer pSqlite3);

		//const char *sqlite3_db_filename(sqlite3 *db, const char *zDbName);
		public Pointer sqlite3_db_filename(Pointer pSqlite3,byte[] zDbName);

		//int sqlite3_db_status(sqlite3*, int op, int *pCur, int *pHiwtr, int resetFlg);
		public int sqlite3_db_status(Pointer pSqlite3,int op,IntByReference pCur,IntByReference pHiwtr,int resetFlg);

		public int sqlite3_exec(
  			Pointer psqlite3,                               /* An open database */
  			byte[] sql, 									/* SQL to be evaluated */
  			Object onull,									/* Callback function */
   			Object onull2,
   			PointerByReference errmsg
		);

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

		//sqlite3_int64 sqlite3_last_insert_rowid(sqlite3*);
		public long sqlite3_last_insert_rowid(Pointer psqlite3);

		public void sqlite3_interrupt(Pointer psqlite3);

		public int sqlite3_errcode(Pointer psqlite3);
		public int sqlite3_extended_errcode(Pointer psqlite3);

		public int sqlite3_db_readonly(Pointer psqlite3, byte[] zDbName);

		public int sqlite3_get_autocommit(Pointer psqlite3);
		public int sqlite3_changes(Pointer psqlite3);
	}
}