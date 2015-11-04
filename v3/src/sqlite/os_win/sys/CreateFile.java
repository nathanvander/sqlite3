package sqlite.os_win.sys;
//import java.util.concurrent.Callable;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinBase;
import sqlite.Function;
import sqlite.BaseFunction;
import java.lang.reflect.Field;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;

/**
* This is a wrapper around the CreateFile function in Windows.  It is separated out for easier testing.
*/
public class CreateFile extends BaseFunction {
	public static Kernel32 kernel32;
	static {
		kernel32=(Kernel32) Native.loadLibrary("kernel32", Kernel32.class,W32APIOptions.DEFAULT_OPTIONS);
	}

	//library definition
	public interface Kernel32 extends Library {
		public WinNT.HANDLE CreateFile(
			String lpFileName,
	   		int dwDesiredAccess,
	     	int dwShareMode,
	   		WinBase.SECURITY_ATTRIBUTES lpSecurityAttributes,
	        int dwCreationDisposition,
	        int dwFlagsAndAttributes,
            WinNT.HANDLE hTemplateFile
		);

		public int GetLastError();
   }

	//---------------------
	//the "real" field names
	public String lpFileName;	//name of file
	public int dwDesiredAccess;
	public int dwShareMode;
	public WinBase.SECURITY_ATTRIBUTES lpSecurityAttributes;	//usually null
	public int dwCreationDisposition;
	public int dwFlagsAndAttributes;
    public WinNT.HANDLE hTemplateFile;	//usually null
    //the return value
    public WinNT.HANDLE handle;
    //--------------------------

	public String[] getParameterNames() {
		return new String[] {"lpFileName","dwDesiredAccess","dwShareMode","lpSecurityAttributes",
			"dwCreationDisposition","dwFlagsAndAttributes","hTemplateFile","handle"};
	}

	//this is a list of only the input parameters
	public String[] getInParams() {return new String[] {"lpFileName","dwDesiredAccess","dwShareMode","lpSecurityAttributes",
			"dwCreationDisposition","dwFlagsAndAttributes","hTemplateFile"};
	}

	//this is a list of only the output parameters
	public String[] getOutParams() {return new String[] {"handle"};}

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

	public WinNT.HANDLE getHandle() {return handle;}

   //================
   //actual method call
   public Integer call() throws Exception {
		start=System.currentTimeMillis();
		handle=kernel32.CreateFile(lpFileName,dwDesiredAccess,dwShareMode,null,
			dwCreationDisposition,dwFlagsAndAttributes,null);
		if (handle.equals(WinBase.INVALID_HANDLE_VALUE)) {
			resultCode=kernel32.GetLastError();
		} else {
			resultCode=0;
		}
		finish=System.currentTimeMillis();
		return Integer.valueOf(resultCode);
   }

   //results
	//===========================
	//test code
   public static void main(String[] args) {
		String fileName="temp.txt";
		int access=WinNT.GENERIC_READ | WinNT.GENERIC_WRITE;
		int shared=WinNT.FILE_SHARE_READ;
		int cd=WinNT.OPEN_ALWAYS;
		int attrib=WinNT.FILE_ATTRIBUTE_NORMAL;

		CreateFile cf=new CreateFile();
		cf.set("lpFileName",fileName);
		cf.set("dwDesiredAccess",access);
		cf.set("dwShareMode",shared);
		cf.set("dwCreationDisposition",cd);
		cf.set("dwFlagsAndAttributes",attrib);

		Integer i=0;
		try {
			i=cf.call();
		} catch (Exception x) {
			x.printStackTrace();
		}
		if (i.intValue()==0) {
			WinNT.HANDLE h=cf.getHandle();
			System.out.println("elapsed: "+cf.getElapsedTime());
			System.out.println(h.toString());
		} else {
			System.out.println("error: "+i.intValue());
		}
   }

}