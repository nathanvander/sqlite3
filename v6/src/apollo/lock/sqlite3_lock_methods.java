package apollo.lock;
import com.sun.jna.ptr.IntByReference;
import apollo.file.sqlite3_file;

/**
* There is no sqlite3_lock_methods in the sqlite code.  This is a subset of sqlite3_io_methods
*/

public interface sqlite3_lock_methods {
	public final static int SQLITE_LOCK_NONE=0;
	public final static int SQLITE_LOCK_SHARED=1;
	public final static int SQLITE_LOCK_RESERVED=2;
	public final static int SQLITE_LOCK_PENDING=3;
	public final static int SQLITE_LOCK_EXCLUSIVE=4;

	public final static int NO_LOCK=0;
	public final static int SHARED_LOCK=1;
	public final static int RESERVED_LOCK=2;
	public final static int PENDING_LOCK=3;
	public final static int EXCLUSIVE_LOCK=4;

	/**
	* Lock
  	* from sqlite code: int (*xLock)(sqlite3_file*, int);
	*
  	* The level is 1..4.
  	* State transitions are:
	**    UNLOCKED -> SHARED
	**    SHARED -> RESERVED
	**    SHARED -> (PENDING) -> EXCLUSIVE
	**    RESERVED -> (PENDING) -> EXCLUSIVE
	**    PENDING -> EXCLUSIVE
	*
	* Returns 0 if successful or an error code if otherwise.
	* The actual lock set goes to winFile->locktype
	*/
  	public int Lock(sqlite3_file f, int level);

	/**
	* Unlock
  	* from sqlite code: int (*xUnlock)(sqlite3_file*, int);
  	** It is not possible to lower the locking level one step at a time.  You
	** must go straight to locking level 0.
  	*/
  	public int Unlock(sqlite3_file f, int level);

 	/**
 	* CheckReservedLock
 	* int (*xCheckReservedLock)(sqlite3_file*, int *pResOut);
 	*
	** This routine checks if there is a RESERVED lock held on the specified
	** file by this or any other process. If such a lock is held, return
	** non-zero, otherwise zero.
	*
	* The return variable is in pResOut.
	*/
  	public int CheckReservedLock(sqlite3_file f, IntByReference pResOut);
}