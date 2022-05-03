package ru.peshekhonov.cloud.messages;

import lombok.Getter;
import ru.peshekhonov.cloud.StatusType;

import java.nio.file.Path;

@Getter
public class StatusData extends Message {

    private final Path path;
    private final StatusType status;
    private final String message;

    public StatusData(Path path, StatusType status, String message) {
        this.path = path;
        this.status = status;
        this.message = message;
    }

    public StatusData(Path path, StatusType status) {
        this(path, status, "");
    }

}
