package ru.peshekhonov.cloud.messages;

import lombok.Getter;
import ru.peshekhonov.cloud.StatusType;

import java.nio.file.Path;

public class StatusData extends Message {

    private final String path;
    private final @Getter
    StatusType status;
    private final @Getter
    String message;

    public StatusData(Path path, StatusType status, String message) {
        this.path = path.toString();
        this.status = status;
        this.message = message;
    }

    public StatusData(Path path, StatusType status) {
        this(path, status, "");
    }

    public Path getPath() {
        return Path.of(path);
    }
}
