package sqlite3.os_win;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.*;

/**
* This duplicates sqlite_vfs, and it probably should be combined. But I want to keep it separate for now
* to keep the method names close to the source.
*/
public interface win_vfs_methods {
    //winOpen,             /* xOpen */
    public int winOpen(Pointer p_sqlite3_vfs, String zName, Pointer p_sqlite3_file, int flags, IntByReference pOutFlags);

    //winDelete,           /* xDelete */
    public int winDelete(Pointer p_sqlite3_vfs,String zName, int syncDir);

    //winAccess,           /* xAccess */
    public int winAccess(Pointer p_sqlite3_vfs,String zName, IntByReference pResOut);

    //winFullPathname,     /* xFullPathname */
  	public int winFullPathName(Pointer p_sqlite3_vfs, int nOut, char[] zOut);

    //winDlOpen,           /* xDlOpen */
  	public Pointer winDlOpen(Pointer p_sqlite3_vfs, String zFileName);

    //winDlError,          /* xDlError */
  	public void winDlError(Pointer p_sqlite3_vfs,int nByte, String zErrMsg);

    //winDlSym,            /* xDlSym */
	public Pointer winDlSym(Pointer p_sqlite3_vfs, String zSymbol);

    //winDlClose,          /* xDlClose */
	public void winDlClose(Pointer p_sqlite3_vfs, Pointer pH);

    //winRandomness,       /* xRandomness */
	public int winRandomness(Pointer p_sqlite3_vfs, int nByte, char[] zOut);

    //winSleep,            /* xSleep */
	public int winSleep(Pointer p_sqlite3_vfs,int microseconds);

    //winCurrentTime,      /* xCurrentTime */
	public int winCurrentTime(Pointer p_sqlite3_vfs,DoubleByReference pCurrentTime);

    //winGetLastError,     /* xGetLastError */
	public int winGetLastError(Pointer p_sqlite3_vfs, char[] zBuf);

    //winCurrentTimeInt64, /* xCurrentTimeInt64 */
	public int winCurrentTimeInt64(Pointer p_sqlite3_vfs,LongByReference pTime);

    //winSetSystemCall,    /* xSetSystemCall */
  	public int winSetSystemCall(Pointer p_sqlite3_vfs,String zName,Pointer sqlite3_syscall_ptr);

    //winGetSystemCall,    /* xGetSystemCall */
	public Pointer winGetSystemCall(Pointer p_sqlite3_vfs, String zName);

    //winNextSystemCall,  /* xNextSystemCall */
	public String winNextSystemCall(Pointer p_sqlite3_vfs, String zName);
}