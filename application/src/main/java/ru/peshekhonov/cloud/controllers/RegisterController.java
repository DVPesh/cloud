package ru.peshekhonov.cloud.controllers;

import io.netty.channel.Channel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;
import ru.peshekhonov.cloud.Client;
import ru.peshekhonov.cloud.FileInfo;


public class RegisterController {

    @FXML
    public TextField usernameField;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public Button userButton;
    @Setter
    private Channel socketChannel;

    @FXML
    public void register(ActionEvent actionEvent) {
        String username = usernameField.getText();
        String login = loginField.getText();
        String password = passwordField.getText();
        if (username == null || username.isBlank() || login == null || login.isBlank() || password == null || password.isBlank()) {
            showAlertDialog(Alert.AlertType.WARNING, "Имя пользователя, логин и пароль должны быть указаны!");
            return;
        }
        Client.getInstance().getCloudController().getNet().startNetty();

    }

    private void showAlertDialog(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        TableView<FileInfo> fileTable = Client.getInstance().getCloudController().getServerPanelController().getFileTable();
        Stage stage = (Stage) fileTable.getScene().getWindow();
        alert.setX(stage.getX() + (stage.getWidth() - Client.ALERT_WIDTH) / 2);
        alert.setY(stage.getY() + (stage.getHeight() - Client.ALERT_HEIGHT) / 2);
        alert.showAndWait();
    }
}
