package ru.peshekhonov.cloud;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Client extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("cloud-template.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Сетевое хранилище");
        stage.setScene(scene);
        stage.show();
    }
}
