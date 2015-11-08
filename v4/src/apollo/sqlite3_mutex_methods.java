package apollo;

/**
* sqlite3_mutex_methods defines the Mutex subsystem for a platform.  This will be implemented as
* a singleton object, so it is in effect static.
*
* Each individual mutex will implement sqlite3_mutex.
*/

public interface sqlite3_mutex_methods {
	/**
	* The xMutexInit method defined by this structure is invoked as part of system initialization
	* by the sqlite3_initialize() function. The xMutexInit routine is called by SQLite exactly once
	* for each effective call to sqlite3_initialize().
	*/
	public int MutexInit();

	/**
	* The xMutexEnd method defined by this structure is invoked as part of system shutdown by the sqlite3_shutdown()
	* function. The implementation of this method is expected to release all outstanding resources obtained by the
	* mutex methods implementation, especially those obtained by the xMutexInit method. The xMutexEnd() interface is
	* invoked exactly once for each call to sqlite3_shutdown().
	*/
  	public int MutexEnd();

  	public sqlite3_mutex MutexAlloc(int type);

  	public void MutexFree(sqlite3_mutex m);

  	public void MutexEnter(sqlite3_mutex m);

  	public int MutexTry(sqlite3_mutex m);

  	public void MutexLeave(sqlite3_mutex m);

  	public int MutexHeld(sqlite3_mutex m);

	//there is no need for this
  	//public int MutexNotheld(sqlite3_mutex m);
}