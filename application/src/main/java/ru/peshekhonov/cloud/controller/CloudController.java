package ru.peshekhonov.cloud.controller;

import io.netty.channel.ChannelHandlerContext;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import lombok.extern.slf4j.Slf4j;
import ru.peshekhonov.cloud.messages.FileListRequest;
import ru.peshekhonov.cloud.network.NettyNet;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private NettyNet net;
    ChannelHandlerContext ctx;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clientFiles = FXCollections.observableArrayList(new String[0]);
        clientListView.setItems(clientFiles);
        serverFiles = FXCollections.observableArrayList(new String[0]);
        serverListView.setItems(serverFiles);

        net = new NettyNet();
        ctx = net.getPipeline().getChannelPipeline().context("outBoundEncoder");

        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    updateClientListView();
                    ctx.writeAndFlush(new FileListRequest());
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
        File file = new File(CLIENT_DIRECTORY, selectedItem);
        if (!file.exists()) {
            return;
        }
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            byte[] array = in.readNBytes(BUFFER_SIZE);
            net.sendFrame(Frame.createStartFrame(selectedItem, file.length(), array));
            array = in.readNBytes(BUFFER_SIZE);
            while (array.length != 0) {
                net.sendFrame(Frame.createContinueFrame(array));
                array = in.readNBytes(BUFFER_SIZE);
            }
        } catch (IOException e) {
            System.err.println("Failed to read file " + "\"" + selectedItem + "\"");
            e.printStackTrace();
        }
    }

    @FXML
    public void buttonCopyToClientOnActionHandler(ActionEvent actionEvent) {

    }
}
