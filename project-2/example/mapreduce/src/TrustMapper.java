import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.util.*;


public class TrustMapper extends Mapper<IntWritable, Node, IntWritable, NodeOrDouble> {
    public void map(IntWritable key, Node value, Context context) throws IOException, InterruptedException {
System.out.println("===================>" + "TrustMapper Begin!!");
    	double p = value.pageRank / value.outgoingSize();
    	// Write out nid, node
    	context.write(key, new NodeOrDouble(value));
    	context.getCounter(COUNTERS.SIZE).increment(1);
    	// If node has no outlinks, increment the LOSS_MASS_COUNT by page rank * 100000
    	if (value.outgoingSize() == 0) {
    		long temp = (long)(value.pageRank * 100000);
System.out.println("===============>This node has no outgoing links, and loss mass is " + temp);
    		context.getCounter(COUNTERS.LOSS_MASS_COUNT).increment(temp);
    	}
    	// Otherwise emit the m (in outlink list), page rank
    	else {
	    	for (int m : value.outgoing) {
	    		context.write(new IntWritable(m), new NodeOrDouble(p));
	    	}
    	}
    }
}
