package sqlite3;
import com.sun.jna.Structure;
import com.sun.jna.Pointer;
import java.util.List;
import java.util.Arrays;

/*
** CAPI3REF: OS Interface Open File Handle
**
** An [sqlite3_file] object represents an open file in the
** [sqlite3_vfs | OS interface layer].  Individual OS interface
** implementations will
** want to subclass this object by appending additional fields
** for their own use.  The pMethods entry is a pointer to an
** [sqlite3_io_methods] object that defines methods for performing
** I/O operations on the open file.
*/
//typedef struct sqlite3_file sqlite3_file;
//struct sqlite3_file {
//  const struct sqlite3_io_methods *pMethods;  /* Methods for an open file */
//};

public class sqlite3_file extends Structure {
	//pointer to sqlite3_io_methods
	//public Pointer pMethods;
	//this doesn't need to be a pointer
	public sqlite3_io_methods pMethods;

	protected List getFieldOrder() {
	     return Arrays.asList(new String[] {"pMethods"});
	}
}