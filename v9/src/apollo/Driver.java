package apollo;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.Pointer;

/** The Driver has one purpose, to enable you to get a connection. There will be one instance of Driver
* per JVM, but one connection per thread.
*/
public class Driver {
	private static Driver instance;
	static {
		instance=new Driver();
	}
	private Driver() {}
	public static Driver get() {return instance;}
	//---------------------------------------------------
	//I'm not clear on when you would use sqlite3_open_v2, so for now, just to keep it simple,
	//we will just use sqlite3_open
	public Connection connect(String filename) throws SQLiteException {
		PointerByReference ppDb=new PointerByReference();
		byte[] fn=NTBA.get(filename);
		int rc=api.sqlite3_open(fn,ppDb);
		if (rc!=0) {
			String msg=getErrorString(rc);
			throw new SQLiteException(rc,msg);
		} else {
			Pointer p=ppDb.getValue();
			if (p==null) {
				//probably won't happen
				throw new SQLiteException(SQLITE_CANTOPEN,"pointer to connection is null");
			}
			Handle.sqlite3_handle h=new Handle.sqlite3_handle(p);
			Connection conn=new Connection(h);
			return conn;
		}
	}

	/**
	* get a read-only connection to the database.
	*^(<dt>[SQLITE_OPEN_READONLY]</dt>
	** <dd>The database is opened in read-only mode.  If the database does not
	** already exist, an error is returned.</dd>)^
	*/
	public Connection connectReadOnly(String filename) throws SQLiteException {
		byte[] fn=NTBA.get(filename);
		PointerByReference ppDb=new PointerByReference();
		int flags=SQLITE_OPEN_READONLY;
		int rc=api.sqlite3_open_v2(fn,ppDb,flags,null);
		if (rc!=0) {
			String msg=getErrorString(rc);
			throw new SQLiteException(rc,msg);
		} else {
			Pointer p=ppDb.getValue();
			if (p==null) {
				//probably won't happen
				throw new SQLiteException(SQLITE_CANTOPEN,"pointer to connection is null");
			}
			Handle.sqlite3_handle h=new Handle.sqlite3_handle(p);
			Connection conn=new Connection(h);
			return conn;
		}
	}

	public String getSqlite3LibVersion() {
		Pointer p=api.sqlite3_libversion();
		return p.getString(0);
	}

	public int getSqlite3LibVersionNumber() {
		return api.sqlite3_libversion_number();
	}

	public static String getErrorString(int code) {
		Pointer p=api.sqlite3_errstr(code);
		if (p==null) {return null;}
		else {return p.getString(0);}
	}

	//==============================================
	//flags for open
	public final static int SQLITE_OPEN_READONLY=         0x00000001;  /* Ok for sqlite3_open_v2() */
	public final static int SQLITE_OPEN_READWRITE=        0x00000002;  /* Ok for sqlite3_open_v2() */
	public final static int SQLITE_OPEN_CREATE=         0x00000004;  /* Ok for sqlite3_open_v2() */
	public final static int SQLITE_OPEN_URI=              0x00000040;  /* Ok for sqlite3_open_v2() */
	public final static int SQLITE_OPEN_MEMORY=           0x00000080;  /* Ok for sqlite3_open_v2() */
	public final static int SQLITE_OPEN_NOMUTEX=          0x00008000;  /* Ok for sqlite3_open_v2() */
	public final static int SQLITE_OPEN_FULLMUTEX=        0x00010000;  /* Ok for sqlite3_open_v2() */
	public final static int SQLITE_OPEN_SHAREDCACHE=      0x00020000;  /* Ok for sqlite3_open_v2() */
	public final static int SQLITE_OPEN_PRIVATECACHE=     0x00040000;  /* Ok for sqlite3_open_v2() */

	//error code
	public final static int SQLITE_CANTOPEN=14;

	public static SQLITE_API api;
	static {
		api=(SQLITE_API)Native.loadLibrary("sqlite3",SQLITE_API.class,W32APIOptions.DEFAULT_OPTIONS);
	}

	public interface SQLITE_API extends Library {
		//SQLITE_API int SQLITE_STDCALL sqlite3_open(
  		//const char *filename,   /* Database filename (UTF-8) */
  		//sqlite3 **ppDb          /* OUT: SQLite db handle */
		//);
		public int sqlite3_open(byte[] filename,PointerByReference ppDb);

		//SQLITE_API int SQLITE_STDCALL sqlite3_open_v2(
  		//const char *filename,   /* Database filename (UTF-8) */
  		//sqlite3 **ppDb,         /* OUT: SQLite db handle */
  		//int flags,              /* Flags */
  		//const char *zVfs        /* Name of VFS module to use */
		//);
		public int sqlite3_open_v2(byte[] filename,PointerByReference ppDb,int flags,byte[] zVfs);

		//The sqlite3_libversion() function returns a pointer to the to the sqlite3_version[] string constant.
		//The sqlite3_libversion() function is provided for use in DLLs since DLL users usually do not have direct
		//access to string constants within the DLL. The sqlite3_libversion_number() function returns an integer equal
		//to SQLITE_VERSION_NUMBER. The sqlite3_sourceid() function returns a pointer to a string constant whose value
		//is the same as the SQLITE_SOURCE_ID C preprocessor macro.

		//const char *sqlite3_libversion(void);
		public Pointer sqlite3_libversion();

		//const char *sqlite3_sourceid(void);
		public Pointer sqlite3_sourceid();

		//int sqlite3_libversion_number(void);
		public int sqlite3_libversion_number();

		//The sqlite3_errstr() interface returns the English-language text that describes the result code, as UTF-8.
		public Pointer sqlite3_errstr(int err);
	}

}