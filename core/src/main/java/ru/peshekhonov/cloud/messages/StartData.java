package ru.peshekhonov.cloud.messages;

import lombok.Getter;

import java.nio.file.Path;

@Getter
public class StartData extends Message {

    private final Path path;
    private final boolean endOfFile;
    private final byte[] data;

    public StartData(Path path, boolean endOfFile, byte[] data) {
        this.path = path;
        this.endOfFile = endOfFile;
        this.data = data;
    }
}
