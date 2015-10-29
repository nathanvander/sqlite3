package sqlite3.os_win;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.WString;


/**
* This contains a list of all Kernel32 functions that are used by Sqlite.
*/

public interface Kernel32Ex extends Kernel32 {
	//------------------------------------
	/**
	* osCharLowerW
	//in User32.lib
	/public String CharLowerW(String lpsz);
	//use String.toLower();
	//
	//public String CharUpperW(String lpsz);
	//use String.toUpper
	//-----------------------------------------

	* aSysCall[0]
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

	//-------------------------------
	/**
	* osCloseHandle
	* aSyscall[3]
	* BOOL WINAPI CloseHandle(_In_ HANDLE hObject);
	*
	* Use Kernel32.CloseHandle
	*/

	//---------------------------------
	/**
	* osCreateFile
	* The osCreateFileA is listed separately from osCreateFileB.  I am combining them.
	* aSyscall[4] & aSyscall[5]
	*
	* Use Kernel32.CreateFile
	*/

	//--------------------------------------
	/**
	* osCreateFileMapping
	* aSyscall[6]
	*
	* Use Kernel32.CreateFileMapping
	*/

	//--------------------------------------
	/**
	* osCreateMutex
	* HANDLE WINAPI CreateMutex(
 	* _In_opt_ LPSECURITY_ATTRIBUTES lpMutexAttributes,
 	* _In_     BOOL                  bInitialOwner,
 	* _In_opt_ LPCTSTR               lpName
	*	);
	*/
	public WinNT.HANDLE CreateMutex(
		WinBase.SECURITY_ATTRIBUTES lpSecurityAttributes,
		boolean bInitialOwner,
		String lpName
	);

	//--------------------------------------------------
	/**
	* osDeleteFile
	*
	* Use Kernel32.DeleteFile
	*/

	//----------------------------------------------------
	/**
	* osFileTimeToLocalFileTime
	* Use Kernel32.FileTimeToLocalFileTime
	*/
	//-----------------------------------------------
	/**
	* osFileTimeToSystemTime
	*
	* BOOL WINAPI FileTimeToSystemTime(
	*  _In_  const FILETIME     *lpFileTime,
	*  _Out_       LPSYSTEMTIME lpSystemTime
	* );
	*/
	public boolean FileTimeToSystemTime(WinBase.FILETIME lpFileTime,WinBase.FILETIME lpSystemTime);


	//----------------------------------------------
	/**
	* osFlushFileBuffers.
	* Use Kernel32.FlushFileBuffers
	*/
	//------------------------------------------------
	/**
	* osFormatMessage.
	* Use Kernel32.FormatMessage
	*/
	//------------------------------------------------
	/**
	* osFreeLibrary
	* BOOL WINAPI FreeLibrary(
	*  _In_ HMODULE hModule
	* );
	*/
	public boolean FreeLibrary(WinDef.HMODULE hModule);

	//-------------------------------------------
	/**
	* osGetCurrentProcessId
	* use Kernel32.GetCurrentProcessId
	*/
	//-------------------------------------------
	/**
	* osGetDiskFreeSpace
	* use Kernel32.GetDiskFreeSpace
	*/
	//-------------------------------------------
	//osGetFileAttributes
	//use Kernel32.GetFileAttributes
	//-------------------------------------------
	/**
	* osGetFileAttributesEx
	* BOOL WINAPI GetFileAttributesEx(
  	* _In_  LPCTSTR                lpFileName,
  	* _In_  GET_FILEEX_INFO_LEVELS fInfoLevelId,
  	* _Out_ LPVOID                 lpFileInformation
	* );
	*/
	//these are the possible arguments to fInfoLevelId
	public final static int GetFileExInfoStandard=0;
	public final static int GetFileExMaxInfoLevel=1;
	//uses Win32FileAttributeData
	public boolean GetFileAttributesEx(String lpFileName,int fInfoLevelId,WinDef.LPVOID lpFileInformation);

	//-------------------------------------------
	//osGetFileSize
	//lowerBits = osGetFileSize(pFile->h, &upperBits);
	//DWORD WINAPI GetFileSize(
	//  _In_      HANDLE  hFile,
	//  _Out_opt_ LPDWORD lpFileSizeHigh
	//);
	public int GetFileSize(WinNT.HANDLE hFile,IntByReference lpFileSizeHigh);

