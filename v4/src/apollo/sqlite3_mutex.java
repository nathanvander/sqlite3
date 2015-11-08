package apollo;

/**
* Mutexes
*
* sqlite3_mutex *sqlite3_mutex_alloc(int);
* void sqlite3_mutex_free(sqlite3_mutex*);
* void sqlite3_mutex_enter(sqlite3_mutex*);
* int sqlite3_mutex_try(sqlite3_mutex*);
* void sqlite3_mutex_leave(sqlite3_mutex*);
*
* The SQLite core uses these routines for thread synchronization. Though they are intended for internal
* use by SQLite, code that links against SQLite is permitted to use any of these routines.
*/
public interface sqlite3_mutex {
	//The [sqlite3_mutex_alloc()] interface takes a single argument
	//which is one of these integer constants.
	public final static int SQLITE_MUTEX_FAST=0;
	public final static int SQLITE_MUTEX_RECURSIVE=        1;
	public final static int SQLITE_MUTEX_STATIC_MASTER=    2;
	public final static int SQLITE_MUTEX_STATIC_MEM=       3;  /* sqlite3_malloc() */
	public final static int SQLITE_MUTEX_STATIC_OPEN=      4;  /* sqlite3BtreeOpen() */
	public final static int SQLITE_MUTEX_STATIC_PRNG=      5;  /* sqlite3_random() */
	public final static int SQLITE_MUTEX_STATIC_LRU=       6;  /* lru page list */
	public final static int SQLITE_MUTEX_STATIC_PMEM=      7;  /* sqlite3PageMalloc() */
	public final static int SQLITE_MUTEX_STATIC_APP1=      8;  /* For use by application */
	public final static int SQLITE_MUTEX_STATIC_APP2=      9;  /* For use by application */
	public final static int SQLITE_MUTEX_STATIC_APP3=     10;  /* For use by application */
	public final static int SQLITE_MUTEX_STATIC_VFS1=     11;  /* For use by built-in VFS */
	public final static int SQLITE_MUTEX_STATIC_VFS2=     12;  /* For use by extension VFS */
	public final static int SQLITE_MUTEX_STATIC_VFS3=     13;  /* For use by application VFS */

	//this is probably defined somewhere else, but we need it here.
	//used if unable to enter the mutex
	//it just means try again in a few milliseconds
	public final static int SQLITE_BUSY=5;   /* The database file is locked temporarily */

	/**
	* alloc() is the initializer.  Create a new object, and then call alloc() on it
	* to allocate memory and otherwise initialize it.
	*/
	public sqlite3_mutex alloc(int type);

	/**
	* de-allocate a previously allocated mutex.
	*/
	public void sqlite3_mutex_free();

	public void sqlite3_mutex_enter();

	public int sqlite3_mutex_try();

	public void sqlite3_mutex_leave();

	//return 1 if true, and 0 if false
	public int sqlite3_mutex_held();
}