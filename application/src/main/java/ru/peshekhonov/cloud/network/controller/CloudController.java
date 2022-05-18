package ru.peshekhonov.cloud.network.controller;

import io.netty.channel.Channel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.peshekhonov.cloud.Configuration;
import ru.peshekhonov.cloud.messages.*;
import ru.peshekhonov.cloud.network.NettyNet;
import ru.peshekhonov.cloud.handlers.StatusHandler;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

@Slf4j
public class CloudController implements Initializable {

    @FXML
    public ListView<String> clientListView;
    @FXML
    public ListView<String> serverListView;
    @FXML
    public Button buttonCopyToServer;
    @FXML
    public Button buttonCopyToClient;

    private ObservableList<String> clientFiles;
    private ObservableList<String> serverFiles;
    private @Getter
    NettyNet net;
    private @Setter
    Channel socketChannel;
    private @Setter
    Path serverDir;
    private Path clientDir = Path.of("files");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clientFiles = FXCollections.observableArrayList(new String[0]);
        clientListView.setItems(clientFiles);
        serverFiles = FXCollections.observableArrayList(new String[0]);
        serverListView.setItems(serverFiles);

        net = new NettyNet();

        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    updateClientListView();
                    socketChannel.writeAndFlush(new FileListRequest());
                });
            }
        }, 100, 3000);

    }

    public void updateServerListView(List<String> serverFileList) {
        int selectedIndex = serverListView.getSelectionModel().getSelectedIndex();
        serverFiles.setAll(serverFileList);
        serverListView.getSelectionModel().select(selectedIndex);
    }

    public void updateClientListView() {
        try {
            int selectedIndex = clientListView.getSelectionModel().getSelectedIndex();
            clientFiles.setAll(Files.list(clientDir).map(Path::getFileName).map(Path::toString).toList());
            clientListView.getSelectionModel().select(selectedIndex);
        } catch (IOException e) {
            log.error("Failed to read list of files");
        }
    }

    @FXML
    public void exitMenuOnActionHandler(ActionEvent actionEvent) {
        Platform.exit();
    }

    @FXML
    public void buttonCopyToServerOnActionHandler(ActionEvent actionEvent) {
        final String selectedItem = clientListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }
        final Path clientPath = clientDir.resolve(selectedItem);
        final Path serverPath = serverDir.resolve(selectedItem);
        if (Files.notExists(clientPath)) {
            return;
        }
        if (!socketChannel.pipeline().get(StatusHandler.class).getTaskMap().containsKey(serverPath)) {
            Thread thread = new Thread(() -> {
                try (SeekableByteChannel channel = Files.newByteChannel(clientPath, StandardOpenOption.READ)) {
                    ByteBuffer buffer = ByteBuffer.allocate(Configuration.BUFFER_SIZE);
                    byte[] array;
                    final long fileSize = channel.size();
                    long size = channel.read(buffer);
                    buffer.flip();
                    array = new byte[(int) size];
                    buffer.get(array);
                    socketChannel.writeAndFlush(new StartData(serverPath, size == -1 || fileSize == size, array)).sync();
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
                } catch (IOException | InterruptedException e) {
                    log.error("[ {} ] client failed to read the file", selectedItem);
                } finally {
                    socketChannel.pipeline().get(StatusHandler.class).getTaskMap().remove(serverPath);
                }
            });
            socketChannel.pipeline().get(StatusHandler.class).getTaskMap().put(serverPath, thread);
            thread.start();
        }
    }

    @FXML
    public void buttonCopyToClientOnActionHandler(ActionEvent actionEvent) {
        String selectedItem = serverListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }
        Path destination = clientDir.resolve(selectedItem).normalize().toAbsolutePath();
        Path source = serverDir.resolve(selectedItem);
        socketChannel.writeAndFlush(new FileRequest(source, destination));
    }
}
