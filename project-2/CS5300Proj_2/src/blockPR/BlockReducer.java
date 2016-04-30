package blockPR;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import blockPR.Ref;

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
			//int blockID = Integer.parseInt(key.toString());
	   		while (values.iterator().hasNext()){
	   			Text value = values.iterator().next();
    			String line = value.toString().trim();
    			String[] parts = line.split("\\s+");
    			int lineType = Integer.parseInt(parts[0]);
    			switch(lineType){
    			case Ref.typNode:
    				
    				break;
    			case Ref.typBE:
    				
    				break;
    			case Ref.typBC:
    				
    				break;
    			}
    		}
		}
		
	}
	
	private void IterateBlockOnce(){
		
	}

}
