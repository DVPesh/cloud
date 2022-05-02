package ru.peshekhonov.cloud.messages;

import lombok.Getter;

@Getter
public class FileRequest extends Message {

    private final String filename;

    public FileRequest(String filename) {
        this.filename = filename;
    }
}
