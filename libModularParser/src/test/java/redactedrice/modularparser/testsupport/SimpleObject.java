package redactedrice.modularparser.testsupport;


public class SimpleObject {
    public int intField;
    public boolean boolField;
    public String strField;
    public SimpleObject so;
    private int[] intArray;

    public SimpleObject(int intField, boolean boolField, String strField, SimpleObject so) {
        this.so = so;
        this.intField = intField;
        this.boolField = boolField;
        this.strField = strField;
    }

    public String toString() {
        String ret = "int: " + intField + ", bool: " + boolField + ", str: " + strField;
        if (so == null) {
            ret += ", so: null";
        } else {
            ret += ", so: '" + so.toString() + "'";
        }
        return ret;
    }

    public SimpleObject getSo() {
        return so;
    }

    public int getInt() {
        return intField;
    }

    public boolean getBool() {
        return boolField;
    }

    public void setInt(int inInt) {
        intField = inInt;
    }

    public int[] getIntArray() {
        return intArray;
    }

    public void setIntArray(int... intArray) {
        this.intArray = intArray;
    }
}
