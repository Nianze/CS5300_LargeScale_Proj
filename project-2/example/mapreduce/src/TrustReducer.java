import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.util.*;

public class TrustReducer extends Reducer<IntWritable, NodeOrDouble, IntWritable, Node> {
    public void reduce(IntWritable key, Iterable<NodeOrDouble> values, Context context)
        throws IOException, InterruptedException {
System.out.println("===================>" + "TrustReducer Begin!!");
    	Node M = null;
    	double curPageRank = 0.0;
    	for (NodeOrDouble p : values) {
    		if (p.isNode()) {
    			M = p.getNode();
    		}
    		else {
    			curPageRank += p.getDouble();
    		}
    	}
    	M.setPageRank(curPageRank);
    	context.write(key, M);
    }
}
