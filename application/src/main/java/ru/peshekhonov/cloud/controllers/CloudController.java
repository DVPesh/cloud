package ru.peshekhonov.cloud.controllers;

import io.netty.channel.Channel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.peshekhonov.cloud.Client;
import ru.peshekhonov.cloud.Configuration;
import ru.peshekhonov.cloud.FileInfo;
import ru.peshekhonov.cloud.handlers.StatusHandler;
import ru.peshekhonov.cloud.messages.FileMoveRequest;
import ru.peshekhonov.cloud.messages.FileRequest;
import ru.peshekhonov.cloud.messages.StartData;
import ru.peshekhonov.cloud.messages.SubsequentData;
import ru.peshekhonov.cloud.network.NettyNet;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ResourceBundle;

@Slf4j
public class CloudController implements Initializable {

    public final static long FILES_INFO_LIST_UPDATE_PERIOD_MS = 3000;

    @FXML
    @Getter
    private ClientPanelController clientPanelController;
    @FXML
    @Getter
    private ServerPanelController serverPanelController;
    @Getter
    private NettyNet net;
    @Setter
    private Channel socketChannel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        net = new NettyNet();
    }

    @FXML
    private void copyToServerButtonOnActionHandler(ActionEvent actionEvent) {
        final FileInfo selectedItem = clientPanelController.getFileTable().getSelectionModel().getSelectedItem();
        final Path clientCurrentPath = clientPanelController.getCurrentPath();
        final Path serverCurrentPath = serverPanelController.getCurrentPath();
        if (selectedItem == null || clientCurrentPath == null || serverCurrentPath == null) {
            return;
        }
        final String filename = selectedItem.getFilename();
        final Path clientPath = clientCurrentPath.resolve(filename);
        final Path serverPath = serverCurrentPath.resolve(filename);
        if (Files.notExists(clientPath) || Files.isDirectory(clientPath)) {
            return;
        }
        if (!socketChannel.pipeline().get(StatusHandler.class).getTaskMap().containsKey(serverPath)) {
            Thread thread = new Thread(() -> {
                try (SeekableByteChannel channel = Files.newByteChannel(clientPath, StandardOpenOption.READ)) {
                    copyFile(serverPath, channel);
                } catch (IOException | InterruptedException e) {
                    log.error("[ {} ] client failed to copy the file", selectedItem);
                } finally {
                    socketChannel.pipeline().get(StatusHandler.class).getTaskMap().remove(serverPath);
                }
            });
            socketChannel.pipeline().get(StatusHandler.class).getTaskMap().put(serverPath, thread);
            thread.start();
        }
    }

    private void copyFile(Path serverPath, SeekableByteChannel channel) throws IOException, InterruptedException {
        ByteBuffer buffer = ByteBuffer.allocate(Configuration.BUFFER_SIZE);
        byte[] array;
        final long fileSize = channel.size();
        long size = channel.read(buffer);
        buffer.flip();
        array = new byte[(int) size];
        buffer.get(array);
        socketChannel.writeAndFlush(new StartData(serverPath, size == -1 || fileSize == size, array, fileSize)).sync();
        buffer.clear();
        int length;
        while ((length = channel.read(buffer)) != -1) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            size += length;
            buffer.flip();
            if (length == Configuration.BUFFER_SIZE) {
                buffer.get(array);
                socketChannel.writeAndFlush(new SubsequentData(serverPath, fileSize == size, array)).sync();
                buffer.clear();
            } else {
                array = new byte[length];
                buffer.get(array);
                socketChannel.writeAndFlush(new SubsequentData(serverPath, true, array)).sync();
                buffer.clear();
            }
        }
    }

    @FXML
    private void copyToClientButtonOnActionHandler(ActionEvent actionEvent) {
        FileInfo selectedItem = serverPanelController.getFileTable().getSelectionModel().getSelectedItem();
        final Path clientCurrentPath = clientPanelController.getCurrentPath();
        final Path serverCurrentPath = serverPanelController.getCurrentPath();
        String selectedFilename = selectedItem.getFilename();
        if (selectedFilename == null || selectedItem.getType() == FileInfo.FileType.DIRECTORY || clientCurrentPath == null || serverCurrentPath == null) {
            return;
        }
        Path destination = clientCurrentPath.resolve(selectedFilename);
        if (Files.isRegularFile(destination)) {
            return;
        }
        Path source = serverCurrentPath.resolve(selectedFilename);
        socketChannel.writeAndFlush(new FileRequest(source, destination));
    }

    @FXML
    private void moveToServerButtonOnActionHandler(ActionEvent actionEvent) {
        final FileInfo selectedItem = clientPanelController.getFileTable().getSelectionModel().getSelectedItem();
        final Path clientCurrentPath = clientPanelController.getCurrentPath();
        final Path serverCurrentPath = serverPanelController.getCurrentPath();
        if (selectedItem == null || clientCurrentPath == null || serverCurrentPath == null) {
            return;
        }
        final String filename = selectedItem.getFilename();
        final Path clientPath = clientCurrentPath.resolve(filename);
        final Path serverPath = serverCurrentPath.resolve(filename);
        if (Files.notExists(clientPath) || Files.isDirectory(clientPath)) {
            return;
        }
        if (!socketChannel.pipeline().get(StatusHandler.class).getTaskMap().containsKey(serverPath)) {
            Thread thread = new Thread(() -> {
                try (SeekableByteChannel channel = Files.newByteChannel(clientPath, StandardOpenOption.READ)) {
                    copyFile(serverPath, channel);
                    channel.close();
                    Files.delete(clientPath);
                } catch (IOException | InterruptedException e) {
                    log.error("[ {} ] client failed to move the file", selectedItem);
                } finally {
                    socketChannel.pipeline().get(StatusHandler.class).getTaskMap().remove(serverPath);
                }
            });
            socketChannel.pipeline().get(StatusHandler.class).getTaskMap().put(serverPath, thread);
            thread.start();
        }
    }

    @FXML
    private void moveToClientButtonOnActionHandler(ActionEvent actionEvent) {
        FileInfo selectedItem = serverPanelController.getFileTable().getSelectionModel().getSelectedItem();
        final Path clientCurrentPath = clientPanelController.getCurrentPath();
        final Path serverCurrentPath = serverPanelController.getCurrentPath();
        String selectedFilename = selectedItem.getFilename();
        if (selectedFilename == null || selectedItem.getType() == FileInfo.FileType.DIRECTORY || clientCurrentPath == null || serverCurrentPath == null) {
            return;
        }
        Path destination = clientCurrentPath.resolve(selectedFilename);
        if (Files.isRegularFile(destination)) {
            return;
        }
        Path source = serverCurrentPath.resolve(selectedFilename);
        socketChannel.writeAndFlush(new FileMoveRequest(source, destination));
    }

    @FXML
    public void executeMenuItemRegistration(ActionEvent actionEvent) {
        Stage registerStage = Client.getInstance().getRegisterStage();
        Stage primaryStage = Client.getInstance().getPrimaryStage();
        registerStage.setX(primaryStage.getX() + (primaryStage.getWidth() - registerStage.getWidth()) / 2);
        registerStage.setY(primaryStage.getY() + (primaryStage.getHeight() - registerStage.getHeight()) / 2);
        registerStage.show();
    }

    @FXML
    public void executeMenuItemLogin(ActionEvent actionEvent) {
        Stage loginStage = Client.getInstance().getLoginStage();
        Stage primaryStage = Client.getInstance().getPrimaryStage();
        loginStage.setX(primaryStage.getX() + (primaryStage.getWidth() - loginStage.getWidth()) / 2);
        loginStage.setY(primaryStage.getY() + (primaryStage.getHeight() - loginStage.getHeight()) / 2);
        loginStage.show();
    }

    @FXML
    public void executeMenuItemLogout(ActionEvent actionEvent) {

    }

    @FXML
    private void exitMenuOnActionHandler(ActionEvent actionEvent) {
        Platform.exit();
    }
}
