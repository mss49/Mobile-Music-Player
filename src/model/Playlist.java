package model;

import java.util.ArrayList;
import java.util.Date;

public class Playlist {
	private ArrayList<Song> playList;
	private int playlist_id;
	private String name;
	private Date createdDate;
	
	
	//default constructor 
	public Playlist(ArrayList<Song> playList, int playlist_id) {
		super();
		this.playList = playList;
		this.playlist_id = playlist_id;
		this.name = "Playlist " + playlist_id; // Default name
		this.createdDate = new Date(); // Sets creation date to current date
	}
	
	//getter and setters
	public ArrayList<Song> getPlayList() {
		return playList;
	}

	public void setPlayList(ArrayList<Song> playList) {
		this.playList = playList;
	}

	public int getPlaylist_id() {
		return playlist_id;
	}

	public void setPlaylist_id(int playlist_id) {
		this.playlist_id = playlist_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	@Override
	public String toString() {
		return name;
	}
	
	
}
