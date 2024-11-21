package model;

import java.util.ArrayList;

public class Artist {
	private String name;
	private int artist_id;
	private ArrayList<Integer> song_ids;
	
	
	
	//default constructor
	public Artist(String name, ArrayList<Integer> song_ids) {
		super();
		this.name = name;
		this.song_ids = song_ids;
	}
	
	//getter and setters
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<Integer> getSong_ids() {
		return song_ids;
	}
	public void setSong_ids(ArrayList<Integer> song_ids) {
		this.song_ids = song_ids;
	}
	
	
}
