package ru.peshekhonov.cloud.messages;

import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileListData extends Message {

    private final @Getter
    List<String> fileList;
    private final String directory;

    public FileListData(Path path) throws IOException {
        this.fileList = Files.list(path)
                .map(Path::getFileName)
                .map(Path::toString)
                .toList();
        this.directory = path.normalize().toAbsolutePath().toString();
    }

    public Path getDirectory() {
        return Path.of(directory);
    }
}
