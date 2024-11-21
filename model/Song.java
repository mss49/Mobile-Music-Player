package model;

import java.util.Date;
import java.util.List;

public class Song {
	private int song_Id;
	private String songTitle;
	private List<Artist> artistList;
	private Date releaseDate;
	private boolean isfavourite;
	
	
	//default constructor
	public Song() {
		super();
	}

	//constructor
	public Song(int song_Id, String songTitle, List<Artist> artistList, Date releaseDate, boolean isfavourite) {
		super();
		this.song_Id = song_Id;
		this.songTitle = songTitle;
		this.artistList = artistList;
		this.releaseDate = releaseDate;
		this.isfavourite = isfavourite;
	}
	
	//getter and setters
	public int getSong_Id() {
		return song_Id;
	}
	public void setSong_Id(int song_Id) {
		this.song_Id = song_Id;
	}
	public String getSongTitle() {
		return songTitle;
	}
	public void setSongTitle(String songTitle) {
		this.songTitle = songTitle;
	}
	public List<Artist> getArtistList() {
		return artistList;
	}
	public void setArtistList(List<Artist> artistList) {
		this.artistList = artistList;
	}
	public Date getReleaseDate() {
		return releaseDate;
	}
	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}
	public boolean isIsfavourite() {
		return isfavourite;
	}
	public void setIsfavourite(boolean isfavourite) {
		this.isfavourite = isfavourite;
	}
	
	
}
