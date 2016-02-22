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

import com.db.hadoop.override.PlayerMapValue;

public class ComputePlayerStats {
	public static class ChessMapper extends Mapper<Object, Text, Text, PlayerMapValue> {

		int one = 1;
		int zero = 0;
		private static final PlayerMapValue pmvWhite = new PlayerMapValue();
		private static final PlayerMapValue pmvBlack = new PlayerMapValue();

		private Text whitePlayer = new Text();
		private Text blackPlayer = new Text();

		boolean completeGame = false;

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

			if (!isValidLine(value.toString()))
				return;

			pmvWhite.setGamesPlayed(one); // setting attribute gamesplayed as 1
			pmvBlack.setGamesPlayed(one);
			String line = value.toString();

			if (line.contains("White \"") && !completeGame) {
				whitePlayer.set(getValue(line) + " White");
				completeGame = true;
				return;
			}
			if (!completeGame)
				return;
			if (line.contains("Black \"")) {
				blackPlayer.set(getValue(line) + " Black");
				return;
			}
			// setting key as <playerid White>
			// blackPlayer.set(game.getBlack() + " Black"); // setting key as
			// <playerid Black>

			if (whoWon(line) == 0) {
				pmvWhite.setGamesWon(one);
				pmvWhite.setGamesLost(zero);
				pmvWhite.setGamesDrew(zero);

				pmvBlack.setGamesWon(zero);
				pmvBlack.setGamesLost(one);
				pmvBlack.setGamesDrew(zero);
			} else if (whoWon(line) == 1) {
				pmvWhite.setGamesWon(zero);
				pmvWhite.setGamesLost(zero);
				pmvWhite.setGamesDrew(one);

				pmvBlack.setGamesWon(zero);
				pmvBlack.setGamesLost(zero);
				pmvBlack.setGamesDrew(one);
			} else {
				pmvWhite.setGamesWon(zero);
				pmvWhite.setGamesLost(one);
				pmvWhite.setGamesDrew(zero);

				pmvBlack.setGamesWon(one);
				pmvBlack.setGamesLost(zero);
				pmvBlack.setGamesDrew(zero);
			}
			context.write(whitePlayer, pmvWhite);
			context.write(blackPlayer, pmvBlack);
			completeGame = false;
		}

		public static boolean isValidLine(String line) {
			if (line == null || line.length() == 0)
				return false;
			if (line.contains("White \"") || line.contains("Black \"") || line.contains("Result \""))
				return true;
			else
				return false;
		}

		public String getValue(String line) {
			StringTokenizer itr = new StringTokenizer(line);
			String key = itr.nextToken().substring(1);
			String initialVal = itr.nextToken();
			String val = initialVal.substring(1, initialVal.length() - 2);
			return val;
		}

		public int whoWon(String line) {

			if (line.contains("1-0"))
				return 0;
			else if (line.contains("0-1"))
				return 2;

			else
				return 1;
		}
	}

	public static class ChessReducer extends Reducer<Text, PlayerMapValue, Text, Text> {
		private Text result = new Text();

		public void reduce(Text key, Iterable<PlayerMapValue> values, Context context)
				throws IOException, InterruptedException {
			float totalGamesPlayed = 0;
			float totalWon = 0;
			float totalLost = 0;
			float totalDrew = 0;
			for (PlayerMapValue val : values) {
				totalGamesPlayed += val.getGamesPlayed();
				totalWon += val.getGamesWon();
				totalLost += val.getGamesLost();
				totalDrew += val.getGamesDrew();
			}
			float winPercentage = totalWon / totalGamesPlayed;
			float lossPercentage = totalLost / totalGamesPlayed;
			float drawPercentage = totalDrew / totalGamesPlayed;
			Text reduceResult = new Text(winPercentage + " " + lossPercentage + " " + drawPercentage);
			result.set(reduceResult);
			context.write(key, result);
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "ComputePlayerStats");
		job.setJarByClass(ComputePlayerStats.class);
		job.setNumReduceTasks(1);
		job.setMapperClass(ChessMapper.class);
		job.setReducerClass(ChessReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(PlayerMapValue.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}

}
