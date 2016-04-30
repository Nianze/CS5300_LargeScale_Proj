package blockPR;

public class Main {
	
	public static boolean selectInputLine(double x) 
	{
		double fromNetID = 0.539;
		double rejectMin = 0.9 * fromNetID;
		double rejectLimit = rejectMin + 0.01;
		return ( ((x >= rejectMin) || (x < rejectLimit)) ? false : true );
	}
	
	
}
