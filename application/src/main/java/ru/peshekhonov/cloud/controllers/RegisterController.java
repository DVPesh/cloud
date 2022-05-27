package ru.peshekhonov.cloud.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ru.peshekhonov.cloud.Client;
import ru.peshekhonov.cloud.messages.RegisterRequest;
import ru.peshekhonov.cloud.network.NettyNet;

public class RegisterController {

    @FXML
    public TextField usernameField;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public Button registerButton;

    @FXML
    public void register(ActionEvent actionEvent) {
        NettyNet net = Client.getInstance().getCloudController().getNet();

        String username = usernameField.getText();
        String login = loginField.getText();
        String password = passwordField.getText();

        if (username == null || username.isBlank() || login == null || login.isBlank() || password == null || password.isBlank()) {
            showAlertDialog(Alert.AlertType.WARNING, "Имя пользователя, логин и пароль должны быть указаны!");
            return;
        }
        if (net.getChannelFuture() == null || !net.getChannelFuture().channel().isActive()) {
            net.startNetty();
        }
        if (net.getChannelFuture() == null || !net.getChannelFuture().channel().isActive()) {
            clearRegisterForm();
            loginField.getScene().getWindow().hide();
            showAlertDialog(Alert.AlertType.ERROR, "Не удалось установить соединение с сервером");
            return;
        }
        clearRegisterForm();
        loginField.getScene().getWindow().hide();
        net.getChannelFuture().channel().writeAndFlush(new RegisterRequest(username, login, password));
    }

    private void showAlertDialog(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        Stage stage = Client.getInstance().getPrimaryStage();
        alert.setX(stage.getX() + (stage.getWidth() - Client.ALERT_WIDTH) / 2);
        alert.setY(stage.getY() + (stage.getHeight() - Client.ALERT_HEIGHT) / 2);
        alert.showAndWait();
    }

    private void clearRegisterForm() {
        usernameField.clear();
        loginField.clear();
        passwordField.clear();
    }
}
