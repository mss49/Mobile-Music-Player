package test.whitebox;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import javax.sound.sampled.*;
import src.main.java.musicplayer.MusicPlayer;
import src.main.java.musicplayer.Song;
import java.util.List;
import javax.swing.SwingUtilities;

public class StatementBasedTest {
    private MusicPlayer player;
    private final String TEST_RESOURCES = "src/test/resources/";
    private File testWavFile;
    private String originalCsvContent;
    
    @BeforeEach
    void setUp() {
        // Save original CSV content
        try {
            File csvFile = new File("songs.csv");
            if (csvFile.exists()) {
                originalCsvContent = new String(java.nio.file.Files.readAllBytes(csvFile.toPath()));
            }
        } catch (IOException e) {
            fail("Could not backup CSV file");
        }
        
        player = new MusicPlayer();
        new File(TEST_RESOURCES).mkdirs();
        createTestWavFile();
    }
    
    @AfterEach
    void tearDown() {
        player.stop();
        testWavFile.delete();
        new File("settings.properties").delete();
        
        // Restore original CSV content
        try {
            if (originalCsvContent != null) {
                java.nio.file.Files.write(new File("songs.csv").toPath(), 
                    originalCsvContent.getBytes());
            }
        } catch (IOException e) {
            fail("Could not restore CSV file");
        }
    }

    private void createTestWavFile() {
        testWavFile = new File(TEST_RESOURCES + "test.wav");
        try {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
            byte[] data = new byte[44100];
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            AudioInputStream ais = new AudioInputStream(bais, format, data.length);
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, testWavFile);
        } catch (IOException e) {
            fail("Could not create test audio file");
        }
    }

    @Test
    @DisplayName("Test playSong statements")
    void testPlaySongStatements() {
        // Test playing a valid song
        Song validSong = new Song("Test Song", testWavFile.getAbsolutePath());
        player.playSong(validSong);
        
        // Test playing a song while another is playing
        Song anotherSong = new Song("Another Song", testWavFile.getAbsolutePath());
        player.playSong(anotherSong);
    }

    @Test
    @DisplayName("Test pause and resume statements")
    void testPauseResumeStatements() {
        // Test pause without playing
        player.pause();
        
        // Test resume without playing
        player.resume();
        
        // Test pause while playing
        Song testSong = new Song("Test Song", testWavFile.getAbsolutePath());
        player.playSong(testSong);
        player.pause();
        
        // Test resume after pause
        player.resume();
    }

    @Test
    @DisplayName("Test stop statements")
    void testStopStatements() {
        // Test stop without playing
        player.stop();
        
        // Test stop while playing
        Song testSong = new Song("Test Song", testWavFile.getAbsolutePath());
        player.playSong(testSong);
        player.stop();
    }

    @Test
    @DisplayName("Test navigation statements")
    void testNavigationStatements() {
        // Test next/previous with empty playlist
        player.playNextSong();
        player.playPreviousSong();
        
        // Add songs and test navigation
        player.addSong("Test Navigation 1", testWavFile.getAbsolutePath());
        player.addSong("Test Navigation 2", testWavFile.getAbsolutePath());
        player.playNextSong();
        player.playPreviousSong();
    }

    @Test
    @DisplayName("Test toggle statements")
    void testToggleStatements() {
        // Test mute toggle without playing
        player.toggleMute();
        player.toggleMute();
        
        // Test loop toggle
        player.toggleLoop();
        player.toggleLoop();
        
        // Test toggles while playing
        Song testSong = new Song("Test Toggle", testWavFile.getAbsolutePath());
        player.playSong(testSong);
        player.toggleMute();
        player.toggleLoop();
    }

    @Test
    @DisplayName("Test file operations statements")
    void testFileOperationsStatements() {
        // Test save/load with empty playlist
        player.saveSongsToCSV();
        player.loadSongsFromCSV();
        
        // Test save/load with songs
        player.addSong("Test Save", testWavFile.getAbsolutePath());
        player.saveSongsToCSV();
        player.loadSongsFromCSV();
    }

    @Test
    @DisplayName("Test sort song list statements")
    void testSortSongListStatements() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                // Statement 1: Load existing songs from CSV
                player.loadSongsFromCSV();
                
                // Statement 2: Sort ascending
                player.sortSongList();
                List<Song> firstSort = player.getPlaylist();
                assertEquals("Test Song 1", firstSort.get(0).getName());
                assertEquals("Test Song 2", firstSort.get(2).getName());
                assertEquals("Test Song 3", firstSort.get(4).getName());
                
                // Statement 3: Sort descending
                player.sortSongList();
                List<Song> secondSort = player.getPlaylist();
                assertEquals("Test Song 3", secondSort.get(0).getName());
                assertEquals("Test Song 2", secondSort.get(2).getName());
                assertEquals("Test Song 1", secondSort.get(4).getName());
                
                // Statement 4: Sort ascending again
                player.sortSongList();
                List<Song> thirdSort = player.getPlaylist();
                assertEquals("Test Song 1", thirdSort.get(0).getName());
                assertEquals("Test Song 2", thirdSort.get(2).getName());
                assertEquals("Test Song 3", thirdSort.get(4).getName());
            });
        } catch (Exception e) {
            fail("GUI operation failed: " + e.getMessage());
        }
    }
} 