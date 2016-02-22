package apollo;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.text.SimpleDateFormat;
import com.sun.jna.platform.win32.Kernel32;

/**
* An OID is an object identifier. It is generated externally to the database and it can be used by multiple
* processes simultaneously.
*
* This is a 12 digit number made up of the following parts. The first 5 digits are the date, with a single digit
* for the year, for example 6 for 2016.  The next 3 digits are the last 3 of the process id of the JVM that generated
* this.  The last 4 are a sequence number, initially beginning with a random number from 10.99.
*
* Because of the design, this cannot generate more than 10,000 unique OIDs per JVM per day.  This should not be
* an issue for the intended purpose, which is a low-volume database, which may have a dozen entries per day.
*/
public class OID {
	//this returns a 2 digit year.  putting a single y won't return just the last digit of the year
	public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyMMdd");
	private static AtomicInteger sequence;
	//static because it won't change
	private static String mypid;

	//static init
	static {
		int r=randomTwoDigit();
		sequence=new AtomicInteger(r*100);
		mypid=getPID();
	}

	public static long getNext() {
		int nextSeq=sequence.incrementAndGet();
		if (nextSeq >9999) {
			nextSeq=nextSeq-10000;
			sequence=new AtomicInteger(nextSeq);
		}
		String sseq=today()+mypid+String.valueOf(nextSeq);
		//System.out.println("OID: created "+sseq);
		try {
			return Long.parseLong(sseq);
		} catch (Exception x) {
			x.printStackTrace();
		}
		return -1;  //error if you get this, won't happen
	}

	//returns only the last 3 digits of the pid
	//this is a String so it can start with a zero
	public static String getPID() {
		String spid=String.valueOf(Kernel32.INSTANCE.GetCurrentProcessId());
		return spid.substring(spid.length()-3);
	}

	//return a random 2 digit number from 10..99
	//it won't start with a 0
	public static int randomTwoDigit() {
		return new Random().nextInt(90)+10;
	}

	//only returns the last digit of the year, like 6
	public static String today() {
		 String sd= DATE_FORMAT.format(new Date());
		 return sd.substring(1);
	}

	public static void main(String[] args) {
		System.out.println(OID.getNext());
	}
}