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
        loadSongs();
        loadPlaylists();
    }
    
    private void loadSongs() {
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
                
                Song song = new Song(songId, title, new ArrayList<>(), releaseDate, isFavorite, mp3Path);
                songs.put(songId, song);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error reading songs CSV: " + e.getMessage());
        }
    }
    
    private void loadPlaylists() {
        try (BufferedReader reader = new BufferedReader(new FileReader("data/playlists.csv"))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                
                int playlistId = Integer.parseInt(parts[0]);
                String name = parts[1].replace("\"", "");
                String songIdsStr = parts[2].replace("\"", "");
                Date createdDate = new SimpleDateFormat("yyyy-MM-dd").parse(parts[3].replace("\"", ""));
                
                ArrayList<Song> playlistSongs = new ArrayList<>();
                if (!songIdsStr.isEmpty()) {
                    for (String songId : songIdsStr.split(";")) {
                        Song song = songs.get(Integer.parseInt(songId));
                        if (song != null) {
                            playlistSongs.add(song);
                        }
                    }
                }
                
                Playlist playlist = new Playlist(playlistSongs, playlistId);
                playlist.setName(name);
                playlists.put(playlistId, playlist);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error reading playlists CSV: " + e.getMessage());
        }
    }
    
    public void saveLibrary() {
        savePlaylists();
        // TODO: Implement saving songs and artists
    }
    
    private void savePlaylists() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("data/playlists.csv"))) {
            // Write header
            writer.println("playlistId,name,songIds,createdDate");
            
            // Write playlist data
            for (Playlist playlist : playlists.values()) {
                StringBuilder sb = new StringBuilder();
                sb.append(playlist.getPlaylist_id()).append(",");
                sb.append("\"").append(playlist.getName().replace("\"", "\"\"")).append("\",");
                sb.append("\"").append(String.join(";", playlist.getPlayList().stream().map(Song::getSongId).map(String::valueOf).toList())).append("\",");
                sb.append("\"").append(new SimpleDateFormat("yyyy-MM-dd").format(playlist.getCreatedDate())).append("\"");
                writer.println(sb.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error writing playlists to CSV: " + e.getMessage());
        }
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
    
    public int getNextPlaylistId() {
        if (playlists.isEmpty()) {
            return 1;
        }
        return Collections.max(playlists.keySet()) + 1;
    }
    
    public Map<Integer, Playlist> getPlaylists() {
        return playlists;
    }
} 