package apollo;
import java.util.UUID;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
* XBase is the abstract base class for Transactions.  A Transaction is a change to any data.  It is a momentary, fleeting
* object that is not recorded. But sometimes it is important to record when it happened.
*
* This uses an OID to record the transaction, because we don't want a central point of failure for assigning
* the id.  This could be used by multiple processes at a time, and certainly will be used by multiple threads
* and we don't want to synchronize the assigning of a transaction id.
*
*/
public abstract class XBase {
	public final static int SQLITE_LOCKED=6;   		/* A table in the database is locked */
	protected long xid;
	protected int timeout;	//the default is 500, meaning 0.5 seconds

	public XBase() {
		xid = OID.getNext();
		timeout=500;	//the default
	}

	/**
	* Change the transaction timeout.  The default is 500 millis.
	*/
	public void setTimeout(int millis) {
		timeout=millis;
	}

	public long getID() {return xid;}

	public abstract void begin() throws SQLiteException;
	public abstract void commit() throws SQLiteException;
	public abstract void rollback() throws SQLiteException;

}