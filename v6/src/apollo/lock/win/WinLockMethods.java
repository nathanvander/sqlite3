package apollo.lock.win;
import apollo.file.sqlite3_file;
import apollo.file.win.WinFile;
import apollo.lock.sqlite3_lock_methods;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinError;
import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.platform.win32.Kernel32;

/**
* The locking system seems way to complicated to me and I don't understand much of it.
* Specifically, I don't understand how it locks between threads on the same process.
* So I am just going to translate the code line by line to make this essentially the
* same code.
*/
public class WinLockMethods implements sqlite3_lock_methods {
	//--------------------
	//static constants
	public final static int LOCKFILE_FAIL_IMMEDIATELY = 1;
	public final static int LOCKFILE_EXCLUSIVE_LOCK=0x00000002;
	public final static int SQLITE_LOCKFILEEX_FLAGS = (LOCKFILE_FAIL_IMMEDIATELY);
	public final static int SQLITE_LOCKFILE_FLAGS =  (LOCKFILE_FAIL_IMMEDIATELY | LOCKFILE_EXCLUSIVE_LOCK);

	public final static int PENDING_BYTE =    (0x40000000);
	public final static int RESERVED_BYTE =    (PENDING_BYTE+1);
	public final static int SHARED_FIRST=(PENDING_BYTE+2);
	public final static int SHARED_SIZE=510;

