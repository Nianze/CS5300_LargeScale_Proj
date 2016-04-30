package blockPR;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import blockPR.Ref;
import blockPR.PRCounter;

public class BlockReducer extends Reducer<Text, Text, Text, Text> {
	
	public class Pair<X, Y> {
		public final X x; 
		public final Y y; 
		public Pair(X x, Y y){
		    this.x = x; 
		    this.y = y; 
		}
		public X first(){return this.x;}
		public Y second(){return this.y;}
	}
	
	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
		int count = Ref.PASS_NUM;
		
		if(count == 0){
			Integer nodeID = Integer.parseInt(key.toString());
			ArrayList<Text> in_link = new ArrayList<Text>();
			ArrayList<Text> out_link = new ArrayList<Text>();
	   		while (values.iterator().hasNext()){
	   			Text value = values.iterator().next();
    			String line = value.toString().trim();
    			String[] parts = line.split("\\s+");
    			
    			if(Integer.parseInt(parts[0]) == nodeID){
    				Text t = new Text(value.toString());
    				out_link.add(t);
    			}else{
    				Text t = new Text(value.toString());
    				in_link.add(t);
    			}
    		}
	   		
	   		// compute PageRank logic
	   		// Eventual output format: "srcNodeID destNodeID srcNodePR srcNumOutLinks"
	   		// Note we only emit entries in the out_link ArrayList and get rid of duplication
	   		int srcNumOutLinks = out_link.size();

   			if(srcNumOutLinks == 0){ // create a special entry for case where a node has no out links
   				Edge obj = new Edge(nodeID, -1, Ref.INITIAL_PR, -1);
				Text outValue = new Text(obj.toString());
				context.write(null, outValue);
   			}else{
   				String line = "";
   				for(Text t : out_link){
   					line = t.toString().trim();
   					String[] parts = line.split("\\s+");
   					Integer srcNodeID = Integer.parseInt(parts[0]);
   					Integer destNodeID = Integer.parseInt(parts[1]);
   					Edge obj = new Edge(srcNodeID, destNodeID, Ref.INITIAL_PR, srcNumOutLinks);
   					Text outValue = new Text(obj.toString());
   					context.write(null, outValue);
   				}
   			}
		}
		
		if(count > 0){
			//int blockID = Integer.parseInt(key.toString());
			// produce in-memory structures
			HashMap<Integer,Node> nodeSet = new HashMap<Integer,Node>();
			HashMap<Integer,Float> initNodePR = new HashMap<Integer,Float>();
			HashMap<Integer,ArrayList<Integer>> BE = new HashMap<Integer, ArrayList<Integer>>(); // key: dstNodeID, value:srcNodeID
			HashMap<Integer, ArrayList<Pair<Integer,Float>>> BC = new HashMap<Integer, ArrayList<Pair<Integer,Float>>>(); // key: dstNodeID, value: <srcNodeID,R>
			
	   		while (values.iterator().hasNext()){
	   			Text value = values.iterator().next();
    			String line = value.toString().trim();
    			String[] parts = line.split("\\s+");
    			int lineType = Integer.parseInt(parts[0]);
    			int srcNodeID = Integer.parseInt(parts[1]);
    			int dstNodeID = 0;
    			
    			switch(lineType){
    			case Ref.typNode:    				
    				Node node = new Node(parts[1]+" "+parts[2]+" "+parts[3]);
    				nodeSet.put(srcNodeID, node);
    				initNodePR.put(srcNodeID, Float.parseFloat(parts[2]));
    				break;
    			case Ref.typBE:    				
    				dstNodeID = Integer.parseInt(parts[2]);
    				ArrayList<Integer> list = BE.get(dstNodeID);
    				if(list == null) list = new ArrayList<Integer>();
    				list.add(srcNodeID);
    				BE.put(dstNodeID, list);
    				break;
    			case Ref.typBC:    				
    				dstNodeID = Integer.parseInt(parts[2]);
    				float R = Float.parseFloat(parts[3]);
    				Pair<Integer,Float> outEdge = new Pair<Integer,Float>(srcNodeID, R);
    				ArrayList<Pair<Integer,Float>> _list = BC.get(dstNodeID);
    				if(_list == null) _list = new ArrayList<Pair<Integer,Float>>();
    				_list.add(outEdge);
    				BC.put(dstNodeID, _list);
    				break;
    			}
    		}
			
	   		// save the 
	   		
			// iterate inside block
			while(IterateBlockOnce(nodeSet, BE, BC) > Ref.THRESHOLD){}
	   		
			// figure out the residual
			
	   		
			// report to Counter
			//context.getCounter(PRCounter.TOTAL_RESIDUAL_ERROR).increment(arg0);
	   		
			// create output
	   		
		}
		
	}
	
	private float IterateBlockOnce(HashMap<Integer,Node> nodeSet,
			HashMap<Integer,ArrayList<Integer>> BE, HashMap<Integer, 
			ArrayList<Pair<Integer,Float>>> BC){
		
		return 0.0f;
	}

}
