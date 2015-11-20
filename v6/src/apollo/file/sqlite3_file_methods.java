package apollo.file;
//import com.sun.jna.Structure;
//import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Arrays;
import com.sun.jna.platform.win32.WinDef;

/**
* See https://www.sqlite.org/c3ref/io_methods.html for description
*
* The lock methods have been moved to sqlite3_lock_methods.
* The shared memory methods have been move to sqlite3_shared_memory_methods.
*
* The class that implements this will be a singleton
*/

public interface sqlite3_file_methods {

  	//int (*xClose)(sqlite3_file*);
  	//public abstract int xClose(Pointer p_sqlite3_file);
  	public int Close(sqlite3_file f);

  	//int (*xRead)(sqlite3_file*, void*, int iAmt, sqlite3_int64 iOfst);
  	//public abstract int xRead(Pointer p_sqlite3_file,ByteBuffer bb,int iAmt, long iOfst);
  	//public abstract int xRead(Pointer p_sqlite3_file,WinDef.LPVOID v,int iAmt, long iOfst);
  	public int Read(sqlite3_file f,byte[] buff,int iAmt, long iOfst);

	//public int (*xWrite)(sqlite3_file*, const void*, int iAmt, sqlite3_int64 iOfst);
  	//public abstract int xWrite(Pointer p_sqlite3_file,ByteBuffer bb,int iAmt, long iOfst);
	public int Write(sqlite3_file f,byte[] buff,int iAmt, long iOfst);

  	//int (*xTruncate)(sqlite3_file*, sqlite3_int64 size);
  	public int Truncate(sqlite3_file f,long size);

  	//int (*xSync)(sqlite3_file*, int flags);
  	public int Sync(sqlite3_file f,int flags);

  	//int (*xFileSize)(sqlite3_file*, sqlite3_int64 *pSize);
  	public int FileSize(sqlite3_file f,LongByReference pSize);

  	//int (*xFileControl)(sqlite3_file*, int op, void *pArg);
  	//The only examples I see use int or long for the arg
  	public int FileControl(sqlite3_file f, int op, LongByReference pArg);

  	//int (*xSectorSize)(sqlite3_file*);
  	//returns the sector size, usually 512.  Or it could be 4096
  	public int SectorSize(sqlite3_file f);

  	//int (*xDeviceCharacteristics)(sqlite3_file*);
  	public int DeviceCharacteristics(sqlite3_file f);
};