	//------------------------------------------------
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
	public int GetFullPathName(String lpFileName,int nBufferLength,char[] lpBuffer, char[] lpFilePart);

	//-------------------------------------------------
	//osGetLastError
	//use Kernel32.GetLastError
	//------------------------------------------------
	//osGetProcAddress
	//FARPROC WINAPI GetProcAddress(
 	// _In_ HMODULE hModule,
  	//_In_ LPCSTR  lpProcName
	//);
	//
	//The FARPROC type is declared as follows:
	//int (FAR WINAPI * FARPROC) ()
	//In C, the FARPROC declaration indicates a callback function that has an unspecified parameter list.
	public Pointer GetProcAddress(WinDef.HMODULE hModule, String lpProcName);

	//------------------------------------------------
	//osGetSystemInfo
	//use Kernel32.GetSystemInfo
	//---------------------------------------------
	//osGetSystemTime
	//use Kernel32.GetSystemTime
	//--------------------------------------------
	//osGetSystemTimeAsFileTime
	//void WINAPI GetSystemTimeAsFileTime(
  	//_Out_ LPFILETIME lpSystemTimeAsFileTime
	//);
	public void GetSystemTimeAsFileTime(WinBase.FILETIME lpSystemTimeAsFileTime);

	//--------------------------------
	//osGetTempPath
	//use Kernel32.GetTempPath
	//-------------------------------
	//osGetTickCount
	//use Kernel32.GetTickCount
	//-------------------------------
	//osGetVersionEx
	//use Kernel32.GetVersionEx
	//----------------------------
	//osHeapAlloc
	//LPVOID WINAPI HeapAlloc(
	//  _In_ HANDLE hHeap,
	//  _In_ DWORD  dwFlags,
	//  _In_ SIZE_T dwBytes
	//);
	public WinDef.LPVOID HeapAlloc(
		WinNT.HANDLE hHeap,
		int dwFlags,
		BaseTSD.SIZE_T dwBytes
	);

	//----------------------------------
	//osHeapCreate
	//Creates a private heap object that can be used by the calling process. The function reserves space in the
	//virtual address space of the process and allocates physical storage for a specified initial portion of this block.
	//
	//HANDLE WINAPI HeapCreate(
	//  _In_ DWORD  flOptions,
	//  _In_ SIZE_T dwInitialSize,
	//  _In_ SIZE_T dwMaximumSize
	//);
	public WinNT.HANDLE HeapCreate(int flOptions,BaseTSD.SIZE_T dwInitialSize,BaseTSD.SIZE_T dwMaximumSize);

	//--------------------------------------
	//osHeapDestroy
	//BOOL WINAPI HeapDestroy(_In_ HANDLE hHeap);
	public boolean HeapDestroy(WinNT.HANDLE hHeap);

	//--------------------------------------
	//osHeapFree
	//BOOL WINAPI HeapFree(
	//  _In_ HANDLE hHeap,
	//  _In_ DWORD  dwFlags,
	//  _In_ LPVOID lpMem
	//);
	public boolean HeapFree(WinNT.HANDLE hHeap,int dwFlags,	WinDef.LPVOID lpMem);
	//--------------------------------------
	//osHeapReAlloc
	//Reallocates a block of memory from a heap. This function enables you to resize a memory block and
	//change other memory block properties. The allocated memory is not movable.
	//LPVOID WINAPI HeapReAlloc(
	//  _In_ HANDLE hHeap,
	//  _In_ DWORD  dwFlags,
	//  _In_ LPVOID lpMem,
	//  _In_ SIZE_T dwBytes
	//);
	public WinDef.LPVOID HeapReAlloc(WinNT.HANDLE hHeap,int dwFlags,WinDef.LPVOID lpMem,BaseTSD.SIZE_T dwBytes);

	//--------------------------------
	//osHeapSize
	//SIZE_T WINAPI HeapSize(
	//  _In_ HANDLE  hHeap,
	//  _In_ DWORD   dwFlags,
	//  _In_ LPCVOID lpMem
	//);
	//Note: BaseTSD.SIZE_T is the type of SIZE_T, but it is basically a long
	public long HeapSize(WinNT.HANDLE hHeap,int dwFlags, Pointer lpMem);

