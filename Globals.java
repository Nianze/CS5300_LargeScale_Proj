package sessionManagement;

import java.util.Hashtable;

public class Globals 
{
	public static Hashtable<String, SessionValues> hashtable;
	public static Hashtable<String, String> ipAddressMapping;
	
	static
	{
		hashtable = new Hashtable<String, SessionValues>();
		ipAddressMapping = new Hashtable<String, String>();
	}
}
