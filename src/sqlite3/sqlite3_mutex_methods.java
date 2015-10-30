package sqlite3;
import com.sun.jna.Structure;
import com.sun.jna.Pointer;
import java.util.List;
import java.util.Arrays;


//typedef struct sqlite3_mutex_methods sqlite3_mutex_methods;
//struct sqlite3_mutex_methods {
public abstract class sqlite3_mutex_methods extends Structure {
	public abstract int xMutexInit();
  	public abstract int xMutexEnd();
  	public abstract sqlite3_mutex xMutexAlloc(int n);
  	public abstract void xMutexFree(Pointer p_sqlite3_mutex );
  	public abstract void xMutexEnter(Pointer p_sqlite3_mutex );
  	public abstract int xMutexTry(Pointer p_sqlite3_mutex );
  	public abstract void xMutexLeave(Pointer p_sqlite3_mutex );
  	public abstract int xMutexHeld(Pointer p_sqlite3_mutex);
  	public abstract int xMutexNotheld(Pointer p_sqlite3_mutex);

	protected List getFieldOrder() {
	     return Arrays.asList(new String[] {});
	}
}

/*
** CAPI3REF: Mutex Types
**
** The [sqlite3_mutex_alloc()] interface takes a single argument
** which is one of these integer constants.
**
** The set of static mutexes may change from one SQLite release to the
** next.  Applications that override the built-in mutex logic must be
** prepared to accommodate additional static mutexes.
*
#define SQLITE_MUTEX_FAST             0
#define SQLITE_MUTEX_RECURSIVE        1
#define SQLITE_MUTEX_STATIC_MASTER    2
#define SQLITE_MUTEX_STATIC_MEM       3  / sqlite3_malloc() /
#define SQLITE_MUTEX_STATIC_MEM2      4  / NOT USED /
#define SQLITE_MUTEX_STATIC_OPEN      4  / sqlite3BtreeOpen() /
#define SQLITE_MUTEX_STATIC_PRNG      5  / sqlite3_random() /
#define SQLITE_MUTEX_STATIC_LRU       6  / lru page list /
#define SQLITE_MUTEX_STATIC_LRU2      7  / NOT USED /
#define SQLITE_MUTEX_STATIC_PMEM      7  / sqlite3PageMalloc() /
#define SQLITE_MUTEX_STATIC_APP1      8  / For use by application /
#define SQLITE_MUTEX_STATIC_APP2      9  / For use by application /
#define SQLITE_MUTEX_STATIC_APP3     10  / For use by application /
#define SQLITE_MUTEX_STATIC_VFS1     11  / For use by built-in VFS /
#define SQLITE_MUTEX_STATIC_VFS2     12  / For use by extension VFS /
#define SQLITE_MUTEX_STATIC_VFS3     13  / For use by application VFS /
*/