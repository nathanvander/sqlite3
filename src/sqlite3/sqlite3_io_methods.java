package sqlite3;
import com.sun.jna.Structure;
import com.sun.jna.Pointer;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Arrays;
import com.sun.jna.platform.win32.WinDef;

/**
* This is the sqlite3_io_methods struct.  Subclass it to use it.
*/

//typedef struct sqlite3_io_methods sqlite3_io_methods;
//struct sqlite3_io_methods {
public abstract class sqlite3_io_methods extends Structure {

	protected List getFieldOrder() {
	     return Arrays.asList(new String[] {"iVersion"});
	 }

	public int iVersion;
  	//int (*xClose)(sqlite3_file*);
  	public abstract int xClose(Pointer p_sqlite3_file);

  	//int (*xRead)(sqlite3_file*, void*, int iAmt, sqlite3_int64 iOfst);
  	//public abstract int xRead(Pointer p_sqlite3_file,ByteBuffer bb,int iAmt, long iOfst);
  	public abstract int xRead(Pointer p_sqlite3_file,WinDef.LPVOID v,int iAmt, long iOfst);

	//public int (*xWrite)(sqlite3_file*, const void*, int iAmt, sqlite3_int64 iOfst);
  	//public abstract int xWrite(Pointer p_sqlite3_file,ByteBuffer bb,int iAmt, long iOfst);
	public abstract int xWrite(Pointer p_sqlite3_file,WinDef.LPVOID v,int iAmt, long iOfst);

  	//int (*xTruncate)(sqlite3_file*, sqlite3_int64 size);
  	public abstract int xTruncate(Pointer p_sqlite3_file,long size);

  	//int (*xSync)(sqlite3_file*, int flags);
  	public abstract int xSync(Pointer p_sqlite3_file,int flags);

  	//int (*xFileSize)(sqlite3_file*, sqlite3_int64 *pSize);
  	public abstract int xFileSize(Pointer p_sqlite3_file,Pointer pSize);

  	//int (*xLock)(sqlite3_file*, int);
  	public abstract int xLock(Pointer p_sqlite3_file, int i);

  	//int (*xUnlock)(sqlite3_file*, int);
  	public abstract int xUnlock(Pointer p_sqlite3_file, int i);

  	//int (*xCheckReservedLock)(sqlite3_file*, int *pResOut);
  	public abstract int xCheckReservedLock(Pointer p_sqlite3_file, Pointer pResOut);

  	//int (*xFileControl)(sqlite3_file*, int op, void *pArg);
  	public abstract int xFileControl(Pointer p_sqlite3_file, int op, Pointer pArg);

  	//int (*xSectorSize)(sqlite3_file*);
  	public abstract int xSectorSize(Pointer p_sqlite3_file);

  	//int (*xDeviceCharacteristics)(sqlite3_file*);
  	public abstract int xDeviceCharacteristics(Pointer p_sqlite3_file);

  	/* Methods above are valid for version 1 */

  	//int (*xShmMap)(sqlite3_file*, int iPg, int pgsz, int, void volatile**);
	public abstract int xShmMap(Pointer p_sqlite3_file, int iPg, int pgsz, int i, Pointer[] pa);

  	//int (*xShmLock)(sqlite3_file*, int offset, int n, int flags);
	public abstract int xShmLock(Pointer p_sqlite3_file, int offset, int n, int flags);

  	//void (*xShmBarrier)(sqlite3_file*);
	public abstract void xShmBarrier(Pointer p_sqlite3_file);

  	//int (*xShmUnmap)(sqlite3_file*, int deleteFlag);
  	public abstract int xShmUnmap(Pointer p_sqlite3_file,int deleteFlag);

  	/* Methods above are valid for version 2 */

  	//int (*xFetch)(sqlite3_file*, sqlite3_int64 iOfst, int iAmt, void **pp);
  	public abstract int xFetch(Pointer p_sqlite3_file, long iOfst, int iAmt, Pointer[] pp);

  	//int (*xUnfetch)(sqlite3_file*, sqlite3_int64 iOfst, void *p);
  	//public abstract int xUnfetch(Pointer sqlite3_file, long iOfst, Pointer p);
  	public abstract int xUnfetch(Pointer sqlite3_file, long iOfst, WinDef.LPVOID p);

  /* Methods above are valid for version 3 */
  /* Additional methods may be added in future releases */
};