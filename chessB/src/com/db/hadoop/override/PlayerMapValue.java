package com.db.hadoop.override;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;

public class PlayerMapValue implements Writable {

	private int gamesPlayed;
	private int gamesWon;
	private int gamesLost;
	private int gamesDrew;
	
	public PlayerMapValue() {
		this.gamesPlayed = 0;
		this.gamesWon = 0;
		this.gamesLost = 0;
		this.gamesDrew = 0;
	}

	public int getGamesPlayed() {
		return gamesPlayed;
	}

	public void setGamesPlayed(int gamesPlayed) {
		this.gamesPlayed = gamesPlayed;
	}

	public int getGamesWon() {
		return gamesWon;
	}

	public void setGamesWon(int gamesWon) {
		this.gamesWon = gamesWon;
	}

	public int getGamesLost() {
		return gamesLost;
	}

	public void setGamesLost(int gamesLost) {
		this.gamesLost = gamesLost;
	}

	public int getGamesDrew() {
		return gamesDrew;
	}

	public void setGamesDrew(int gamesDrew) {
		this.gamesDrew = gamesDrew;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		// TODO Auto-generated method stub
		gamesPlayed = in.readInt();
		gamesWon = in.readInt();
		gamesLost = in.readInt();
		gamesDrew = in.readInt();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		// TODO Auto-generated method stub
		out.writeInt(gamesPlayed);
		out.writeInt(gamesWon);
		out.writeInt(gamesLost);
		out.writeInt(gamesDrew);
	}
	
	
	
}