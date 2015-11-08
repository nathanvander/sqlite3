package apollo.win.sys;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Memory;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.platform.win32.WinError;
import java.util.List;
import java.util.Arrays;

/**
* A CriticalSection is basically the same as a Mutex, except a Mutex synchronizes between multiple processes
* and a CriticalSection synchronizes threads on the same process.  CriticalSections are slightly faster.
*
* To acquire ownership of a Mutex, you call WaitForSingleObject() and to release it, you call ReleaseMutex().
* To acquire ownership of a CriticalSection, you call EnterCriticalSection() and to release it, you call
* LeaveCriticalSection.  The sqlite code actually uses CriticalSections instead of Mutexes (except for WinCE code)
* so we should do the same.
*
* struct CRITICAL_SECTION {
*   long LockCount;
*   long RecursionCount;
*   void * OwningThread;
*   void * LockSemaphore;
*   #if defined(_WIN64)
*     unsigned __int64 SpinCount;
*   #else
*   unsigned long SpinCount;
*   #endif
* };
*
* Note that the name is for information only and does not guarantee uniqueness.
*/
public class CriticalSection {
	//none of these fields are used. But this has to be declared as a Critical Section
	public class CRITICAL_SECTION_STRUCT extends Structure {
		public long LockCount;
		public long RecursionCount;
		public Pointer OwningThread;
		public Pointer LockSemaphore;
		public long SpinCount;

		protected List getFieldOrder() {
		     return Arrays.asList(new String[] {"LockCount","RecursionCount","OwningThread","LockSemaphore","SpinCount"});
		}

		public CRITICAL_SECTION_STRUCT() {
			super();
			allocateMemory();
		}

		protected void finalize() {
			clear();	//clear native memory
		}
	}

	//non struct variables
	private String name;
	private Thread owner;
	private CRITICAL_SECTION_STRUCT cs;

	/**
	* Create a new CriticalSection with the given name. You must initialize it before using it.
	* This creates a mutex as the critical section
	*/
	public CriticalSection(String name) {
		this.name=name;
		cs=new CRITICAL_SECTION_STRUCT();
	}

	public String getName() {return name;}
	//do we hold critical section?
	public boolean isHeld() {
		return owner!=null && owner.equals(Thread.currentThread());
	}
	//this doesn't guarantee you can enter, it just means no one owns the critical section
	//to the best of our knowledge
	public boolean isUnlocked() {
		return owner==null;
	}
	public Thread getOwner() {return owner;}

	//return the object we are actually locking on
	public Object getLockObject() {return cs;}

	/**
	* Initialize the CriticalSection
	*/
	public void init() {
		if (cs==null) {throw new IllegalStateException("cs is null");}
		kernel32.InitializeCriticalSection(cs.getPointer());
	}

	/**
	* This will block if this thread is not the owner.
	*/
	public void enter() {
		if (cs==null) {throw new IllegalStateException("cs is null");}
		kernel32.EnterCriticalSection(cs.getPointer());
		this.owner=Thread.currentThread();
	}

	/**
	* Try to obtain ownership of the critical section.  This will not block
	* but will return false if unsuccessful.
	*/
	public boolean tryEnter() {
		boolean b=kernel32.TryEnterCriticalSection(cs.getPointer());
		if (b) {
			this.owner=Thread.currentThread();
		}
		return b;
	}

	public void leave() {
		kernel32.LeaveCriticalSection(cs.getPointer());
		this.owner=null;
	}

	/**
	* Delete the CriticalSection.  Don't use it after you call this.
	* This is the destructor.
	*
	* Warning: there is a problem with timing on this class.  It is being called
	* while another thread is still in the critical section.  You don't have to call this.
	*/

	protected void finalize() {
		System.out.println("deleting critical section");
		kernel32.DeleteCriticalSection(cs.getPointer());
		cs=null;
	}

	//====================
	//usually I put the library code at the beginning but for readability I will put it at the end.
	public static Kernel32Ex kernel32;
	static {
		kernel32=(Kernel32Ex) Native.loadLibrary("kernel32", Kernel32Ex.class,W32APIOptions.DEFAULT_OPTIONS);
	}

	//define a new library that deals only with mutex methods
   public interface Kernel32Ex extends Kernel32 {
		/**
		*void WINAPI InitializeCriticalSection(
		*  _Out_ LPCRITICAL_SECTION lpCriticalSection
		*);
		* lpCriticalSection [out]
		*	A pointer to the critical section object.
		*/
		public void InitializeCriticalSection(Pointer p);

		/**
		* Waits for ownership of the specified critical section object. The function returns when
		* the calling thread is granted ownership.
		* void WINAPI EnterCriticalSection(
		*  _Inout_ LPCRITICAL_SECTION lpCriticalSection
		*);
		*/
		public void EnterCriticalSection(Pointer p);

		/**
		* Attempts to enter a critical section without blocking. If the call is successful, the calling
		* thread takes ownership of the critical section.
		* BOOL WINAPI TryEnterCriticalSection(
		*  _Inout_ LPCRITICAL_SECTION lpCriticalSection
		*/
		public boolean TryEnterCriticalSection(Pointer p);

		/**
		* Releases ownership of the specified critical section object.
		* void WINAPI LeaveCriticalSection(
  		* _Inout_ LPCRITICAL_SECTION lpCriticalSection
		* );
		*/
		public void LeaveCriticalSection(Pointer p);

		/**
		* void WINAPI DeleteCriticalSection(
  		* _Inout_ LPCRITICAL_SECTION lpCriticalSection
		* );
		*/
		public void DeleteCriticalSection(Pointer p);
	}
}