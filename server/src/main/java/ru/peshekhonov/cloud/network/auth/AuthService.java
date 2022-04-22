package ru.peshekhonov.cloud.network.auth;

import java.sql.*;

public class AuthService {

    private final static String ERROR_MESSAGE = "Database access error occurs";
    private final static String DB_URL = "jdbc:sqlite:accounts.db";
    private final static String SQL_GET_USER = "SELECT id, username FROM users WHERE login = ? AND password = ?";
    private final static String SQL_SET_USERNAME = "UPDATE users SET username = ? WHERE id = ?";
    private final static String SQL_SET_LOGIN = "UPDATE users SET login = ? WHERE id = ?";
    private final static String SQL_SET_PASSWORD = "UPDATE users SET password = ? WHERE id = ?";
    private final static String SQL_CHECK_LOGIN = "SELECT login FROM users WHERE login = ?";
    private final static String SQL_CHECK_PASSWORD = "SELECT password FROM users WHERE password = ?";
    private final static String SQL_CHECK_ID = "SELECT id FROM users WHERE id = ?";
    private final static String SQL_CREATE_USER = "INSERT INTO users (login, password, username) VALUES (?, ?, ?)";

    private static PreparedStatement getUserPrepared;
    private static PreparedStatement setUsernamePrepared;
    private static PreparedStatement setLoginPrepared;
    private static PreparedStatement setPasswordPrepared;
    private static PreparedStatement checkLoginPrepared;
    private static PreparedStatement checkPasswordPrepared;
    private static PreparedStatement checkIdPrepared;
    private static PreparedStatement createUserPrepared;
    private static Connection connection;

    public static void start() throws SQLException {
        connection = DriverManager.getConnection(DB_URL);
        getUserPrepared = connection.prepareStatement(SQL_GET_USER);
        setUsernamePrepared = connection.prepareStatement(SQL_SET_USERNAME);
        setLoginPrepared = connection.prepareStatement(SQL_SET_LOGIN);
        setPasswordPrepared = connection.prepareStatement(SQL_SET_PASSWORD);
        checkLoginPrepared = connection.prepareStatement(SQL_CHECK_LOGIN);
        checkPasswordPrepared = connection.prepareStatement(SQL_CHECK_PASSWORD);
        checkIdPrepared = connection.prepareStatement(SQL_CHECK_ID);
        createUserPrepared = connection.prepareStatement(SQL_CREATE_USER);
    }

    public static void stop() {
        try {
            if (getUserPrepared != null) {
                getUserPrepared.close();
            }
        } catch (SQLException e) {
            System.err.println(ERROR_MESSAGE);
            e.printStackTrace();
        }
        try {
            if (setUsernamePrepared != null) {
                setUsernamePrepared.close();
            }
        } catch (SQLException e) {
            System.err.println(ERROR_MESSAGE);
            e.printStackTrace();
        }
        try {
            if (setLoginPrepared != null) {
                setLoginPrepared.close();
            }
        } catch (SQLException e) {
            System.err.println(ERROR_MESSAGE);
            e.printStackTrace();
        }
        try {
            if (setPasswordPrepared != null) {
                setPasswordPrepared.close();
            }
        } catch (SQLException e) {
            System.err.println(ERROR_MESSAGE);
            e.printStackTrace();
        }
        try {
            if (checkLoginPrepared != null) {
                checkLoginPrepared.close();
            }
        } catch (SQLException e) {
            System.err.println(ERROR_MESSAGE);
            e.printStackTrace();
        }
        try {
            if (checkPasswordPrepared != null) {
                checkPasswordPrepared.close();
            }
        } catch (SQLException e) {
            System.err.println(ERROR_MESSAGE);
            e.printStackTrace();
        }
        try {
            if (checkIdPrepared != null) {
                checkIdPrepared.close();
            }
        } catch (SQLException e) {
            System.err.println(ERROR_MESSAGE);
            e.printStackTrace();
        }
        try {
            if (createUserPrepared != null) {
                createUserPrepared.close();
            }
        } catch (SQLException e) {
            System.err.println(ERROR_MESSAGE);
            e.printStackTrace();
        }
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println(ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public static User getUserByLoginAndPassword(String login, String password) throws SQLException {
        getUserPrepared.setString(1, login);
        getUserPrepared.setString(2, password);
        ResultSet resultSet = getUserPrepared.executeQuery();
        if (resultSet.next()) {
            return new User(resultSet.getInt("id"), resultSet.getString("login"),
                    resultSet.getString("password"), resultSet.getString("username"));
        }
        return null;
    }

    public static synchronized void createUser(String login, String password, String username) throws SQLException {
        createUserPrepared.setString(1, login);
        createUserPrepared.setString(2, password);
        createUserPrepared.setString(3, username);
        createUserPrepared.executeUpdate();
    }

    public static boolean doesLoginExist(String login) throws SQLException {
        checkLoginPrepared.setString(1, login);
        ResultSet resultSet = checkLoginPrepared.executeQuery();
        return resultSet.next();
    }

    public static boolean doesPasswordExist(String password) throws SQLException {
        checkPasswordPrepared.setString(1, password);
        ResultSet resultSet = checkPasswordPrepared.executeQuery();
        return resultSet.next();
    }

    public static boolean doesIdExist(int id) throws SQLException {
        checkIdPrepared.setInt(1, id);
        ResultSet resultSet = checkIdPrepared.executeQuery();
        return resultSet.next();
    }
}