	public final static int SQLITE_OK =0;
	public final static int SQLITE_BUSY=5;   /* The database file is locked */
	public final static int SQLITE_IOERR=10;   /* Some kind of disk I/O error occurred */
	public final static int SQLITE_IOERR_LOCK=(SQLITE_IOERR | (15<<8));
	//--------------------------
	private static WinLockMethods instance=new WinLockMethods();
	private WinLockMethods() {}
	public static WinLockMethods getInstance() {return instance;}
	public static void assert1(boolean b,String msg) {
		if (!b) {throw new RuntimeException(msg+" is false");}
	}
	//---------------------------
	//instance methods
	public int Lock(sqlite3_file f, int level) {return winLock(f,level);}
  	public int Unlock(sqlite3_file f, int level) {return winUnlock(f,level);}
  	public int CheckReservedLock(sqlite3_file f, IntByReference pResOut) {return winCheckReservedLock(f,pResOut);}
//===========================
/*
** Lock the file with the lock specified by parameter locktype - one
** of the following:
**
**     (1) SHARED_LOCK
**     (2) RESERVED_LOCK
**     (3) PENDING_LOCK
**     (4) EXCLUSIVE_LOCK
**
** Sometimes when requesting one lock state, additional lock states
** are inserted in between.  The locking might fail on one of the later
** transitions leaving the lock state different from what it started but
** still short of its goal.  The following chart shows the allowed
** transitions and the inserted intermediate states:
**
**    UNLOCKED -> SHARED
**    SHARED -> RESERVED
**    SHARED -> (PENDING) -> EXCLUSIVE
**    RESERVED -> (PENDING) -> EXCLUSIVE
**    PENDING -> EXCLUSIVE
**
** This routine will only increase a lock.  The winUnlock() routine
** erases all locks at once and returns us immediately to locking level 0.
** It is not possible to lower the locking level one step at a time.  You
** must go straight to locking level 0.
*/
static int winLock(sqlite3_file id, int locktype){
  int rc = SQLITE_OK;    /* Return code from subroutines */
  //int res = 1;           /* Result of a Windows lock call */
  boolean res=true;
  int newLocktype;       /* Set pFile->locktype to this value before exiting */
  boolean gotPendingLock = false; //0;/* True if we acquired a PENDING lock this time */
  WinFile pFile = (WinFile)id;
  int lastErrno = WinError.NO_ERROR;

  assert1(id!=null, "id!=0" );
  //OSTRACE(("LOCK file=%p, oldLock=%d(%d), newLock=%d\n",
  //         pFile->h, pFile->locktype, pFile->sharedLockByte, locktype));

  /* If there is already a lock of this type or more restrictive on the
  ** OsFile, do nothing. Don't use the end_lock: exit path, as
  ** sqlite3OsEnterMutex() hasn't been called yet.
  */
  if( pFile.locktype>=locktype ){
    //OSTRACE(("LOCK-HELD file=%p, rc=SQLITE_OK\n", pFile->h));
    return SQLITE_OK;
  }

  /* Do not allow any kind of write-lock on a read-only database
  */
  if( (pFile.ctrlFlags & WinFile.WINFILE_RDONLY)!=0 && locktype>=RESERVED_LOCK ){
    return SQLITE_IOERR_LOCK;
  }

  /* Make sure the locking sequence is correct
  */
  assert1( pFile.locktype!=NO_LOCK || locktype==SHARED_LOCK,"a" );
  assert1( locktype!=PENDING_LOCK,"b" );
  assert1( locktype!=RESERVED_LOCK || pFile.locktype==SHARED_LOCK,"c" );

  /* Lock the PENDING_LOCK byte if we need to acquire a PENDING lock or
  ** a SHARED lock.  If we are acquiring a SHARED lock, the acquisition of
  ** the PENDING_LOCK byte is temporary.
  */
  newLocktype = pFile.locktype;
  if(   (pFile.locktype==NO_LOCK)
     || (   (locktype==EXCLUSIVE_LOCK)
         && (pFile.locktype==RESERVED_LOCK))
  ){
    int cnt = 3;
    while( cnt-->0 && (res = winLockFile(pFile.h, SQLITE_LOCKFILE_FLAGS,
                                         PENDING_BYTE, 0, 1, 0)) ){

      /* Try 3 times to get the pending lock.  This is needed to work
      ** around problems caused by indexing and/or anti-virus software on
      ** Windows systems.
      ** If you are using this code as a model for alternative VFSes, do not
      ** copy this retry logic.  It is a hack intended for Windows only.
      */
      lastErrno = os.GetLastError();
      //OSTRACE(("LOCK-PENDING-FAIL file=%p, count=%d, result=%d\n",
      //         pFile->h, cnt, res));
      if( lastErrno==WinError.ERROR_INVALID_HANDLE ){
        pFile.lastErrno = lastErrno;
        rc = SQLITE_IOERR_LOCK;
        //OSTRACE(("LOCK-FAIL file=%p, count=%d, rc=%s\n",
        //         pFile->h, cnt, sqlite3ErrName(rc)));
        return rc;
      }
      //if( cnt>0 ) sqlite3_win32_sleep(1);
      if( cnt>0 ) os.Sleep(1);
    }
    gotPendingLock = res;
    if( !res ){
      lastErrno = os.GetLastError();
      //throw an exception here?
    }
  }

  /* Acquire a shared lock
  */
  if( locktype==SHARED_LOCK && res ){
    assert1( pFile.locktype==NO_LOCK,"NO_LOCK" );
    res = winGetReadLock(pFile);
    if( res ){
      newLocktype = SHARED_LOCK;
    }else{
      lastErrno = os.GetLastError();
    }
  }

  /* Acquire a RESERVED lock
  */
  if( locktype==RESERVED_LOCK && res ){
    assert1( pFile.locktype==SHARED_LOCK,"pFile.locktype==SHARED_LOCK" );
    res = winLockFile(pFile.h, SQLITE_LOCKFILE_FLAGS, RESERVED_BYTE, 0, 1, 0);
    if( res ){
      newLocktype = RESERVED_LOCK;
    }else{
      lastErrno = os.GetLastError();
    }
  }

  /* Acquire a PENDING lock
  */
  if( locktype==EXCLUSIVE_LOCK && res ){
    newLocktype = PENDING_LOCK;
    gotPendingLock = false;  //????
  }

  /* Acquire an EXCLUSIVE lock
  */
  if( locktype==EXCLUSIVE_LOCK && res ){
    assert1( pFile.locktype>=SHARED_LOCK,"" );
    res = winUnlockReadLock(pFile);
    res = winLockFile(pFile.h, SQLITE_LOCKFILE_FLAGS, SHARED_FIRST, 0,
                      SHARED_SIZE, 0);
    if( res ){
      newLocktype = EXCLUSIVE_LOCK;
    }else{
      lastErrno = os.GetLastError();
      winGetReadLock(pFile);
    }
  }

  /* If we are holding a PENDING lock that ought to be released, then
  ** release it now.
  */
  if( gotPendingLock && locktype==SHARED_LOCK ){
    winUnlockFile(pFile.h, PENDING_BYTE, 0, 1, 0);
  }

  /* Update the state of the lock has held in the file descriptor then
  ** return the appropriate result code.
  */
  if( res ){
    rc = SQLITE_OK;
  }else{
    pFile.lastErrno = lastErrno;
    rc = SQLITE_BUSY;
    //OSTRACE(("LOCK-FAIL file=%p, wanted=%d, got=%d\n",
    //         pFile->h, locktype, newLocktype));
  }
  pFile.locktype = newLocktype;
  //OSTRACE(("LOCK file=%p, lock=%d, rc=%s\n",
  //         pFile->h, pFile->locktype, sqlite3ErrName(rc)));
  return rc;
}

/*
** This routine checks if there is a RESERVED lock held on the specified
** file by this or any other process. If such a lock is held, return
** non-zero, otherwise zero.
*/
static int winCheckReservedLock(sqlite3_file id, IntByReference pResOut){
  int res;
  WinFile pFile = (WinFile)id;

  //SimulateIOError( return SQLITE_IOERR_CHECKRESERVEDLOCK; );
  //OSTRACE(("TEST-WR-LOCK file=%p, pResOut=%p\n", pFile->h, pResOut));

  //assert1( id!=0 );
  assert1(id!=null,"id!=0");
  if( pFile.locktype>=RESERVED_LOCK ){
    res = 1;
    //OSTRACE(("TEST-WR-LOCK file=%p, result=%d (local)\n", pFile->h, res));
  }else{
    boolean bres = winLockFile(pFile.h, SQLITE_LOCKFILEEX_FLAGS,RESERVED_BYTE, 0, 1, 0);
    res = (bres) ? 1 : 0;
    //if( res ){
	if (res==1){
      winUnlockFile(pFile.h, RESERVED_BYTE, 0, 1, 0);
    }
    //res = !res;   //whats the purpose of this?
    res = (res==1) ? 0 : 1;
    //OSTRACE(("TEST-WR-LOCK file=%p, result=%d (remote)\n", pFile->h, res));
  }
  //*pResOut = res;
  pResOut.setValue(res);
  //OSTRACE(("TEST-WR-LOCK file=%p, pResOut=%p, *pResOut=%d, rc=SQLITE_OK\n",
  //         pFile->h, pResOut, *pResOut));
  return SQLITE_OK;
}


/*
** Lower the locking level on file descriptor id to locktype.  locktype
** must be either NO_LOCK or SHARED_LOCK.
**
** If the locking level of the file descriptor is already at or below
** the requested locking level, this routine is a no-op.
**
** It is not possible for this routine to fail if the second argument
** is NO_LOCK.  If the second argument is SHARED_LOCK then this routine
** might return SQLITE_IOERR;
*/
static int winUnlock(sqlite3_file id, int locktype){
  int type;
  WinFile pFile = (WinFile)id;
  int rc = SQLITE_OK;
  assert1(pFile!=null, "pFile!=0" );
  assert1(locktype<=SHARED_LOCK, "locktype<=SHARED_LOCK" );
  //OSTRACE(("UNLOCK file=%p, oldLock=%d(%d), newLock=%d\n",
  //         pFile->h, pFile->locktype, pFile->sharedLockByte, locktype));
  type = pFile.locktype;
  if( type>=EXCLUSIVE_LOCK ){
    winUnlockFile(pFile.h, SHARED_FIRST, 0, SHARED_SIZE, 0);
    if( locktype==SHARED_LOCK && !winGetReadLock(pFile) ){
      /* This should never happen.  We should always be able to
      ** reacquire the read lock */
      //rc = winLogError(SQLITE_IOERR_UNLOCK, osGetLastError(),
      //                 "winUnlock", pFile->zPath);
      System.out.println("winUnlock: unable to reacquire the read lock");
    }
  }
  if( type>=RESERVED_LOCK ){
    winUnlockFile(pFile.h, RESERVED_BYTE, 0, 1, 0);
  }
  if( locktype==NO_LOCK && type>=SHARED_LOCK ){
    winUnlockReadLock(pFile);
  }
  if( type>=PENDING_LOCK ){
    winUnlockFile(pFile.h, PENDING_BYTE, 0, 1, 0);
  }
  pFile.locktype = locktype;
  //OSTRACE(("UNLOCK file=%p, lock=%d, rc=%s\n",
  //         pFile->h, pFile->locktype, sqlite3ErrName(rc)));
  return rc;
}

//==============================================
/*
** Lock a file region.
*/
static boolean winLockFile(
  WinNT.HANDLE phFile,	//LPHANDLE phFile,
  int flags,			//DWORD flags,
  int offsetLow,		//  DWORD offsetLow,
  int offsetHigh,
  int numBytesLow,
  int numBytesHigh
){
//#if SQLITE_OS_WINCE
//  /*
//  ** NOTE: Windows CE is handled differently here due its lack of the Win32
//  **       API LockFile.
//  */
//  return winceLockFile(phFile, offsetLow, offsetHigh,
//                       numBytesLow, numBytesHigh);
//#else
//  if( osIsNT() ){
    WinBase.OVERLAPPED ovlp=new WinBase.OVERLAPPED();
    //memset(&ovlp, 0, sizeof(OVERLAPPED));
    ovlp.Offset = offsetLow;
    ovlp.OffsetHigh = offsetHigh;
    return os.LockFileEx(phFile, flags, 0, numBytesLow, numBytesHigh, ovlp);
//  }else{
//    return osLockFile(*phFile, offsetLow, offsetHigh, numBytesLow,
//                      numBytesHigh);
//  }
//#endif
}

/*
** Unlock a file region.
 */
static boolean winUnlockFile(
  WinNT.HANDLE phFile,
  int offsetLow,
  int offsetHigh,
  int numBytesLow,
  int numBytesHigh
){
//#if SQLITE_OS_WINCE
//  /*
//  ** NOTE: Windows CE is handled differently here due its lack of the Win32
//  **       API UnlockFile.
//  */
//  return winceUnlockFile(phFile, offsetLow, offsetHigh,
//                         numBytesLow, numBytesHigh);
//#else
//  if( osIsNT() ){
    //OVERLAPPED ovlp;
    //memset(&ovlp, 0, sizeof(OVERLAPPED));
    WinBase.OVERLAPPED ovlp=new WinBase.OVERLAPPED();
    ovlp.Offset = offsetLow;
    ovlp.OffsetHigh = offsetHigh;
    return os.UnlockFileEx(phFile, 0, numBytesLow, numBytesHigh, ovlp);
//  }else{
//    return osUnlockFile(*phFile, offsetLow, offsetHigh, numBytesLow,
//                        numBytesHigh);
//  }
//#endif
}

//======================================
/*
** Acquire a reader lock.
** Different API routines are called depending on whether or not this
** is Win9x or WinNT.
*
* Returns true if success
*/
static boolean winGetReadLock(WinFile pFile){
  boolean res=false;
  //OSTRACE(("READ-LOCK file=%p, lock=%d\n", pFile->h, pFile->locktype));
  //if( osIsNT() ){
//#if SQLITE_OS_WINCE
//    /*
//    ** NOTE: Windows CE is handled differently here due its lack of the Win32
//    **       API LockFileEx.
//    */
//    res = winceLockFile(&pFile->h, SHARED_FIRST, 0, 1, 0);
//#else
    res = winLockFile(pFile.h, SQLITE_LOCKFILEEX_FLAGS, SHARED_FIRST, 0,
                      SHARED_SIZE, 0);
//#endif
//  }
//#ifdef SQLITE_WIN32_HAS_ANSI
//  else{
//    int lk;
//    sqlite3_randomness(sizeof(lk), &lk);
//    pFile->sharedLockByte = (short)((lk & 0x7fffffff)%(SHARED_SIZE - 1));
//    res = winLockFile(&pFile->h, SQLITE_LOCKFILE_FLAGS,
//                      SHARED_FIRST+pFile->sharedLockByte, 0, 1, 0);
//  }
//#endif
  if( res == false ){
    pFile.lastErrno = os.GetLastError();
    /* No need to log a failure to lock */
  }
  //OSTRACE(("READ-LOCK file=%p, result=%d\n", pFile->h, res));
  return res;
}

/*
** Undo a readlock
*/
static boolean winUnlockReadLock(WinFile pFile){
  boolean res;
  int lastErrno;
  //OSTRACE(("READ-UNLOCK file=%p, lock=%d\n", pFile->h, pFile->locktype));
//  if( osIsNT() ){
    res = winUnlockFile(pFile.h, SHARED_FIRST, 0, SHARED_SIZE, 0);
//  }
//#ifdef SQLITE_WIN32_HAS_ANSI
//  else{
//    res = winUnlockFile(&pFile->h, SHARED_FIRST+pFile->sharedLockByte, 0, 1, 0);
//  }
//#endif
  if( res==false && ((lastErrno = os.GetLastError())!=WinError.ERROR_NOT_LOCKED) ){
    pFile.lastErrno = lastErrno;
    //winLogError(SQLITE_IOERR_UNLOCK, pFile->lastErrno,
    //            "winUnlockReadLock", pFile->zPath);
    System.out.println("ERROR: SQLITE_IOERR_UNLOCK windows error: "+lastErrno);
  }
  //OSTRACE(("READ-UNLOCK file=%p, result=%d\n", pFile->h, res));
  return res;
}

//=========================
	public static Kernel32Ex os;
	static {
		os=(Kernel32Ex) Native.loadLibrary("kernel32", Kernel32Ex.class,W32APIOptions.DEFAULT_OPTIONS);
	}

