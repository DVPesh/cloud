package ru.peshekhonov.cloud.messages;

import lombok.Getter;

@Getter
public class FileInfoListRequest extends Message {

    private final String directory;

    public FileInfoListRequest(String directory) {
        this.directory = directory;
    }
}
