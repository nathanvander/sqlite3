package apollo;
import com.sun.jna.Structure;
import com.sun.jna.PointerType;
import com.sun.jna.Pointer;

/**
* These are used to wrap pointer, to make it clear which one we are talking about.  It creates more overhead,
* but more type safety.
*/
public interface Handle {
	//this is used to wrap a pointer, so we don't forget what type it is
	public class sqlite3_handle extends PointerType {
		public sqlite3_handle(Pointer p) {
			super(p);
		}
	}
	public class sqlite3_stmt_handle extends PointerType{
		public sqlite3_stmt_handle(Pointer p) {
			super(p);
		}
	}
  	//public class sqlite3_vfs_handle extends PointerType{
	//	public sqlite3_vfs_handle(Pointer p) {
	//		super(p);
	//	}
	//}
}