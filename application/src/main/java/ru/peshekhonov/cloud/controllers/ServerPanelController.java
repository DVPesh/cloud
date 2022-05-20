package ru.peshekhonov.cloud.controllers;

import io.netty.channel.Channel;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import lombok.Setter;
import ru.peshekhonov.cloud.FileInfo;
import ru.peshekhonov.cloud.messages.FileInfoListRequest;

import java.net.URL;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class ServerPanelController implements Initializable {

    @FXML
    public TextField textField;
    @FXML
    public TableView<FileInfo> fileTable;
    @FXML
    public TableColumn<FileInfo, ImageView> iconColumn;
    @FXML
    public TableColumn<FileInfo, String> filenameColumn;
    @FXML
    public TableColumn<FileInfo, Long> fileSizeColumn;
    @FXML
    public TableColumn<FileInfo, String> lastModifiedColumn;
    @FXML
    public TableColumn<FileInfo, Long> loadFactorColumn;

    private Path currentPath, previousPath;

    private @Setter
    Channel socketChannel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Image dirImage = new Image("/folder-icon.png", 0, 15, true, false);
        Image fileImage = new Image("/file-icon.png", 0, 15, true, false);

        iconColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(
                param.getValue().getType() == FileInfo.FileType.DIRECTORY ? new ImageView(dirImage) : new ImageView(fileImage))
        );

        filenameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        filenameColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setCellFactory(column -> new TableCell<FileInfo, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    String text = String.format("%,d bytes", item);
                    if (item == -1L) {
                        text = "";
                    }
                    setText(text);
                }
            }
        });

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        lastModifiedColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtf)));

        fileTable.setOnMouseClicked(event -> {
            FileInfo item = fileTable.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 2 && item != null) {
                showSelectedDirectoryList(item);
            }
        });

        fileTable.setOnKeyPressed(event -> {
            FileInfo item = fileTable.getSelectionModel().getSelectedItem();
            if (item == null) {
                return;
            }
            if (event.getCode() == KeyCode.ENTER) {
                showSelectedDirectoryList(item);
            } else if (event.getCode() == KeyCode.F3) {
                fileTable.edit(fileTable.getSelectionModel().getSelectedIndex(), filenameColumn);
            }
        });

        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (currentPath != null) {
                        requestFileInfoList(currentPath);
                    }
                });
            }
        }, 100, 3000);
    }

    private void requestFileInfoList(Path directory) {
        socketChannel.writeAndFlush(new FileInfoListRequest(directory));
    }

    private void clearList() {
        previousPath = null;
        currentPath = null;
        textField.clear();
        fileTable.getItems().clear();
    }

    public void updateListAndPreserveSelection(List<FileInfo> fileInfoList, Path directory) {
        if (currentPath == null || !currentPath.equals(previousPath)) {
            updateList(fileInfoList, directory);
        } else if (directory.equals(currentPath) && fileTable.getEditingCell() == null) {
            int selectedIndex = fileTable.getSelectionModel().getSelectedIndex();
            updateList(fileInfoList, directory);
            fileTable.getSelectionModel().select(selectedIndex);
        }
    }

    private void updateList(List<FileInfo> fileInfoList, Path directory) {
        previousPath = currentPath;
        currentPath = directory;
        textField.setText(directory.toString());
        fileTable.getItems().setAll(fileInfoList);
        fileTable.sort();
    }

    private void showSelectedDirectoryList(FileInfo item) {
        if (item.getType() != FileInfo.FileType.DIRECTORY) {
            return;
        }
        try {
            Path path = Paths.get(textField.getText()).resolve(item.getFilename()).normalize();
            previousPath = currentPath;
            currentPath = path;
            filenameColumn.setEditable(true);
            requestFileInfoList(currentPath);
        } catch (InvalidPathException e) {
            clearList();
        }
    }

    @FXML
    public void rootButtonOnActionHandler(ActionEvent actionEvent) {
        filenameColumn.setEditable(false);
        previousPath = currentPath;
        currentPath = Path.of("");
        requestFileInfoList(currentPath);
    }

    @FXML
    public void workingDirectoryButtonOnActionHandler(ActionEvent actionEvent) {
        filenameColumn.setEditable(true);
        previousPath = currentPath;
        currentPath = Path.of("user");
        requestFileInfoList(currentPath);
    }

    @FXML
    public void textFieldOnActionHandler(ActionEvent actionEvent) {

    }

    @FXML
    public void upButtonOnActionHandler(ActionEvent actionEvent) {

    }

    @FXML
    public void filenameColumnOnEditCommitHandler(TableColumn.CellEditEvent<FileInfo, String> fileInfoStringCellEditEvent) {

    }
}
