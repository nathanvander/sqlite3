package apollo;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.Pointer;

/**
* A ResultSet is really a subset of Statement.  They could be combined, but it is cleaner to separate them.
*/
public class ResultSet {
	private Handle.sqlite3_stmt_handle hstmt;
	private boolean hasMore=false;

	public ResultSet(Handle.sqlite3_stmt_handle h) {
		hstmt=h;
	}

	/**
	* This closes the associated Statement.  Don't use this after closing it.
	*/
	public void close() {
		int rc=api.sqlite3_finalize(hstmt.getPointer());
		if (rc!=0) {
			//don't throw an exception
			System.out.println("warning: Statement.close() produced the following error: "+rc);
		}
	}

	public void reset() throws SQLiteException {
		int rc=api.sqlite3_reset(hstmt.getPointer());
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
			return step();
		} else {
			throw new SQLiteException(rc,"error in step()");
		}
	}

	//from jdbc
	public boolean next() throws SQLiteException {
		boolean b=step();
		hasMore=b;
		return b;
	}

	//from Iterator
	public boolean hasNext() {
		return hasMore;
	}

	//returns true is the statement has stepped at least once
	//but not run until completion.
	public boolean isBusy() {
		int i=api.sqlite3_stmt_busy(hstmt.getPointer());
		return (i==1)?true:false;
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
		//also in Statemet
		//int sqlite3_finalize(sqlite3_stmt *pStmt);
		public int sqlite3_finalize(Pointer pStmt);

		public int sqlite3_reset(Pointer pStmt);

		public int sqlite3_step(Pointer pStmt);

		public int sqlite3_stmt_busy(Pointer psqlite3_stmt);
	}
}