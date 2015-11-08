package apollo.win.sys;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinError;
import com.sun.jna.win32.W32APIOptions;
import java.util.Arrays;
import java.util.List;

/**
* The FileSystem class, as used here, is simply a wrapper around Windows functions that deal with files
* that do not require a handle as input.  Any function that requires a handle is in the File class.
*
* These are all static methods.
*
* The file creation system is way more complicated than it should be, so this has 4 methods to make it easier.
* These are not the only possible combinations for opening a file.
*/

public class FileSystem {
	static int tempCount=0;
	/**
	* Create file exclusive.  This means that you must create a new file, and it fails if the file already exists.
	* You must have a filename. Please check if the file already exists before calling this.
	*/
	public static WinNT.HANDLE createFileExclusive(String fileName, boolean deleteOnClose) {
		if (fileName==null) {throw new IllegalArgumentException("fileName is null");}
		int dwDesiredAccess = READWRITE;
		int dwShareMode = FILE_SHARE_READ | FILE_SHARE_WRITE;
		int dwCreationDisposition = CREATE_NEW;
		int dwFlagsAndAttributes =0;
		if (deleteOnClose) {
			dwFlagsAndAttributes = (FILE_ATTRIBUTE_TEMPORARY
                               | FILE_ATTRIBUTE_HIDDEN
                               | FILE_FLAG_DELETE_ON_CLOSE
                               | FILE_FLAG_RANDOM_ACCESS);
		} else {
			dwFlagsAndAttributes = FILE_ATTRIBUTE_NORMAL | FILE_FLAG_RANDOM_ACCESS;
		}
		WinNT.HANDLE h=kernel32.CreateFile(fileName, dwDesiredAccess, dwShareMode,null,
			dwCreationDisposition, dwFlagsAndAttributes,null);
		//check handle
		if (h==null || h.equals(WinBase.INVALID_HANDLE_VALUE)) {
			int error=kernel32.GetLastError();
			if (error==WinError.ERROR_FILE_EXISTS) {
				throw new RuntimeException("ERROR_FILE_EXISTS");
			} else {
				throw new RuntimeException("Windows error: "+String.valueOf(error));
			}
		} else {
			return h;
		}
	}

	//-------------------------------------------------------------
	/**
	* This is the normal mode.  Create a file if it doesn't exist, or open it if it does.
	* Don't delete it upon exit.
	*/
	public static WinNT.HANDLE openFile(String fileName) {
		if (fileName==null) {throw new IllegalArgumentException("fileName is null");}
		int dwDesiredAccess = READWRITE;
		int dwShareMode = FILE_SHARE_READ | FILE_SHARE_WRITE;
		int dwCreationDisposition = OPEN_ALWAYS;
		int dwFlagsAndAttributes = FILE_ATTRIBUTE_NORMAL | FILE_FLAG_RANDOM_ACCESS;

		WinNT.HANDLE h=kernel32.CreateFile(fileName, dwDesiredAccess, dwShareMode,null,
			dwCreationDisposition, dwFlagsAndAttributes,null);
		//check handle
		//according to the docs, it should never have an error
		if (h==null || h.equals(WinBase.INVALID_HANDLE_VALUE)) {
			int error=kernel32.GetLastError();
			throw new RuntimeException("Windows error: "+String.valueOf(error));
		} else {
			return h;
		}
	}

