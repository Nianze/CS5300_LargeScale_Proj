package sessionManagement;

import java.util.Hashtable;

public class Globals 
{
	public static Hashtable<String, SessionValues> hashtable;
	public static Hashtable<String, String> ipAddressMapping;
	public static final int R = 2;
	public static final int W = 3;
	public static final int WQ = 2;
	public static final String cookieDomain = ".nl443.bigdata.systems";
	public static String currentServerID = "";
	public static String rebootNum = ""; 
	
	static
	{
		hashtable = new Hashtable<String, SessionValues>();
		ipAddressMapping = new Hashtable<String, String>();
	}
}
