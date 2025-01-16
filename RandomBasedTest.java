package test.blackbox;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.util.Random;
import javax.sound.sampled.*;
import javax.swing.SwingUtilities;
import src.main.java.musicplayer.MusicPlayer;
import src.main.java.musicplayer.Song;

public class RandomBasedTest {
    private MusicPlayer player;
    private final String TEST_RESOURCES = "src/test/resources/";
    private File testWavFile;
    private Random random;
    private String originalCsvContent;
    private final String[] SAMPLE_SONG_NAMES = {
        "Song A", "Song B", "Song C", "Test Song", "Music Track"
    };
    
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
        
        random = new Random();
        new File(TEST_RESOURCES).mkdirs();
        createTestWavFile();
    }
    
    private void createTestWavFile() {
        testWavFile = new File(TEST_RESOURCES + "test.wav");
        try {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
            byte[] data = new byte[1024]; // Smaller audio data
            random.nextBytes(data);
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            AudioInputStream ais = new AudioInputStream(bais, format, data.length);
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, testWavFile);
        } catch (IOException e) {
            fail("Could not create test audio file");
        }
    }
    
    @AfterEach
    void tearDown() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                player.stop();
            });
        } catch (Exception e) {
            // Ignore cleanup errors
        }
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
    @DisplayName("Test random playback operations")
    void testRandomPlayback() {
        for (int i = 0; i < 20; i++) { // Reduced iterations
            int operation = random.nextInt(6);
            final Song testSong = new Song(getRandomSongName(), testWavFile.getAbsolutePath());
            
            try {
                SwingUtilities.invokeAndWait(() -> {
                    switch (operation) {
                        case 0: player.playSong(testSong); break;
                        case 1: player.pause(); break;
                        case 2: player.resume(); break;
                        case 3: player.stop(); break;
                        case 4: player.toggleMute(); break;
                        case 5: player.toggleLoop(); break;
                    }
                });
                Thread.sleep(100); // Longer delay between operations
            } catch (Exception e) {
                fail("GUI operation failed");
            }
        }
    }

    @Test
    @DisplayName("Test random playlist navigation")
    void testRandomNavigation() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                // Add a small fixed number of songs
                for (int i = 0; i < 5; i++) {
                    player.addSong(getRandomSongName(), testWavFile.getAbsolutePath());
                }
            });

            for (int i = 0; i < 10; i++) { // Reduced iterations
                SwingUtilities.invokeAndWait(() -> {
                    if (random.nextBoolean()) {
                        player.playNextSong();
                    } else {
                        player.playPreviousSong();
                    }
                });
                Thread.sleep(100);
            }
        } catch (Exception e) {
            fail("Navigation operation failed");
        }
    }

    private String getRandomSongName() {
        return SAMPLE_SONG_NAMES[random.nextInt(SAMPLE_SONG_NAMES.length)];
    }
} 