package model;

import java.util.ArrayList;

public class Playlist {
	private ArrayList<Song> playList;
	private int playlist_id;
	
	public void savePlayList(Playlist playList) {
		//file saver for this class
	}
	
	public void loadPlaylist(int playlist_id) {
		//file reader for loading the playlist
	}
	
	//default constructor 
	public Playlist(ArrayList<Song> playList, int playlist_id) {
		super();
		this.playList = playList;
		this.playlist_id = playlist_id;
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


	
	
}
