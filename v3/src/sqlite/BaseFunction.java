package sqlite;
import java.lang.reflect.Field;

public abstract class BaseFunction implements Function {
	// time fields
	protected long start;	//record when function started and stopped for testing purposes.
	protected long finish;

    //result, will be zero if success
    protected int resultCode;

	public long getElapsedTime() {return finish-start;}

	//just use reflection
	//pass in the name of the subclass
	protected Class getType(Class k,String paramName) {
		try {
			Field f=k.getField(paramName);
			f.setAccessible(true);
			return f.getType();
		} catch (Exception x) {
			x.printStackTrace();
			return null;
		}
	}

	//throw exception if it is the wrong type
	protected void set(Class k,String paramName,Object o) {
		try {
			Field f=k.getField(paramName);
			f.setAccessible(true);
			f.set(this,o);
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	protected Object get(Class k,String paramName) {
		try {
			Field f=k.getField(paramName);
			f.setAccessible(true);
			return f.get(this);
		} catch (Exception x) {
			x.printStackTrace();
			return null;
		}
	}

	//this is the same number returned from the call.
	public int getResult() {return resultCode;}
}