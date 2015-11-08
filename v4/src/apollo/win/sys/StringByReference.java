package apollo.win.sys;

// This is a class that facilitates passing a String by reference to
// C library routines so that the string  may be modified by the C
// routine and the modifications is reflected on JAVA side as well
// Save this in a file StringByReference.java in current directory

import com.sun.jna.ptr.ByReference;

public class StringByReference extends ByReference {
    public StringByReference() {
        this(0);
    }

    public StringByReference(int size) {
        super(size < 4 ? 4 : size);
        getPointer().clear(size < 4 ? 4 : size);
    }

    public StringByReference(String str) {
        super(str.length() < 4 ? 4 : str.length() + 1);
        setValue(str);
    }

    protected void setValue(String str) {
        getPointer().setString(0, str);
    }

    public String getValue() {
        return getPointer().getString(0);
    }
}
