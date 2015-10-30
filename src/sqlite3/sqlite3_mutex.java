package sqlite3;
import com.sun.jna.Structure;
import com.sun.jna.Pointer;
import java.util.List;
import java.util.Arrays;
import com.sun.jna.platform.win32.WinNT;
/**
struct sqlite3_mutex {
  pthread_mutex_t mutex;     // Mutex controlling the lock
#if SQLITE_MUTEX_NREF || defined(SQLITE_ENABLE_API_ARMOR)
  int id;                    // Mutex type
#endif
#if SQLITE_MUTEX_NREF
  volatile int nRef;         // Number of entrances
  HANDLE volatile pthread_t owner;  // Thread that is within this mutex
  int trace;                 // True to trace changes
#endif
*/

public class sqlite3_mutex extends Structure {
	WinNT.HANDLE mutex;	/* Mutex controlling the lock */
	int id;			/* Mutex type */
	int nRef;		/* Number of entrances */
	WinNT.HANDLE owner;	/* Thread that is within this mutex */
	int trace;		/* True to trace changes */

	public sqlite3_mutex(Pointer p) {
		super(p);
	}

	protected List getFieldOrder() {
	     return Arrays.asList(new String[] {"mutex","id","nRef","owner","trace"});
	 }
}


