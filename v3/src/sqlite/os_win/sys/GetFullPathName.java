package sqlite.os_win.sys;
//import java.util.concurrent.Callable;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.Kernel32;
import sqlite.Function;
import sqlite.BaseFunction;
import java.lang.reflect.Field;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.ptr.IntByReference;

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
*
* Signatuure: public int GetFullPathName(String lpFileName,int nBufferLength,char[] lpBuffer, char[] lpFilePart);
*
* Update: The lpFilePart is the address of part of the buffer that contains the filename portion. It does not work
* properly in Java.  So just ignore it.  If you need the filename part, you can just split it on the backslash.
*
* Bufferlength is set to 255 by default. That should be long enough for almost any purpose.
*/

public class GetFullPathName extends BaseFunction {
	public static API kernel32;
	static {
		kernel32=(API) Native.loadLibrary("kernel32", API.class,W32APIOptions.DEFAULT_OPTIONS);
	}

	//change the name so it doesn't conflict
	public interface API extends Library {
		public int GetFullPathName(String lpFileName,int nBufferLength,char[] lpBuffer, IntByReference lpFilePart);
		public int GetLastError();
	}

	//---------------------
	//fieldnames
	public String lpFileName;
	public int nBufferLength=255;	//this should be long enough
	public char[] lpBuffer;
	//public IntByReference pFilePart;

	//return value, see above description
	public int returnValue;	//not the same as resultCode
	//public String filePart;	//get from pFilePart
	//---------------------

	//this is a list of only the input parameters
	public String[] getInParams() {return new String[] {"lpFileName","nBufferLength","lpBuffer"};
	}

	//this is a list of only the output parameters
	public String[] getOutParams() {return new String[] {"returnValue"};}

	//just use reflection
	public Class getType(String paramName) {
		return super.getType(this.getClass(),paramName);
	}

	//throw exception if it is the wrong type
	public void set(String paramName,Object o) {
		super.set(this.getClass(),paramName,o);
	}

	public Object get(String paramName) {
		return super.get(this.getClass(),paramName);
	}

	//--------------------------

	public void setFileName(String fileName) {
		lpFileName=fileName;
	}

	//the default is 255.  This will change it if you want something different
	public void setBufferLength(int len) {
		nBufferLength=len;
		lpBuffer=new char[len];
	}

	public String getPath() {return String.valueOf(lpBuffer).trim();}


   //================
   //actual method call
   public Integer call() {
		start=System.currentTimeMillis();
		if (nBufferLength==0) {nBufferLength=255;}
		if (lpBuffer==null) {lpBuffer=new char[nBufferLength];}

		returnValue=kernel32.GetFullPathName(lpFileName,nBufferLength,lpBuffer,null);

		//don't confuse returnValue and resultCode
		//returnValue is what we got back from the function
		//resultCode is what we return
		if (returnValue==0) {
			//failure
			resultCode=kernel32.GetLastError();
		} else {
			//success.  returnValue has the length of buffer used in passing the path
			resultCode=0;
		}
		finish=System.currentTimeMillis();
		return Integer.valueOf(resultCode);
   }

   //======================
	public static void main(String[] args) {
		String fileName=args[0];
	   	GetFullPathName g=new GetFullPathName();
	   	g.setFileName(fileName);
		int result=g.call();
	   	if (result==0) {
			//success
		   	System.out.println("elapsed: "+g.getElapsedTime());
		   	System.out.println("path: "+g.getPath());
	   } else {
		   System.out.println("error: "+result);
	   }
   }
}

