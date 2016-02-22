package com.db.hadoop.comp;

import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.db.hadoop.override.ChessMapValue;

import sun.util.logging.resources.logging;

public class ComputeChessScore {
	public static class ChessMapper extends Mapper<Object, Text, Text, ChessMapValue> {
		
		int one = 1;
		int zero = 0;
		
		
		private static final ChessMapValue cmvWhite = new ChessMapValue();
		private static final ChessMapValue cmvBlack = new ChessMapValue();
		private static final ChessMapValue cmvDraw = new ChessMapValue();
		
		private Text whitewinCount = new Text();
		private Text drawwinCount = new Text();
		private Text blackwinCount = new Text();
		
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			
			if(!isValidLine(value.toString())) return;
			cmvWhite.setTotalGame(one);
			cmvBlack.setTotalGame(one);
			cmvDraw.setTotalGame(one);

			whitewinCount.set("White");
			drawwinCount.set("Draw");
			blackwinCount.set("Black");
			String line=value.toString();

			if (whoWon(line) == 0) {
				cmvWhite.setWinCount(one);
				cmvDraw.setWinCount(zero);
				cmvBlack.setWinCount(zero);

			} else if (whoWon(line) == 1) {
				cmvDraw.setWinCount(one);
				cmvBlack.setWinCount(zero);
				cmvWhite.setWinCount(zero);

			} else {
				cmvBlack.setWinCount(one);
				cmvDraw.setWinCount(zero);
				cmvWhite.setWinCount(zero);
			}
			context.write(whitewinCount, cmvWhite);
			context.write(drawwinCount, cmvDraw);
			context.write(blackwinCount, cmvBlack);
		}
		
		public static boolean isValidLine(String line){
			if(line==null || line.length()==0)
				return false;
			if(line.contains("Result \""))
				return true;
			else return false;
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
	
	public static class ChessCombiner extends Reducer<Text, ChessMapValue, Text, ChessMapValue> {
		private ChessMapValue combinerResult = new ChessMapValue();
		
		public void reduce(Text key, Iterable<ChessMapValue> values, Context context)
				throws IOException, InterruptedException {
			int sumWinCount = 0;
			int sumTotalCount = 0;
			for (ChessMapValue val : values) {
				sumWinCount += val.getWinCount();
				sumTotalCount += val.getTotalGame();
			}
			combinerResult.setWinCount(sumWinCount);
			combinerResult.setTotalGame(sumTotalCount);
			context.write(key, combinerResult);
		}
	}

	public static class ChessReducer extends Reducer<Text, ChessMapValue, Text, Text> {
		private Text result = new Text();

		public void reduce(Text key, Iterable<ChessMapValue> values, Context context)
				throws IOException, InterruptedException {
			float totalNumberOfGame = 0;
			int totalNumberResult = 0;
			for (ChessMapValue val : values) {
				totalNumberResult = totalNumberResult + val.getWinCount();
				totalNumberOfGame = totalNumberOfGame + val.getTotalGame();
			}
			float winPercentage = totalNumberResult / totalNumberOfGame;
			Text reduceResult = new Text(totalNumberResult + " " + winPercentage);
			result.set(reduceResult);
			context.write(key, result);
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "ComputeChessScore");
		job.setJarByClass(ComputeChessScore.class);
		job.setNumReduceTasks(1);
		job.setMapperClass(ChessMapper.class);
		job.setCombinerClass(ChessCombiner.class);
		job.setReducerClass(ChessReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(ChessMapValue.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
