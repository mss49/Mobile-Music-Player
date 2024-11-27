package model;

import java.util.ArrayList;

public class Artist {
	private String name;
	//implement in CSV text file with 
	private int artist_id;
	private ArrayList<Integer> song_ids;
	
	
	
	//default constructor
	public Artist(String name, ArrayList<Integer> song_ids, int artist_id) {
		super();
		this.name = name;
		this.song_ids = song_ids;
		this.artist_id = artist_id;
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
	public int getArtist_id() {
		return artist_id;
	}
	
	
}
