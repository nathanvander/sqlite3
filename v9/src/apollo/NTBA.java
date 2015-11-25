package apollo;

//null-terminated byte array
public class NTBA {
	public static byte[] get(String str) {
		byte[] stringBytes=null;
		try {stringBytes=str.getBytes("ISO-8859-1");} catch (Exception x) {x.printStackTrace();}
		byte[] ntBytes=new byte[stringBytes.length+1];
		System.arraycopy(stringBytes, 0, ntBytes, 0, stringBytes.length);
		return ntBytes;
	}
}