package randomBlockPR;

public class Ref {
/**
 * there are three types of output to save in each pass:
 * 1. Node type: 0_nodeID_PR_Degree
 * 2. BE type: 1_srcNodeID_dstNodeID
 * 3. BC type: 2_srcNodeID_dstNodeID_PR_Deg_I/O, where PR = PR(src), Deg = Degree(src), I:in,O:out
 */
	public static int PASS_NUM = 0;
	public static int[] AVG_BLOCK_ITER = {
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0}; 
	
	public static final int NUM_NODES = 685022;	
	public static final int NUM_BLOCK = 68;
	public static final float INITIAL_PR = (1.0f/NUM_NODES);
	public static final float DAMPING_FACTOR = 0.85f;
	public static final float THRESHOLD = 0.001f;
	
	public static final int typNode = 0; //code for type of node
	public static final int typBE = 1; // code for type of block edges
	public static final int typBC = 2; // code for type of boundary conditions
	public static final int typBC_InBlock = 1; // code for input edge of BC type
	public static final int typBC_outBlock = 0; // code for output edge of BC type
		
	public static final int[] BLOCK_BOUNDARY = {
			 0,    10328, 20373, 30629, 40645,
            50462,   60841,  70591,  80118,  90497, 100501, 110567, 120945,
            130999, 140574, 150953, 161332, 171154, 181514, 191625, 202004,
            212383, 222762, 232593, 242878, 252938, 263149, 273210, 283473,
            293255, 303043, 313370, 323522, 333883, 343663, 353645, 363929,
            374236, 384554, 394929, 404712, 414617, 424747, 434707, 444489,
            454285, 464398, 474196, 484050, 493968, 503752, 514131, 524510,
            534709, 545088, 555467, 565846, 576225, 586604, 596585, 606367,
            616148, 626448, 636240, 646022, 655804, 665666, 675448, 685230 };
	
	public static int blockIDofNode(int nodeID){
		Integer id = new Integer(nodeID);
		int guess = id.hashCode() % 68;
		return guess;
	}
	
	public static void initPassNum(){
		PASS_NUM = 0;
	}
	
	public static void incPassNum(){
		PASS_NUM ++;
	}
	
	public static void incBlockIterNum(int blockID, int inc){
		AVG_BLOCK_ITER[blockID] += inc;
	}
	
}
