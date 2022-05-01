package ru.peshekhonov.cloud.messages;

import lombok.Getter;
import ru.peshekhonov.cloud.MessageType;

import java.nio.ByteBuffer;

@Getter
public class SubsequentData extends Message {

    private final String filename;
    private final boolean endOfFile;
    private final byte[] data;

    public SubsequentData(String filename, boolean endOfFile, byte[] data) {
        this.filename = filename;
        this.endOfFile = endOfFile;
        this.data = data;
    }

//    @Override
//    public MessageType getMessageType() {
//        return MessageType.CONTINUE;
//    }
}
