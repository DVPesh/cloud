package ru.peshekhonov.cloud.frames;

import java.io.Serializable;

public class StartData implements Serializable {

    private final String filename;
    private final long length;
    private final byte[] data;

    public StartData(String filename, long length, byte[] data) {
        this.filename = filename;
        this.length = length;
        this.data = data;
    }

    public String getFilename() {
        return filename;
    }

    public long getLength() {
        return length;
    }

    public byte[] getData() {
        return data;
    }
}
