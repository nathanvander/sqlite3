package apollo;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.platform.win32.Kernel32;
import java.util.Hashtable;

/**
* A Mutex controls access to a shared resource across multiple processes.  This requires Windows kernel calls, but the
* Linux code should be very similar.
*
* Every mutex has an owner, which is the thread-id, and can be 0.  So -1 means it is unowned.  These only work within
* the process.  So you won't know the owner of a thread in another process.
*
* Mutexes have to work both across processes and across threads.  Thus, a mutex can exist independently from the process,
* much like a file.  The operating system automatically closes them when the last process referencing them exits.
*
* Having a reference to a mutex does not mean that you own it.  Mutices are initially created as unowned.
*
* Note on abandoned mutices:
* If a thread terminates without releasing its ownership of a mutex object, the mutex object is considered to be abandoned.
* A waiting thread can acquire ownership of an abandoned mutex object, but the wait function will return WAIT_ABANDONED
* to indicate that the mutex object is abandoned. An abandoned mutex object indicates that an error has occurred and that
* any shared resource being protected by the mutex object is in an undefined state. If the thread proceeds as though the
* mutex object had not been abandoned, it is no longer considered abandoned after the thread releases its ownership. This
* restores normal behavior if a handle to the mutex object is subsequently specified in a wait function.
*/
public class Mutex {
	//mutices is plural form of Mutex. Hashtable is threadsafe
	private static Hashtable<String,Mutex> mutices=new Hashtable<String,Mutex>();
	//indicated that the ownership is unknown. it may be unowned or it may be another process
	public final static long UNOWNED=-1;
	//indicates that the ownership was recently freed by a thread in this process
	public final static long FREED=-2;
	//this only returns a reference to the Mutex, it does not give you ownership of it
	public static Mutex getMutex(String n) {
		Mutex mu=mutices.get(n);
		if (mu==null) {
			mu=new Mutex(n);
			mutices.put(n,mu);
		}
		return mu;
	}

	public static int getCurrentProcessId() {
		return kernel32.GetCurrentProcessId();
	}
	//---------------------------------------------
	//instance variables
 	private String name;
 	private WinNT.HANDLE handle;
 	private long owner=UNOWNED;

 	//create mutex
 	public Mutex(String name) {
  		this.name=name;
  		//create a new mutex without taking ownership of it
  		//you can create a mutex that already exists in another process.  This just means it is new to this process
  		//System.out.println("warning expensive system call kernel32.CreateMutex()");
  		handle=kernel32.CreateMutex(null,false,name);
  		if (handle==null || handle.equals(WinBase.INVALID_HANDLE_VALUE)) {
   			//the create function failed
   			//System.out.println("warning: system call kernel32.GetLastError()");
   			int error=kernel32.GetLastError();
   			if (error==ERROR_INVALID_HANDLE) {
    			//there is already a different type of object with that name
    			throw new RuntimeException("ERROR_INVALID_HANDLE.  An object already exists with the name "+name);
   			} else if (error==ERROR_ACCESS_DENIED) {
				// if the caller has limited access rights, the function will fail with ERROR_ACCESS_DENIED
    			throw new RuntimeException("ERROR_ACCESS_DENIED. Try using OpenMutex to access the mutex.");
   			} else {
    			throw new RuntimeException("unknown error "+error+", when creating mutex "+name);
   			}
  		} //else it is valid
  		System.out.println("thread "+Thread.currentThread().getId()+": mutex "+name+" created at "+System.currentTimeMillis());
 	}

