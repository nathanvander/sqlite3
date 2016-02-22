package apollo;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import java.lang.reflect.Field;

/**
* A data store is a repository of a set of data objects. These objects are modelled using classes defined in a database schema.
*/
public class DataStore {
	private static String filename;
	private static int majorVersion=0;
	private static int minorVersion=12;

	//this is the program initializer
	static {
		api=(SQLITE_API)Native.loadLibrary("sqlite3",SQLITE_API.class,W32APIOptions.DEFAULT_OPTIONS);

		config();
		filename="main.sqlite";	//the default filename
	}
	private static void config() {
		//hard-coded config changes
		//The default mode is serialized. This changes it to multi-threaded
		int rc=api.sqlite3_config(SQLITE_CONFIG_MULTITHREAD,null);
		if (rc!=0) {
			//this will only happen if the dll was compiled to be single-threaded
			throw new RuntimeException("cannot set config option to multithread");
		}
	}

	//can be null, which means temporary, in memory
	public static void setDbFileName(String fn) {
		filename=fn;
	}

	public static String getDbFileName() {return filename;}

	public static int getLibVersionNumber() {
		return api.sqlite3_libversion_number();
	}

	//see java.sql.Driver.getMajorVersion();
	public static int getMajorVersion() {return majorVersion;}
	public static int getMinorVersion() {return minorVersion;}

	//returns the English-language text that describes the result code
	public static String getErrorString(int code) {
		Pointer p=api.sqlite3_errstr(code);
		if (p==null) {return null;}
		else {return p.getString(0);}
	}

	public static boolean isThreadSafe() {
		//i'm not sure if this can return 2 but if it can this function will return true
		return (api.sqlite3_threadsafe()>0);
	}

	//utility function
	//this adds a null byte to the end, the way C likes it
	public static byte[] getByteArray(String str) {
		byte[] stringBytes=null;
		try {stringBytes=str.getBytes("ISO-8859-1");} catch (Exception x) {x.printStackTrace();}
		byte[] ntBytes=new byte[stringBytes.length+1];
		System.arraycopy(stringBytes, 0, ntBytes, 0, stringBytes.length);
		return ntBytes;
	}

	//--------------------------------------------
	public static Schema getSchema() throws SQLiteException {
		Connection c=Connection.getConnection();
		return new Schema(c);
	}

	//when you just want data quickly
	//be sure to close it when you are done
	public static QuickResultSet query(String sql) throws SQLiteException {
		Connection c=Connection.getConnection();
		return new QuickResultSet(c,sql);
	}

	public static Transaction createTransaction() throws SQLiteException {
		Connection c=Connection.getConnection();
		return new Transaction(c);
	}

	public static Table get(String tableName,long rowid) throws SQLiteException {
		//find class name
		String sql1="SELECT class_name FROM _table_class WHERE table_name='"+tableName+"'";
		QuickResultSet q=query(sql1);
		String class_name=null;
		if (q.next()) {
			class_name=q.getString(0);
		} else {
			throw new SQLiteException(31,"no such tableName "+tableName);
		}
		q.close();

		//create object
		Table record=null;
		Class k=null;
		try {
			k=Class.forName(class_name);
			record=(Table)k.newInstance();
		} catch (Exception x) {
			throw new SQLiteException(31,x.getMessage());
		}

		//select data
		String sql2="SELECT * FROM '"+tableName+"' WHERE id="+rowid;
		QuickResultSet q2=query(sql2);
		//put it in object

		if (q2.next()) {
			try {
			int cols=q2.getColumnCount();
			for (int i=0;i<cols;i++) {
				String colName=q2.getColumnName(i);
				//get the field
				Field f=k.getField(colName);
				f.setAccessible(true);
				String ft=f.getType().getName();

				//look at the fieldtype
				if (ft.equals("java.lang.String")) {
					String s=q2.getString(i);
					f.set(record,s);
				} else if (ft.equals("int")) {
					int v=q2.getInt(i);
					f.setInt(record,v);
				} else if (ft.equals("long")) {
					long v=q2.getLong(i);
					f.setLong(record,v);
				} else if (ft.equals("float")) {
					//there is no getFloat method
					float v=(float)q2.getDouble(i);
					f.setFloat(record,v);
				} else if (ft.equals("double")) {
					double v=q2.getDouble(i);
					f.setDouble(record,v);
				} else {
					throw new RuntimeException("unknown field type "+ft);
				}
			}
			} catch (Exception x) {
				throw new SQLiteException(31,x.getMessage());
			}
		} else {
			throw new SQLiteException(31,"record not found");
		}
		q2.close();
		//return it to user
		return record;
	}

	//==============================================
	//configuration options
	public final static int SQLITE_CONFIG_SINGLETHREAD=  1;  /* nil */
	public final static int SQLITE_CONFIG_MULTITHREAD=   2;  /* nil */
	public final static int SQLITE_CONFIG_SERIALIZED=    3;  /* nil */

	public static SQLITE_API api;


	public interface SQLITE_API extends Library {
		//int sqlite3_libversion_number(void);
		public int sqlite3_libversion_number();

		//The sqlite3_errstr() interface returns the English-language text that describes the result code, as UTF-8.
		public Pointer sqlite3_errstr(int err);

		//int sqlite3_config(int, ...);
		//The sqlite3_config() interface is used to make global configuration changes to SQLite in order to tune
		//SQLite to the specific needs of the application.
		public int sqlite3_config(int op, Object arg);

		//The sqlite3_threadsafe() function returns zero if and only if SQLite was compiled with mutexing code
		//omitted due to the SQLITE_THREADSAFE compile-time option being set to 0.
		public int sqlite3_threadsafe();

	}
}