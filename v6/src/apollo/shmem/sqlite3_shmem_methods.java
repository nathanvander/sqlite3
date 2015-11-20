package apollo.shmem;
import apollo.file.sqlite3_file;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public interface sqlite3_shmem_methods {

  	//int (*xShmMap)(sqlite3_file*, int iPg, int pgsz, int, void volatile**);
	//public abstract int xShmMap(sqlite3_file f, int iPg, int pgsz, int i, Pointer[] pa);
	public int ShmMap(sqlite3_file f, int iPg, int pgsz, int i, PointerByReference mm);

  	//int (*xShmLock)(sqlite3_file*, int offset, int n, int flags);
	//public abstract int xShmLock(Pointer p_sqlite3_file, int offset, int n, int flags);
	public int ShmLock(sqlite3_file f, int offset, int n, int flags);

  	//void (*xShmBarrier)(sqlite3_file*);
	public void ShmBarrier(sqlite3_file f);

  	//int (*xShmUnmap)(sqlite3_file*, int deleteFlag);
  	public int ShmUnmap(sqlite3_file f,int deleteFlag);

  	/* Methods above are valid for version 2 */

  	//int (*xFetch)(sqlite3_file*, sqlite3_int64 iOfst, int iAmt, void **pp);
  	//public abstract int xFetch(Pointer p_sqlite3_file, long iOfst, int iAmt, Pointer[] pp);
  	public int Fetch(sqlite3_file f, long iOfst, int iAmt, PointerByReference mm);

  	//int (*xUnfetch)(sqlite3_file*, sqlite3_int64 iOfst, void *p);
  	//public abstract int xUnfetch(Pointer sqlite3_file, long iOfst, Pointer p);
  	public int Unfetch(sqlite3_file f, long iOfst, Pointer p);
}