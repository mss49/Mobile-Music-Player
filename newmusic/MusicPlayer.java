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

    public MusicPlayer() {
        playlist = new ArrayList<>();
        createGUI();
        loadSongsFromCSV();
    }

    private void createGUI() {
        frame = new JFrame("Music Player");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.setLayout(new BorderLayout());

        // Create the search bar
        JPanel searchPanel = new JPanel();
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

        // Create the playlist panel
        DefaultListModel<Song> playlistModel = new DefaultListModel<>();
        JList<Song> playlistList = new JList<>(playlistModel);
        JScrollPane playlistScrollPane = new JScrollPane(playlistList);
        playlistScrollPane.setPreferredSize(new Dimension(200, 300));

        // Create the button panel
        JPanel buttonPanel = new JPanel();
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

        volumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
        volumeSlider.addChangeListener(e -> updateVolume());
        buttonPanel.add(volumeSlider);

        loopButton = new JToggleButton("Loop");
        loopButton.addActionListener(e -> toggleLoop());
        buttonPanel.add(loopButton);

        // Create a main panel to hold the song list and button panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(songListScrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Create a panel to hold both the song list and playlist
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new GridLayout(1, 2)); // Two columns for song list and playlist
        sidePanel.add(mainPanel);
        sidePanel.add(playlistScrollPane);

        // Add components to the frame
        frame.add(searchPanel, BorderLayout.NORTH);
        frame.add(sidePanel, BorderLayout.CENTER);
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
        frame.setVisible(true);
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
        String path = JOptionPane.showInputDialog(frame, "Enter song path:");
        if (name != null && path != null) {
            addSong(name, path);
            songListModel.addElement(new Song(name, path));
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

    public List<Song> searchSongs(String query) {
        List<Song> results = new ArrayList<>();
        for (Song song : playlist) {
            if (song.getName().toLowerCase().contains(query.toLowerCase())) {
                results.add(song);
            }
        }
        return results;
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
            String newName = JOptionPane.showInputDialog(frame, "Enter new song name:", selectedSong.getName());
            if (newName != null && !newName.trim().isEmpty()) {
                renameSong(selectedSong.getName(), newName.trim());
                int selectedIndex = songList.getSelectedIndex();
                songListModel.set(selectedIndex, new Song(newName.trim(), selectedSong.getPath()));
                songList.repaint();
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
        List<Song> results = searchSongs(query);
        songListModel.clear();
        for (Song song : results) {
            songListModel.addElement(song);
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
            long seconds = currentPosition / 1000000;
            timeLabel.setText(String.format("%d:%02d", seconds / 60, seconds % 60));
            
            // Update progress bar
            int progress = (int)((currentPosition * 100.0) / totalLength);
            progressBar.setValue(progress);
        }
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

    @Override
    public String toString() {
        return name;
    }
}
