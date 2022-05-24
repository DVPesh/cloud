package ru.peshekhonov.cloud.messages;

import lombok.Getter;

import java.nio.file.Path;

public class FileRenameRequest extends Message {

    private final String filename;
    @Getter
    private final String newFilename;

    public FileRenameRequest(Path filename, String newFilename) {
        this.filename = filename.toString();
        this.newFilename = newFilename;
    }

    public Path getFilename() {
        return Path.of(filename);
    }
}
