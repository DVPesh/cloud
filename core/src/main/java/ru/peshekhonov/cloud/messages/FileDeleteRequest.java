package ru.peshekhonov.cloud.messages;

import java.nio.file.Path;

public class FileDeleteRequest extends Message {

    private final String filename;

    public FileDeleteRequest(Path path) {
        filename = path.toString();
    }

    public Path getPath() {
        return Path.of(filename);
    }
}
