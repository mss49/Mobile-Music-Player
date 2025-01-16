package test.blackbox;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import javax.sound.sampled.*;
import src.main.java.musicplayer.MusicPlayer;
import src.main.java.musicplayer.Song;
import javax.swing.SwingUtilities;
import java.util.List;
import java.awt.event.ActionEvent;

public class SpecificationBasedTest {
    private MusicPlayer player;
    private final String TEST_RESOURCES = "src/test/resources/";
    private File testWavFile;
    private String originalCsvContent;
    
    @BeforeEach
    void setUp() {
        try {
            File csvFile = new File("songs.csv");
            if (csvFile.exists()) {
                originalCsvContent = new String(java.nio.file.Files.readAllBytes(csvFile.toPath()));
            }
        } catch (IOException e) {
            fail("Could not backup CSV file");
        }
        
        player = new MusicPlayer();
        // Create test resources directory
        new File(TEST_RESOURCES).mkdirs();
        
        // Create a valid test WAV file
        testWavFile = new File(TEST_RESOURCES + "test.wav");
        try {
            // Create basic WAV file
            AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
            byte[] data = new byte[44100]; // 1 second of silence
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            AudioInputStream ais = new AudioInputStream(bais, format, data.length);
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, testWavFile);
        } catch (IOException e) {
            fail("Could not create test audio file");
        }
    }
    
    @AfterEach
    void tearDown() {
        player.stop();
        testWavFile.delete();
        new File("settings.properties").delete();
        
        try {
            if (originalCsvContent != null) {
                java.nio.file.Files.write(new File("songs.csv").toPath(), 
                    originalCsvContent.getBytes());
            }
        } catch (IOException e) {
            fail("Could not restore CSV file");
        }
    }

    @Test
    @DisplayName("Test basic playback controls")
    void testBasicPlayback() {
        Song testSong = new Song("Test Song", testWavFile.getAbsolutePath());
        player.playSong(testSong);
        player.pause();
        player.resume();
        player.stop();
    }

    @Test
    @DisplayName("Test playing invalid file")
    void testInvalidFile() {
        Song invalidSong = new Song("Invalid Song", "nonexistent.wav");
        
        // Capture error output to verify error handling
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));
        
        player.playSong(invalidSong);
        
        // Restore original error stream
        System.setErr(originalErr);
        
        // Verify error was logged
        assertTrue(errContent.toString().contains("Error playing song"), 
            "Should log error message when playing invalid file");
        assertDoesNotThrow(() -> player.stop());
    }

    @Test
    @DisplayName("Test next and previous")
    void testNextPrevious() {
        player.playNextSong();
        player.playPreviousSong();
        // Should handle gracefully when no songs are loaded
    }

    @Test
    @DisplayName("Test loop toggle")
    void testLoopToggle() {
        player.toggleLoop();
        player.toggleLoop();
        // Should toggle loop state without errors
    }

    @Test
    @DisplayName("Test mute toggle")
    void testMuteToggle() {
        player.toggleMute();
        player.toggleMute();
        // Should toggle mute state without errors
    }

    @Test
    @DisplayName("Test search functionality")
    void testSearchSongs() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                // Load existing songs from CSV
                player.loadSongsFromCSV();
                
                // Test 1: Exact match
                player.searchSongs("Test Song 1");
                List<Song> results = player.getPlaylist();
                assertTrue(results.stream().anyMatch(s -> s.getName().equals("Test Song 1")));
                
                // Test 2: Partial match
                player.searchSongs("Test");
                results = player.getPlaylist();
                assertTrue(results.stream().anyMatch(s -> s.getName().equals("Test Song 1")));
                
                // Test 3: Case insensitive match
                player.searchSongs("test");
                results = player.getPlaylist();
                assertTrue(results.stream().anyMatch(s -> s.getName().equals("Test Song 1")));
            
            });
        } catch (Exception e) {
            fail("GUI operation failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test volume control")
    void testVolumeControl() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                // Test volume changes
                player.setVolume(50);
                assertEquals(50, player.getVolume());
                
                // Test volume limits
                player.setVolume(150);
                assertEquals(100, player.getVolume());
                
                player.setVolume(-10);
                assertEquals(0, player.getVolume());
            });
        } catch (Exception e) {
            fail("GUI operation failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test keyboard shortcuts")
    void testKeyboardShortcuts() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                // Load existing songs
                player.loadSongsFromCSV();
                
                // Test volume shortcuts
                player.getFrame().getRootPane().getActionMap().get("volumeUp")
                    .actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "volumeUp"));
                assertTrue(player.getVolume() > 0);
                
                player.getFrame().getRootPane().getActionMap().get("volumeDown")
                    .actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "volumeDown"));
                
                // Test navigation shortcuts
                player.getFrame().getRootPane().getActionMap().get("next")
                    .actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "next"));
                
                player.getFrame().getRootPane().getActionMap().get("previous")
                    .actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "previous"));
            });
        } catch (Exception e) {
            fail("GUI operation failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test rename functionality")
    void testRenameSong() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                // Load songs and select first one
                player.loadSongsFromCSV();
                player.addSong("Rename Test", testWavFile.getAbsolutePath());
                
                // Test renaming a song
                player.renameSong("Rename Test", "New Name");
                List<Song> songs = player.getPlaylist();
                assertTrue(songs.stream().anyMatch(s -> s.getName().equals("New Name")));
                assertFalse(songs.stream().anyMatch(s -> s.getName().equals("Rename Test")));
            });
        } catch (Exception e) {
            fail("GUI operation failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test rename selected song")
    void testRenameSelectedSong() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                // Load songs and select first one
                player.loadSongsFromCSV();
                player.getSongList().setSelectedIndex(0);
                
                // Test renaming selected song
                player.renameSelectedSong();
                
                // Verify song list is not empty after rename operation
                assertFalse(player.getPlaylist().isEmpty());
                
                // Verify first song is still selected
                assertEquals(0, player.getSongList().getSelectedIndex());
            });
        } catch (Exception e) {
            fail("GUI operation failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test save and load settings")
    void testSettings() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                // Load initial songs
                player.loadSongsFromCSV();
                
                // Set up some settings
                player.setVolume(75);
                
                // Save settings
                player.saveSettings();
                
                // Create new player instance to test loading
                MusicPlayer newPlayer = new MusicPlayer();
                
                // Verify settings were loaded
                assertEquals(75, newPlayer.getVolume());
                
                // Clean up
                newPlayer.getFrame().dispose();
            });
        } catch (Exception e) {
            fail("GUI operation failed: " + e.getMessage());
        } finally {
            // Clean up settings file
            new File("settings.properties").delete();
        }
    }
} 