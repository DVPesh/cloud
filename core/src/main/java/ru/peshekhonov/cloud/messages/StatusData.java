package ru.peshekhonov.cloud.messages;

import lombok.Getter;
import ru.peshekhonov.cloud.MessageType;
import ru.peshekhonov.cloud.StatusType;

@Getter
public class StatusData extends Message {

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

    @Override
    public MessageType getMessageType() {
        return MessageType.STATUS;
    }
}
