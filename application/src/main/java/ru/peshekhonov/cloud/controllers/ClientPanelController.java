package ru.peshekhonov.cloud.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import lombok.Getter;
import ru.peshekhonov.cloud.Client;
import ru.peshekhonov.cloud.FileInfo;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

public class ClientPanelController implements Initializable {

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
    private Path currentPath;

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

        updateList(Path.of("."));

        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (currentPath != null && fileTable.getEditingCell() == null) {
                        int selectedIndex = fileTable.getSelectionModel().getSelectedIndex();
                        updateList();
                        fileTable.getSelectionModel().select(selectedIndex);
                    } else if (currentPath == null && !filenameColumn.isEditable()) {
                        int selectedIndex = fileTable.getSelectionModel().getSelectedIndex();
                        updateDiscs();
                        fileTable.getSelectionModel().select(selectedIndex);
                    }
                });
            }
        }, 100, 3000);
    }

    private void showSelectedDirectoryList(FileInfo item) {
        try {
            Path path = Paths.get(textField.getText()).resolve(item.getFilename());
            if (Files.isDirectory(path)) {
                updateList(path);
            } else if (Files.notExists(path)) {
                clearList();
            }
        } catch (InvalidPathException e) {
            clearList();
        }
    }

    private void clearList() {
        currentPath = null;
        textField.clear();
        fileTable.getItems().clear();
    }

    private void updateList() {
        if (currentPath != null) {
            updateList(currentPath);
        }
    }

    private void updateList(Path path) {
        filenameColumn.setEditable(true);
        try (Stream<Path> pathStream = Files.list(path)) {
            currentPath = path.normalize().toAbsolutePath();
            if (!textField.isFocused()) {
                textField.setText(currentPath.toString());
            }
            fileTable.getItems().clear();
            for (Path element : pathStream.toList()) {      //нельзя использовать Stream API из-за IOException!
                fileTable.getItems().add(new FileInfo(element));
            }
            fileTable.sort();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось обновить список файлов клиента", ButtonType.OK);
            Stage stage = (Stage) fileTable.getScene().getWindow();
            alert.setX(stage.getX() + (stage.getWidth() - Client.ALERT_WIDTH) / 2);
            alert.setY(stage.getY() + (stage.getHeight() - Client.ALERT_HEIGHT) / 2);
            alert.showAndWait();
        }
    }

    @FXML
    private void rootButtonOnActionHandler(ActionEvent actionEvent) {
        filenameColumn.setEditable(false);
        updateDiscs();
    }

    private void updateDiscs() {
        try {
            clearList();
            for (Path path : FileSystems.getDefault().getRootDirectories()) {
                if (Files.exists(path)) {
                    fileTable.getItems().add(new FileInfo(path));
                }
            }
        } catch (IOException e) {
            clearList();
        }
    }

    @FXML
    private void textFieldOnActionHandler(ActionEvent actionEvent) {
        try {
            Path path = Path.of(textField.getText());
            if (Files.isDirectory(path)) {
                updateList(path);
            } else {
                clearList();
            }
        } catch (InvalidPathException e) {
            clearList();
        }
    }

    @FXML
    private void upButtonOnActionHandler(ActionEvent actionEvent) {
        try {
            Path path = Paths.get(textField.getText());
            Path parentPath = path.getParent();
            if (parentPath == null && Files.isDirectory(path)) {
                updateList(path);
                return;
            }
            if (parentPath != null && Files.isDirectory(parentPath)) {
                updateList(parentPath);
            } else {
                clearList();
            }
        } catch (InvalidPathException e) {
            clearList();
        }
    }

    @FXML
    private void filenameColumnOnEditCommitHandler(TableColumn.CellEditEvent<FileInfo, String> fileInfoStringCellEditEvent) {

    }
}