	public String getName() {return name;}
 	//return the owner of the mutex.  Returns -1 is unowned or if it is owned by another process.
 	//if this is greater than -1, then you know who the owner is
	public long getOwner() {return owner;}
	//--------------------------------------------------
	/**
	* acquire(). Acquire a lock on the mutex, waiting up to millis.  If millis is
	* 0, then it will return immediately. If it is greater, then it will block until time expires
	* The return code is one of the following specified values:
	*	SUCCESS_ALREADY_OWNED.  This is a success code, indicating that the thread already owns
	*		the mutex.  This prevents re-entrancy.
	*   SUCCESS_TRANSFERRED_OWNERSHIP.  This is a success code, indicating that the thread acquired ownership without a kernel call
	*	OBJECT_WAIT_TIMEOUT.  This is a failure code, indicating that the thread was unable to acquire ownership
	*	WAIT_OBJECT_0.  This is a success code
	* 	WAIT_ABANDONED.	Success. The mutex was abandoned by its owner so we own it now.
	*	WAIT_TIMEOUT. This is a failure code.
	*	any other value is a failure, with the error code returned
	*
	* This seems very complicated, but the usual result is either a WAIT_OBJECT_0 - received it from kernel
	* or a SUCCESS_TRANSFERRED_OWNERSHIP
	* The usual fail is an OBJECT_WAIT_TIMEOUT.  You can increase the wait time to prevent this.
	*/
	public int acquire(int millis) {
		//first check to see if we already own it
		System.out.println("thread "+Thread.currentThread().getId()+": trying to acquire ownership of mutex "+name+" at "+System.currentTimeMillis());
		if (owner==Thread.currentThread().getId()) {
			//there is no need to acquire this because you already own it
			System.out.println("thread "+owner+" already owns the mutex "+name);
			return SUCCESS_ALREADY_OWNED;
		} else if (owner>-1) {
			synchronized(this) {
				//someone else in this process owns it
				//wait on it for the specified number of millis
				System.out.println("another thread "+owner+" owns the mutex. waiting "+millis+" milliseconds to try to acquire it");
				try {
					this.wait(millis);
				} catch (InterruptedException x) {
					//do nothing, it is just an early wakeup call
				}
				//check the status again
				if (owner==FREED) {
					//we have acquired it without a kernel call
					owner=Thread.currentThread().getId();
					//success
					System.out.println("thread "+owner+" has now acquired the mutex "+name);
					return SUCCESS_TRANSFERRED_OWNERSHIP;
				} else {
					//this is a failure.  Try again
					System.out.println("failed to acquire mutex because it is still owned by "+owner);
					return OBJECT_WAIT_TIMEOUT;
				}
			}  //end synchronized
		} else {
			System.out.println("thread "+Thread.currentThread().getId()+": waiting on ownership from kernel of mutex "+name+" at "+System.currentTimeMillis());
			//we don't know who owns it. Maybe another process owns it or it is unowned
			//this requires a kernel call
			//System.out.println("warning: system call kernel32.WaitForSingleObject()");
			int rc=kernel32.WaitForSingleObject(handle, millis);
			if (rc==WAIT_OBJECT_0) {
				//the object is acquired
				owner=Thread.currentThread().getId();
				System.out.println("thread "+owner+": acquired the mutex "+name+" at "+System.currentTimeMillis());
				return rc;
			} else if (rc==WAIT_ABANDONED) {
				//treat is as success
				owner=Thread.currentThread().getId();
				System.out.println("mutex "+name+ "was abandoned by its owner, but thread "+owner+" owns it now");
				return rc;
			} else if (rc==WAIT_TIMEOUT) {
				//this is a failure.  Try again
				System.out.println("failed to acquire mutex "+name+" after waiting. It is owned by an unknown thread in an unknown process");
				return WAIT_TIMEOUT;
			} else if (rc==WAIT_FAILED) {
				//big big problem
				int error=kernel32.GetLastError();
				System.out.println("unknown error "+error+" when trying to acquire mutex "+name);
				return error;
			} else {
				//some other return code.  should never happen
				System.out.println("unknown return code "+rc+" when trying to acquire mutex "+name);
				return rc;
			}
		}
	}

	//other acquire methods
	//try to acquire the mutex or immediately fail.  true if success
	public boolean tryAcquire() {
		int rc=acquire(0);
		if (rc==SUCCESS_ALREADY_OWNED || rc==SUCCESS_TRANSFERRED_OWNERSHIP
			|| rc==WAIT_OBJECT_0 || rc==WAIT_ABANDONED)
		{
			return true;
		} else {
			return false;
		}
	}

	//acquire the mutex or wait.  true if success
	public boolean acquireOrBlock(int millis) {
		int rc=acquire(millis);
		if (rc==SUCCESS_ALREADY_OWNED || rc==SUCCESS_TRANSFERRED_OWNERSHIP
			|| rc==WAIT_OBJECT_0 || rc==WAIT_ABANDONED)
		{
			return true;
		} else {
			return false;
		}
	}

