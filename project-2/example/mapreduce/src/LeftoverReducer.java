                        import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.util.*;


public class LeftoverReducer extends Reducer<IntWritable, Node, IntWritable, Node> {
    public static double alpha = 0.85;
    public void reduce(IntWritable nid, Iterable<Node> Ns, Context context) throws IOException, InterruptedException {
System.out.println("===================>" + "LeftoverReducer Begin!!");
    	Node n = Ns.iterator().next();
    	Configuration conf = context.getConfiguration();
System.out.println("===================>" + "Conf leftover field: " + conf.get("leftover"));
    	double leftover = Long.parseLong(conf.get("leftover")) * 1.0 / 100000;
    	long size = Long.parseLong(conf.get("size"));
System.out.println("===================>" + "leftover: " + leftover);
System.out.println("===================>" + "size: " + size);
    	double newPageRank = alpha * (1.0 / size) + (1 - alpha) * (leftover / size + n.getPageRank());
System.out.println("===================>" + "Old PageRank: " + n.getPageRank());
System.out.println("===================>" + "New PageRank: " + newPageRank);
    	n.setPageRank(newPageRank);
        context.write(nid, n);
    }
}
