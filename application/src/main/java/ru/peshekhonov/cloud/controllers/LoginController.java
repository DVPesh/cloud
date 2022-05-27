package ru.peshekhonov.cloud.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ru.peshekhonov.cloud.Client;
import ru.peshekhonov.cloud.messages.LoginRequest;
import ru.peshekhonov.cloud.network.NettyNet;

public class LoginController {

    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public Button loginButton;

    @FXML
    public void login(ActionEvent actionEvent) {
        NettyNet net = Client.getInstance().getCloudController().getNet();

        String login = loginField.getText();
        String password = passwordField.getText();

        if (login == null || login.isBlank() || password == null || password.isBlank()) {
            showAlertDialog(Alert.AlertType.WARNING, "Логин и пароль должны быть указаны!");
            return;
        }
        if (net.getChannelFuture() == null || !net.getChannelFuture().channel().isActive()) {
            net.startNetty();
        }
        if (net.getChannelFuture() == null || !net.getChannelFuture().channel().isActive()) {
            clearLoginForm();
            loginField.getScene().getWindow().hide();
            showAlertDialog(Alert.AlertType.ERROR, "Не удалось установить соединение с сервером");
            return;
        }
        clearLoginForm();
        loginField.getScene().getWindow().hide();
        net.getChannelFuture().channel().writeAndFlush(new LoginRequest(login, password));
    }

    private void showAlertDialog(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        Stage stage = Client.getInstance().getPrimaryStage();
        alert.setX(stage.getX() + (stage.getWidth() - Client.ALERT_WIDTH) / 2);
        alert.setY(stage.getY() + (stage.getHeight() - Client.ALERT_HEIGHT) / 2);
        alert.showAndWait();
    }

    private void clearLoginForm() {
        loginField.clear();
        passwordField.clear();
    }
}
