package apollo;
import java.util.*;
import java.lang.reflect.*;

/**
* Pickle is a way of preserving the state of the object. Fieldnames are not saved.  If the object implements
* the Table interface, then it uses getFieldNames() for the field order. If it doesn't, then it uses the order
* from Class.getFields(), which should be in declaration order, however, there is no guarantee.  At least the field order
* should be consistent if you use the same JVM number.
*
* Here are the basic conventions:
*	Object values are surrounded by curly braces.
* 	A String is represented by ${}.  So embedded double quotes are not a problem.
*	Nulls are represented with a V, for void.
*	Field values are separated by commas.
*	For boolean values, F and T are used.
*
* This isn't meant to handle everything, just the most common situations.
* It only saves public fields.
*
* Later versions of this may want to handle arrays, hashmaps or lists.
*/

public class Pickle {
	public static String pickle(Object o) {
		if (o==null) {
			//we don't know the class of the object
			return "V";	//for void
		}

		String className=o.getClass().getName();
		//step 1, see if it is a string or some other commmon type
		if (className.equals("java.lang.String")) {
			return "${"+o.toString()+"}";
		} else if (className.equals("java.lang.Boolean")) {
			Boolean bb=(Boolean)o;
			boolean b=bb.booleanValue();
			if (b) {
				return "Z{T}";
			} else {
				return "Z{F}";
			}
		} else if (className.equals("java.lang.Short")) {
			Short s=(Short)o;
			return "S{"+s.shortValue()+"}";
		} else if (className.equals("java.lang.Integer")) {
			Integer ii=(Integer)o;
			return "I{"+ii.intValue()+"}";
		} else if (className.equals("java.lang.Long")) {
			Long ll=(Long)o;
			return "L{"+ll.longValue()+"}";
		} else if (className.equals("java.lang.Float")) {
			Float ff=(Float)o;
			//we use the double value
			return "D{"+ff.doubleValue()+"}";
		} else if (className.equals("java.lang.Double")) {
			Double dd=(Double)o;
			return "D{"+dd.doubleValue()+"}";
		} else if (o instanceof Table) {
			try {
				Table t=(Table)o;
				Class k=o.getClass();
				//String kn=k.getName();
				String[] fieldNames=t.getFieldNames();
				StringBuffer sb=new StringBuffer(className+"{");
				for (int i=0;i<fieldNames.length;i++) {
					if (i>0) {sb.append(",");}
					String fn=fieldNames[i];
					Field f=k.getField(fn);
					String fieldType=f.getType().getName();
					Object value=f.get(o);
					String pfv=pickleFieldValue(fieldType,value);
					sb.append(pfv);
				}
				sb.append("}");
				return sb.toString();
			} catch (Exception x) {
				x.printStackTrace();
			}
		} else {
			//just use reflection
			try {
				//System.out.println("using reflection");
				Class k=o.getClass();
				Field[] ff=k.getFields();
				StringBuffer sb=new StringBuffer(className+"{");
				//System.out.println(sb.toString());
				for (int i=0;i<ff.length;i++) {
					if (i>0) {sb.append(",");}
					Field f=ff[i];
					f.setAccessible(true);
					String fn=f.getName();
					String fieldType=f.getType().getName();
					Object value=f.get(o);
					String pfv=pickleFieldValue(fieldType,value);
					//System.out.println(pfv);
					sb.append(pfv);
				}
				sb.append("}");
				//System.out.println(sb.toString());
				return sb.toString();
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
		throw new RuntimeException("compiler, are you happy now?");
	}

	//this only handles some of the possibilities
	public static String pickleFieldValue(String fieldType,Object v) {
		//System.out.println("fieldType="+fieldType);
		if (v==null) {
			return "V";
		} else if (
			fieldType.equals("java.lang.String") ||
			fieldType.equals("java.lang.Integer") ||
			fieldType.equals("java.lang.Long") ||
			fieldType.equals("java.lang.Double")
			)	{  //this only has some of the combinations
			return pickle(v);
		} else if ( fieldType.equals("boolean") || fieldType.equals("Z")) {
			Boolean bb=(Boolean)v;
			if (bb.booleanValue()) {return "T";}
			else {return "F";}
		} else if ( fieldType.equals("double") || fieldType.equals("D")) {
			Double dd=(Double)v;
			return dd.toString();
		} else if (fieldType.equals("int") || fieldType.equals("I")) {
			Integer ii=(Integer)v;
			return ii.toString();
		} else if (fieldType.equals("long") || fieldType.equals("J")) {
			Long ll=(Long)v;
			return ll.toString();
		} else if (fieldType.equals("float") || fieldType.equals("F")) {
			Float ff=(Float)v;
			return ff.toString();
		} else if (fieldType.equals("short") || fieldType.equals("S")) {
			Short s=(Short)v;
			return s.toString();
		} else {
			throw new RuntimeException("unknown type "+fieldType);
		}
	}

	public static Object unpickle(String p) {
			//first look for the class name
			int lb=p.indexOf("{");
			String className=p.substring(0,lb);
			System.out.println(className);
			Class k=null;
			Object o=null;
			try {
				k=Class.forName(className);
				o=k.newInstance();
			} catch (Exception x) {
				x.printStackTrace();
				throw new RuntimeException("problems creating an object of type "+className);
			}
			//strip off the curly braces
			String guts=p.substring(lb+1,p.length()-1);
			String[] values=guts.split(",");
			if (o instanceof Table) {
				Table t=(Table)o;
				String[] fieldNames=t.getFieldNames();
				if (values.length!=fieldNames.length) {
					System.out.println("warning: there are "+fieldNames.length+" fields, but only "+values.length+" values");
				}
				for (int i=0;i<fieldNames.length;i++) {
					String fn=fieldNames[i];
					Field f=null;
					try {
						k.getField(fn);
					} catch (NoSuchFieldException x) {
						x.printStackTrace();
						continue;
					}
					f.setAccessible(true);
					String fieldType=f.getType().getName();
					String value=values[i];
					if (value.equals("V")) {
						//it's a null
						continue;
					} else if (value.startsWith("$")) {
						String sval=value.substring(2,value.length()-1);
						if (fieldType.equals("java.lang.String")) {
							try {
							f.set(o,sval);
							} catch (IllegalAccessException x) {
								x.printStackTrace();
							}
						} else {
							throw new RuntimeException("expecting field #"+i+" to be a String");
						}
					} else {
						try {
						unpickleField(f,fieldType,o, value);
						} catch (IllegalAccessException x) {
							x.printStackTrace();
						}
					}
				}
			} else {
				Field[] ff=k.getFields();
				if (values.length!=ff.length) {
					System.out.println("warning: there are "+ff.length+" fields, but only "+values.length+" values");
				}
				for (int i=0;i<ff.length;i++) {
					Field f=ff[i];
					f.setAccessible(true);
					String fn=f.getName();
					String fieldType=f.getType().getName();
					String value=values[i];
					if (value.equals("V")) {
						//it's a null
						continue;
					} else if (value.startsWith("$")) {
						String sval=value.substring(2,value.length()-1);
						if (fieldType.equals("java.lang.String")) {
							try {
								f.set(o,sval);
							} catch (IllegalAccessException x) {
								x.printStackTrace();
							}
						} else {
							throw new RuntimeException("expecting field #"+i+" to be a String");
						}
					} else {
						try {
							unpickleField(f,fieldType,o, value);
						} catch (IllegalAccessException x) {
							x.printStackTrace();
						}
					}
				}
			}
			return o;
	}

	public static void unpickleField(Field f,String fieldType,Object o,String value) throws IllegalAccessException {
						if (fieldType.equals("boolean")) {
							if (value.equals("T") || value.equals("1") ) {
								f.setBoolean(o,true);
							} else if (value.equals("F") || value.equals("0")) {
								f.setBoolean(o,false);
							} else {
								throw new RuntimeException("expecting field "+f.getName()+" to be a boolean");
							}
						} else if (fieldType.equals("int")) {
							f.setInt(o,Integer.parseInt(value));
						} else if (fieldType.equals("long")) {
							f.setLong(0,Long.parseLong(value));
						} else if (fieldType.equals("short")) {
							f.setShort(o,Short.parseShort(value));
						} else if (fieldType.equals("double")) {
							f.setDouble(o,Double.parseDouble(value));
						} else if (fieldType.equals("float")) {
							f.setFloat(o,Float.parseFloat(value));
						} else {
							System.out.println("unknown fieldType "+fieldType);
						}
	}
}