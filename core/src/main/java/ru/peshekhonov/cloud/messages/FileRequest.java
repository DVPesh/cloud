package ru.peshekhonov.cloud.messages;

import lombok.Getter;

import java.nio.file.Path;

@Getter
public class FileRequest extends Message {

    private final Path path;

    public FileRequest(Path path) {
        this.path = path;
    }
}