	//------------------------------
	//osHeapValidate
	//BOOL WINAPI HeapValidate(
	//  _In_     HANDLE  hHeap,
	//  _In_     DWORD   dwFlags,
	//  _In_opt_ LPCVOID lpMem
	//);
	public boolean HeapValidate(WinNT.HANDLE hHeap,int dwFlags,Pointer lpMem);

	//------------------------------
	//osHeapCompact
	//SIZE_T WINAPI HeapCompact(
	//  _In_ HANDLE hHeap,
	//  _In_ DWORD  dwFlags
	//);
	public long HeapCompact(WinNT.HANDLE hHeap,int dwFlags);

	//-----------------------------
	//osLoadLibrary
	//HMODULE WINAPI LoadLibrary(
	//  _In_ LPCTSTR lpFileName
	//);
	public WinDef.HMODULE LoadLibrary(String lpFileName);

	//-----------------------------------
	//osLocalFree
	//use Kernel32.LocalFree
	//------------------------------------------
	//osLockFile
    // BOOL WINAPI LockFile(
	//   _In_ HANDLE hFile,
	//   _In_ DWORD  dwFileOffsetLow,
	//   _In_ DWORD  dwFileOffsetHigh,
	//   _In_ DWORD  nNumberOfBytesToLockLow,
	//   _In_ DWORD  nNumberOfBytesToLockHigh
	// );
	public boolean LockFile(
		WinNT.HANDLE hFile,
		int dwFileOffsetLow,
		int dwFileOffsetHigh,
		int  nNumberOfBytesToLockLow,
		int nNumberOfBytesToLockHigh
	);

	//------------------------------------------
	//osLockFileEx
	//BOOL WINAPI UnlockFileEx(
	//  _In_       HANDLE       hFile,
	//  _Reserved_ DWORD        dwReserved,
	//  _In_       DWORD        nNumberOfBytesToUnlockLow,
	//  _In_       DWORD        nNumberOfBytesToUnlockHigh,
	//  _Inout_    LPOVERLAPPED lpOverlapped
	//);
	public boolean LockFileEx(
		WinNT.HANDLE hFile,
		int  dwReserved, //dwReserved - Reserved parameter; must be set to zero.
		int  nNumberOfBytesToLockLow,
		int  nNumberOfBytesToLockHigh,
		WinBase.OVERLAPPED lpOverlapped
	);

	//--------------------------------
	//osMapViewOfFile
	//use Kernel32.MapViewOfFile
	//---------------------------------
	//osMultiByteToWideChar
	//int MultiByteToWideChar(
	//  _In_      UINT   CodePage,
	//  _In_      DWORD  dwFlags,
	//  _In_      LPCSTR lpMultiByteStr,
	//  _In_      int    cbMultiByte,
	//  _Out_opt_ LPWSTR lpWideCharStr,
	//  _In_      int    cchWideChar
	//);
	public int MultiByteToWideChar(
		WinDef.UINT CodePage,
		WinDef.DWORD dwFlags,
		String lpMultiByteStr,
		int cbMultiByte,
		WString lpWideCharStr, //wrapper around a string
		int cchWideChar
	);

	//--------------------------------
	//osQueryPerformanceCounter
	//BOOL WINAPI QueryPerformanceCounter(
  	//_Out_ LARGE_INTEGER *lpPerformanceCount
	//);
	public boolean QueryPerformanceCounter(LongByReference lpPerformanceCount);

	//------------------------------
	//osReadFile
	//use Kernel32.ReadFile
	//------------------------------
	/**
	* Sets the physical file size for the specified file to the current position of the file pointer.
	* The physical file size is also referred to as the end of the file. The SetEndOfFile function can be used
	*	to truncate or extend a file. To set the logical end of a file, use the SetFileValidData function.
	*
	* BOOL WINAPI SetEndOfFile(
  	* _In_ HANDLE hFile
	* );
	*/
	public boolean SetEndOfFile(WinNT.HANDLE hFile);

