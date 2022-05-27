package ru.peshekhonov.cloud.messages;

import java.nio.file.Path;

public class CreateDirectoryRequest extends Message {

    private final String directory;

    public CreateDirectoryRequest(Path directory) {
        this.directory = directory.toString();
    }

    public Path getDirectory() {
        return Path.of(directory);
    }
}
