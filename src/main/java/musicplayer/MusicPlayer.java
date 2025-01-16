import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MusicPlayer {
    private List<Song> playlist;
    private String csvFilePath = "songs.csv";
    private Clip currentClip;
    private boolean isPlaying;
    private JFrame frame;
    private DefaultListModel<Song> songListModel;
    private JList<Song> songList;
    private JSlider volumeSlider;
    private JLabel nowPlayingLabel;
    private JLabel timeLabel;
    private Timer timer;
    private boolean isMuted = false;
    private float lastVolume = 100;
    private JProgressBar progressBar;
    private boolean isLooping = false;
    private JToggleButton loopButton;
    private int currentSongIndex = -1;
    private boolean sortAscending = true;
    private boolean isDarkMode = false;
    private final Color LIGHT_BG = new Color(240, 240, 240);
    private final Color LIGHT_FG = Color.BLACK;
    private final Color DARK_BG = new Color(50, 50, 50);
    private final Color DARK_FG = Color.WHITE;
    private JPanel searchPanel;
    private JPanel buttonPanel;

    public MusicPlayer() {
        playlist = new ArrayList<>();
        createGUI();
        loadSongsFromCSV();
        loadSettings();
    }

    private void createGUI() {
        frame = new JFrame("Music Player");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.setLayout(new BorderLayout());

        // Create the search bar
        searchPanel = new JPanel();
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        JButton sortButton = new JButton("Sort A-Z");
        sortButton.addActionListener(e -> {
            sortSongList();
            sortButton.setText(sortAscending ? "Sort Z-A" : "Sort A-Z");
        });
        searchButton.addActionListener(e -> searchSongs(searchField.getText()));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(sortButton);

        // Create the song list panel
        songListModel = new DefaultListModel<>();
        songList = new JList<>(songListModel);
        JScrollPane songListScrollPane = new JScrollPane(songList);
        songListScrollPane.setPreferredSize(new Dimension(200, 300));

        // Create the button panel
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 4));
        JButton playButton = new JButton("Play");
        playButton.addActionListener(e -> playSelectedSong());
        JButton pauseButton = new JButton("Pause");
        pauseButton.addActionListener(e -> pause());
        JButton resumeButton = new JButton("Resume");
        resumeButton.addActionListener(e -> resume());
        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(e -> stop());
        JButton addButton = new JButton("Add Song");
        addButton.addActionListener(e -> addSong());
        JButton renameButton = new JButton("Rename Song");
        renameButton.addActionListener(e -> renameSelectedSong());
        JButton deleteButton = new JButton("Delete Song");
        deleteButton.addActionListener(e -> deleteSelectedSong());
        JButton previousButton = new JButton("Previous");
        JButton nextButton = new JButton("Next");
        previousButton.addActionListener(e -> playPreviousSong());
        nextButton.addActionListener(e -> playNextSong());

        buttonPanel.add(previousButton);
        buttonPanel.add(playButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(resumeButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(addButton);
        buttonPanel.add(renameButton);
        buttonPanel.add(deleteButton);

        JButton muteButton = new JButton("Mute");
        muteButton.addActionListener(e -> toggleMute());
        buttonPanel.add(muteButton);

        loopButton = new JToggleButton("Loop");
        loopButton.addActionListener(e -> toggleLoop());
        buttonPanel.add(loopButton);

        volumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
        volumeSlider.addChangeListener(e -> updateVolume());
        buttonPanel.add(volumeSlider);

        JButton themeButton = new JButton("Dark Mode");
        themeButton.addActionListener(e -> {
            toggleTheme();
            themeButton.setText(isDarkMode ? "Light Mode" : "Dark Mode");
        });
        searchPanel.add(themeButton);

        // Create a main panel to hold the song list and button panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(songListScrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add components to the frame
        frame.add(searchPanel, BorderLayout.NORTH);
        frame.add(mainPanel, BorderLayout.CENTER);
        
        nowPlayingLabel = new JLabel("Now Playing: None");
        nowPlayingLabel.setHorizontalAlignment(JLabel.CENTER);
        timeLabel = new JLabel("0:00");
        timeLabel.setHorizontalAlignment(JLabel.CENTER);
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentClip != null && currentClip.isOpen()) {
                    int mouseX = e.getX();
                    int progressBarWidth = progressBar.getWidth();
                    float percentage = (float) mouseX / progressBarWidth;
                    long newPosition = (long) (currentClip.getMicrosecondLength() * percentage);
                    currentClip.setMicrosecondPosition(newPosition);
                    updateTimeLabel(); // Update the display immediately
                }
            }
        });
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(nowPlayingLabel, BorderLayout.NORTH);
        southPanel.add(progressBar, BorderLayout.CENTER);
        southPanel.add(timeLabel, BorderLayout.EAST);
        frame.add(southPanel, BorderLayout.SOUTH);

        // Add key bindings after creating the frame
        addKeyboardShortcuts();

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveSettings();
            }
        });

        frame.setVisible(true);
    }

    private void addKeyboardShortcuts() {
        InputMap inputMap = frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = frame.getRootPane().getActionMap();

        // Space for Play/Pause
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "playPause");
        actionMap.put("playPause", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (isPlaying) pause();
                else resume();
            }
        });

        // Left arrow for Previous
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "previous");
        actionMap.put("previous", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                playPreviousSong();
            }
        });

        // Right arrow for Next
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "next");
        actionMap.put("next", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                playNextSong();
            }
        });

        // P for Pause
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "pause");
        actionMap.put("pause", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                pause();
            }
        });

        // R for Resume
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "resume");
        actionMap.put("resume", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                resume();
            }
        });

        // M for Mute toggle
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0), "mute");
        actionMap.put("mute", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                toggleMute();
            }
        });

        // Up arrow for volume up
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "volumeUp");
        actionMap.put("volumeUp", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                volumeSlider.setValue(Math.min(volumeSlider.getValue() + 10, 100));
            }
        });

        // Down arrow for volume down
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "volumeDown");
        actionMap.put("volumeDown", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                volumeSlider.setValue(Math.max(volumeSlider.getValue() - 10, 0));
            }
        });
    }

    private void playSelectedSong() {
        Song selectedSong = songList.getSelectedValue();
        if (selectedSong != null) {
            currentSongIndex = songList.getSelectedIndex();
            playSong(selectedSong);
        }
    }

    private void addSong() {
        String name = JOptionPane.showInputDialog(frame, "Enter song name:");
        if (name != null && !name.trim().isEmpty()) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Audio Files", "wav", "mp3"));
            
            if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                String path = fileChooser.getSelectedFile().getAbsolutePath();
                addSong(name, path);
                songListModel.addElement(new Song(name, path));
            }
        }
    }

    private void loadSongsFromCSV() {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 2) {
                    String name = data[0].trim();
                    String path = data[1].trim();
                    playlist.add(new Song(name, path));
                    songListModel.addElement(new Song(name, path));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading songs: " + e.getMessage());
        }
    }

    private void saveSongsToCSV() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(csvFilePath))) {
            for (Song song : playlist) {
                pw.println(song.getName() + "," + song.getPath());
            }
        } catch (IOException e) {
            System.err.println("Error saving songs: " + e.getMessage());
        }
    }

    public void playSong(Song song) {
        try {
            if (currentClip != null) {
                currentClip.stop();
                currentClip.close();
            }

            File audioFile = new File(song.getPath());
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            currentClip = AudioSystem.getClip();
            currentClip.open(audioStream);
            if (isLooping) {
                currentClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
            currentClip.start();
            nowPlayingLabel.setText("Now Playing: " + song.getName());
            isPlaying = true;
            if (timer != null) timer.stop();
            timer = new Timer(1000, e -> updateTimeLabel());
            timer.start();

            currentClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    if (isLooping && currentClip.getMicrosecondPosition() >= currentClip.getMicrosecondLength()) {
                        // Existing loop code...
                    } else if (!isLooping && currentClip.getMicrosecondPosition() >= currentClip.getMicrosecondLength()) {
                        // Auto-play next song
                        SwingUtilities.invokeLater(() -> {
                            if (currentSongIndex < songListModel.getSize() - 1) {
                                playNextSong();
                            }
                        });
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Error playing song: " + e.getMessage());
        }
    }

    public void pause() {
        if (currentClip != null && isPlaying) {
            currentClip.stop();
            isPlaying = false;
        }
    }

    public void resume() {
        if (currentClip != null && !isPlaying) {
            currentClip.start();
            isPlaying = true;
        }
    }

    public void addSong(String name, String path) {
        playlist.add(new Song(name, path));
        saveSongsToCSV();
    }

    public void removeSong(String name) {
        playlist.removeIf(song -> song.getName().equals(name));
        saveSongsToCSV();
    }

    public void renameSong(String oldName, String newName) {
        for (Song song : playlist) {
            if (song.getName().equals(oldName)) {
                song.setName(newName);
                break;
            }
        }
        saveSongsToCSV();
    }

    public List<Song> getPlaylist() {
        return new ArrayList<>(playlist);
    }

    public void stop() {
        if (currentClip != null) {
            currentClip.stop();
            currentClip.close();
            isPlaying = false;
            currentSongIndex = -1;
            nowPlayingLabel.setText("Now Playing: None");
            if (timer != null) {
                timer.stop();
                timeLabel.setText("0:00");
                progressBar.setValue(0);
            }
        }
    }

    private void renameSelectedSong() {
        Song selectedSong = songList.getSelectedValue();
        if (selectedSong != null) {
            // Prompt for new name
            String newName = JOptionPane.showInputDialog(frame, "Enter new song name:", selectedSong.getName());
            
            // File chooser for new path
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Audio Files", "wav", "mp3"));
            String newPath = selectedSong.getPath();
            
            int choice = JOptionPane.showConfirmDialog(frame, 
                "Would you like to change the file path?", 
                "Change Path", 
                JOptionPane.YES_NO_OPTION);
                
            if (choice == JOptionPane.YES_OPTION && 
                fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                newPath = fileChooser.getSelectedFile().getAbsolutePath();
            }
            
            // Update if either field was changed
            if ((newName != null && !newName.trim().isEmpty()) || 
                !newPath.equals(selectedSong.getPath())) {
                
                // Update name if changed
                if (newName != null && !newName.trim().isEmpty()) {
                    renameSong(selectedSong.getName(), newName.trim());
                } else {
                    newName = selectedSong.getName();
                }
                
                // Update list and save changes
                int selectedIndex = songList.getSelectedIndex();
                songListModel.set(selectedIndex, new Song(newName, newPath));
                songList.repaint();
                saveSongsToCSV();
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a song to rename.");
        }
    }

    private void deleteSelectedSong() {
        Song selectedSong = songList.getSelectedValue();
        if (selectedSong != null) {
            int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete the selected song?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                removeSong(selectedSong.getName());
                songListModel.removeElement(selectedSong);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a song to delete.");
        }
    }

    // Method to handle song search
    private void searchSongs(String query) {
        songListModel.clear();
        if (query.trim().isEmpty()) {
            // If search is empty, show all songs
            for (Song song : playlist) {
                songListModel.addElement(song);
            }
        } else {
            // Show only matching songs
            for (Song song : playlist) {
                if (song.getName().toLowerCase().contains(query.toLowerCase())) {
                    songListModel.addElement(song);
                }
            }
        }
    }

    private void updateVolume() {
        if (currentClip != null) {
            FloatControl gainControl = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);
            float range = gainControl.getMaximum() - gainControl.getMinimum();
            float gain = (range * (volumeSlider.getValue() / 100.0f)) + gainControl.getMinimum();
            gainControl.setValue(gain);
        }
    }

    private void updateTimeLabel() {
        if (currentClip != null) {
            long currentPosition = currentClip.getMicrosecondPosition();
            long totalLength = currentClip.getMicrosecondLength();
            String currentTime = formatDuration(currentPosition);
            String totalTime = formatDuration(totalLength);
            timeLabel.setText(currentTime + " / " + totalTime);
            
            int progress = (int)((currentPosition * 100.0) / totalLength);
            progressBar.setValue(progress);
        }
    }

    private String formatDuration(long microseconds) {
        long seconds = microseconds / 1_000_000;
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }

    private void toggleMute() {
        if (currentClip != null) {
            isMuted = !isMuted;
            if (isMuted) {
                lastVolume = volumeSlider.getValue();
                volumeSlider.setValue(0);
            } else {
                volumeSlider.setValue((int) lastVolume);
            }
            updateVolume();
        }
    }

    private void toggleLoop() {
        isLooping = loopButton.isSelected();
        if (currentClip != null) {
            currentClip.loop(isLooping ? Clip.LOOP_CONTINUOUSLY : 0);
        }
    }

    private void playPreviousSong() {
        if (currentSongIndex > 0) {
            currentSongIndex--;
            Song previousSong = songListModel.getElementAt(currentSongIndex);
            playSong(previousSong);
            songList.setSelectedIndex(currentSongIndex);
        }
    }

    private void playNextSong() {
        if (currentSongIndex < songListModel.getSize() - 1) {
            currentSongIndex++;
            Song nextSong = songListModel.getElementAt(currentSongIndex);
            playSong(nextSong);
            songList.setSelectedIndex(currentSongIndex);
        }
    }

    private void sortSongList() {
        List<Song> songs = new ArrayList<>();
        for (int i = 0; i < songListModel.getSize(); i++) {
            songs.add(songListModel.getElementAt(i));
        }
        
        Comparator<Song> comparator = Comparator.comparing(Song::getName, String.CASE_INSENSITIVE_ORDER);
        if (!sortAscending) {
            comparator = comparator.reversed();
        }
        Collections.sort(songs, comparator);
        
        songListModel.clear();
        for (Song song : songs) {
            songListModel.addElement(song);
        }
        
        playlist.clear();
        playlist.addAll(songs);
        saveSongsToCSV();
        
        sortAscending = !sortAscending;
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        Color bg = isDarkMode ? DARK_BG : LIGHT_BG;
        Color fg = isDarkMode ? DARK_FG : LIGHT_FG;

        frame.getContentPane().setBackground(bg);
        songList.setBackground(bg);
        songList.setForeground(fg);
        searchPanel.setBackground(bg);
        buttonPanel.setBackground(bg);
        progressBar.setBackground(bg);
        progressBar.setForeground(fg);
        nowPlayingLabel.setBackground(fg);
        nowPlayingLabel.setForeground(fg);
        timeLabel.setBackground(bg);
        timeLabel.setForeground(fg);

        // Update all components in the frame
        SwingUtilities.updateComponentTreeUI(frame);
    }

    private void saveSettings() {
        try {
            Properties props = new Properties();
            props.setProperty("lastSong", String.valueOf(currentSongIndex));
            props.setProperty("volume", String.valueOf(volumeSlider.getValue()));
            props.store(new FileOutputStream("settings.properties"), null);
        } catch (IOException e) {
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }

    private void loadSettings() {
        try {
            Properties props = new Properties();
            if (new File("settings.properties").exists()) {
                props.load(new FileInputStream("settings.properties"));
                
                // Restore volume
                String volume = props.getProperty("volume", "100");
                volumeSlider.setValue(Integer.parseInt(volume));
                
                // Restore last played song
                String lastSong = props.getProperty("lastSong", "-1");
                int songIndex = Integer.parseInt(lastSong);
                if (songIndex >= 0 && songIndex < songListModel.getSize()) {
                    songList.setSelectedIndex(songIndex);
                    currentSongIndex = songIndex;
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading settings: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MusicPlayer::new);
    }
}

class Song {
    private String name;
    private String path;

    public Song(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return name;
    }
}
