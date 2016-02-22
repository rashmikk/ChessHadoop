package com.db.hadoop.comp;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.Counters;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobID;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.task.JobContextImpl;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.db.hadoop.comp.MoveFrequency.COUNTERS_TOTAL;


public class MoveFrequency extends Configured implements Tool {
	
	static Configuration cf;
	
	public enum COUNTERS_TOTAL {
		totalcount
	}
	
	static  double sum_frequency;
	
	@Override
	public int run(String[] args) throws Exception {
		// TODO Auto-generated method stub
		cf = new Configuration();
		Job job = Job.getInstance(cf,"job1");
		job.setJarByClass(MoveFrequency.class);
		job.setNumReduceTasks(1);
		job.setMapperClass(ChessMapper.class);
		job.setReducerClass(ChessReducer.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(IntWritable.class);
		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		//System.exit(job.waitForCompletion(true) ? 0 : 1);
		 job.waitForCompletion(true);
		 
		 org.apache.hadoop.mapreduce.Counters counters = job.getCounters();
		 org.apache.hadoop.mapreduce.Counter c1 = counters.findCounter(COUNTERS_TOTAL.totalcount);
		 System.out.println(c1.getDisplayName()+":"+c1.getValue());
		 sum_frequency=c1.getValue();
		 System.out.println("customCounterCount==> " + sum_frequency);
		 
		 Job job2 = Job.getInstance(cf,"job2");
		job2.setJarByClass(MoveFrequency.class);
		job2.setNumReduceTasks(1);
//		job2.setJobID(job.getJobID());
		job2.setMapperClass(FrequencyMapper.class);
		job2.setReducerClass(FrequencyReducer.class);
		job2.setOutputKeyClass(IntWritable.class);
		job2.setOutputValueClass(Text.class);
		job2.setMapOutputKeyClass(DoubleWritable.class);
		job2.setMapOutputValueClass(IntWritable.class);
		job2.setSortComparatorClass(LongWritable.DecreasingComparator.class);
		//job2.setInputFormatClass(TextInputFormat.class);
		FileInputFormat.addInputPath(job2, new Path(args[1]));
		FileOutputFormat.setOutputPath(job2, new Path(args[2]));
		System.out.println("job2:sum==> " + sum_frequency);
		return job2.waitForCompletion(true) ? 0 : 1;	
		}
	
	public static void main(String args[]) throws Exception {
		int res = ToolRunner.run(cf, new MoveFrequency(), args);
		System.exit(res);
	}
}
	
	class ChessMapper extends Mapper<Object, Text, IntWritable, IntWritable> {

		private static final String OUTPUT_PATH = "intermediate_output";
		private  final IntWritable one = new IntWritable(1);
		private IntWritable noOfMoves = new IntWritable();
		private IntWritable total_key = new IntWritable(-1);

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			if(!isValidLine(value.toString())) return;
			String line=value.toString();
			
			String plyCountStr = getValue(line);
			
			if(plyCountStr==null || plyCountStr.length()==0)
				return;
			int plyCount=0;
			try{
				plyCount=new Integer(plyCountStr).intValue(); 
			}
			catch(Exception e){
				return;
			}
			
			
			noOfMoves.set(plyCount);
			context.write(noOfMoves, one);
			context.write(total_key, one);
			context.getCounter(COUNTERS_TOTAL.totalcount).increment(1L);
		}
		
		public static boolean isValidLine(String line){
			if(line==null || line.length()==0)
				return false;
			if(line.contains("PlyCount \""))
				return true;
			else return false;
		}
		
		public String getValue(String line){
		 StringTokenizer itr = new StringTokenizer(line);
		 String key=itr.nextToken().substring(1);
		 String initialVal=itr.nextToken();
		 String val=initialVal.substring(1,initialVal.length()-2);
		 return val;
	}
		
		public int whoWon(String line){
			
			if(line.contains("1-0"))
				return 0;
			else if(line.contains("0-1"))
				return 2;
			
			else 
				return 1;
		}
	}

	class ChessReducer extends Reducer<IntWritable, IntWritable, IntWritable, IntWritable> {
		private IntWritable result = new IntWritable();
		

		public void reduce(IntWritable key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			result.set(sum);
			context.write(key, result);
		}
	}

	class FrequencyMapper extends Mapper<Object, Text, DoubleWritable, IntWritable> {

		private  final IntWritable one = new IntWritable(1);
		private IntWritable noOfMoves = new IntWritable();
		
		private String jobID;
		private double sumofOccurence=0;
		
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			//org.apache.hadoop.mapreduce.Counter counter = context.getCounter(COUNTERS.totalcount);

			StringTokenizer itr = new StringTokenizer(value.toString());
			
			int plyCount = new Integer(itr.nextToken()).intValue();
			double cnt = new Double(itr.nextToken()).doubleValue();
			if(plyCount==-1){
				sumofOccurence=cnt;
				return;
			}
			
			double frequency = (cnt*100) / sumofOccurence;
			DoubleWritable dw = new DoubleWritable(frequency);
			noOfMoves.set(plyCount);
			context.write(dw, noOfMoves);
		}
	}

    class FrequencyReducer extends Reducer<DoubleWritable, IntWritable, IntWritable, Text> {
		private Text result = new Text();
		

		
		public void reduce(DoubleWritable key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {

			int noOfmove = 0;
			for (IntWritable val : values) {
				noOfmove = val.get();
				IntWritable keyMove=new IntWritable(noOfmove);
				result.set(key+"%");
				context.write(keyMove, result);
			}
			
		}
	}



