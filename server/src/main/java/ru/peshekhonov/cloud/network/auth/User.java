package ru.peshekhonov.cloud.network.auth;

public class User {

    private final int id;
    private final String login;
    private final String password;
    private final String userName;

    public User(int id, String login, String password, String userName) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.userName = userName;
    }

    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }

}
