package blockPR;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import blockPR.Ref;

public class BlockMapper extends Mapper<LongWritable, Text, Text, Text> {

	public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		String line = value.toString().trim();
		// parts[0]: srcNodeID, parts[1]: dstNodeID, parts[2]: srcNodePR, parts[3]: srcNodeDegree 
		String[] parts = line.split("\\s+");	
		
		// deal with special case: extra newline character at the end
		if(line.equalsIgnoreCase("")) return;
		
		int count = Ref.PASS_NUM; 
		
		// case that count == 0, initialize output file: src_dst_PR_Degree  
		if(count == 0){
			Text srcIDKey = new Text(parts[0]);
			Text dstIDKey = new Text(parts[1]);
			
			if(Integer.parseInt(parts[1]) != -1){				
				context.write(srcIDKey, value);
				context.write(dstIDKey, value);
			}
			else{ // ignore the special entry we created			
				context.write(srcIDKey, value);
			}
		}
		
		// case that count > 0, write three type of value for reducer: Node,BE,BC 
		if(count > 0){
			int srcNodeID = Integer.parseInt(parts[0]);
			int dstNodeID = Integer.parseInt(parts[1]);
			float pageRank = Float.parseFloat(parts[2]);
			int degree = Integer.parseInt(parts[3]);
			int srcBlockID = Ref.blockIDofNode(srcNodeID);
			int dstBlockID = Ref.blockIDofNode(dstNodeID);
			
			// key: blockID, srcNodeKey for node type and BE type while dstNodeKey for BC type
			Text srcNodeKey = new Text(Integer.toString(srcBlockID));
			Text dstNodeKey = new Text(Integer.toString(dstBlockID));
			
			// create node type output: 0_nodeID_PR_Degree
			String srcNodeVal = "" + Ref.typNode +"_"+ parts[0] +"_"+ parts[2] +"_"+ parts[3];
			Text nodeTypeVal = new Text(srcNodeVal);
			context.write(srcNodeKey, nodeTypeVal);
			
			// create BE type output: 1_srcNodeID_dstNodeID, where both srcNode and dstNode are in srcNodeBlock
			if(srcBlockID == dstBlockID){
				String blockEdge = "" + Ref.typBE +"_"+ parts[0] +"_"+ parts[1];
				Text BETypeVal = new Text(blockEdge);
				context.write(srcNodeKey, BETypeVal);
			}else{			
			// create BC type output: 2_srcNodeID_dstNodeID_R, where R = PR(srcNode)/Degree(srcNode)
				float R = pageRank / degree;
				String boundaryCondition = "" + Ref.typBC +"_"+ parts[0] +"_"+ parts[1] +"_"+ R;
				Text BCTypeVal = new Text(boundaryCondition);
				context.write(dstNodeKey, BCTypeVal);
			}
		}
		
		
		
	}

}
