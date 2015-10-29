package sqlite3;
import com.sun.jna.Structure;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.PointerByReference;
import java.util.List;
import java.util.Arrays;

public abstract class sqlite3_vfs extends Structure {
//typedef struct sqlite3_vfs sqlite3_vfs;
//typedef void (*sqlite3_syscall_ptr)(void);
//struct sqlite3_vfs {
	//-----------
	//fields
  	public int iVersion;            /* Structure version number (currently 3) */
  	public int szOsFile;            /* Size of subclassed sqlite3_file */
  	public int mxPathname;          /* Maximum file pathname length */
    public Pointer pNext;      /* Next registered VFS. sqlite3_vfs */
  	public String zName;       /* Name of this virtual file system */
  	public Pointer pAppData;          /* Pointer to application-specific data */
	//------------------

	protected List getFieldOrder() {
	     return Arrays.asList(new String[] {"iVersion","szOsFile","mxPathName","pNext","zName","pAppData"});
	}

  	//int (*xOpen)(sqlite3_vfs*, const char *zName, sqlite3_file*,
  	//             int flags, int *pOutFlags);
  	//Open a file.  The 3rd argument is a pointer to the filehandle
  	public abstract int xOpen(Pointer p_sqlite3_vfs, String zName, Pointer p_sqlite3_file, int flags, IntByReference pOutFlags);

  	//int (*xDelete)(sqlite3_vfs*, const char *zName, int syncDir);
  	public abstract int xDelete(Pointer p_sqlite3_vfs,String zName, int syncDir);

  	//int (*xAccess)(sqlite3_vfs*, const char *zName, int flags, int *pResOut);
  	public abstract int xAccess(Pointer p_sqlite3_vfs,String zName, IntByReference pResOut);

  	//int (*xFullPathname)(sqlite3_vfs*, const char *zName, int nOut, char *zOut);
  	public abstract int xFullPathName(Pointer p_sqlite3_vfs, int nOut, char[] zOut);

  	//void *(*xDlOpen)(sqlite3_vfs*, const char *zFilename);
  	public abstract Pointer xDlOpen(Pointer p_sqlite3_vfs, String zFileName);

  	//void (*xDlError)(sqlite3_vfs*, int nByte, char *zErrMsg);
  	public abstract void xDlError(Pointer p_sqlite3_vfs,int nByte, String zErrMsg);

  	//void (*(*xDlSym)(sqlite3_vfs*,void*, const char *zSymbol))(void);
  	public abstract Pointer xDlSym(Pointer p_sqlite3_vfs, String zSymbol);

  	//void (*xDlClose)(sqlite3_vfs*, void*);
  	//note: the second argument is a handle
  	public abstract void xDlClose(Pointer p_sqlite3_vfs, Pointer pH);

  	//int (*xRandomness)(sqlite3_vfs*, int nByte, char *zOut);
  	public abstract int xRandomness(Pointer p_sqlite3_vfs, int nByte, char[] zOut);

  	//int (*xSleep)(sqlite3_vfs*, int microseconds);
  	public abstract int xSleep(Pointer p_sqlite3_vfs,int microseconds);

  	//int (*xCurrentTime)(sqlite3_vfs*, double*);
  	public abstract int xCurrentTime(Pointer p_sqlite3_vfs,DoubleByReference pCurrentTime);

  	//int (*xGetLastError)(sqlite3_vfs*, int, char *);
  	public abstract int xGetLastError(Pointer p_sqlite3_vfs, char[] zBuf);

  	/*
  	** The methods above are in version 1 of the sqlite_vfs object
  	** definition.  Those that follow are added in version 2 or later
  	*/
  	//int (*xCurrentTimeInt64)(sqlite3_vfs*, sqlite3_int64*);
  	public abstract int xCurrentTimeInt64(Pointer p_sqlite3_vfs,LongByReference pTime);
  	/*
  	** The methods above are in versions 1 and 2 of the sqlite_vfs object.
  	** Those below are for version 3 and greater.
  	*/
  	//int (*xSetSystemCall)(sqlite3_vfs*, const char *zName, sqlite3_syscall_ptr); //Pointer
  	public abstract int xSetSystemCall(Pointer p_sqlite3_vfs,String zName,Pointer sqlite3_syscall_ptr);

  	//sqlite3_syscall_ptr (*xGetSystemCall)(sqlite3_vfs*, const char *zName);
  	public abstract Pointer xGetSystemCall(Pointer p_sqlite3_vfs, String zName);

  	//const char *(*xNextSystemCall)(sqlite3_vfs*, const char *zName);
  	public abstract String xNextSystemCall(Pointer p_sqlite3_vfs, String zName);
}