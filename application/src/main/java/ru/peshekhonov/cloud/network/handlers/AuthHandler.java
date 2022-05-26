package ru.peshekhonov.cloud.network.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import ru.peshekhonov.cloud.Client;
import ru.peshekhonov.cloud.messages.AuthNotOk;
import ru.peshekhonov.cloud.messages.AuthOk;
import ru.peshekhonov.cloud.messages.Message;

@Slf4j
public class AuthHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof AuthOk status) {
            log.info("server granted access to user");
            Client.username = status.getUsername();
            Client.login = status.getLogin();
            Platform.runLater(() -> {
                Client.getInstance().getPrimaryStage().setTitle("Сетевое хранилище: " + Client.username);
            });
            ctx.pipeline().remove(AuthHandler.class);
        } else if (msg instanceof AuthNotOk status) {
            switch (status.getType()) {
                case ERROR6:
                    log.error(status.getType().getText());
                    Platform.runLater(() -> {
                        showAlertDialog(Alert.AlertType.ERROR, "Ошибка доступа к базе учётных данных");
                    });
                    break;
                case ERROR7:
                    log.error(status.getType().getText());
                    Platform.runLater(() -> {
                        showAlertDialog(Alert.AlertType.ERROR, "Сервер не может создать директорию для файлов клиента");
                    });
                    break;
                case AUTH_NOT_OK1:
                    Platform.runLater(() -> {
                        showAlertDialog(Alert.AlertType.WARNING, "Такой логин уже существует");
                    });
                    break;
                case AUTH_NOT_OK2:
                    Platform.runLater(() -> {
                        showAlertDialog(Alert.AlertType.WARNING, "Такой пароль уже существует");
                    });
                    break;
                case AUTH_NOT_OK3:
                    Platform.runLater(() -> {
                        showAlertDialog(Alert.AlertType.WARNING, "Пользователь с такими учётными данными не существует");
                    });
                    break;
                case AUTH_NOT_OK4:
                    Platform.runLater(() -> {
                        showAlertDialog(Alert.AlertType.WARNING, "Такой логин запрещён");
                    });
            }
        }
    }

    private void showAlertDialog(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        Stage stage = Client.getInstance().getPrimaryStage();
        alert.setX(stage.getX() + (stage.getWidth() - Client.ALERT_WIDTH) / 2);
        alert.setY(stage.getY() + (stage.getHeight() - Client.ALERT_HEIGHT) / 2);
        alert.showAndWait();
    }
}