	//release the mutex
	public void release() {
		if (Thread.currentThread().getId()!=owner) {
			System.out.println("warning: "+Thread.currentThread().getId()+" is not the owner but is trying to release the mutex");
			//do nothing
			return;
		}
		System.out.println("thread "+owner+" is releasing mutex "+name);
		owner=FREED;
		//first see if any local threads are waiting on it
		synchronized(this) {
			System.out.println("seeing if any local threads want mutex "+name);
			this.notifyAll();
		}
		//wait a little
		try {Thread.sleep(10);} catch (Exception x) {}
		//if no one locally grabbed it, tell the kernel
		if (owner==FREED) {
			owner=UNOWNED;
			System.out.println("no local threads want mutex "+name+" so releasing it to kernel at "+System.currentTimeMillis());
			//System.out.println("warning: system call kernel32.ReleaseMutex()");
			boolean b=kernel32.ReleaseMutex(handle);
  			if (!b) {
   				int error=kernel32.GetLastError();
   				//don't throw an exception, just give a warning
   				System.out.println("warning: error "+error+" on ReleaseMutex()");
			}
		}
	}

 	public void close() {
  		boolean b=kernel32.CloseHandle(handle);
 	}
	//================================================
 	//error codes
 	public final static int ERROR_FILE_NOT_FOUND=2;
 	public final static int ERROR_ACCESS_DENIED=5;
 	public final static int ERROR_INVALID_HANDLE=6;
 	public final static int ERROR_ALREADY_EXISTS=183;

 	//status of wait
 	public static final int	WAIT_OBJECT_0=0;		//same as 0x00000000L
	public static final int	WAIT_ABANDONED=128; 	//same as 0x00000080L
	public static final int	WAIT_TIMEOUT=258;		//same as 0x00000102L
	public static final int	WAIT_FAILED=-1;			//same as 0xFFFFFFFF

 	//custom return codes
 	//the thread already owns the mutex
 	public final static int SUCCESS_ALREADY_OWNED=16000;
 	//ownership was transferred without going through the o/s calls
 	public final static int SUCCESS_TRANSFERRED_OWNERSHIP=16001;
 	public final static int OBJECT_WAIT_TIMEOUT=16128;

 	//synchronization access rights
 	public final static int SYNCHRONIZE=0x00100000;

 	//library code
 	public static Kernel32Ex kernel32;
 	static {
		kernel32=(Kernel32Ex)Native.loadLibrary("kernel32", Kernel32Ex.class,W32APIOptions.DEFAULT_OPTIONS);
 	}

 	//define a new library that deals only with Mutex methods
    public interface Kernel32Ex extends Kernel32 {

 	//HANDLE WINAPI CreateMutex(
   	//  _In_opt_ LPSECURITY_ATTRIBUTES lpMutexAttributes,
 	//  _In_     BOOL                  bInitialOwner,
 	//  _In_opt_ LPCTSTR               lpName
 	//);
 	//lpMutexAttributes [in, optional]
 	//A pointer to a SECURITY_ATTRIBUTES structure. If this parameter is NULL, the handle cannot be inherited by child processes.
 	//
 	//If lpName matches the name of an existing event, semaphore, waitable timer, job, or file-mapping object,
 	//the function fails and the GetLastError function returns ERROR_INVALID_HANDLE.
 	//This occurs because these objects share the same namespace.
 	//
 	//If the mutex is a named mutex and the object existed before this function call, the return value is a handle
 	//to the existing object, GetLastError returns ERROR_ALREADY_EXISTS, bInitialOwner is ignored, and the calling
 	//thread is not granted ownership.
 	//
 	//However, if the caller has limited access rights, the function will fail with ERROR_ACCESS_DENIED and the
 	//caller should use the OpenMutex function.
  		public WinNT.HANDLE CreateMutex(Object lpMutexAttributes,boolean bInitialOwner,String name);

	//DWORD WINAPI WaitForSingleObject(
	//  _In_ HANDLE hHandle,
	//  _In_ DWORD  dwMilliseconds
	//);
	//in kernel32
	//	public int WaitForSingleObject(WinNT.HANDLE handle, int millis);

 	//BOOL WINAPI ReleaseMutex(
 	//  _In_ HANDLE hMutex
 	//);
 		public boolean ReleaseMutex(WinNT.HANDLE handle);

 	//BOOL WINAPI CloseHandle(
 	//  _In_ HANDLE hObject
 	//);
 	//in Kernel32
  	//public boolean CloseHandle(WinNT.HANDLE handle);

 	//DWORD WINAPI GetLastError(void);
 	//in kernel32
  	//	public int GetLastError();
 	}
}