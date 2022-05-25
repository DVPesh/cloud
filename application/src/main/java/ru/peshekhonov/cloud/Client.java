package ru.peshekhonov.cloud;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import ru.peshekhonov.cloud.controllers.CloudController;
import ru.peshekhonov.cloud.controllers.LoginController;
import ru.peshekhonov.cloud.controllers.RegisterController;

import java.io.IOException;

public class Client extends Application {

    @Getter
    private static Client instance;

    private FXMLLoader cloudLoader;
    private FXMLLoader loginLoader;
    private FXMLLoader registerLoader;

    public final static double ALERT_WIDTH = 366;
    public final static double ALERT_HEIGHT = 185;

    @Getter
    private Stage primaryStage;
    @Getter
    private Stage loginStage;
    @Getter
    private Stage registerStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        cloudLoader = new FXMLLoader(Client.class.getResource("cloud-template.fxml"));
        primaryStage.setTitle("Сетевое хранилище");
        primaryStage.setScene(new Scene(cloudLoader.load()));

        loginStage = new Stage();
        loginLoader = new FXMLLoader(Client.class.getResource("authDialog.fxml"));
        loginStage.initOwner(primaryStage);
        loginStage.initModality(Modality.WINDOW_MODAL);
        loginStage.setScene(new Scene(loginLoader.load()));
        loginStage.setTitle("Авторизация");
        loginStage.setResizable(false);

        registerStage = new Stage();
        registerLoader = new FXMLLoader(Client.class.getResource("registerDialog.fxml"));
        registerStage.initOwner(primaryStage);
        registerStage.initModality(Modality.WINDOW_MODAL);
        registerStage.setScene(new Scene(registerLoader.load()));
        registerStage.setTitle("Регистрация");
        registerStage.setResizable(false);

        primaryStage.show();
        loginStage.show();
        loginStage.hide();
        registerStage.show();
        registerStage.hide();
    }

    @Override
    public void init() throws Exception {
        instance = this;
    }

    public CloudController getCloudController() {
        return cloudLoader.getController();
    }

    public RegisterController getRegisterController() {
        return registerLoader.getController();
    }

    public LoginController getLoginController() {
        return loginLoader.getController();
    }

    @Override
    public void stop() throws Exception {
        CloudController cloudController = cloudLoader.getController();
        if (cloudController.getNet() != null && cloudController.getNet().getHard() != null && !cloudController.getNet().getHard().isShuttingDown()) {
            cloudController.getNet().getHard().shutdownGracefully();
        }
        super.stop();
    }
}
