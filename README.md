# Group 12: Ubiquitous Music Player Application

### Prerequisites
- Java JDK 17 or higher
- JUnit 5
   
### Eclipse Project Setup

1. **Option 1: Clone Repository**
   ```bash
   git clone https://github.com/mss49/Mobile-Music-Player.git
   ```
   
2.  **Option 2: Download ZIP**
- Visit https://github.com/mss49/Mobile-Music-Player
- Click the green 'Code' button
- Select 'Download ZIP'
- Extract the ZIP file to your desired location

3. **Import Project**
   - Open Eclipse
   - File > Open Projects from File System.. > Directory >
   - Browse to cloned repository (Mobile-Music-Player-main)
   - Click Finish
   - 

4. **Configure Build Path**
   - Right-click project > Properties > Java Build Path
   - Select Classpath > Add Library > JUnit > JUnit 5
   - Add Library > JRE System Library > JDK 17
   - Apply and Close

### Important Note About Test Files
Due to GitHub file limitations, the test audio .wav files cannot be downloaded from GitHub and must be downloaded separately.

Google Drive: [[Drive](https://drive.google.com/drive/folders/1KzXpnDLazx7LJHMH8nlDGSR_i8F3B4MZ?usp=drive_link)] 

### Test File Setup
1. Open the Google Drive
2. Select all 3 tracks, right click, download
3. Unzip the files
4. Place them in: `music` directory - replace if necessary


**Verify Project Structure**
   ```
   src/
     main/
       java/
         musicplayer/
           MusicPlayer.java
           Song.java
   test/
     blackbox/
           RandomBasedTest.java
           SpecificationBasedTest.java
           
     whitebox/
           BranchBasedTest.java
           StatementBasedTest.java
   music/
     test.wav
     test2.wav
     test3.wav
   songs.csv
           
   ```



### First-Time Setup
1. To launch the program, open the MusicPlayer.java file and select Run in Eclipse
2. Or right click the MusicPlayer.java file in the project explorer -> Run As -> Java Application
3. When exiting, the application will create:
   - `settings.properties` file for storing preferences

4. To add music:
   - Click "Add Song" button
   - Enter song name when prompted
   - Select audio file from file chooser
   - Supported formats: .wav
  
## Running Tests

### Test Categories

1. **Specification-Based Tests**
   - Settings management
   - Playback controls
   - File operations
   - Search functionality
   - Volume control
   - Loop/Mute toggles
   - Keyboard shortcuts

2. **Random-Based Tests**
   - Random playback operations
   - Random playlist navigation

3. **Statement-Based Tests**
   - PlaySong statements
   - Pause/Resume statements
   - Stop statements
   - Toggle statements
   - File operations statements

4. **Branch-Based Tests**
   - Theme toggle branches
   - Volume control branches
   - Navigation control branches
   - Loop control branches

### Running Tests in Eclipse

1. **Run All Tests**
   - Right-click on project folder in project explorer
   - Run As > JUnit Test

2. **Run Specific Test Class**
   - Right-click desired test package (e.g. test.blackbox) or test file (e.g. RandomBasedTest.java)
   - Run As > JUnit Test

3. **Run Individual Test**
   - Open test file
   - Right-click on test method
   - Run As > JUnit Test

### Test Coverage

To view test coverage:
1. Right-click project folder in project explorer
2. Coverage As > JUnit Test
3. View coverage results in Coverage view


