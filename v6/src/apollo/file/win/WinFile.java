package apollo.file.win;
import com.sun.jna.Structure;
import com.sun.jna.Pointer;
import java.util.List;
import java.util.Arrays;
import apollo.file.win.sys.File;
import apollo.file.sqlite3_file;
import apollo.file.sqlite3_vfs;
import com.sun.jna.platform.win32.WinNT;

/**
* This is just a wrapper around the file handle.  This is mostly just a struct with all the
* functionality in WinFileMethods
*/
public class WinFile extends sqlite3_file {
	//Allowed values for winFile.ctrlFlags
	public final static int WINFILE_RDONLY=0x02;   /* Connection is read only */
	public final static int WINFILE_PERSIST_WAL=0x04;   /* Persistent WAL mode */
	public final static int WINFILE_PSOW=0x10;   /* SQLITE_IOCAP_POWERSAFE_OVERWRITE */

	//from struct winFile
	//we only use a subset of these until we are clear on what they mean and if they are needed
	//----------------------
	public WinNT.HANDLE h;
	public int locktype;            /* Type of lock currently held on this file */
  	public int sharedLockByte;   	/* Randomly chosen byte used as a shared lock */
  	public int ctrlFlags;			/* Flags.  See WINFILE_* below */
  	public int lastErrno;        /* The Windows errno from the last I/O error */
	//---------------------

	//This is not in struct winFile
	public File sysFile;

	protected List getFieldOrder() {
	     return Arrays.asList(new String[] {"pMethods","pVfs","locktype","sharedLockByte","ctrlFlags","lastErrno","sysFile"});
	}

	public WinFile(sqlite3_vfs vfs,String n,WinNT.HANDLE h) {
		super.pMethods=null;	//fix this asap
		super.pVfs=vfs;
		this.h=h;
		sysFile=new File(n,h);
	}

	public String getName() {return sysFile.getName();}
}