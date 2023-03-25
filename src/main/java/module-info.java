module com.example.p2p_audio {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens com.example.p2p_audio to javafx.fxml;
    exports com.example.p2p_audio;
}