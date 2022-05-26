package ru.peshekhonov.cloud.messages;

import lombok.Getter;

@Getter
public class RegisterRequest extends Message {

    private final String username;
    private final String login;
    private final String password;

    public RegisterRequest(String username, String login, String password) {
        this.username = username;
        this.login = login;
        this.password = password;
    }
}
