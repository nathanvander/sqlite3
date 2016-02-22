package apollo;
import com.sun.jna.Pointer;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.PointerType;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.LinkedList;

/**
*
* To get a Connection, call getConnection().  When done, call recycle(). This reuses the connections.
* Usually, you should not call close().
*/
public class Connection {
	//This is here because I don't like using raw pointers.  Also, it may be needed by other classes
	//such as Blob
	public static class Handle extends PointerType {
		public Handle(Pointer p) {
			super(p);
		}
	}

	private static AtomicInteger counter=new AtomicInteger();
	private static LinkedList<Connection> pool=new LinkedList<Connection>();
	private int id;
	private Handle handle;
	private boolean closed=false;

	//private ArrayList statements

	public static Connection getConnection() throws SQLiteException {
		if (pool.size()==0) {
			Connection c=new Connection();
			return c;
		} else {
			return pool.getFirst();
		}
	}

	//called only by DataStore
	protected Connection() throws SQLiteException {
		handle=open(DataStore.getDbFileName());
		id=counter.incrementAndGet();	//equivalent of ++counter;
	}

	protected Handle getHandle() {return handle;}

	//returns a pointer to the connection object
	private static Handle open(String filename) throws SQLiteException {
		byte[] fn=DataStore.getByteArray(filename);
		PointerByReference ppDb=new PointerByReference();
		int flags= SQLITE_OPEN_READWRITE | SQLITE_OPEN_CREATE | SQLITE_OPEN_NOMUTEX;
		int rc=api.sqlite3_open_v2(fn,ppDb,flags,null);
		if (rc!=0) {
			String msg=DataStore.getErrorString(rc);
			throw new SQLiteException(rc,msg);
		} else {
			Pointer pdb=ppDb.getValue();
			if (pdb==null) {
				//probably won't happen
				throw new SQLiteException(SQLITE_CANTOPEN,"pointer to connection is null");
			}
			return new Handle(pdb);
		}
	}

	public int getId() {return id;}

	public boolean isClosed() {
		return closed;
	}

	//don't use this unless you will never use the connection again
	public void close() {
		int rc=api.sqlite3_close_v2(handle.getPointer());
		closed=true;
		if (rc!=0) {
			System.out.println("warning: error "+rc+" on close()");
		}
	}

	/**
	* Used for all sql commands except for SELECT, including DDL statements, inserts, updates and deletes.
	* Returns the number of rows affected.
	*
	* This must be called within a transaction.
	*/
	public int exec(String sql) throws SQLiteException {
		System.out.println(sql);
		PointerByReference pbr=new PointerByReference();
		byte[] basql=DataStore.getByteArray(sql);
		int rc=api.sqlite3_exec(handle.getPointer(),basql,null,null,pbr);
		if (rc==0) {
			//System.out.println("success");
			return getChanges();
		} else {
			Pointer perr=pbr.getValue();
			String err=perr.getString(0);
			System.out.println("error in "+sql);
			System.out.println("error code "+rc);
			throw new SQLiteException(rc,err);
		}
	}

	//prepare is used only by select statements.  That is why i call it a query
	public ResultSet query(String sql) throws SQLiteException {
		System.out.println(sql);
		return new ResultSet(handle,sql);
	}

	//to do
	//public PreparedStatement prepare(String sql);

	public int getChanges() {
		return api.sqlite3_changes(handle.getPointer());
	}

	//use this instead of close().  It returns it to the Connection pool
	public void recycle() {
		//make sure statements are closed
		pool.addLast(this);
	}

	//==============================================
	//flags for open
	public final static int SQLITE_OPEN_READONLY=         0x00000001;  /* Ok for sqlite3_open_v2() */
	public final static int SQLITE_OPEN_READWRITE=        0x00000002;  /* Ok for sqlite3_open_v2() */
	public final static int SQLITE_OPEN_CREATE=           0x00000004;  /* Ok for sqlite3_open_v2() */
	public final static int SQLITE_OPEN_URI=              0x00000040;  /* Ok for sqlite3_open_v2() */
	public final static int SQLITE_OPEN_MEMORY=           0x00000080;  /* Ok for sqlite3_open_v2() */
	public final static int SQLITE_OPEN_NOMUTEX=          0x00008000;  /* Ok for sqlite3_open_v2() */
	public final static int SQLITE_OPEN_FULLMUTEX=        0x00010000;  /* Ok for sqlite3_open_v2() */
	public final static int SQLITE_OPEN_SHAREDCACHE=      0x00020000;  /* Ok for sqlite3_open_v2() */
	public final static int SQLITE_OPEN_PRIVATECACHE=     0x00040000;  /* Ok for sqlite3_open_v2() */

	//error message
	public final static int SQLITE_CANTOPEN=14;

	public static SQLITE_API api;
	static {
		api=(SQLITE_API)Native.loadLibrary("sqlite3",SQLITE_API.class,W32APIOptions.DEFAULT_OPTIONS);
	}

	public interface SQLITE_API extends Library {
		//SQLITE_API int SQLITE_STDCALL sqlite3_open_v2(
	  	//const char *filename,   /* Database filename (UTF-8) */
	  	//sqlite3 **ppDb,         /* OUT: SQLite db handle */
	  	//int flags,              /* Flags */
	  	//const char *zVfs        /* Name of VFS module to use */
		//);
		public int sqlite3_open_v2(byte[] filename,PointerByReference ppDb,int flags,byte[] zVfs);

		//SQLITE_API int SQLITE_STDCALL sqlite3_close(sqlite3*);
		public int sqlite3_close(Pointer pSqlite3);

		//The sqlite3_close_v2() interface is intended for use with host languages that are garbage collected,
		//and where the order in which destructors are called is arbitrary.
		//SQLITE_API int SQLITE_STDCALL sqlite3_close_v2(sqlite3*);
		public int sqlite3_close_v2(Pointer pSqlite3);

		public int sqlite3_exec(
  			Pointer psqlite3,                               /* An open database */
  			byte[] sql, 									/* SQL to be evaluated */
  			Object onull,									/* Callback function */
   			Object onull2,
   			PointerByReference errmsg
		);

		public int sqlite3_changes(Pointer psqlite3);
	}
}