package ru.peshekhonov.cloud.network.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import ru.peshekhonov.cloud.Client;
import ru.peshekhonov.cloud.FileInfo;
import ru.peshekhonov.cloud.StatusType;
import ru.peshekhonov.cloud.messages.Message;
import ru.peshekhonov.cloud.messages.StatusData;

@Slf4j
public class ClientStatusHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof StatusData status) {
            switch (status.getStatus()) {
                case ERROR3:
                    log.error("[ {} ] {}", status.getPath().getFileName().toString(), StatusType.ERROR3.getText());
                    Platform.runLater(() -> {
                        showAlertDialog(Alert.AlertType.ERROR, "Не удалось переименовать файл");
                    });
                    break;
                case ERROR4:
                    log.error("[ {} ] {}", status.getPath().getFileName().toString(), StatusType.ERROR4.getText());
                    Platform.runLater(() -> {
                        showAlertDialog(Alert.AlertType.ERROR, "Ошибка ввода-вывода при удалении файла");
                    });
                    break;
                case WARNING1:
                    log.error("[ {} ] {}", status.getPath().getFileName().toString(), StatusType.WARNING1.getText());
                    Platform.runLater(() -> {
                        showAlertDialog(Alert.AlertType.WARNING, "Невозможно удалить непустую директорию");
                    });
            }
        } else {
            ctx.fireChannelRead(msg);
        }
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
