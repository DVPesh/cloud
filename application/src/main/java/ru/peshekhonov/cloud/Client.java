package ru.peshekhonov.cloud;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import ru.peshekhonov.cloud.controllers.CloudController;

import java.io.IOException;

public class Client extends Application {

    @Getter
    private static Client instance;

    private FXMLLoader fxmlLoader;

    public final static double ALERT_WIDTH = 366;
    public final static double ALERT_HEIGHT = 185;

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
        instance = this;
    }

    public CloudController getCloudController() {
        return fxmlLoader.getController();
    }

    @Override
    public void stop() throws Exception {
        CloudController cloudController = fxmlLoader.getController();
        if (cloudController.getNet() != null && cloudController.getNet().getHard() != null && !cloudController.getNet().getHard().isShuttingDown()) {
            cloudController.getNet().getHard().shutdownGracefully();
        }
        super.stop();
    }
}
