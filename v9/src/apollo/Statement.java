package apollo;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
* The main purpose of a Statement is to allow for bindings.  Without those, you could just go from
* Connection to ResultSet
*/
public class Statement {
	private Handle.sqlite3_stmt_handle hstmt;

	public Statement(Handle.sqlite3_stmt_handle h) {
		if (h==null) {throw new IllegalArgumentException("sqlite3_stmt handle is null");}
		this.hstmt=h;
	}

	/** This is the destructor.  You must call this when finised.
	*/
	public void close() {
		int rc=api.sqlite3_finalize(hstmt.getPointer());
		if (rc!=0) {
			//don't throw an exception
			System.out.println("warning: Statement.close() produced the following error: "+rc);
		}
	}

	protected Handle.sqlite3_stmt_handle getHandle() {return hstmt;}

	/**
	* Return the sql that created this statement.
	*/
	public String getSql() {
		Pointer psql=api.sqlite3_sql(hstmt.getPointer());
		if (psql==null) {return null;}
		else {return psql.getString(0);}
	}

	//compare to JDBC executeQuery()
	public ResultSet query() {
		return new ResultSet(hstmt);
	}

	//almost the same as ResultSet.step
	//use after binds
	public boolean exec() throws SQLiteException {
		int rc=api.sqlite3_step(hstmt.getPointer());
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
			return exec();
		} else {
			throw new SQLiteException(rc,"error in exec()");
		}
	}


	//-----------------------------------------
	//bindings
	//index is the index to bind, nData is the number of bytes to bind, usually the full length of zData
	public void bind_blob(int index,byte[] zData, int nData) {
		int rc=api.sqlite3_bind_blob(hstmt.getPointer(),index,zData,nData,null);
	}

	//============================================
	public final static int SQLITE_TRANSIENT=-1;
	public final static int SQLITE_STATIC=0;
	public final static int SQLITE_BUSY=5;
	public final static int SQLITE_ROW=         100;  /* sqlite3_step() has another row ready */
	public final static int SQLITE_DONE=        101;  /* sqlite3_step() has finished executing */

	public static SQLITE_API api;
	static {
		api=(SQLITE_API)Native.loadLibrary("sqlite3",SQLITE_API.class,W32APIOptions.DEFAULT_OPTIONS);
	}

	public interface SQLITE_API extends Library {
		public int sqlite3_step(Pointer pStmt);
		//const char *sqlite3_sql(sqlite3_stmt *pStmt);
		public Pointer sqlite3_sql(Pointer pStmt);

		//int sqlite3_finalize(sqlite3_stmt *pStmt);
		public int sqlite3_finalize(Pointer pStmt);

		public int sqlite3_bind_blob(Pointer sqlite3_stmt, int indez, byte[] zData, int nData, Object xDel);

		//int sqlite3_bind_double(sqlite3_stmt*, int, double);
		//int sqlite3_bind_int(sqlite3_stmt*, int, int);
		//int sqlite3_bind_int64(sqlite3_stmt*, int, sqlite3_int64);
		//int sqlite3_bind_null(sqlite3_stmt*, int);
		//int sqlite3_bind_text(sqlite3_stmt*,int,const char*,int,void(*)(void*));
		//int sqlite3_bind_value(sqlite3_stmt*, int, const sqlite3_value*);
		//int sqlite3_bind_zeroblob(sqlite3_stmt*, int, int n);
		//int sqlite3_bind_parameter_count(sqlite3_stmt*);
		//int sqlite3_bind_parameter_index(sqlite3_stmt*, const char *zName);
		//const char *sqlite3_bind_parameter_name(sqlite3_stmt*, int);
		//int sqlite3_clear_bindings(sqlite3_stmt*);
	}

}
