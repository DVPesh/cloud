package ru.peshekhonov.cloud.messages;

import lombok.Getter;

@Getter
public class LoginRequest extends Message {

    private final String login;
    private final String password;

    public LoginRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }
}