	/**
	* Create a temporary file that is deleted upon exit.  The file name is returned through the out parameter.
	*/
	public static WinNT.HANDLE createTempFile(StringByReference outTempName) {
		String tempPath=getTempPath();
		String tempFileName=getTempFileName(tempPath);

		if (tempFileName==null) {throw new IllegalArgumentException("fileName is null");}
		int dwDesiredAccess = READWRITE;
		int dwShareMode = FILE_SHARE_READ | FILE_SHARE_WRITE;
		int dwCreationDisposition = CREATE_NEW;  //should this be CREATE_ALWAYS?
		int dwFlagsAndAttributes = FILE_ATTRIBUTE_TEMPORARY | FILE_FLAG_DELETE_ON_CLOSE | FILE_FLAG_RANDOM_ACCESS;

		WinNT.HANDLE h=kernel32.CreateFile(tempFileName, dwDesiredAccess, dwShareMode,null,
			dwCreationDisposition, dwFlagsAndAttributes,null);
		if (h==null || h.equals(WinBase.INVALID_HANDLE_VALUE)) {
			int error=kernel32.GetLastError();
			throw new RuntimeException("Windows error: "+String.valueOf(error));
		} else {
			outTempName.setValue(tempFileName);
			return h;
		}
	}

	/**
	* Open a file for read-only access.  It must already exist
	*/
	public static WinNT.HANDLE openReadOnly(String fileName) {
		if (fileName==null) {throw new IllegalArgumentException("fileName is null");}
		int dwDesiredAccess = READ;
		int dwShareMode = FILE_SHARE_READ | FILE_SHARE_WRITE;
		int dwCreationDisposition = OPEN_EXISTING;
		int dwFlagsAndAttributes = FILE_ATTRIBUTE_NORMAL | FILE_FLAG_RANDOM_ACCESS;

		WinNT.HANDLE h=kernel32.CreateFile(fileName, dwDesiredAccess, dwShareMode,null,
			dwCreationDisposition, dwFlagsAndAttributes,null);
		//check handle
		if (h==null || h.equals(WinBase.INVALID_HANDLE_VALUE)) {
			int error=kernel32.GetLastError();
			if (error==WinError.ERROR_FILE_NOT_FOUND) {
				throw new RuntimeException("ERROR_FILE_NOT_FOUND");
			} else {
				throw new RuntimeException("Windows error: "+String.valueOf(error));
			}
		} else {
			return h;
		}
	}

	//----------------------------------------------------

	public static boolean fileExists(String fileName) {
		//call get file attributes
		int i=kernel32.GetFileAttributes(fileName);
		if (i==WinBase.INVALID_FILE_ATTRIBUTES) {
			return false;
		} else {
			//this have the file system attributes
			return true;
		}
	}

	public static boolean isDirectory(String fileName) {
		int attr=kernel32.GetFileAttributes(fileName);
		return ( (attr!=WinBase.INVALID_FILE_ATTRIBUTES) && (attr&WinNT.FILE_ATTRIBUTE_DIRECTORY)>0);
	}

	public static String getFullPathName(String fileName) {
		int nBufferLength=255;
		char[] lpBuffer=new char[nBufferLength];
		int rv=kernel32.GetFullPathName(fileName,nBufferLength,lpBuffer,null);
		//return value is length in bytes of the string copied to lpBuffer
		if (rv>0) {
			return String.valueOf(lpBuffer).trim();
		} else {
			//this should never fail
			int error=kernel32.GetLastError();
			throw new RuntimeException("Windows error: "+String.valueOf(error));
		}
	}

	//Delete
	public static boolean deleteFile(String fileName) {
		return kernel32.DeleteFile(fileName);
	}

	//Access
	public static int getFileAttributes(String fileName) {
		return kernel32.GetFileAttributes(fileName);
	}

	public static int getLastError() {
		return kernel32.GetLastError();
	}

	//GetCurrentDirectory
	public static String getCurrentDirectory() {
		int nBufferLength=255;
		char[] lpBuffer=new char[nBufferLength];
		int rv=kernel32.GetCurrentDirectory(nBufferLength,lpBuffer);
		if (rv>0) {
			return String.valueOf(lpBuffer).trim();
		} else {
			//this should never fail
			int error=kernel32.GetLastError();
			throw new RuntimeException("Windows error: "+String.valueOf(error));
		}
	}


	//* osGetCurrentProcessId
	//* use Kernel32.GetCurrentProcessId
	public static int getCurrentProcessId() {
		return kernel32.GetCurrentProcessId();
	}

