package apollo;
import java.lang.reflect.*;
import java.lang.annotation.*;

/**
* This really should be called SchemaTransaction, but there isn't another class called Schema, so this won't
* be confusing, and I like the shorter name.  A Schema allows you to make changes to the database.
*
* The only changes allowed are: createTable, dropTable, createIndex, and dropIndex
*
* Compare to Transaction.  This should probably extend Transaction, but that is for a later version.
*
* This has a close() method.  This should be used only if you used the Schema just to get a read only view,
* for example to list table names.  If you call commit() or rollback() don't call close as well.
*
*/
public class Schema extends XBase {
	private static boolean initialized=false;		//we want a deferred initialization, not upon class loading
	private Connection conn;
	private Mutex mutex;
	private boolean closed=false;

	//we want this loaded once upon initialization, but not upon class loading.
	//this is so we can set the database file name
	private static synchronized void classInit() throws SQLiteException {
		//create the _table_class table

				try {
					Schema s=new Schema();
					s.conn=Connection.getConnection();
					s.begin();  //begin a schema transaction
					String sql="CREATE TABLE IF NOT EXISTS _table_class (table_name TEXT PRIMARY KEY NOT NULL,class_name TEXT NOT NULL)";
					//System.out.println(sql);
					int rows=s.conn.exec(sql);
					s.commit();	//this recycles the connection
					initialized=true;
				} catch (SQLiteException x) {
					x.printStackTrace();
				}
	}

	//called only by classInit()
	private Schema() {super();}

	//called only by DataStore
	protected Schema(Connection c) throws SQLiteException {
		super();
		this.conn=c;
		if (!initialized) {
			classInit();
		}
	}

	protected void finalize() {
		conn=null;
		mutex=null;
	}

	//call this if you are done with the schema and didn't start a transaction with begin
	//if you called begin, DONT call this or it will screw up the connection
	public void close() {
		if (!closed) {
			conn.recycle();
			closed=true;
		}
	}

	public boolean isClosed() {return closed;}

	//---------------------------------------------------
	public void begin() throws SQLiteException {
		//System.out.println("beginning transaction "+getID());
		//acquire the sqlite_master mutex
		mutex=Mutex.getMutex("sqlite_master");
		boolean b=mutex.acquireOrBlock(timeout);
		if (!b) {
			throw new SQLiteException(SQLITE_LOCKED,"unable to acquire mutex on sqlite_master, try again shortly");
		}

		conn.exec("--begin schema transaction '"+getID()+"'");
		conn.exec("--acquired mutex sqlite_master");
		//this will throw an exception if it fails or if a transaction is already active in the connection
		conn.exec("BEGIN DEFERRED TRANSACTION");
	}


	//public methods
	public void commit() throws SQLiteException {
		conn.exec("COMMIT TRANSACTION");
		conn.exec("--transaction "+getID()+" committed");
		conn.exec("--released mutex sqlite_master");
		//drop the lock
		mutex.release();
		//notify connection that the transaction is closed
		if (!closed) {
			conn.recycle();
			closed=true;
		}
		//System.out.println("transaction "+xid+" committed");
	}

	public void rollback() throws SQLiteException {
		conn.exec("ROLLBACK TRANSACTION");
		conn.exec("--transaction "+getID()+" rolled back");
		conn.exec("--released mutex sqlite_master");
		mutex.release();
		if (!closed) {
			conn.recycle();
			closed=true;
		}
		//System.out.println("transaction "+xid+" rolled back");
	}

	//---------------------------------------------------
	//we pass in a table object instead of a class in order to limit what can be passed here
	public void createTable(Table t) throws SQLiteException {
		try {
		if (t==null) {return;}
		String table_name=t.getTableName();
		String class_name=t.getClass().getName();
		String[] field_names=t.getFieldNames();
		Class k=Class.forName(class_name);
		StringBuilder sb=new StringBuilder("CREATE TABLE IF NOT EXISTS "+table_name+" ("+"\r\n");
		for (int i=0;i<field_names.length;i++) {
			String fn=field_names[i];
			Field f=k.getField(fn);
			String fieldType=f.getType().getName();
			String sqliteType="TEXT";	//the default

			//convert the type
			if (	fieldType.equals("int") ||
					fieldType.equals("java.lang.Integer") ||
					fieldType.equals("long") ||
					fieldType.equals("java.lang.Long")
				) {
				sqliteType="INTEGER";
			} else if (fieldType.equals("float") ||
					fieldType.equals("java.lang.Float") ||
					fieldType.equals("double") ||
					fieldType.equals("java.lang.Double")
				) {
				sqliteType="REAL";
			} else if (fieldType.equals("[B")) {
				sqliteType="BLOB";
			}

			//get annotations
			boolean isPk=false;
			boolean notNull=false;
   			Annotation[] aa=f.getDeclaredAnnotations();
   			for (int j=0;j<aa.length;j++) {
				String astr=aa[j].toString();
				//System.out.println(astr);
    			if (astr.equals("@PrimaryKey()") || astr.equals("@apollo.Table$PrimaryKey()")) {
					isPk=true;
				} else if (astr.equals("@NotNull()") || astr.equals("@apollo.Table$NotNull()")) {
					notNull=true;
				}
			}

			//now make the line
			//fieldname
			sb.append(fn+" ");
			//type
			sb.append(sqliteType+" ");
			if (isPk) {
				sb.append("PRIMARY KEY ");
			}
			if (notNull) {
				sb.append("NOT NULL");
			}

			//add comma
			if (i!=field_names.length-1) {
				sb.append(",");
			}
			//add new line
			sb.append("\r\n");
		} //end for
		//add last line
		sb.append(")");
		//System.out.println(sb.toString());

		//execute it
		conn.exec(sb.toString());

		//see if it is in tableclass
		String sql="SELECT table_name FROM _table_class WHERE table_name='"+table_name+"'";
		ResultSet rs=conn.query(sql);
		boolean tableNameExists=rs.next();
		rs.close();

		//now insert into tableclass
		if (!tableNameExists) {
			String sql3="INSERT INTO _table_class VALUES ('"+table_name+"','"+class_name+"')";
			//System.out.println(sql);
			conn.exec(sql3);
		}

		} catch (SQLiteException sx) {
			throw sx;
		} catch (Exception x) {
			throw new SQLiteException(1,x.getMessage());
		}
	}

