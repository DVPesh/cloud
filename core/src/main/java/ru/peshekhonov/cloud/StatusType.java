package ru.peshekhonov.cloud;

import lombok.Getter;

@Getter
public enum StatusType {
    OK("the file was successfully copied"),
    HANDLED_ERROR1("I/O error"),
    HANDLED_ERROR2("the file already exists"),
    HANDLED_ERROR3("SubsequentData timeout"),
    HANDLED_ERROR4("files information list cannot be obtained"),
    ERROR1("the file does not exist on server"),
    ERROR2("server failed to read the file");

    private final String text;

    StatusType(String text) {
        this.text = text;
    }
}
