package view;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.MusicLibrary;
import model.Song;
import model.Playlist;
import java.util.concurrent.TimeUnit;

public class MusicPlayerGUI extends Application {
    private MusicLibrary musicLibrary;
    private ListView<Song> songListView;
    private ListView<Playlist> playlistListView;
    private Label nowPlayingLabel;
    private Button playButton;
    private Button pauseButton;
    private Button nextButton;
    private Button previousButton;
    private Slider volumeSlider;
    private ProgressBar songProgress;
    private javazoom.jl.player.Player currentPlayer;
    private Thread playerThread;
    private boolean isPlaying;

    @Override
    public void start(Stage primaryStage) {
        try {
            musicLibrary = MusicLibrary.getInstance();
            musicLibrary.loadLibrary();

            // Populate song list
            songListView = new ListView<>();
            songListView.getItems().addAll(musicLibrary.getAllSongs());

            // Create main layout
            BorderPane mainLayout = new BorderPane();
            mainLayout.setPadding(new Insets(10));

            // Create left panel (Playlists)
            VBox leftPanel = createPlaylistPanel();
            mainLayout.setLeft(leftPanel);

            // Create center panel (Songs)
            VBox centerPanel = createSongListPanel();
            mainLayout.setCenter(centerPanel);

            // Create bottom panel (Controls)
            VBox bottomPanel = createControlPanel();
            mainLayout.setBottom(bottomPanel);

            // Create the scene
            Scene scene = new Scene(mainLayout, 800, 600);
            primaryStage.setTitle("Music Player");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load music library");
            alert.setContentText("An error occurred while loading the music library: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    private VBox createPlaylistPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(200);

        Label playlistLabel = new Label("Playlists");
        playlistListView = new ListView<>();
        Button newPlaylistButton = new Button("New Playlist");

        panel.getChildren().addAll(playlistLabel, playlistListView, newPlaylistButton);

        // Add event handlers
        newPlaylistButton.setOnAction(e -> createNewPlaylist());

        return panel;
    }

    private VBox createSongListPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));

        Label songsLabel = new Label("Songs");
        if (songListView == null) {
            songListView = new ListView<>();
            songListView.getItems().addAll(musicLibrary.getAllSongs());
        }
        TextField searchField = new TextField();
        searchField.setPromptText("Search songs...");

        panel.getChildren().addAll(songsLabel, searchField, songListView);

        // Add event handlers
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterSongs(newValue);
        });

        return panel;
    }

    private VBox createControlPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));

        // Now Playing Label
        nowPlayingLabel = new Label("Not Playing");

        // Progress Bar
        songProgress = new ProgressBar(0);
        songProgress.setPrefWidth(Double.MAX_VALUE);

        // Control Buttons
        HBox buttonBox = new HBox(10);
        previousButton = new Button("Prev");
        playButton = new Button("Play");
        pauseButton = new Button("Pause");
        nextButton = new Button("Next");
        buttonBox.getChildren().addAll(previousButton, playButton, pauseButton, nextButton);

        // Volume Control
        HBox volumeBox = new HBox(10);
        Label volumeLabel = new Label("Volume:");
        volumeSlider = new Slider(0, 100, 50);
        volumeBox.getChildren().addAll(volumeLabel, volumeSlider);

        panel.getChildren().addAll(nowPlayingLabel, songProgress, buttonBox, volumeBox);

        // Add event handlers
        setupControlHandlers();

        return panel;
    }

    private void setupControlHandlers() {
        playButton.setOnAction(e -> playSong());
        pauseButton.setOnAction(e -> pauseSong());
        nextButton.setOnAction(e -> playNextSong());
        previousButton.setOnAction(e -> playPreviousSong());
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateVolume(newVal.doubleValue());
        });
    }

    private void createNewPlaylist() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Playlist");
        dialog.setHeaderText("Create a new playlist");
        dialog.setContentText("Please enter playlist name:");

        dialog.showAndWait().ifPresent(name -> {
            // Create new playlist logic
        });
    }

    private void filterSongs(String searchText) {
        // Implement song filtering logic
    }

    private void playSong() {
        Song selectedSong = songListView.getSelectionModel().getSelectedItem();
        if (selectedSong != null) {
            if (currentPlayer != null) {
                stopCurrentSong();
            }
            
            playerThread = new Thread(() -> {
                try {
                    currentPlayer = selectedSong.getSongPlayer();
                    if (currentPlayer != null) {
                        isPlaying = true;
                        javafx.application.Platform.runLater(() -> {
                            nowPlayingLabel.setText("Now Playing: " + selectedSong.getSongTitle());
                        });
                        
                        while (isPlaying && !Thread.currentThread().isInterrupted()) {
                            try {
                                if (!currentPlayer.play(1)) {
                                    break;
                                }
                            } catch (Exception e) {
                                if (!"Stream Closed".equals(e.getMessage())) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        nowPlayingLabel.setText("Error playing: " + e.getMessage());
                    });
                }
            });
            playerThread.setDaemon(true);
            playerThread.start();
        }
    }

    private void pauseSong() {
        if (currentPlayer != null && isPlaying) {
            isPlaying = false;
            if (playerThread != null) {
                playerThread.interrupt();
            }
        }
    }

    private void stopCurrentSong() {
        isPlaying = false;
        if (playerThread != null) {
            playerThread.interrupt();
            try {
                playerThread.join(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        Song currentSong = songListView.getSelectionModel().getSelectedItem();
        if (currentSong != null) {
            currentSong.closeStreams();
        }
        
        if (currentPlayer != null) {
            try {
                currentPlayer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            currentPlayer = null;
        }
        playerThread = null;
    }

    private void playNextSong() {
        // Implement next song logic
    }

    private void playPreviousSong() {
        // Implement previous song logic
    }

    private void updateVolume(double value) {
        // Implement volume update logic
    }

    public static void main(String[] args) {
        launch(args);
    }
} 