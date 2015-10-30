package sqlite3;
import com.sun.jna.Structure;
import com.sun.jna.Pointer;
import java.util.List;
import java.util.Arrays;

//typedef struct sqlite3_mem_methods sqlite3_mem_methods;
//struct sqlite3_mem_methods {
public abstract class sqlite3_mem_methods extends Structure {
	protected List getFieldOrder() {
	     return Arrays.asList(new String[] {"pAppData"});
	 }

  	public abstract Pointer xMalloc(int i);         /* Memory allocation function */
  	public abstract void xFree(Pointer p);          /* Free a prior allocation */
  	public abstract Pointer xRealloc(Pointer p,int i); /* Resize an allocation */
  	public abstract int xSize(Pointer p);           /* Return the size of an allocation */
  	public abstract int xRoundup(int i);          	/* Round up request size to allocation size */
  	public abstract int xInit(Pointer p);           /* Initialize the memory allocator */
  	public abstract void xShutdown(Pointer p);      /* Deinitialize the memory allocator */
  	public Pointer pAppData;                		/* Argument to xInit() and xShutdown() */
};