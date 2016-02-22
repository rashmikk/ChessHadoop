package com.db.hadoop.override;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;

public class ChessMapValue implements Writable {


	private int winCount;
	private int totalGame;
	/*private final static IntWritable one = new IntWritable(1);
	private final static IntWritable zero = new IntWritable(0);*/
	
	public ChessMapValue() {
		this.winCount = 0;
		this.totalGame = 0;
	}


	@Override
	public String toString() {
		return "ChessMapValue [winCount=" + winCount + ", totalGame=" + totalGame + "]";
	}


	public int getWinCount() {
		return winCount;
	}


	public void setWinCount(int winCount) {
		this.winCount = winCount;
	}


	public int getTotalGame() {
		return totalGame;
	}


	public void setTotalGame(int totalGame) {
		this.totalGame = totalGame;
	}


	@Override
	public void readFields(DataInput in) throws IOException {
		// TODO Auto-generated method stub
		winCount = in.readInt();
		totalGame = in.readInt();
	}


	@Override
	public void write(DataOutput out) throws IOException {
		// TODO Auto-generated method stub
		out.writeInt(winCount);
		out.writeInt(totalGame);
	}
	
	
	
}