package ru.peshekhonov.cloud.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import ru.peshekhonov.cloud.Frame;
import ru.peshekhonov.cloud.network.Net;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class CloudController implements Initializable {

    @FXML
    public ListView<String> clientListView;
    @FXML
    public ListView<String> serverListView;
    @FXML
    public Button buttonCopyToServer;
    @FXML
    public Button buttonCopyToClient;

    private final static File CLIENT_DIRECTORY = new File("files");
    private final static int BUFFER_SIZE = 8192;

    private Net net;
    private ObservableList<String> clientFiles;
    private ObservableList<String> serverFiles;
    private long timestamp = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clientFiles = FXCollections.observableArrayList(CLIENT_DIRECTORY.list());
        clientListView.setItems(clientFiles);
        serverFiles = FXCollections.observableArrayList(new String[0]);
        serverListView.setItems(serverFiles);
        net = new Net(this);
    }

    public void updateServerListView(String[] serverFileList) {
        long time = timestamp;
        timestamp = System.currentTimeMillis();
        if (time != 0 && timestamp - time < 3000) {
            return;
        }
        int selectedIndex = serverListView.getSelectionModel().getSelectedIndex();
        serverFiles.setAll(serverFileList);
        serverListView.getSelectionModel().select(selectedIndex);
    }

    public void updateClientListView() {
        int selectedIndex = clientListView.getSelectionModel().getSelectedIndex();
        clientFiles.setAll(CLIENT_DIRECTORY.list());
        clientListView.getSelectionModel().select(selectedIndex);
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