	//getTempPath
	public static String getTempPath() {
		int nBufferLength=255;
		char[] lpBuffer=new char[nBufferLength];
		int rv=kernel32.GetTempPath(nBufferLength,lpBuffer);
		if (rv>0) {
			return String.valueOf(lpBuffer).trim();
		} else {
			//this should never fail
			int error=kernel32.GetLastError();
			throw new RuntimeException("Windows error: "+String.valueOf(error));
		}
	}

	//pass in either tempPath or current directory
	public static String getTempFileName(String pathName) {
		int id=++tempCount;
		StringByReference outFileName=new StringByReference();
		int rv=kernel32.GetTempFileName(pathName,"tmp",id,outFileName);
		if (rv==0) {
			//this should never fail
			int error=kernel32.GetLastError();
			throw new RuntimeException("Windows error: "+String.valueOf(error));
		} else {
			return outFileName.getValue();
		}
	}

	//-----------------------------------
	//flags for CreateFile
	//desired access for create file
	public static final int READ=0x80000000;
	public static final int WRITE=0x40000000;
	public static final int READWRITE=(READ | WRITE);

	//shared mode for create file
	public static final int FILE_SHARE_NONE=0x00000000;
	//other processes can request delete access.
	public static final int FILE_SHARE_DELETE=0x00000004;
	public static final int FILE_SHARE_READ=0x00000001;
	public static final int FILE_SHARE_WRITE=0x00000002;
	public static final int FILE_SHARE_READWRITE=(FILE_SHARE_READ | FILE_SHARE_WRITE);

	//dwCreationDisposition
	//overwrites existing file if necessary. always succeeds
	public static final int CREATE_ALWAYS=2;
	//create new file. err if the file already exists
	public static final int CREATE_NEW=1;
	//if file exists, open it. if not, create it. always succeeds
	public static final int OPEN_ALWAYS=4;
	//open file only if it exists. if not error
	public static final int OPEN_EXISTING=3;
	//open file and truncate it to zero only if it exists. if it doesn't exist, err
	public static final int TRUNCATE_EXISTING=5;

	//dwFlagsAndAttributes. There are more than these, but these are the most common
	//NORMAL is only valid if used alone
	//attributes
	public static final int FILE_ATTRIBUTE_HIDDEN = 2;
	public static final int FILE_ATTRIBUTE_NORMAL= 128;
	public static final int FILE_ATTRIBUTE_TEMPORARY= 256;
	public static final int FILE_ATTRIBUTE_READONLY= 1;

	//file flags
	public static final int FILE_FLAG_DELETE_ON_CLOSE=0x04000000;
	public static final int FILE_FLAG_OVERLAPPED=0x40000000;
	public static final int FILE_FLAG_RANDOM_ACCESS=0x10000000;

	//====================
	//library code
	public static Kernel32Ex kernel32;
	static {
		kernel32=(Kernel32Ex) Native.loadLibrary("kernel32", Kernel32Ex.class,W32APIOptions.DEFAULT_OPTIONS);
	}

	//define a new library that deals only with FileSystem methods
   	public interface Kernel32Ex extends Kernel32 {
		/**
		* BOOL WINAPI AreFileApisANSI(void);
		*
		* If the set of file I/O functions is using the ANSI code page, the return value is nonzero.
		* If the set of file I/O functions is using the OEM code page, the return value is zero.
		* Library: Kernel32.lib
		*
		* Comment: I don't know why this is needed, because it is used for 8-bit input and output operations.
		* It is normally false.
		*/
		public boolean AreFileApisANSI();

		/**
		* SetFileApisToANSI function
		* Causes the file I/O functions to use the ANSI character set code page for the current process.
		* This function is useful for 8-bit console input and output operations.
		*
		* void WINAPI SetFileApisToANSI(void);
		*/
		public void SetFileApisToANSI();

