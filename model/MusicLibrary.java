package model;

import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;

public class MusicLibrary {
    private Map<Integer, Song> songs;
    private Map<Integer, Artist> artists;
    private Map<Integer, Playlist> playlists;
    private static MusicLibrary instance;
    
    private MusicLibrary() {
        songs = new HashMap<>();
        artists = new HashMap<>();
        playlists = new HashMap<>();
    }
    
    public static MusicLibrary getInstance() {
        if (instance == null) {
            instance = new MusicLibrary();
        }
        return instance;
    }
    
    public void addSong(Song song) {
        songs.put(song.getSongId(), song);
        saveLibrary();
    }
    
    public void addArtist(Artist artist) {
        artists.put(artist.getArtist_id(), artist);
        saveLibrary();
    }
    
    public void addPlaylist(Playlist playlist) {
        playlists.put(playlist.getPlaylist_id(), playlist);
        saveLibrary();
    }
    
    public Song getSong(int songId) {
        return songs.get(songId);
    }
    
    public Artist getArtist(int artistId) {
        return artists.get(artistId);
    }
    
    public Playlist getPlaylist(int playlistId) {
        return playlists.get(playlistId);
    }
    
    public void loadLibrary() {
        try (BufferedReader reader = new BufferedReader(new FileReader("data/songs.csv"))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                
                int songId = Integer.parseInt(parts[0]);
                String title = parts[1].replace("\"", "");
                String artistId = parts[2].replace("\"", "");
                Date releaseDate = new SimpleDateFormat("yyyy-MM-dd").parse(parts[3].replace("\"", ""));
                boolean isFavorite = Boolean.parseBoolean(parts[4]);
                String mp3Path = parts[5].replace("\"", "");
                
                // Create and add song to library
                Song song = new Song(songId, title, new ArrayList<>(), releaseDate, isFavorite, mp3Path);
                songs.put(songId, song);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error reading CSV: " + e.getMessage());
        }
    }
    
    public void saveLibrary() {
        // TODO: Implement saving all data to CSV files
    }
    
    public void removeSong(int songId) {
        songs.remove(songId);
        saveLibrary();
    }
    
    public void removeArtist(int artistId) {
        artists.remove(artistId);
        saveLibrary();
    }
    
    public void removePlaylist(int playlistId) {
        playlists.remove(playlistId);
        saveLibrary();
    }
    
    public Collection<Song> getAllSongs() {
        return songs.values();
    }
} 