package sqlite3.os_win;
import sqlite3.*;
import com.sun.jna.Structure;
import com.sun.jna.Pointer;
import java.util.List;
import java.util.Arrays;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinDef;

/*
** The winFile structure is a subclass of sqlite3_file* specific to the win32
** portability layer.
*/
public class WinFile extends sqlite3_file {
	protected List getFieldOrder() {
	     return Arrays.asList(new String[] {"pMethods","pVfs","h","locktype","sharedLockByte","ctrlFlags","lastErrno",
	     	"pShm","zPath","szChunk","nFetchOut","hMap","pMapRegion","mmapSize","mmapSizeActual","mmapSizeMax"});
	 }

//typedef struct winFile winFile;
//struct winFile {
  	//const sqlite3_io_methods *pMethod; /*** Must be first ***/
  	//sqlite3_vfs *pVfs;      /* The VFS used to open this file */
  	public Pointer pVfs;		//p_sqlite3_vfs;
  	public WinNT.HANDLE h;               /* Handle for accessing the file */
  	//u8 locktype;            /* Type of lock currently held on this file */
  	public short locktype;
  	public short sharedLockByte;   /* Randomly chosen byte used as a shared lock */
  	//u8 ctrlFlags;           /* Flags.  See WINFILE_* below */
  	public short ctrlFlags;
  	//DWORD lastErrno;        /* The Windows errno from the last I/O error */
  	public int lastErrno;
	//#ifndef SQLITE_OMIT_WAL
  	//winShm *pShm;           /* Instance of shared memory on this file */
  	public Pointer pShm;
	//#endif
  	//const char *zPath;      /* Full pathname of this file */
  	public String zPath;
  	public int szChunk;            /* Chunk size configured by FCNTL_CHUNK_SIZE */

//#if SQLITE_OS_WINCE
//  LPWSTR zDeleteOnClose;  /* Name of file to delete when closing */
//  HANDLE hMutex;          /* Mutex used to control access to shared lock */
//  HANDLE hShared;         /* Shared memory segment used for locking */
//  winceLock local;        /* Locks obtained by this instance of winFile */
//  winceLock *shared;      /* Global shared lock memory for the file  */
//#endif
//#if SQLITE_MAX_MMAP_SIZE>0
  	public int nFetchOut;                /* Number of outstanding xFetch references */
  	public WinNT.HANDLE hMap;                  /* Handle for accessing memory mapping */
  	//void *pMapRegion;             /* Area memory mapped */
	public WinDef.LPVOID pMapRegion;             /* Area memory mapped */
	public long mmapSize;       /* Usable size of mapped region */
	public long mmapSizeActual; /* Actual size of mapped region */
	public long mmapSizeMax;    /* Configured FCNTL_MMAP_SIZE value */
//#endif


/*
** Allowed values for winFile.ctrlFlags
*/
	//#define WINFILE_RDONLY          0x02   /* Connection is read only */
	public final static short WINFILE_RDONLY = 0x02;   /* Connection is read only */
	//#define WINFILE_PERSIST_WAL     0x04   /* Persistent WAL mode */
	public final static short WINFILE_PERSIST_WAL = 0x04;
	//#define WINFILE_PSOW            0x10   /* SQLITE_IOCAP_POWERSAFE_OVERWRITE */
	public final static short WINFILE_PSOW = 0x10;
};
