package apollo;
import java.util.ArrayList;
import java.lang.reflect.*;
import java.lang.annotation.*;

/**
* A Transaction may have multiple mutexes.
*/
public class Transaction extends XBase {
	private Connection conn;
	private ArrayList<Mutex> mutices=new ArrayList<Mutex>();

	//called only by DataStore
	protected Transaction(Connection c) {
		super();
		conn=c;
	}

	public void begin() throws SQLiteException {
		//the deferred transaction just means that the file lock is not set until a write begins.
		//we don't care about that, because we use mutexes to lock
		conn.exec("--begin transaction '"+getID()+"'");
		conn.exec("BEGIN DEFERRED TRANSACTION");
	}

	public void commit() throws SQLiteException {
		conn.exec("COMMIT TRANSACTION");
		conn.exec("--commit transaction '"+getID()+"'");
		releaseMutices();
		conn.recycle();
	}

	public void rollback() throws SQLiteException {
		conn.exec("ROLLBACK TRANSACTION");
		conn.exec("--rollback transaction '"+getID()+"'");
		releaseMutices();
		conn.recycle();
	}

	private void releaseMutices() throws SQLiteException {
		for (int i=0;i<mutices.size();i++) {
			Mutex mu=(Mutex)mutices.get(i);
			mu.release();
			conn.exec("--released mutex "+mu.getName());
		}
	}

	public static String format(String s) {
		if (s==null || s.equals("")) {
			return "NULL";
		} else {
			s=s.replace("'","''");
			return "'"+s+"'";
		}
	}

	//insert the record and return the oid
	public long insert(Table record) throws SQLiteException {
		StringBuilder sql=null;
		long oid=-1;
		try {
		if (record==null) {return -1;}	//don't even mess with it

		//get the insert mutex on the table
		String smu=record.getTableName()+"_insert";
		Mutex m=Mutex.getMutex(smu);
		boolean b=m.acquireOrBlock(timeout);
		if (!b) {
			//just try again in a few milliseconds
			throw new SQLiteException(SQLITE_LOCKED,"unable to acquire mutex on sqlite_master");
		} else {
			mutices.add(m);
			conn.exec("--acquired mutex "+m.getName());
		}

		oid=OID.getNext();
		//now create the sql to do the insert
		sql=new StringBuilder("INSERT INTO "+record.getTableName()+" ("+"\r\n");

		//list fieldnames
		String[] fn=record.getFieldNames();
		for (int i=0;i<fn.length;i++) {
			if (i>0) {sql.append(",");}
			String fieldName=fn[i];
			sql.append(fieldName);
		}
		sql.append(") VALUES (");

		//list values
		String class_name=record.getClass().getName();
		Class k=null;
		//try {
			k=Class.forName(class_name);
		//} catch (ClassNotFoundException x) {
		//	x.printStackTrace();
		//}
		boolean havePk=false;
		for (int j=0;j<fn.length;j++) {
			if (j>0) {sql.append(",");}
			//look at the type
			String fieldName=fn[j];
			Field f=null;
			//try {
			f=k.getField(fieldName);
			//} catch (NoSuchFieldException x) {
			//	x.printStackTrace();
			//}
			f.setAccessible(true);
			String ft=f.getType().getName();

			if (!havePk) {
				//see if it is the primary key. if so use the oid
				boolean isPk=false;
   				Annotation[] aa=f.getDeclaredAnnotations();
   				for (int q=0;q<aa.length;q++) {
					String astr=aa[q].toString();
					//System.out.println(astr);
    				if (astr.equals("@PrimaryKey()") || astr.equals("@apollo.Table$PrimaryKey()")) {
						isPk=true;
					}
				}
				if (isPk) {
					sql.append(oid);
					havePk=true;
					continue;	//next for
				}
			}

			//set the tid
			if (fieldName.equals("_tid") && record.saveTransactionID()) {
				sql.append(getID());
				continue;
			}

			if (ft.equals("java.lang.String")) {
				String v=(String)f.get(record);
				sql.append(format(v));
			} else if (ft.equals("int")) {
				int v=f.getInt(record);
				sql.append(v);
			} else if (ft.equals("long")) {
				long v=f.getLong(record);
				sql.append(v);
			} else if (ft.equals("float")) {
				float v=f.getFloat(record);
				sql.append(v);
			} else if (ft.equals("double")) {
				double v=f.getDouble(record);
				sql.append(v);
			} else {
				throw new RuntimeException("unknown field type "+ft);
			}
		}  //end for
		sql.append(")");

		} catch (Exception x) {
			throw new SQLiteException(31,x.getMessage());
		}

		//now execute it
		conn.exec(sql.toString());
		return oid;
	}

