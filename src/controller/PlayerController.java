package controller;

import model.Song;
import model.CustomMP3Player;
import javazoom.jl.decoder.JavaLayerException;

public class PlayerController {
    private CustomMP3Player currentPlayer;
    private Thread playerThread;
    private volatile boolean isPlaying;
    private volatile boolean isPaused;
    private Song currentSong;
    private volatile int pausedFrame;
    private volatile boolean stopRequested;

    public void handlePlayButton(Song song) {
        try {
            if (currentPlayer != null) {
                if (isPaused && song == currentSong) {
                    // Resume playback from paused position
                    resumeSong();
                } else {
                    // Play new song
                    playSong(song);
                }
            } else {
                // First time playing
                playSong(song);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playSong(Song song) throws Exception {
        if (song == null) {
            throw new IllegalArgumentException("Song cannot be null");
        }
        
        stopPlayback();
        currentSong = song;
        pausedFrame = 0;
        currentPlayer = song.getSongPlayer();
        if (currentPlayer == null) {
            throw new IllegalStateException("Failed to create player for song");
        }
        startPlayback();
    }

    private void resumeSong() throws Exception {
        if (currentSong == null) return;
        
        // Create new player instance
        currentPlayer = currentSong.getSongPlayer();
        
        // Skip to the saved position
        if (pausedFrame > 0) {
            currentPlayer.skipToFrame(pausedFrame);
        }
        
        // Start playback
        startPlayback();
    }

    private void startPlayback() {
        if (currentPlayer == null) return;
        
        stopRequested = false;
        playerThread = new Thread(() -> {
            try {
                isPlaying = true;
                isPaused = false;
                currentPlayer.play();
            } catch (JavaLayerException e) {
                e.printStackTrace();
            } finally {
                isPlaying = false;
                isPaused = false;
            }
        });
        playerThread.start();
    }

    public void pausePlayback() {
        if (currentPlayer != null && isPlaying) {
            try {
                // Store current position before stopping
                pausedFrame = currentPlayer.getCurrentFrame();
                
                // Stop playback
                stopRequested = true;
                currentPlayer.close();
                
                // Wait for player thread to finish
                if (playerThread != null) {
                    playerThread.join(100);  // Wait up to 100ms for thread to end
                    playerThread = null;
                }
                
                // Update state
                isPlaying = false;
                isPaused = true;
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void stopPlayback() {
        if (currentPlayer != null) {
            stopRequested = true;
            currentPlayer.close();
            
            if (playerThread != null) {
                playerThread.interrupt();
                try {
                    playerThread.join(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                playerThread = null;
            }
            
            currentPlayer = null;
            isPlaying = false;
            isPaused = false;
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void shutdown() {
        try {
            stopPlayback();
            if (playerThread != null) {
                stopRequested = true;
                playerThread.interrupt();
                try {
                    playerThread.join(1000); // Wait up to 1 second for thread to finish
                } catch (InterruptedException e) {
                    // Ignore interruption
                }
            }
            if (currentPlayer != null) {
                currentPlayer.close();
                currentPlayer = null;
            }
            isPlaying = false;
            isPaused = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
