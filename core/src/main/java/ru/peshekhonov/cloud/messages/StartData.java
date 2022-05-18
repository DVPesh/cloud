package ru.peshekhonov.cloud.messages;

import lombok.Getter;

import java.nio.file.Path;

public class StartData extends Message {

    private final String path;
    private final @Getter
    boolean endOfFile;
    private final @Getter
    byte[] data;

    public StartData(Path path, boolean endOfFile, byte[] data) {
        this.path = path.toString();
        this.endOfFile = endOfFile;
        this.data = data;
    }

    public Path getPath() {
        return Path.of(path);
    }
}
