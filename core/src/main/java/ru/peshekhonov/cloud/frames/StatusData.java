package ru.peshekhonov.cloud.frames;

import ru.peshekhonov.cloud.StatusType;

import java.io.Serializable;

public class StatusData implements Serializable {

    private final String filename;
    private final StatusType status;
    private final String message;

    public StatusData(String filename, StatusType status, String message) {
        this.filename = filename;
        this.status = status;
        this.message = message;
    }

    public StatusData(String filename, StatusType status) {
        this(filename, status, "");
    }

    public String getFilename() {
        return filename;
    }

    public StatusType getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
