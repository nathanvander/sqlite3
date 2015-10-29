package sqlite3.os_win;
import com.sun.jna.Pointer;
import java.nio.ByteBuffer;
/*
** This vector defines all the methods that can operate on an
** sqlite3_file for win32.
*
* I am making this an interface, because the methods haven't been coded yet.
* The class that implements this, to be called, winIoMethod, should also extend sqlite3_io_methods.
*
*/
public interface win_io_methods {
	public int iVersion();
	public int winClose(Pointer p_sqlite3_file);  /* xClose */
  	public int winRead(Pointer p_sqlite3_file,ByteBuffer bb,int iAmt, long iOfst);  /* xRead */
  	public int winWrite(Pointer p_sqlite3_file,ByteBuffer bb,int iAmt, long iOfst); /* xWrite */
  	public int winTruncate(Pointer p_sqlite3_file,long size);  		/* xTruncate */
  	public int winSync(Pointer p_sqlite3_file,int flags);       	/* xSync */
  	public int winFileSize(Pointer p_sqlite3_file,Pointer pSize);	/* xFileSize */
  	public int winLock(Pointer p_sqlite3_file, int i);            	/* xLock */
  	public int winUnlock(Pointer p_sqlite3_file, int i);  			 /* xUnlock */
  	public int winCheckReservedLock(Pointer p_sqlite3_file, Pointer pResOut);  		/* xCheckReservedLock */
  	public int winFileControl(Pointer p_sqlite3_file, int op, Pointer pArg);	  /* xFileControl */
  	public int winSectorSize(Pointer p_sqlite3_file);                  /* xSectorSize */
  	public int winDeviceCharacteristics(Pointer p_sqlite3_file);       /* xDeviceCharacteristics */
  	public int winShmMap(Pointer p_sqlite3_file, int iPg, int pgsz, int i, Pointer[] pa);  /* xShmMap */
  	public int winShmLock(Pointer p_sqlite3_file, int offset, int n, int flags); /* xShmLock */
  	public void winShmBarrier(Pointer p_sqlite3_file);                  /* xShmBarrier */
  	public int winShmUnmap(Pointer p_sqlite3_file,int deleteFlag);                    /* xShmUnmap */
  	public int winFetch(Pointer p_sqlite3_file, long iOfst, int iAmt, Pointer[] pp);  /* xFetch */
  	public int winUnfetch(Pointer sqlite3_file, long iOfst, Pointer p);   /* xUnfetch */
};

