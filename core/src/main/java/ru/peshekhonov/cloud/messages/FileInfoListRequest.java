package ru.peshekhonov.cloud.messages;

import java.nio.file.Path;

public class FileInfoListRequest extends Message {

    private final String directory;

    public FileInfoListRequest(Path directory) {
        this.directory = directory.toString();
    }

    public Path getDirectory() {
        return Path.of(directory);
    }
}
