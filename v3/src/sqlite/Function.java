package sqlite;
import java.util.concurrent.Callable;
/**
* A Function is a discrete unit of work. This could also be called a "job".  Because it extends Callable, it could
* be run in a separate thread, but I don't see the advantage to that in most cases.
*
* Using it involves three steps.  First, set the input parameters with set().  Second, call the function.  Third,
* get the output parameters.
*
* The function call will return an integer, which should be zero, or an error code if it is not zero.
* There can be multiple output parameters
*/
public interface Function extends Callable<Integer> {
	//returns the time in milliseconds it took to run the function.
	public long getElapsedTime();

	//parameters
	//get a list of all parameters that this function uses
	public String[] getParameterNames();
	//this is a list of only the input parameters
	public String[] getInParams();
	//this is a list of only the output parameters
	public String[] getOutParams();
	public Class getType(String paramName);

	//this is the same number returned from the call.
	public int getResult();

	//throw IllegalArgumentException if it is the wrong type
	public void set(String paramName,Object o);
	public Object get(String paramName);

}