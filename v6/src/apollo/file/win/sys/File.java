package apollo.file.win.sys;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinError;
import com.sun.jna.win32.W32APIOptions;

/**
* The word File is overused.  Here it simply means system functions that require a file handle. This is more like
* RandomAccessFile in Java.
*
* This is a different meaning than in Java, where a File contains information about a file
*/
public class File {
	String name;
	private WinNT.HANDLE handle;

	//create a file.  The handle comes from the filesystem
	public File(String n,WinNT.HANDLE h) {
		if (n==null || h==null) {throw new IllegalArgumentException("null");}
		name=n;
		handle=h;
	}

	public boolean close() {
		boolean b=kernel32.CloseHandle(handle);
		handle=null;   //help the gc
		return b;
	}
	//------------------------------
	//metadata
	public boolean isClosed() {return handle==null;}
	public int getSize() {return kernel32.GetFileSize(handle,null);}
	//public int getSectorSize()
	public String getName() {return name;}
	public WinNT.HANDLE getHandle() {return handle;}

	//---------------------------------
	//read and write
	public boolean flushFileBuffers() {
		return kernel32.FlushFileBuffers(handle);
	}

	//readFile
	//the buffer contains data read from the file
	//call setFilePointer before this to set the location
	public boolean readBytes(byte[] buff, int n) {
		IntByReference nRead=new IntByReference();
		return kernel32.ReadFile(handle,buff,n,nRead,null);
	}

	//writeFile
	//the buffer contains the data to be written to the file
	public boolean writeBytes(byte[] buff,int n) {
		IntByReference nWritten=new IntByReference();
		boolean b= kernel32.WriteFile(handle,buff,n,nWritten,null);
		System.out.println("written="+nWritten.getValue());
		return b;
	}

	//-------------------------------
	//truncate
	//the return value, if successful, is the location of the new pointer.
	public int setFilePointer(long lowerBits) {
		int FILE_BEGIN=0;
		int rv=kernel32.SetFilePointer(handle,lowerBits,null,FILE_BEGIN);
		if (rv==WinBase.INVALID_SET_FILE_POINTER) {
			//error
			int error=kernel32.GetLastError();
			throw new RuntimeException("Windows error: "+String.valueOf(error));
		} else {
			return rv;
		}
	}

	//this sets the end of the file at the current position
	public boolean setEndOfFile() {
		return kernel32.SetEndOfFile(handle);
	}


	//=========================================
	public static Kernel32Ex kernel32;
	static {
		kernel32=(Kernel32Ex) Native.loadLibrary("kernel32", Kernel32Ex.class,W32APIOptions.DEFAULT_OPTIONS);
	}
	public interface Kernel32Ex extends Kernel32 {
		//DWORD WINAPI GetFileSize(
  		//_In_      HANDLE  hFile,
  		//_Out_opt_ LPDWORD lpFileSizeHigh
		//);
		//lpFileSizeHigh [out, optional]
		//A pointer to the variable where the high-order doubleword of the file size is returned.
		//This parameter can be NULL if the application does not require the high-order doubleword.
		public int GetFileSize(WinNT.HANDLE h,LongByReference lpFileSizeHigh);

		//DWORD WINAPI SetFilePointer(
		//  _In_        HANDLE hFile,
		//  _In_        LONG   lDistanceToMove,
		//  _Inout_opt_ PLONG  lpDistanceToMoveHigh,
		//  _In_        DWORD  dwMoveMethod
		//);
		//lpDistanceToMoveHigh [in, out, optional]
		//	A pointer to the high order 32-bits of the signed 64-bit distance to move.
		//	If you do not need the high order 32-bits, this pointer must be set to NULL.
		public int SetFilePointer(WinNT.HANDLE h,long lDistanceToMove,LongByReference lpDistanceToMoveHigh,int dwMoveMethod);

		//BOOL WINAPI SetEndOfFile(
		//  _In_ HANDLE hFile
		//);
		public boolean SetEndOfFile(WinNT.HANDLE h);

	}

}