	//---------------------------
	//osSetFilePointer
	//Moves the file pointer of the specified file.
	//DWORD WINAPI SetFilePointer(
 	// _In_        HANDLE hFile,
  	//_In_        LONG   lDistanceToMove,
  	//_Inout_opt_ PLONG  lpDistanceToMoveHigh,
  	//_In_        DWORD  dwMoveMethod
	//);
	public int SetFilePointer(WinNT.HANDLE hFile,long lpDistanceToMove,LongByReference lpDistanceToMoveHigh,int dwMoveMethod);

	//-----------------------
	//Sleep for millis
	//VOID WINAPI Sleep(
  	//_In_ DWORD dwMilliseconds
	//);
	//Note: You should probably use Thread.sleep instead.  This is here for completeness
	public void Sleep(int dwMilliseconds);

	//---------------------------
	//osSystemTimeToFileTime
	//BOOL WINAPI SystemTimeToFileTime(
	//  _In_  const SYSTEMTIME *lpSystemTime,
	//  _Out_       LPFILETIME lpFileTime
	//);
	public boolean SystemTimeToFileTime(WinBase.FILETIME lpFileTime,WinBase.FILETIME lpSystemTime);
	//---------------------------------
   //return osUnlockFile(*phFile, offsetLow, offsetHigh, numBytesLow,numBytesHigh);
   //BOOL WINAPI UnlockFile(
	//_In_ HANDLE hFile,
 	// _In_ DWORD  dwFileOffsetLow,
  	//_In_ DWORD  dwFileOffsetHigh,
  	//_In_ DWORD  nNumberOfBytesToUnlockLow,
  	//_In_ DWORD  nNumberOfBytesToUnlockHigh
	//);
	public boolean UnlockFile(
		WinNT.HANDLE hFile,
		int dwFileOffsetLow,
		int dwFileOffsetHigh,
		int nNumberOfBytesToLockLow,
		int nNumberOfBytesToLockHigh
	);

	//----------------------------------------
    //return osUnlockFileEx(*phFile, 0, numBytesLow, numBytesHigh, &ovlp);
	//BOOL WINAPI UnlockFileEx(
	//  _In_       HANDLE       hFile,
	//  _Reserved_ DWORD        dwReserved,
	//  _In_       DWORD        nNumberOfBytesToUnlockLow,
	//  _In_       DWORD        nNumberOfBytesToUnlockHigh,
	//  _Inout_    LPOVERLAPPED lpOverlapped
	//);
	public boolean UnlockFileEx(
		WinNT.HANDLE hFile,
		int  dwReserved, //dwReserved - Reserved parameter; must be set to zero.
		int  nNumberOfBytesToUnlockLow,
		int  nNumberOfBytesToUnlockHigh,
		WinBase.OVERLAPPED lpOverlapped
	);

	//-------------------------------------
	//osUnmapViewOfFile
	//use Kernel32.UnmapViewOfFile
	//---------------------------------------
	//osWideCharToMultiByte
	//int WideCharToMultiByte(
	//  _In_      UINT    CodePage,
	//  _In_      DWORD   dwFlags,
	//  _In_      LPCWSTR lpWideCharStr,
	//  _In_      int     cchWideChar,
	//  _Out_opt_ LPSTR   lpMultiByteStr,
	//  _In_      int     cbMultiByte,
	//  _In_opt_  LPCSTR  lpDefaultChar,
	//  _Out_opt_ LPBOOL  lpUsedDefaultChar
	//);
	public int WideCharToMultiByte(
		WinDef.UINT CodePage,
		WinDef.DWORD dwFlags,
		String lpWideCharStr,
		WString lpMultiByteStr,
		int cbMultiByte,
		String lpDefaultChar,
		IntByReference lpUsedDefaultChar /* holds a bool */
	);

	//---------------------------------
	//osWriteFile
	//use Kernel32.WriteFile

	//---------------------------------
	//osCreateEventEx
	//HANDLE WINAPI CreateEventEx(
 	// _In_opt_ LPSECURITY_ATTRIBUTES lpEventAttributes,
 	// _In_opt_ LPCTSTR               lpName,
  	//_In_     DWORD                 dwFlags,
  	//_In_     DWORD                 dwDesiredAccess
	//);
	public WinNT.HANDLE CreateEventEx(
		WinBase.SECURITY_ATTRIBUTES lpSecurityAttributes,
		String lpName,
		int dwFlags,
		int dwDesiredAccess
	);

