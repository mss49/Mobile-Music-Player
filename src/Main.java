import javafx.application.Application;
import view.MusicPlayerGUI;

/**
 * Starts the GUI for the MusicPlayer
 */
public class Main {
	public static void main(String[] args) {
		try {
			MusicPlayerGUI.launch(MusicPlayerGUI.class, args);
		} catch (Exception e) {
			System.err.println("Error starting application: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}

