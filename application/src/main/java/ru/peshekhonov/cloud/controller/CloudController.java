package ru.peshekhonov.cloud.controller;

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
import ru.peshekhonov.cloud.messages.FileListRequest;
import ru.peshekhonov.cloud.messages.StartData;
import ru.peshekhonov.cloud.messages.SubsequentData;
import ru.peshekhonov.cloud.network.NettyNet;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

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

    private final static Path CLIENT_DIRECTORY = Path.of("files");
    private final static int BUFFER_SIZE = 8192;

    private ObservableList<String> clientFiles;
    private ObservableList<String> serverFiles;
    private @Getter
    NettyNet net;
    private @Setter
    Channel socketChannel;

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
            clientFiles.setAll(Files.list(CLIENT_DIRECTORY).map(Path::getFileName).map(Path::toString).toList());
            clientListView.getSelectionModel().select(selectedIndex);
        } catch (IOException e) {
            log.error("Fail to read list of files");
        }
    }

    @FXML
    public void exitMenuOnActionHandler(ActionEvent actionEvent) {
        Platform.exit();
    }

    @FXML
    public void buttonCopyToServerOnActionHandler(ActionEvent actionEvent) {
        String selectedItem = clientListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }
        Path path = CLIENT_DIRECTORY.resolve(selectedItem);
        if (Files.notExists(path)) {
            return;
        }
        try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            byte[] array;
            final long fileSize = channel.size();
            long size = channel.read(buffer);
            buffer.flip();
            array = new byte[(int) size];
            buffer.get(array);
            socketChannel.writeAndFlush(new StartData(selectedItem, size == -1 || fileSize == size, array)).sync();
            buffer.clear();
            int length;
            while ((length = channel.read(buffer)) != -1) {
                size += length;
                buffer.flip();
                if (length == BUFFER_SIZE) {
                    buffer.get(array);
                    socketChannel.writeAndFlush(new SubsequentData(selectedItem, fileSize == size, array)).sync();
                    buffer.clear();
                } else {
                    array = new byte[length];
                    buffer.get(array);
                    socketChannel.writeAndFlush(new SubsequentData(selectedItem, true, array)).sync();
                    buffer.clear();
                }
            }
        } catch (IOException e) {
            log.error("Failed to read file " + "\"" + selectedItem + "\"");
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    @FXML
    public void buttonCopyToClientOnActionHandler(ActionEvent actionEvent) {

    }
}
