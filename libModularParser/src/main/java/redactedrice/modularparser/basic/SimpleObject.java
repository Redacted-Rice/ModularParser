package redactedrice.modularparser.basic;

public class SimpleObject {
	public int intField;
	public boolean boolField;
	public String strField;
	public SimpleObject so;
	
	public SimpleObject(int intField, boolean boolField, String strField, SimpleObject so) {
		this.so = so;
		this.intField = intField;
		this.boolField = boolField;
		this.strField = strField;
	}
}