	public void dropTable(String tableName) throws SQLiteException {
		String sql="DROP TABLE IF EXISTS "+tableName;
		//System.out.println(sql);
		conn.exec(sql);
		String sql2="DELETE FROM _table_class WHERE table_name='"+tableName+"'";
		//System.out.println(sql2);
		conn.exec(sql2);
	}

	public void createIndex(String indexName,String tableName,String[] columns) throws SQLiteException {
		//join the columns
		StringBuilder sb=new StringBuilder();
		for (int i=0;i<columns.length;i++) {
			sb.append(columns[i]);
			if (i!=columns.length-1) {
				sb.append(",");
			}
		}

		String indexSql="CREATE INDEX IF NOT EXISTS "+indexName+" ON "+tableName+" ("+sb.toString()+")";
		//System.out.println(indexSql);
		conn.exec(indexSql);
	}

	public void dropIndex(String indexName) throws SQLiteException {
		String dropSql="DROP INDEX IF EXISTS "+indexName;
		//System.out.println(dropSql);
		conn.exec(dropSql);
	}

	//list tables
	public ResultSet listTables() throws SQLiteException {
		String listTablesSql="SELECT name FROM sqlite_master WHERE type='table' ORDER BY name";
		return conn.query(listTablesSql);
	}

	//list indexes.  indices?
	public ResultSet listIndexes() throws SQLiteException {
		String listTablesSql="SELECT name,tbl_name FROM sqlite_master WHERE type='index' ORDER BY name";
		return conn.query(listTablesSql);
	}

	public ResultSet listTempTables() throws SQLiteException {
		String listTablesSql="SELECT name FROM sqlite_temp_master WHERE type='table' ORDER BY name";
		return conn.query(listTablesSql);
	}

	//retrieve a list of columns in the table
	public ResultSet listColumns(String table) throws SQLiteException {
		String sql="PRAGMA table_info("+table+")";
		return conn.query(sql);
	}

	//==========================
	//not sure if these are needed.  These are the built-in tables
	//the sqlite_master table contains the names of all the tables and indexes in the database
	//it is created automatically by the database
	//this is read only
	public class sqlite_master implements Table {
  		public String type;		//type is either "table" or "index"
  		public @PrimaryKey String name;
  		public String tbl_name;
  		public int rootpage;
  		public String sql;

  		public sqlite_master() {}
  		public String getTableName() {return "sqlite_master";}
  		public String[] getFieldNames() {
			return new String[]{"type","name","tbl_name","rootpage","sql"};
		}
		public boolean saveTransactionID() {return false;}
		public String[] getIndices() {return null;}

	}

	//this is a pseudo-table called by PRAGMA table_info
	//it records the columns in a table
	public class table_info implements Table {
		public @PrimaryKey int cid;
		public String name;
		public String type;
		public @Bool int notnull;	//0 is false, 1 is true
		public String dflt_value;
		public @Bool int pk;		//0 is false, 1 is true
		public String tbl_name;		//I added this

		public table_info() {}
		public String getTableName() {return "table_info";}
		public String[] getFieldNames() {
			return new String[]{"cid","name","type","notnull","dflt_value","pk","tbl_name"};
		}
		public boolean saveTransactionID() {return false;}
		public String[] getIndices() {return null;}
	}

	//the _table_class table holds the java class that represent the tbl_name
	public class _table_class implements Table {
		public @PrimaryKey String tbl_name;
		public @NotNull String class_name;
		public _table_class() {}
		public String getTableName() {return "_table_class";}
		public String[] getFieldNames() {
			return new String[]{"tbl_name","class_name"};
		}
		public boolean saveTransactionID() {return false;}
		public String[] getIndices() {return null;}
	}
}