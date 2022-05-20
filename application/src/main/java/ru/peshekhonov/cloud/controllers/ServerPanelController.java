package ru.peshekhonov.cloud.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import ru.peshekhonov.cloud.Client;
import ru.peshekhonov.cloud.FileInfo;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;

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


    }

    private void clearList() {
        currentPath = null;
        textField.clear();
        fileTable.getItems().clear();
    }

    public void updateList(List<FileInfo> fileInfoList, Path directory) {
        if (directory.equals(currentPath)) {
            textField.setText(directory.toString());
            fileTable.getItems().setAll(fileInfoList);
            fileTable.sort();
        }
    }

    @FXML
    public void rootButtonOnActionHandler(ActionEvent actionEvent) {

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