		/**
		* SetFileApisToOEM function
		* Causes the file I/O functions for the process to use the OEM character set code page. This
		* function is useful for 8-bit console input and output operations.
		*
		* void WINAPI SetFileApisToOEM(void);
		*/
		public void SetFileApisToOEM();

		//osGetFullPathName
		/**
		* DWORD WINAPI GetFullPathName(
		*   _In_  LPCTSTR lpFileName,
		*   _In_  DWORD   nBufferLength,
		*  _Out_ LPTSTR  lpBuffer,
		*   _Out_ LPTSTR  *lpFilePart
		* );
		* nBufferLength [in]
		* 	The size of the buffer to receive the null-terminated string for the drive and path, in TCHARs.
		* lpBuffer [out]
		*   A pointer to a buffer that receives the null-terminated string for the drive and path.
		* lpFilePart [out]
		* A pointer to a buffer that receives the address (within lpBuffer) of the final file name component in the path.
		* This parameter can be NULL.
		* If lpBuffer refers to a directory and not a file, lpFilePart receives zero.
		* Return value:
		*
		* If the function succeeds, the return value is the length, in TCHARs, of the string copied to lpBuffer,
		* not including the terminating null character.
		* If the lpBuffer buffer is too small to contain the path, the return value is the size, in TCHARs,
		* of the buffer that is required to hold the path and the terminating null character.
		* If the function fails for any other reason, the return value is zero. To get extended error information, call GetLastError.
		*/
		public int GetFullPathName(String lpFileName,int nBufferLength,char[] lpBuffer, IntByReference lpFilePart);

		//Creates a name for a temporary file. If a unique file name is generated, an empty file is created and
		//the handle to it is released; otherwise, only a file name is generated.
		/**
		* UINT WINAPI GetTempFileName(
 		* 	_In_  LPCTSTR lpPathName,
  		*   _In_  LPCTSTR lpPrefixString,
  		*   _In_  UINT    uUnique,
  		* _Out_ LPTSTR  lpTempFileName
		* );
		*/
		public int GetTempFileName(String path,String prefix,int unique,StringByReference tempFileName);

		//this is in Kernel32, but I am changing the method sig
		public int GetTempPath(int nBufferLength,char[] buffer);

		//this is in Kernel32, but I am changing the method sig
		public int GetCurrentDirectory(int nBufferLength,char[] buffer);

		//commented out because not needed
		//BOOL WINAPI GetFileAttributesEx(
  		//_In_  LPCTSTR                lpFileName,
  		//_In_  GET_FILEEX_INFO_LEVELS fInfoLevelId,
  		//_Out_ LPVOID                 lpFileInformation
		//);
		//public boolean GetFileAttributesEx(String lpFileName,int fInfoLevelId,PointerByReference lpFileInformation);
	}
	//end Kernel32Ex

	//typedef struct _WIN32_FILE_ATTRIBUTE_DATA {
  	//DWORD    dwFileAttributes;
  	//FILETIME ftCreationTime;
  	//FILETIME ftLastAccessTime;
  	//FILETIME ftLastWriteTime;
  	//DWORD    nFileSizeHigh;
  	//DWORD    nFileSizeLow;
	//} WIN32_FILE_ATTRIBUTE_DATA, *LPWIN32_FILE_ATTRIBUTE_DATA;
	//this is the structure filled in by GetFileAttributesEx
	//public class WIN32_FILE_ATTRIBUTE_DATA extends Structure {
  	//	int dwFileAttributes;
  	//	WinBase.FILETIME ftCreationTime;
  	//	WinBase.FILETIME ftLastAccessTime;
  	//	WinBase.FILETIME ftLastWriteTime;
  	//	int nFileSizeHigh;
  	//	int nFileSizeLow;
	//
	//	protected List getFieldOrder() {
	//	     return Arrays.asList(new String[] {"dwFileAttributes","ftCreationTime","ftLastWriteTime","nFileSizeHigh",
	//	     	"nFileSizeLow"});
	//	}
	//}
}