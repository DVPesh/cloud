package ru.peshekhonov.cloud.controllers;

import io.netty.channel.Channel;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import ru.peshekhonov.cloud.Client;
import ru.peshekhonov.cloud.FileInfo;
import ru.peshekhonov.cloud.messages.CreateDirectoryRequest;
import ru.peshekhonov.cloud.messages.FileDeleteRequest;
import ru.peshekhonov.cloud.messages.FileInfoListRequest;
import ru.peshekhonov.cloud.messages.FileRenameRequest;

import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
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
    private TableColumn<FileInfo, Double> loadFactorColumn;
    @Getter
    private Path currentPath = Path.of("");
    private Path previousPath;
    @Setter
    private Channel socketChannel;

    private enum Mode {
        RENAME, CREATE_DIR, REGULAR
    }

    private Mode mode;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Image dirImage = new Image("/folder-icon.png", 0, 15, true, false);
        Image fileImage = new Image("/file-icon.png", 0, 15, true, false);

        iconColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(
                param.getValue().getType() == FileInfo.FileType.DIRECTORY ? new ImageView(dirImage) : new ImageView(fileImage))
        );

        filenameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        filenameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        filenameColumn.setEditable(true);

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

        loadFactorColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getLoadFactor()));
        loadFactorColumn.setCellFactory(column -> new ProgressBarTableCell<FileInfo>() {
            @Override
            public void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (item < 0) setGraphic(null);
                }
            }
        });

        fileTable.setOnMouseClicked(event -> {
            FileInfo item = fileTable.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 2 && item != null) {
                showSelectedDirectoryList(item);
            }
        });

        fileTable.setOnKeyPressed(event -> {
            FileInfo item = fileTable.getSelectionModel().getSelectedItem();
            switch (event.getCode()) {
                case ENTER:
                    if (item != null) {
                        showSelectedDirectoryList(item);
                    }
                    break;
                case F3:
                    if (item != null) {
                        mode = Mode.RENAME;
                        fileTable.edit(fileTable.getSelectionModel().getSelectedIndex(), filenameColumn);
                    }
                    break;
                case F4:
                    if (socketChannel != null && currentPath != null) {
                        fileTable.getItems().add(0, new FileInfo());
                        fileTable.getSelectionModel().select(0);
                        mode = Mode.CREATE_DIR;
                        fileTable.edit(0, filenameColumn);
                    }
                    break;
                case DELETE:
                    if (item != null && socketChannel != null && currentPath != null) {
                        socketChannel.writeAndFlush(new FileDeleteRequest(currentPath.resolve(item.getFilename())));
                    }
            }
        });

        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (currentPath != null && socketChannel != null) {
                        requestFileInfoList(currentPath);
                    }
                });
            }
        }, 100, CloudController.FILES_INFO_LIST_UPDATE_PERIOD_MS);
    }

    private void requestFileInfoList(Path directory) {
        if (socketChannel != null) {
            socketChannel.writeAndFlush(new FileInfoListRequest(directory));
        }
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
            requestFileInfoList(currentPath);
        } catch (InvalidPathException e) {
            clearList();
        }
    }

    @FXML
    private void rootButtonOnActionHandler(ActionEvent actionEvent) {
        previousPath = currentPath;
        currentPath = Path.of("");
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
            requestFileInfoList(currentPath);
        } catch (InvalidPathException e) {
            clearList();
        }
    }

    @FXML
    private void upButtonOnActionHandler(ActionEvent actionEvent) {
        if (currentPath == null) {
            return;
        }
        Path parentPath = currentPath.getParent();
        previousPath = currentPath;
        if (parentPath == null) {
            currentPath = Path.of("");
        } else {
            currentPath = parentPath;
        }
        requestFileInfoList(currentPath);
    }

    @FXML
    private void filenameColumnOnEditCommitHandler(TableColumn.CellEditEvent<FileInfo, String> fileInfoStringCellEditEvent) {
        String filename = fileInfoStringCellEditEvent.getOldValue();
        String newFilename = fileInfoStringCellEditEvent.getNewValue();
        try {
            switch (mode) {
                case RENAME:
                    if (socketChannel != null && currentPath != null) {
                        socketChannel.writeAndFlush(new FileRenameRequest(currentPath.resolve(filename), newFilename));
                    }
                    break;
                case CREATE_DIR:
                    if (socketChannel != null && currentPath != null) {
                        socketChannel.writeAndFlush(new CreateDirectoryRequest(currentPath.resolve(newFilename)));
                    }
            }
        } catch (InvalidPathException e) {
            showAlertDialog(Alert.AlertType.WARNING, "Такое название не допустимо!");
        }
        mode = Mode.REGULAR;
    }

    private void showAlertDialog(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        Stage stage = Client.getInstance().getPrimaryStage();
        alert.setX(stage.getX() + (stage.getWidth() - Client.ALERT_WIDTH) / 2);
        alert.setY(stage.getY() + (stage.getHeight() - Client.ALERT_HEIGHT) / 2);
        alert.showAndWait();
    }
}
