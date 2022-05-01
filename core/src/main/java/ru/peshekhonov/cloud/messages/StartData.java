package ru.peshekhonov.cloud.messages;

import lombok.Getter;
import ru.peshekhonov.cloud.MessageType;

import java.nio.ByteBuffer;

@Getter
public class StartData extends Message {

    private final String filename;
    private final boolean endOfFile;
    private final byte[] data;

    public StartData(String filename, boolean endOfFile, byte[] data) {
        this.filename = filename;
        this.endOfFile = endOfFile;
        this.data = data;
    }

//    @Override
//    public MessageType getMessageType() {
//        return MessageType.START;
//    }
}
