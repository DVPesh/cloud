package ru.peshekhonov.cloud.messages;

import lombok.Getter;

@Getter
public class AuthOk extends Message {

    private final String login;
    private final String username;

    public AuthOk(String login, String username) {
        this.login = login;
        this.username = username;
    }
}
