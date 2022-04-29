package ru.peshekhonov.cloud;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.peshekhonov.cloud.controller.CloudController;

import java.io.IOException;

public class Client extends Application {

    public static Client INSTANCE;
    private FXMLLoader fxmlLoader;

    @Override
    public void start(Stage stage) throws IOException {
        fxmlLoader = new FXMLLoader(Client.class.getResource("cloud-template.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Сетевое хранилище");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void init() throws Exception {
        INSTANCE = this;
    }

    public CloudController getCloudController() {
        return fxmlLoader.getController();
    }
}
