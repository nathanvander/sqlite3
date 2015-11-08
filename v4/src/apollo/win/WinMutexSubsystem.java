package apollo.win;
import apollo.sqlite3_mutex_methods;
import apollo.sqlite3_mutex;

/**
* The WinMutexSubsystem lets you get mutexes.  There are 11 cached mutexes.
*/

public class WinMutexSubsystem implements sqlite3_mutex_methods {
	private static WinMutexSubsystem instance;
	static {
		//the one and only time this will be called
		instance=new WinMutexSubsystem();
	}

	public static WinMutexSubsystem getInstance() {return instance;}
	//-----------------------
	//the variable name is taken right from the sqlite code
	private WinMutex[] winMutex_staticMutexes;

	//empty constructor
	private WinMutexSubsystem() {}

	//you should only call this once, although it probably won't hurt to call it more than once
	public int MutexInit() {
		//create an array of mutexes
		winMutex_staticMutexes=new WinMutex[14];
		return 0;
	}

	/**
	* The xMutexEnd method defined by this structure is invoked as part of system shutdown by the sqlite3_shutdown()
	* function. The implementation of this method is expected to release all outstanding resources obtained by the
	* mutex methods implementation, especially those obtained by the xMutexInit method. The xMutexEnd() interface is
	* invoked exactly once for each call to sqlite3_shutdown().
	*/
  	public int MutexEnd() {return 0;}
		//no-op.  See finalize()

  	public sqlite3_mutex MutexAlloc(int type) {
		WinMutex mu=null;
		if (type==0 || type==1 || type>13) {
			//create a new mutex
			mu=new WinMutex();
			mu.alloc(type);
		} else {
			//get it from the cached array
			mu=winMutex_staticMutexes[type];
			if (mu==null) {
				//if null create a new one
				mu=new WinMutex();
				mu.alloc(type);
			}
		}
		return mu;
	}

  	public void MutexFree(sqlite3_mutex m) {
		m.sqlite3_mutex_free(); //a no-op
	}

  	public void MutexEnter(sqlite3_mutex m) {
		m.sqlite3_mutex_enter();
	}

	//this will either return 0 for success or SQLITE_BUSY (5) for failure.
	//if busy, just try again
  	public int MutexTry(sqlite3_mutex m) {
		return m.sqlite3_mutex_try();
	}

  	public void MutexLeave(sqlite3_mutex m) {
		m.sqlite3_mutex_leave();
	}

  	public int MutexHeld(sqlite3_mutex m) {
		return m.sqlite3_mutex_held();
	}

	//there is no need for both of these
  	//public int MutexNotheld(sqlite3_mutex m);

	protected void finalize() {
		winMutex_staticMutexes=null;
	}
}