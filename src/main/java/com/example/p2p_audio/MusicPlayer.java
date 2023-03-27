package com.example.p2p_audio;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MusicPlayer extends Application {
    private MediaPlayer mediaPlayer;
    private final boolean firstStreaming = false;

    private final BlockingQueue<double[]> audioQueue = new LinkedBlockingQueue<>() {
    };
    @Override
    public void start(Stage primaryStage) throws IOException {
        P2PNetwork node = null;

        /*
        Initial code block, which obtains the host machines "outgoing" IP address. The IP that is used when sending a packet to google
         */
        String ip = "";
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            ip = socket.getLocalAddress().getHostAddress();
            socket.close();
            node = new P2PNetwork(ip, 55123);
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }

        //firstStreaming = checkFirst();

        // Load the MP3 file from a local file path
        File file = new File("C:\\Users\\mcatt\\IdeaProjects\\P2P_Audio\\src\\main\\java\\com\\example\\p2p_audio\\MowMow105.mp3");
        String filePath = file.toURI().toString();
        Media media = new Media(filePath);
        // Create a media player with the loaded media file
        mediaPlayer = new MediaPlayer(media);
        AudioSpectrumListener audioSpectrumListener = new AudioSpectrumListener() {
            @Override
            public void spectrumDataUpdate(double timestamp, double duration, float[] magnitudes, float[] phases) {
                double[] data = new double[magnitudes.length];
                for (int i = 0; i < magnitudes.length; i++) {
                    data[i] = magnitudes[i];
                    System.out.println(data[i]);
                }

                //node.addToBuffer(data);
            }
        };

        mediaPlayer.setAudioSpectrumListener(audioSpectrumListener);

        // Create play button
        Button playButton = new Button("Play");
        playButton.setOnAction(event -> mediaPlayer.play());

        // Create pause button
        Button pauseButton = new Button("Pause");
        pauseButton.setOnAction(event -> mediaPlayer.pause());

        // Create stop button
        Button stopButton = new Button("Stop");
        final Slider timeSlider = new Slider();
        stopButton.setOnAction(event -> {
            mediaPlayer.stop();
            timeSlider.setValue(0);
        });


        timeSlider.setMin(0);
        timeSlider.setMax(100);
        timeSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (timeSlider.isValueChanging()) {
                    double value = timeSlider.getValue();
                    mediaPlayer.seek(mediaPlayer.getMedia().getDuration().multiply(value / 100.0));
                }
            }
        });

        // Bind time slider to media player
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            double value = newValue.toSeconds() / mediaPlayer.getTotalDuration().toSeconds() * 100.0;
            timeSlider.setValue(value);
        });

        // Create control bar
        HBox controlBar = new HBox();
        controlBar.setAlignment(Pos.CENTER);
        controlBar.setSpacing(10);
        controlBar.setPadding(new Insets(10, 10, 10, 10));
        controlBar.getChildren().addAll(playButton, pauseButton, stopButton, timeSlider);

        // Create root node
        BorderPane root = new BorderPane();
        root.setCenter(new MediaView(mediaPlayer));
        root.setBottom(controlBar);

        // Create scene
        Scene scene = new Scene(root, 300, 300);

        // Set stage properties
        primaryStage.setTitle("Media Player");
        primaryStage.setScene(scene);
        primaryStage.show();

        mediaPlayer.play();
    }

    @Override
    public void stop() throws Exception {
        mediaPlayer.stop();
        mediaPlayer.dispose();
    }
    public static void main(String[] args) {
        launch(args);
    }
}