package com.example.p2p_audio;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

import java.io.File;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {
        // Load the MP3 file from a local file path
        File file = new File("C:\\Users\\mcatt\\IdeaProjects\\P2P_Audio\\src\\main\\java\\com\\example\\p2p_audio\\MowMow105.mp3");
        String filePath = file.toURI().toString();
        Media media = new Media(filePath);

        // Create a media player with the loaded media file
        MediaPlayer mediaPlayer = new MediaPlayer(media);

        // Create a media view to display the media player
        MediaView mediaView = new MediaView(mediaPlayer);

        // Create a play button to start playing the media
        Button playButton = new Button("Play");
        playButton.setOnAction(event -> mediaPlayer.play());

        // Create a pause button to pause the media
        Button pauseButton = new Button("Pause");
        pauseButton.setOnAction(event -> mediaPlayer.pause());

        // Create a stop button to stop the media
        Button stopButton = new Button("Stop");
        stopButton.setOnAction(event -> mediaPlayer.stop());

        // Create a layout for the media player controls
        BorderPane controls = new BorderPane();
        controls.setPadding(new Insets(10));
        controls.setLeft(playButton);
        controls.setCenter(pauseButton);
        controls.setRight(stopButton);

        // Create a layout for the media player view and controls
        BorderPane root = new BorderPane();
        root.setCenter(mediaView);
        root.setBottom(controls);

        // Create a scene and add the root layout to it
        Scene scene = new Scene(root, 640, 480);

        // Set the title of the stage and add the scene to it
        stage.setTitle("Media Player");
        stage.setScene(scene);

        // Show the stage and start playing the media
        stage.show();
        mediaPlayer.play();
    }
    public static void main(String[] args) {
        launch();
    }
}