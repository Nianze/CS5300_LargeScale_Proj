package gsBlockPR;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import gsBlockPR.Ref;
import gsBlockPR.PRCounter;

public class BlockReducer extends Reducer<Text, Text, Text, Text> {
	
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
			int blockID = Integer.parseInt(key.toString());
			// produce in-memory structures
			HashMap<Integer,Node> nodeSet = new HashMap<Integer,Node>();
			HashMap<Integer,Float> initNodePR = new HashMap<Integer,Float>(); 
			HashMap<Integer,ArrayList<Integer>> BE = new HashMap<Integer, ArrayList<Integer>>(); // key: dstNodeID, value:srcNodeID
			HashMap<Integer, ArrayList<String>> inputBC = new HashMap<Integer, ArrayList<String>>(); // key: dstNodeID, value: <srcNodeID,Degree,PR>
			HashMap<Integer, ArrayList<String>> outputBC = new HashMap<Integer, ArrayList<String>>(); // key: srcNodeID, value: <dstNodeID,Degree,PR>
			
	   		while (values.iterator().hasNext()){
	   			Text value = values.iterator().next();
    			String line = value.toString().trim();    			
    			String[] parts = line.split("_");
    			int lineType = Integer.parseInt(parts[0]);
    			int srcNodeID = Integer.parseInt(parts[1]);
    			int dstNodeID = 0;
    			
    			switch(lineType){
    			case Ref.typNode:    				
    				Node node = new Node(parts[1]+" "+parts[2]+" "+parts[3]);
    				nodeSet.put(srcNodeID, node);
    				initNodePR.put(srcNodeID, Float.parseFloat(parts[2]));
//    				System.out.println("<node> "+ node.toString());
    				break;
    			case Ref.typBE:    				
    				dstNodeID = Integer.parseInt(parts[2]);
    				ArrayList<Integer> list = BE.get(dstNodeID);
    				if(list == null) list = new ArrayList<Integer>();
    				list.add(srcNodeID);
    				BE.put(dstNodeID, list);
//    				System.out.println("<BE> key:"+dstNodeID+" val:"+srcNodeID);
    				break;
    			case Ref.typBC:    				
    				dstNodeID = Integer.parseInt(parts[2]);
    				float PR = Float.parseFloat(parts[3]);
    				int deg = Integer.parseInt(parts[4]);
    				int typ = Integer.parseInt(parts[5]);
    				switch(typ){
    				case Ref.typBC_InBlock:
    					String EdgeIn = new String(""+srcNodeID +"_"+ PR +"_"+ deg);
        				ArrayList<String> _list = inputBC.get(dstNodeID);
        				if(_list == null) _list = new ArrayList<String>();
        				_list.add(EdgeIn);
        				inputBC.put(dstNodeID, _list);
//        				System.out.println("<inputBC> key:"+dstNodeID+" val:"+EdgeIn.toString());
    					break;
    				case Ref.typBC_outBlock:
    					String EdgeOut = new String(""+dstNodeID +"_"+ PR +"_"+ deg);
    					ArrayList<String> _list_ = outputBC.get(srcNodeID);
        				if(_list_ == null) _list_ = new ArrayList<String>();
        				_list_.add(EdgeOut);
        				outputBC.put(srcNodeID, _list_);
//        				System.out.println("<outputBC> key:"+srcNodeID+" val:"+EdgeOut.toString());
    					break;
    				}
    			}
    		}
			// iterate inside block
	   		int iterNum = 1;
			while(IterateBlockOnce(nodeSet, BE, inputBC) > Ref.THRESHOLD){iterNum++;}
			Ref.incBlockIterNum(blockID,iterNum);
			
			// keep track of lowest 2 nodes' PR
			int lowNID1 = Ref.NUM_NODES, lowNID2 = Ref.NUM_NODES + 1;
			// figure out the overall residual in one block
			Float blockRes = 0.0f;
			for(int nid : initNodePR.keySet()){
				float oldVal = initNodePR.get(nid);
				float newVal = nodeSet.get(nid).PR;
				blockRes += Math.abs(oldVal-newVal)/newVal;
	   			// keep track of the lowest two node ID
	   			if(nid < lowNID1){
	   				lowNID2 = lowNID1;
	   				lowNID1 = nid;
	   			} else if (nid < lowNID2){
	   				lowNID2 = nid;
	   			}
			}
	   		// output the lowest two node ID and their PRs
//	   		System.out.println("node: " + lowNID1 +" PR:"+nodeSet.get(lowNID1).PR);
//	   		System.out.println("node: " + lowNID2 +" PR:"+nodeSet.get(lowNID2).PR);
//			System.out.println("block " + key.toString() +" Res Val:"+blockRes);
//	   		System.out.println("----------------");
	   		
			// report to Counter
			blockRes *= 1000000;
			context.getCounter(PRCounter.TOTAL_RESIDUAL_ERROR).increment(blockRes.longValue());
			
			// create output
	   		for(int dstNID : BE.keySet()){
	   			for(int srcNID : BE.get(dstNID)){
	   				Node srcNode = nodeSet.get(srcNID);
	   				Edge edgeInfo = new Edge(srcNID, dstNID, srcNode.PR, srcNode.Degree);
	   				Text text = new Text(edgeInfo.toString());
	   				context.write(null, text);
	   			}
	   		}
	   		
	   		for(int srcNID : outputBC.keySet()){
	   			for(String bcEdge : outputBC.get(srcNID)){
	   				String[] parts = bcEdge.split("_");
	   				int dstNID = Integer.parseInt(parts[0]);
	   				Node srcNode = nodeSet.get(srcNID);
	   				float srcPR = srcNode.PR;
	   				int srcDegree = srcNode.Degree;
	   				Edge edgeInfo = new Edge(srcNID,dstNID,srcPR,srcDegree);
	   				Text text = new Text(edgeInfo.toString());
	   				context.write(null, text);
	   			}
	   		}
		}
		
	}
	
	private float IterateBlockOnce(HashMap<Integer,Node> nodeSet,
								   HashMap<Integer,ArrayList<Integer>> BE, 
								   HashMap<Integer,ArrayList<String>> BC){
		float iterateRes = 0.0f;
		for(int dstNID : nodeSet.keySet()){
			float nextPR = 0.0f;
			if(BE.get(dstNID) != null){
				for(int srcNID : BE.get(dstNID)){
					Node srcNode = nodeSet.get(srcNID);
					nextPR += srcNode.PR / srcNode.Degree;
				}
			}
			if(BC.get(dstNID) != null){
				for(String bcEdge : BC.get(dstNID)){
	   				String[] parts = bcEdge.split("_");
	   				float srcPR = Float.parseFloat(parts[1]);
	   				int srcDegree = Integer.parseInt(parts[2]);
					nextPR += srcPR / srcDegree;
				}
			}
			nextPR = ((1-Ref.DAMPING_FACTOR)/Ref.NUM_NODES) + Ref.DAMPING_FACTOR*nextPR;
			float oldPR = nodeSet.get(dstNID).PR;
			iterateRes += Math.abs(nextPR - oldPR) / nextPR;
			//update next PR to destination node
			Node nextNode = nodeSet.get(dstNID).newPRNode(nextPR);
			nodeSet.put(dstNID, nextNode);
		}
//		System.out.println("temp res for one iteration: "+iterateRes);
		return iterateRes;		
	}

}
