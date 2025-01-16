package test.whitebox;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import javax.sound.sampled.*;
import src.main.java.musicplayer.MusicPlayer;
import src.main.java.musicplayer.Song;
import java.awt.Color;
import javax.swing.SwingUtilities;
public class BranchBasedTest {
    private MusicPlayer player;
    private final String TEST_RESOURCES = "src/test/resources/";
    private File testWavFile;
    private String originalCsvContent;
    
    @BeforeEach
    void setUp() {
        // Backup original CSV content
        try {
            File csvFile = new File("songs.csv");
            if (csvFile.exists()) {
                originalCsvContent = new String(java.nio.file.Files.readAllBytes(csvFile.toPath()));
            }
        } catch (IOException e) {
            fail("Could not backup CSV file");
        }

        try {
            SwingUtilities.invokeAndWait(() -> {
                player = new MusicPlayer();
            });
        } catch (Exception e) {
            fail("GUI initialization failed");
        }
        
        new File(TEST_RESOURCES).mkdirs();
        createTestWavFile();
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

    @Test
    @DisplayName("Test playback control branches")
    void testPlaybackControlBranches() {
        Song testSong = new Song("Test Song", testWavFile.getAbsolutePath());
        
        // Branch 1: Play with no previous playback
        player.playSong(testSong);
        assertTrue(player.isPlaying());
        
        // Branch 2: Play while already playing
        player.playSong(testSong);
        assertTrue(player.isPlaying());
        
        // Branch 3: Pause while playing
        player.pause();
        assertFalse(player.isPlaying());
        
        // Branch 4: Pause while already paused
        player.pause();
        assertFalse(player.isPlaying());
        
        // Branch 5: Resume while paused
        player.resume();
        assertTrue(player.isPlaying());
        
        // Branch 6: Resume while already playing
        player.resume();
        assertTrue(player.isPlaying());
        
        // Branch 7: Stop while playing
        player.stop();
        assertFalse(player.isPlaying());
        
        // Branch 8: Stop while already stopped
        player.stop();
        assertFalse(player.isPlaying());
    }

    @Test
    @DisplayName("Test volume control branches")
    void testVolumeControlBranches() {
        Song testSong = new Song("Test Song", testWavFile.getAbsolutePath());
        player.playSong(testSong);
        
        // Branch 1: Initial mute state
        assertFalse(player.isMuted());
        
        // Branch 2: Mute while playing
        player.toggleMute();
        assertTrue(player.isMuted());
        
        // Branch 3: Unmute while playing
        player.toggleMute();
        assertFalse(player.isMuted());
        
        // Branch 4: Volume boundaries
        player.setVolume(0);   // Minimum volume
        assertEquals(0, player.getVolume());
        
        player.setVolume(50);  // Mid volume
        assertEquals(50, player.getVolume());
        
        player.setVolume(100); // Maximum volume
        assertEquals(100, player.getVolume());
        
        // Branch 5: Invalid volume values
        player.setVolume(-10); // Below minimum
        assertEquals(0, player.getVolume());
        
        player.setVolume(110); // Above maximum
        assertEquals(100, player.getVolume());
    }

    @Test
    @DisplayName("Test loop control branches")
    void testLoopControlBranches() {
        Song testSong = new Song("Test Song", testWavFile.getAbsolutePath());
        
        // Branch 1: Initial loop state
        assertFalse(player.isLooping());
        
        // Branch 2: Enable loop without playback
        player.toggleLoop();
        assertTrue(player.isLooping());
        
        // Branch 3: Start playback with loop enabled
        player.playSong(testSong);
        assertTrue(player.isLooping());
        
        // Branch 4: Disable loop during playback
        player.toggleLoop();
        assertFalse(player.isLooping());
        
        // Branch 5: Re-enable loop during playback
        player.toggleLoop();
        assertTrue(player.isLooping());
    }

    @Test
    @DisplayName("Test theme toggle branches")
    void testThemeToggleBranches() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                // Branch 1: Initial theme state (Light mode)
                assertFalse(player.isDarkMode());
                
                // Branch 2: Toggle to Dark mode
                player.toggleTheme();
                assertTrue(player.isDarkMode());

                
                // Branch 3: Toggle back to Light mode
                player.toggleTheme();
                assertFalse(player.isDarkMode());
                
                // Branch 4: Verify component colors updated
                assertFalse(player.getNowPlayingLabel().getForeground().equals(Color.WHITE));
            });
        } catch (Exception e) {
            fail("GUI operation failed: " + e.getMessage());
        }
    }
}