	//define a new library that deals only with mutex methods
   public interface Kernel32Ex extends Kernel32 {

		/** LockFile
		* BOOL WINAPI LockFile(
		*  _In_ HANDLE hFile,
		*  _In_ DWORD  dwFileOffsetLow,
		*  _In_ DWORD  dwFileOffsetHigh,
		*  _In_ DWORD  nNumberOfBytesToLockLow,
		*  _In_ DWORD  nNumberOfBytesToLockHigh
		*);
		* OffsetHigh is 0, unless the position is greater than an int can handle.
		* likewise, nNumberOfBytesToLockHigh is 0
		*
		* If the function fails, the return value is zero (FALSE). To get extended error information, call GetLastError.
		*/
		public boolean LockFile(WinNT.HANDLE hFile,int offsetLow,int offsetHigh,int nBytesLow,int nBytesHigh);

		/**
		* BOOL WINAPI UnlockFile(
		*  _In_ HANDLE hFile,
		*  _In_ DWORD  dwFileOffsetLow,
		*  _In_ DWORD  dwFileOffsetHigh,
		*  _In_ DWORD  nNumberOfBytesToUnlockLow,
		*  _In_ DWORD  nNumberOfBytesToUnlockHigh
		*);
		*/
		public boolean UnlockFile(WinNT.HANDLE hFile,int offsetLow,int offsetHigh,int nBytesLow,int nBytesHigh);

