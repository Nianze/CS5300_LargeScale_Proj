package randomBlockPR;

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

import randomBlockPR.BlockMapper;
import randomBlockPR.BlockReducer;

public class Driver {

	public static void main(String[] args) throws Exception {	
		if(args.length != 2){
			System.out.println("Please run the program with 2 arguments: s3n://input_folder/file_name s3n://output_folder/file_name");
			System.out.println("For example: s3n://edu-cornell-cs-cs5300s16-nl443-mapreduce/input/edges.txt s3n://edu-cornell-cs-cs5300s16-nl443-mapreduce/output/pass");
			return;
		}
		
		String input = args[0];
		String output = args[1];
		
		Float average_residual_error = 1.0f;
		Ref.initPassNum();
		
		while(average_residual_error > Ref.THRESHOLD && Ref.PASS_NUM < 9){
			Configuration conf = new Configuration();
			Job job = Job.getInstance(conf, "BlockPageRank");
			job.setJarByClass(blockPR.Driver.class);
			job.setMapperClass(BlockMapper.class);
			job.setReducerClass(BlockReducer.class);
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(Text.class);
			
			if (Ref.PASS_NUM == 0) FileInputFormat.setInputPaths(job, new Path(input));
			else FileInputFormat.setInputPaths(job, new Path(output+Ref.PASS_NUM));			
			FileOutputFormat.setOutputPath(job, new Path(output+(Ref.PASS_NUM+1)));

			job.waitForCompletion(true);
			
			if (Ref.PASS_NUM != 0){
				// get counter values and display residual error for this pass
				org.apache.hadoop.mapreduce.Counters counters = job.getCounters();
				Long residual_error = counters.findCounter(PRCounter.TOTAL_RESIDUAL_ERROR).getValue();
				Float total_residual_error = residual_error/1000000.0f;
				average_residual_error = total_residual_error/Ref.NUM_NODES;
				
				System.out.println("Result from current pass " + Ref.PASS_NUM);
				System.out.println("Total Residual Error: " + total_residual_error);
				System.out.println("Average Residual Error: " + average_residual_error);
				System.out.println("=========Pass "+Ref.PASS_NUM+" over=========");
			}
			Ref.incPassNum();
		}
		
		int numPass = Ref.PASS_NUM - 1;
		System.out.println("=========The end of MapReduce=========");
		System.out.println("Final Average Residual Error: " + average_residual_error);
		System.out.println("Total Pass Number: "+(Ref.PASS_NUM-1));
		System.out.println("The average number of iterations per Block: ");
		for(int i = 0; i < Ref.NUM_BLOCK; i++){
			Float avg = (float)Ref.AVG_BLOCK_ITER[i] / numPass;
			System.out.println("<Block "+ i + "> The average number of iterations: " + avg.toString());
		}
		
	}

}
