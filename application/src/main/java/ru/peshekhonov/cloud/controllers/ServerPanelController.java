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
import lombok.Getter;
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
    private TextField textField;
    @FXML
    @Getter
    private TableView<FileInfo> fileTable;
    @FXML
    private TableColumn<FileInfo, ImageView> iconColumn;
    @FXML
    private TableColumn<FileInfo, String> filenameColumn;
    @FXML
    private TableColumn<FileInfo, Long> fileSizeColumn;
    @FXML
    private TableColumn<FileInfo, String> lastModifiedColumn;
    @FXML
    private TableColumn<FileInfo, Long> loadFactorColumn;
    @Getter
    private Path currentPath = Path.of("user");
    private Path previousPath;
    @Setter
    private Channel socketChannel;

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

    public void clearList() {
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
        if (!textField.isFocused()) {
            textField.setText(directory.toString());
        }
        fileTable.getItems().setAll(fileInfoList);
        fileTable.sort();
    }

    private void showSelectedDirectoryList(FileInfo item) {
        if (item.getType() != FileInfo.FileType.DIRECTORY) {
            return;
        }
        if (currentPath == null) {
            clearList();
            return;
        }
        try {
            Path path = currentPath.resolve(item.getFilename());
            previousPath = currentPath;
            currentPath = path;
            filenameColumn.setEditable(true);
            requestFileInfoList(currentPath);
        } catch (InvalidPathException e) {
            clearList();
        }
    }

    @FXML
    private void rootButtonOnActionHandler(ActionEvent actionEvent) {
        filenameColumn.setEditable(false);
        previousPath = currentPath;
        currentPath = Path.of("");
        requestFileInfoList(currentPath);
    }

    @FXML
    private void workingDirectoryButtonOnActionHandler(ActionEvent actionEvent) {
        filenameColumn.setEditable(true);
        previousPath = currentPath;
        currentPath = Path.of("user");
        requestFileInfoList(currentPath);
    }

    @FXML
    private void textFieldOnActionHandler(ActionEvent actionEvent) {
        try {
            String str = textField.getText();
            Path path = Path.of(str).normalize();
            if (path.isAbsolute()) {
                clearList();
                return;
            }
            previousPath = currentPath;
            currentPath = path;
            filenameColumn.setEditable(!str.equals(""));
            requestFileInfoList(currentPath);
        } catch (InvalidPathException e) {
            clearList();
        }
    }

    @FXML
    private void upButtonOnActionHandler(ActionEvent actionEvent) {
        Path parentPath = currentPath.getParent();
        previousPath = currentPath;
        if (parentPath == null) {
            currentPath = Path.of("");
            filenameColumn.setEditable(false);
        } else {
            currentPath = parentPath;
            filenameColumn.setEditable(true);
        }
        requestFileInfoList(currentPath);
    }

    @FXML
    private void filenameColumnOnEditCommitHandler(TableColumn.CellEditEvent<FileInfo, String> fileInfoStringCellEditEvent) {

    }
}
