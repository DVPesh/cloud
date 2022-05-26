package ru.peshekhonov.cloud.messages;

import lombok.Getter;
import ru.peshekhonov.cloud.StatusType;

@Getter
public class AuthNotOk extends Message {

    private final StatusType type;

    public AuthNotOk(StatusType type) {
        this.type = type;
    }
}
