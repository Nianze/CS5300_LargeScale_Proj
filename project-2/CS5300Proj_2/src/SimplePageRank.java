import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;

public class SimplePageRank 
{
	public static final int NUM_NODES = 5;
	public static final int NUM_MR_PASSES = 5;
	public static final float INITIAL_PR = (1.0f/NUM_NODES);
	public static final float DAMPING_FACTOR = 0.85f;
	public static final int NUM_BLOCK = 68;
	public static int count = 0;	
 	
	public static enum HADOOP_COUNTERS
	{
		TOTAL_RESIDUAL_ERROR,
		NUM_DATA_POINTS
	};
	
	public static class EdgeInfo
	{
		public Integer srcID;
		public Integer destID;
		public Float srcPR;
		public Integer srcNumOutLinks;
		
		public EdgeInfo(Integer srcID, Integer destID, Float srcPR, Integer srcNumOutLinks)
		{
			this.srcID = srcID;
			this.destID = destID;
			this.srcPR = srcPR;
			this.srcNumOutLinks = srcNumOutLinks;
		}
		
		public EdgeInfo(String line)
		{
			line = line.trim();
			String[] parts = line.split(" ");
			this.srcID = Integer.parseInt(parts[0]);
			this.destID = Integer.parseInt(parts[1]);
			this.srcPR = Float.parseFloat(parts[2]);
			this.srcNumOutLinks = Integer.parseInt(parts[3]);
		}
		
		@Override
		public String toString()
		{
			return srcID + " " + destID + " " + srcPR + " " + srcNumOutLinks;
		}
	}
	
	// In the map function if we see a particular node appear in srcNodeID or destNodeID we emit an entry associated with that nodeID key
	// Note that this implementation will generate 2 time the amount of intermediate entries
	public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, Text> 
	{
		@Override
		public void map(LongWritable key, Text value, OutputCollector<IntWritable, Text> output, Reporter reporter) throws IOException 
		{
			String line = value.toString().trim();
			String[] parts = line.split("\\s+");
			
			// error checking for an extra newline character at the end
			if(line.equalsIgnoreCase(""))
				return;
			
			Integer srcNodeID = Integer.parseInt(parts[0]);
			Integer destNodeID = Integer.parseInt(parts[1]);
				
			IntWritable srcIDKey = new IntWritable(srcNodeID);
			IntWritable destIDKey = new IntWritable(destNodeID);
			
			if(destIDKey.get() != -1)
			{
				output.collect(srcIDKey, value);
				output.collect(destIDKey, value);
			}
			else // ignore the special entry we created
			{
				output.collect(srcIDKey, value);
			}
		}
	}
	  
