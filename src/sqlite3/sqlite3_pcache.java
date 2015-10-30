package sqlite3;
import com.sun.jna.Structure;
import com.sun.jna.Pointer;
import java.util.List;
import java.util.Arrays;

//typedef struct sqlite3_pcache sqlite3_pcache;
//see also: sqlite3_pcache_methods2
//	PCache
//	PCache1

public abstract class sqlite3_pcache extends Structure {
	int szPage;
	int bPurgeable;

	protected List getFieldOrder() {
	     return Arrays.asList(new String[] {"pMethods"});
	}
}