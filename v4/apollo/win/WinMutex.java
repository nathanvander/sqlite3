package apollo.win;
import apollo.sqlite3_mutex;
import apollo.win.sys.CriticalSection;

/**
* There is one WinMutex object for each mutex. This implements sqlite3_mutex by using a CriticalSection.
* This doesn't distinguish between a mutex of type SQLITE_MUTEX_FAST and SQLITE_MUTEX_RECURSIVE.  They are both
* allocated dynamically.
*/

public class WinMutex implements sqlite3_mutex  {
	private CriticalSection mutex;
	private int type;
	private static int counter=0;
	private int id;

	/**
	* This has an empty constructor. You must call alloc() to initilize it. This is protected because
	* it should be called from WinMutexSubsystem.
	*/
	protected WinMutex() {}

	/**
	* The word alloc() is historical, this should really be called init().  This involves 2 steps:
	* 1. creating the CriticalSection with a unique name.  This actually does the allocation.
	* 2. calling init() on the section.
	*/
	public sqlite3_mutex alloc(int type) {
		id=++counter;
		this.type=type;
		//create a name for the mutex
		String name=null;
		switch (type) {
			case SQLITE_MUTEX_FAST:
				name="SQLITE_MUTEX_FAST_"+String.valueOf(id); break;
			case SQLITE_MUTEX_RECURSIVE:
				name="SQLITE_MUTEX_RECURSIVE_"+String.valueOf(id); break;
			case SQLITE_MUTEX_STATIC_MASTER:
				name="SQLITE_MUTEX_STATIC_MASTER"; break;
			case SQLITE_MUTEX_STATIC_MEM:
				name="SQLITE_MUTEX_STATIC_MEM"; break;
			case SQLITE_MUTEX_STATIC_OPEN:
				name="SQLITE_MUTEX_STATIC_OPEN"; break;
			case SQLITE_MUTEX_STATIC_PRNG:
				name="SQLITE_MUTEX_STATIC_PRNG"; break;
			case SQLITE_MUTEX_STATIC_LRU:
				name="SQLITE_MUTEX_STATIC_LRU"; break;
			case SQLITE_MUTEX_STATIC_PMEM:
				name="SQLITE_MUTEX_STATIC_PMEM"; break;
			case SQLITE_MUTEX_STATIC_APP1:
				name="SQLITE_MUTEX_STATIC_APP1"; break;
			case SQLITE_MUTEX_STATIC_APP2:
				name="SQLITE_MUTEX_STATIC_APP2"; break;
			case SQLITE_MUTEX_STATIC_APP3:
				name="SQLITE_MUTEX_STATIC_APP3"; break;
			case SQLITE_MUTEX_STATIC_VFS1:
				name="SQLITE_MUTEX_STATIC_VFS1"; break;
			case SQLITE_MUTEX_STATIC_VFS2:
				name="SQLITE_MUTEX_STATIC_VFS2"; break;
			case SQLITE_MUTEX_STATIC_VFS3:
				name="SQLITE_MUTEX_STATIC_VFS3"; break;
			default:
				name="MUTEX_"+String.valueOf(id); break;
		}

		//create the critical section.
		//this does the allocation itself
		mutex=new CriticalSection(name);
		//init()
		mutex.init();
		return this;
	}

	public int getId() {return id;}
	public String getName() {return mutex.getName();}

	/**
	* de-allocate a previously allocated mutex.
	*/
	public void sqlite3_mutex_free() {
		//this is a no-op.  The mutex is cleaned up by the garbage collector.
		//see finalize();
	}

	//this will block
	public void sqlite3_mutex_enter() {
		mutex.enter();
	}

	/**
	* ^If another thread is already within the mutex,
	** sqlite3_mutex_enter() will block and sqlite3_mutex_try() will return
	** SQLITE_BUSY.  ^The sqlite3_mutex_try() interface returns [SQLITE_OK]
	** upon successful entry.
	*/
	public int sqlite3_mutex_try() {
		boolean b=mutex.tryEnter();
		if (b) {
			return 0; //SQLITE_OK
		} else {
			return SQLITE_BUSY;
		}
	}

	public void sqlite3_mutex_leave() {
		mutex.leave();
	}

	public int sqlite3_mutex_held() {
		//true if mutex has an owner and it is me, that is the current thread
		boolean b=mutex.isHeld();
		return b ? 1 : 0;
	}

	protected void finalize() {
		mutex=null;
	}

}