	public static class Reduce extends MapReduceBase implements Reducer<IntWritable, Text, IntWritable, Text> 
	{
		@Override
		public void reduce(IntWritable key, Iterator<Text> values, OutputCollector<IntWritable, Text> output, Reporter reporter) throws IOException 
		{
			// We first differentiate the intermediate values between A->B (out_link) and B->A (in_link)
			// in_link is used to calculate PR(A)
			// out_link is used to calculate the PR contribution of A to other nodes it points to
			Integer nodeID = key.get();
			ArrayList<Text> in_link = new ArrayList<Text>();
			ArrayList<Text> out_link = new ArrayList<Text>();
	   		while (values.hasNext())
	   		{
	   			Text value = values.next();
    			String line = value.toString().trim();
    			String[] parts = line.split("\\s+");
    			
    			if(Integer.parseInt(parts[0]) == nodeID)
    			{
    				Text t = new Text(value.toString());
    				out_link.add(t);
    			}
    			else
    			{
    				Text t = new Text(value.toString());
    				in_link.add(t);
    			}
    		}
	   		
	   		// compute PageRank logic
	   		// Eventual output format: "srcNodeID destNodeID srcNodePR srcNumOutLinks"
	   		// Note we only emit entries in the out_link ArrayList and get rid of duplication
	   		if(count == 0)
	   		{
	   			// first run, need to setup srcNumOutLinks, initial PR and proper output format
	   			int srcNumOutLinks = out_link.size();
	   			
	   			if(srcNumOutLinks == 0) // create a special entry for case where a node has no out links
	   			{
   					EdgeInfo obj = new EdgeInfo(key.get(), -1, INITIAL_PR, -1);
   					Text outValue = new Text(obj.toString());
   					output.collect(null, outValue);
	   			}
	   			else
	   			{
	   				String line = "";
	   				for(Text t : out_link)
	   				{
	   					line = t.toString().trim();
	   					String[] parts = line.split("\\s+");
	   					Integer srcNodeID = Integer.parseInt(parts[0]);
	   					Integer destNodeID = Integer.parseInt(parts[1]);
	   					EdgeInfo obj = new EdgeInfo(srcNodeID, destNodeID, INITIAL_PR, srcNumOutLinks);
	   					Text outValue = new Text(obj.toString());
	   					output.collect(null, outValue);
	   				}
	   			}
	   		}
	   		else
	   		{
	   			// calculate new PR of the node based on the PR of in_link nodes
	   			String line = "";
	   			Float inSumPR = 0.0f;
	   			for(Text t :  in_link)
	   			{
	   				line = t.toString().trim();
	   				String[] parts = line.split("\\s+");
	   				Float srcPR = Float.parseFloat(parts[2]);
	   				Integer srcNumOutLinks = Integer.parseInt(parts[3]);
	   				inSumPR += srcPR/srcNumOutLinks;
	   			}
	   			
	   			String temp = out_link.get(0).toString().trim();
	   			String[] temp_parts = temp.split("\\s+");
	   			Float new_PR = ((1-DAMPING_FACTOR)/NUM_NODES) + DAMPING_FACTOR*(inSumPR);
	   			Float old_PR = Float.parseFloat(temp_parts[2]);
	   			Float residualError = Math.abs(old_PR-new_PR)/new_PR;
	   			residualError *= 1000000;
	   			
	   			reporter.incrCounter(HADOOP_COUNTERS.TOTAL_RESIDUAL_ERROR, residualError.longValue());
	   			reporter.incrCounter(HADOOP_COUNTERS.NUM_DATA_POINTS, 1);
	   			
	   			for(Text t :  out_link)
	   			{
	   				line = t.toString().trim();
	   				String[] parts = line.split("\\s+");
	   				EdgeInfo obj = new EdgeInfo(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), new_PR, Integer.parseInt(parts[3]));
	   				Text text = new Text(obj.toString());
	   				output.collect(null, text);
	   			}
	   		}
		}
	}
	  
	public static void main (String[] args) throws Exception
	{
		// filter a subset from edges.txt (filter parameters for netID ah935)
		try
		{
			// setup writing output file resources
			/*File file = new File("/home/nanandy/Desktop/CS5300/output.txt");
			if (!file.exists()) file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			// setup reading input file resources
			String line = "";
			BufferedReader br = new BufferedReader(new FileReader("/home/nanandy/Desktop/CS5300/edges.txt"));
			while((line = br.readLine()) != null)
			{
				String newLine = line.trim();
				String[] parts = newLine.split("\\s+");
				if (selectInputLine(Double.parseDouble(parts[2])))
				{
					bw.write(line+"\n");
				}
			}
			
			bw.close();
			br.close();*/
			
			// Note: pass 0 sets up the initial input file in a certain format + initialize some info
			// So there is no calculation or output for pass 0
			while(count <= NUM_MR_PASSES)
			{
				// setup hadoop config stuff
				JobConf conf = new JobConf(SimplePageRank.class);
				conf.setJobName("SimplePageRank");
				conf.setOutputKeyClass(IntWritable.class);
				conf.setOutputValueClass(Text.class);
				conf.setMapperClass(Map.class);
				conf.setReducerClass(Reduce.class);
				conf.setInputFormat(TextInputFormat.class);
				conf.setOutputFormat(TextOutputFormat.class);
			
				if (count == 0)
					FileInputFormat.setInputPaths(conf, new Path("/home/parallels/Desktop/CS5300/temp.txt"));
				else
					FileInputFormat.setInputPaths(conf, new Path("/home/parallels/Desktop/CS5300/pass"+count));
				
				FileOutputFormat.setOutputPath(conf, new Path("/home/parallels/Desktop/CS5300/pass"+(count+1)));
				RunningJob rj = JobClient.runJob(conf);

				if (count != 0)
				{
					// get counter values and display residual error for this pass
					Counters counter = rj.getCounters();
					Long num_data_points = counter.getCounter(HADOOP_COUNTERS.NUM_DATA_POINTS);
					Long residual_error = counter.getCounter(HADOOP_COUNTERS.TOTAL_RESIDUAL_ERROR);
					Float total_residual_error = residual_error/1000000.0f;
					Float average_residual_error = total_residual_error/num_data_points;
		    
					System.out.println("Result from pass " + count);
					System.out.println("Total Residual Error: " + total_residual_error);
					System.out.println("Average Residual Error: " + average_residual_error);
					System.out.println();
				}
				
				count++;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static boolean selectInputLine(double x) 
	{
		double fromNetID = 0.539;
		double rejectMin = 0.9 * fromNetID;
		double rejectLimit = rejectMin + 0.01;
		return ( ((x >= rejectMin) || (x < rejectLimit)) ? false : true );
	}

}

	


