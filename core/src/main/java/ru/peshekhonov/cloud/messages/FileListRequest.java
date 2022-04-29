package ru.peshekhonov.cloud.messages;

import ru.peshekhonov.cloud.MessageType;

public class FileListRequest extends Message {

    @Override
    public MessageType getMessageType() {
        return MessageType.FILE_LIST_REQUEST;
    }
}