	//-------------------------------------
	//osWaitForSingleObject
	//use Kernel32.WaitForSingleObject
	//----------------------------
	//osWaitForSingleObjectEx
	//DWORD WINAPI WaitForSingleObjectEx(
	//  _In_ HANDLE hHandle,
	//  _In_ DWORD  dwMilliseconds,
	//  _In_ BOOL   bAlertable
	//);
	public int WaitForSingleObjectEx(WinNT.HANDLE hHandle,int dwBilliseconds,boolean bAlertable);

	//-----------------------------
	//osSetFilePointerEx
	//BOOL WINAPI SetFilePointerEx(
	//  _In_      HANDLE         hFile,
	//  _In_      LARGE_INTEGER  liDistanceToMove,
	//  _Out_opt_ PLARGE_INTEGER lpNewFilePointer,
	//  _In_      DWORD          dwMoveMethod
	//);
	public boolean SetFilePointerEx (
		WinNT.HANDLE hFile,
		long liDistanceToMove,
		LongByReference lpNewFilePointer,
		int dwMoveMethod
	);

	//---------------------------------
	//osGetFileInformationByHandleEx
	//BOOL WINAPI GetFileInformationByHandleEx(
	//_In_  HANDLE                    hFile,
	//_In_  FILE_INFO_BY_HANDLE_CLASS FileInformationClass,
	//_Out_ LPVOID                    lpFileInformation,
	//_In_  DWORD                     dwBufferSize
	//);

	public boolean GetFileInformationByHandleEx(
		WinNT.HANDLE hFile,
		int FileInformationClass,
		PointerByReference lpFileInformation,
		int dwBufferSize
	);
	//IMPORTANT: uses FileStandardInfo.java
	/**
	typedef enum _FILE_INFO_BY_HANDLE_CLASS {
	  FileBasicInfo                   = 0,
	  FileStandardInfo                = 1,
	  FileNameInfo                    = 2,
	  FileRenameInfo                  = 3,
	  FileDispositionInfo             = 4,
	  FileAllocationInfo              = 5,
	  FileEndOfFileInfo               = 6,
	  FileStreamInfo                  = 7,
	  FileCompressionInfo             = 8,
	  FileAttributeTagInfo            = 9,
	  FileIdBothDirectoryInfo         = 10, // 0xA
	  FileIdBothDirectoryRestartInfo  = 11, // 0xB
	  FileIoPriorityHintInfo          = 12, // 0xC
	  FileRemoteProtocolInfo          = 13, // 0xD
	  FileFullDirectoryInfo           = 14, // 0xE
	  FileFullDirectoryRestartInfo    = 15, // 0xF
	  FileStorageInfo                 = 16, // 0x10
	  FileAlignmentInfo               = 17, // 0x11
	  FileIdInfo                      = 18, // 0x12
	  FileIdExtdDirectoryInfo         = 19, // 0x13
	  FileIdExtdDirectoryRestartInfo  = 20, // 0x14
	  MaximumFileInfoByHandlesClass
	} FILE_INFO_BY_HANDLE_CLASS, *PFILE_INFO_BY_HANDLE_CLASS;
	*/

	//----------------------------------------------
	//osMapViewOfFileFromApp
	//PVOID WINAPI MapViewOfFileFromApp(
  	//_In_ HANDLE  hFileMappingObject,
  	//_In_ ULONG   DesiredAccess,
  	//_In_ ULONG64 FileOffset,
  	//_In_ SIZE_T  NumberOfBytesToMap
	//);
	public Pointer MapViewOfFileFromApp(
		WinNT.HANDLE hFileMappingObject,
		long DesiredAccess,
		long FileOffset,
		BaseTSD.SIZE_T NumberOfBytesToMap
	);

