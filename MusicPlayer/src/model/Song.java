package model;

import java.io.FileInputStream;
import java.util.Date;
import java.util.List;

import javazoom.jl.player.Player;

public class Song {

	private String songTitle;
	private List<Artist> artistList;
	private Date releaseDate;
	private boolean isfavourite;
	String mp3FilePath;
	
	
	//returns a Player object which can be used by the controller to play a song
	//returns an error or null pointer if filepath is incorrect
	public Player getSongPlayer() {
		try(FileInputStream fis = new FileInputStream(this.mp3FilePath)){
			Player player = new Player(fis);
			return player;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//must save the song to CSV
	public void saveSongToCSV() {
		
	}

	//constructor
	public Song(String songTitle, List<Artist> artistList, Date releaseDate, boolean isfavourite, String mp3FilePath) {
		super();
		this.songTitle = songTitle;
		this.artistList = artistList;
		this.releaseDate = releaseDate;
		this.isfavourite = isfavourite;
		this.mp3FilePath = mp3FilePath;
	}

	//public getter and setters
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


	public String getMp3FilePath() {
		return mp3FilePath;
	}


	public void setMp3FilePath(String mp3FilePath) {
		this.mp3FilePath = mp3FilePath;
	}
	
	
	
}
