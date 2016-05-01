package blockPR;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.Counters;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import blockPR.BlockMapper;
import blockPR.BlockReducer;

public class Driver {

	public static void main(String[] args) throws Exception {				
		Float average_residual_error = 1.0f;
		Ref.initPassNum();
		
		while(average_residual_error > Ref.THRESHOLD && Ref.PASS_NUM < 15){
			Configuration conf = new Configuration();
			Job job = Job.getInstance(conf, "BlockPageRank");
			job.setJarByClass(blockPR.Driver.class);
			job.setMapperClass(BlockMapper.class);
			job.setReducerClass(BlockReducer.class);
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(Text.class);
			
			if (Ref.PASS_NUM == 0) FileInputFormat.setInputPaths(job, new Path("/home/parallels/Desktop/CS5300/edges.txt"));
			else FileInputFormat.setInputPaths(job, new Path("/home/parallels/Desktop/CS5300/pass"+Ref.PASS_NUM));			
			FileOutputFormat.setOutputPath(job, new Path("/home/parallels/Desktop/CS5300/pass"+(Ref.PASS_NUM+1)));
			
			job.waitForCompletion(true);
			
			if (Ref.PASS_NUM != 0){
				// get counter values and display residual error for this pass
				org.apache.hadoop.mapreduce.Counters counters = job.getCounters();
				Long residual_error = counters.findCounter(PRCounter.TOTAL_RESIDUAL_ERROR).getValue();
				Float total_residual_error = residual_error/1000000.0f;
				average_residual_error = total_residual_error/Ref.NUM_NODES;
				
				System.out.println("Result in current pass " + (Ref.PASS_NUM+1));
				System.out.println("Total Residual Error: " + total_residual_error);
				System.out.println("Average Residual Error: " + average_residual_error);
				System.out.println("=========One Pass over=========");
			}
			Ref.incPassNum();
		}
	}

}
