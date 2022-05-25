package ru.peshekhonov.cloud.messages;

import java.nio.file.Path;

public class FileMoveRequest extends Message {

    private final String source;
    private final String destination;

    public FileMoveRequest(Path source, Path destination) {
        this.source = source.toString();
        this.destination = destination.toString();
    }

    public Path getSource() {
        return Path.of(source);
    }

    public Path getDestination() {
        return Path.of(destination);
    }
}
