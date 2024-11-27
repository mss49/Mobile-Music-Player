package model;

import java.io.BufferedInputStream;
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
	private int songId;
	private FileInputStream fileInputStream;
	private BufferedInputStream bufferedInputStream;
	
	
	//returns a Player object which can be used by the controller to play a song
	//returns an error or null pointer if filepath is incorrect
	public Player getSongPlayer() throws Exception {
		if (fileInputStream != null) {
			try {
				fileInputStream.close();
			} catch (Exception e) {
				// Ignore closing errors
			}
		}
		
		fileInputStream = new FileInputStream(this.mp3FilePath);
		bufferedInputStream = new BufferedInputStream(fileInputStream);
		return new Player(bufferedInputStream);
	}
	
	//must save the song to CSV
	public void saveSongToCSV() {
		
	}

	//constructor
	public Song(int songId, String songTitle, List<Artist> artistList, Date releaseDate, boolean isfavourite, String mp3FilePath) {
		super();
		this.songId = songId;
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

	public int getSongId() {
		return songId;
	}

	public void setSongId(int songId) {
		this.songId = songId;
	}

	@Override
	public String toString() {
		return songTitle;
	}
	
	public void closeStreams() {
		try {
			if (bufferedInputStream != null) {
				bufferedInputStream.close();
			}
			if (fileInputStream != null) {
				fileInputStream.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			bufferedInputStream = null;
			fileInputStream = null;
		}
	}
	
}