		//BOOL WINAPI LockFileEx(
		//  _In_       HANDLE       hFile,
		//  _In_       DWORD        dwFlags,
		//  _Reserved_ DWORD        dwReserved,
		//  _In_       DWORD        nNumberOfBytesToLockLow,
		//  _In_       DWORD        nNumberOfBytesToLockHigh,
		//  _Inout_    LPOVERLAPPED lpOverlapped
		//);
		public boolean LockFileEx(WinNT.HANDLE hFile,int dwFlags,int dwReserved,
			int nNumberOfBytesToLockLow, int nNumberOfBytesToLockHigh, WinBase.OVERLAPPED lpOverlapped);

		//BOOL WINAPI UnlockFileEx(
		//  _In_       HANDLE       hFile,
		//  _Reserved_ DWORD        dwReserved,
		//  _In_       DWORD        nNumberOfBytesToUnlockLow,
		//  _In_       DWORD        nNumberOfBytesToUnlockHigh,
		//  _Inout_    LPOVERLAPPED lpOverlapped
		//);
		public boolean UnlockFileEx(WinNT.HANDLE hFile,int dwReserved,
			int nNumberOfBytesToUnlockLow, int nNumberOfBytesToUnlockHigh, WinBase.OVERLAPPED lpOverlapped);


		public void Sleep(int DURATION);
	}

//==========================
//end WinLockMethods
}