	//in later versions, we will want to store the previous value for historical purposes
	//note that the original tid is stored, not the current tid
	public void update(Table record) throws SQLiteException {
		StringBuilder sql=null;
		try {
		if (record==null) {return;}	//don't even mess with it

		//get the update mutex on the table
		String smu=record.getTableName()+"_update";
		Mutex m=Mutex.getMutex(smu);
		boolean b=m.acquireOrBlock(timeout);
		if (!b) {
			//just try again in a few milliseconds
			throw new SQLiteException(SQLITE_LOCKED,"unable to acquire mutex on sqlite_master");
		} else {
			mutices.add(m);
			conn.exec("--acquired mutex "+m.getName());
		}

		sql=new StringBuilder("UPDATE "+record.getTableName()+" SET\r\n");
		String[] fn=record.getFieldNames();
		String class_name=record.getClass().getName();
		Class k=Class.forName(class_name);

		boolean havePk=false;
		String pkFieldName=null;
		long pk=-1;	//this will be replaced with the actual
		for (int i=0;i<fn.length;i++) {
			if (i>0) {sql.append(",\r\n");}
			String fieldName=fn[i];
			Field f=k.getField(fieldName);
			f.setAccessible(true);
			String ft=f.getType().getName();

			//look for the primarykey
			if (!havePk) {
				//see if it is the primary key.
				boolean isPk=false;
   				Annotation[] aa=f.getDeclaredAnnotations();
   				for (int j=0;j<aa.length;j++) {
					String astr=aa[j].toString();
					//System.out.println(astr);
    				if (astr.equals("@PrimaryKey()") || astr.equals("@apollo.Table$PrimaryKey()")) {
						isPk=true;
					}
				}
				if (isPk) {
					//get the oid
					long v=f.getLong(record);
					if (v<1) {
						throw new RuntimeException("trying to update record with primary key of "+v);
					}
					pk=v;
					pkFieldName=fieldName;
					havePk=true;
					continue;	//next for
				}
			} //end havePk

			sql.append(fieldName+"=");
			if (ft.equals("java.lang.String")) {
				String v=(String)f.get(record);
				sql.append(format(v));
			} else if (ft.equals("int")) {
				int v=f.getInt(record);
				sql.append(v);
			} else if (ft.equals("long")) {
				long v=f.getLong(record);
				sql.append(v);
			} else if (ft.equals("float")) {
				float v=f.getFloat(record);
				sql.append(v);
			} else if (ft.equals("double")) {
				double v=f.getDouble(record);
				sql.append(v);
			} else {
				throw new RuntimeException("unknown field type "+ft);
			}
		}
		sql.append(" WHERE "+pkFieldName+"="+pk);
		} catch (Exception x) {
			throw new SQLiteException(31,x.getMessage());
		}
		//now execute it
		conn.exec(sql.toString());
	}

	public void delete(String tableName,long rowid) throws SQLiteException {
		if (tableName==null) return;
		//even though this is a delete, get the update mutex on the table
		String smu=tableName+"_update";
		Mutex m=Mutex.getMutex(smu);
		boolean b=m.acquireOrBlock(timeout);
		if (!b) {
			//just try again in a few milliseconds
			throw new SQLiteException(SQLITE_LOCKED,"unable to acquire mutex on sqlite_master");
		} else {
			mutices.add(m);
			conn.exec("--acquired mutex "+m.getName());
		}

		//for this purpose assume that the primary key is named "id"
		String sql="DELETE FROM "+tableName+" WHERE id="+rowid;
		//now execute it
		conn.exec(sql.toString());
	}

	//you can call multiple queries in the same Transaction
	public ResultSet query(String sql) throws SQLiteException {
		return new ResultSet(conn.getHandle(),sql);
	}
}