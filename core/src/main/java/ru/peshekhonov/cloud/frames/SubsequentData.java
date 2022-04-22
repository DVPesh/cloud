package ru.peshekhonov.cloud.frames;

import java.io.Serializable;

public class SubsequentData implements Serializable {

    private final byte[] data;

    public SubsequentData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
}