	//----------------------------------------------------
	//osCreateFile2
	//HANDLE WINAPI CreateFile2(
	//  _In_     LPCWSTR                           lpFileName,
	//  _In_     DWORD                             dwDesiredAccess,
	//  _In_     DWORD                             dwShareMode,
	//  _In_     DWORD                             dwCreationDisposition,
	//  _In_opt_ LPCREATEFILE2_EXTENDED_PARAMETERS pCreateExParams
	//);
	//
	//typedef struct _CREATEFILE2_EXTENDED_PARAMETERS {
	  //DWORD                 dwSize;
	  //DWORD                 dwFileAttributes;
	  //DWORD                 dwFileFlags;
	  //DWORD                 dwSecurityQosFlags;
	  //LPSECURITY_ATTRIBUTES lpSecurityAttributes;
	  //HANDLE                hTemplateFile;
	//} CREATEFILE2_EXTENDED_PARAMETERS, *PCREATEFILE2_EXTENDED_PARAMETERS, *LPCREATEFILE2_EXTENDED_PARAMETERS;

	public WinNT.HANDLE CreateFile2 (
		String lpFileName,
		int dwDesiredAccess,
		int dwSharedMode,
		int dwCreationDisposition,
		Pointer pCreateExParams
	);

	//--------------------------------------------
	//osLoadPackagedLibrary
	//HMODULE WINAPI LoadPackagedLibrary(
	//  _In_       LPCWSTR lpwLibFileName,
	//  _Reserved_ DWORD   Reserved
	//);
	public WinDef.HMODULE LoadPackagedLibrary(
		String lpwLibFileName,
		int Reserved
	);

	//---------------------------------------------
	//osGetTickCount64
	//ULONGLONG WINAPI GetTickCount64(void);
	public long GetTickCount64();

	//--------------------------------------------
	//osGetNativeSystemInfo
	//call Kernel32.GetNativeSystemInfo
	//--------------------------------------------
	//osOutputDebugString
	//void WINAPI OutputDebugString(
	//  _In_opt_ LPCTSTR lpOutputString
	//);
	public void OutputDebugString(String lpOutputString);
	//-----------------------------------------------
	//osGetProcessHeap
	//HANDLE WINAPI GetProcessHeap(void);
	public WinNT.HANDLE GetProcessHeap();

	//--------------------------------------------------
	//HANDLE WINAPI CreateFileMappingFromApp(
	//  _In_     HANDLE               hFile,
	//  _In_opt_ PSECURITY_ATTRIBUTES SecurityAttributes,
	//  _In_     ULONG                PageProtection,
	//  _In_     ULONG64              MaximumSize,
	//  _In_opt_ PCWSTR               Name
	//);
	public WinNT.HANDLE CreateFileMappingFromApp(
		WinNT.HANDLE hFile,
		WinBase.SECURITY_ATTRIBUTES SecurityAttributes,
		long PageProtection,
		long MaximumSize,
		String Name
	);

	//-------------------------------------------------
	//osInterlockedCompareExchange
	//LONG __cdecl InterlockedCompareExchange(
	//  _Inout_ LONG volatile *Destination,
	//  _In_    LONG          Exchange,
	//  _In_    LONG          Comparand
	//);
	/*
	** NOTE: On some sub-platforms, the InterlockedCompareExchange "function"
	**       is really just a macro that uses a compiler intrinsic (e.g. x64).
	**       So do not try to make this is into a redefinable interface.
	*/
	public long InterlockedCompareExchange(
		LongByReference Destination,
		long Exchange,
		long Comparand
	);

	//------------------------------------
	//RPC_STATUS RPC_ENTRY UuidCreate(
	//   UUID __RPC_FAR *Uuid
	//);
	//typedef struct _GUID {
	//  unsigned long  Data1;
	//  unsigned short Data2;
	//  unsigned short Data3;
	//  unsigned char  Data4[8];
	//} GUID, UUID;
	public int UuidCreate(Pointer Uuid);

	//-----------------------------------
	//osUuidCreateSequential
	//RPC_STATUS RPC_ENTRY UuidCreateSequential(
	//   UUID __RPC_FAR *Uuid
	//);
	public int UuidCreateSequential(Pointer Uuid);

	//---------------------------
	//osFlushViewOfFile
	//BOOL WINAPI FlushViewOfFile(
	//  _In_ LPCVOID lpBaseAddress,
	//  _In_ SIZE_T  dwNumberOfBytesToFlush
	//);
	public boolean FlushViewOfFile(
		Pointer lpBaseAddress,
		BaseTSD.SIZE_T dwNumberOfBytesToFlush
	);

}; /* End of the overrideable system calls */