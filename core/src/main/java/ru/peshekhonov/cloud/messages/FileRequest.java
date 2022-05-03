package ru.peshekhonov.cloud.messages;

import lombok.Getter;

import java.nio.file.Path;

@Getter
public class FileRequest extends Message {

    private final Path source;
    private final Path destination;

    public FileRequest(Path source, Path destination) {
        this.source = source;
        this.destination = destination;
    }
}
