package ru.peshekhonov.cloud.controllers;

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
import ru.peshekhonov.cloud.Metadata;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
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
    private TableColumn<FileInfo, Double> loadFactorColumn;
    @Getter
    private Path currentPath;
    @Setter
    private Map<Path, Metadata> startHandlerMap;

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
                    if (currentPath != null && !currentPath.equals(Path.of(""))) {
                        fileTable.getItems().add(0, new FileInfo());
                        fileTable.getSelectionModel().select(0);
                        mode = Mode.CREATE_DIR;
                        fileTable.edit(0, filenameColumn);
                    }
                    break;
                case DELETE:
                    try {
                        if (currentPath != null && item != null) {
                            Files.deleteIfExists(currentPath.resolve(item.getFilename()));
                        }
                    } catch (IOException e) {
                        String message = e instanceof DirectoryNotEmptyException ? "Невозможно удалить непустую директорию" : "Не удалось удалить файл";
                        Alert.AlertType alertType = e instanceof DirectoryNotEmptyException ? Alert.AlertType.WARNING : Alert.AlertType.ERROR;
                        showAlertDialog(alertType, message);
                    }
            }
        });

        updateList(Path.of("."));

        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (currentPath == null) {
                        return;
                    }
                    if (currentPath.equals(Path.of(""))) {
                        int selectedIndex = fileTable.getSelectionModel().getSelectedIndex();
                        updateDiscs();
                        fileTable.getSelectionModel().select(selectedIndex);
                    } else if (fileTable.getEditingCell() == null) {
                        int selectedIndex = fileTable.getSelectionModel().getSelectedIndex();
                        updateList();
                        fileTable.getSelectionModel().select(selectedIndex);
                    }
                });
            }
        }, 100, CloudController.FILES_INFO_LIST_UPDATE_PERIOD_MS);
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
        if (!textField.isFocused()) {
            textField.clear();
        }
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
                fileTable.getItems().add(new FileInfo(element, startHandlerMap));
            }
            fileTable.sort();
        } catch (Exception e) {
            showAlertDialog(Alert.AlertType.WARNING, "Не удалось обновить список файлов клиента");
        }
    }

    @FXML
    private void rootButtonOnActionHandler(ActionEvent actionEvent) {
        currentPath = Path.of("");
        filenameColumn.setEditable(false);
        updateDiscs();
    }

    private void updateDiscs() {
        if (currentPath == null) {
            return;
        }
        try {
            clearList();
            for (Path path : FileSystems.getDefault().getRootDirectories()) {
                if (Files.exists(path)) {
                    fileTable.getItems().add(new FileInfo(path));
                }
            }
            currentPath = Path.of("");
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
        if (currentPath == null) {
            mode = Mode.REGULAR;
            return;
        }
        if (mode == Mode.RENAME) {
            try {
                String filename = fileInfoStringCellEditEvent.getOldValue();
                Path path = currentPath.resolve(filename);
                Files.move(path, path.resolveSibling(fileInfoStringCellEditEvent.getNewValue()));
            } catch (Exception e) {
                showAlertDialog(Alert.AlertType.ERROR, "Не удалось переименовать файл");
            }
        } else if (mode == Mode.CREATE_DIR) {
            try {
                Files.createDirectory(currentPath.resolve(fileInfoStringCellEditEvent.getNewValue()));
            } catch (FileAlreadyExistsException e) {
                showAlertDialog(Alert.AlertType.WARNING, "Файл с таким названием уже существует");
            } catch (InvalidPathException e) {
                showAlertDialog(Alert.AlertType.WARNING, "Такое название не допустимо!");
            } catch (IOException e) {
                showAlertDialog(Alert.AlertType.ERROR, "Не удалось создать директорию");
            }
        }
        mode = Mode.REGULAR;
    }

    private void showAlertDialog(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        Stage stage = (Stage) fileTable.getScene().getWindow();
        alert.setX(stage.getX() + (stage.getWidth() - Client.ALERT_WIDTH) / 2);
        alert.setY(stage.getY() + (stage.getHeight() - Client.ALERT_HEIGHT) / 2);
        alert.showAndWait();
    }
}
