package com.plannerapp.plannerapp.Controllers.LoginWindow;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.plannerapp.plannerapp.Scenes.SceneManager;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CreateAccountController {
    public Button create_acc_btn;
    public PasswordField password_fld;
    public TextField newusername_fld;
    public Label error_lbl;
    public Button back_btn;

    private final SceneManager sceneManager = new SceneManager();
    private static final String DATABASE_URL = "jdbc:sqlite:PlannerAppDB.db";
    private static final String DUPLICATE_USERNAME_ERROR = "Error: Username already exists.";
    private static final String INVALID_INPUT_ERROR = "Error: Username or Password must be entered";

    private boolean isInputValid(String username, String password) {
        return username != null && password != null && !username.trim().isEmpty() && !password.trim().isEmpty();
    }

    private Connection establishConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }

    private boolean isUsernameUnique(Connection connection, String username) throws SQLException {
        String sql = "SELECT 1 FROM Users WHERE username = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return !rs.next(); // Returns true if no rows found (username is unique)
            }
        }
    }

    private void showError(String message) {
        error_lbl.setText(message);
        error_lbl.setVisible(true);
    }

    private boolean addUserToDatabase(String username, String password) {
        if (!isInputValid(username, password)) {
            showError(INVALID_INPUT_ERROR);
            return false;
        }

        String trimmedUsername = username.trim();
        String trimmedPassword = password.trim();

        try (Connection connection = establishConnection()) {
            if (!isUsernameUnique(connection, trimmedUsername)) {
                showError(DUPLICATE_USERNAME_ERROR);
                return false;
            }

            String insertSql = "INSERT INTO Users (username, password) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
                stmt.setString(1, trimmedUsername);
                stmt.setString(2, trimmedPassword);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            handleSQLException(e);
            return false;
        }
    }

    private void handleSQLException(SQLException e) {
        // SQLite constraint violation for unique username
        if ("23000".equals(e.getSQLState()) && e.getErrorCode() == 19) {
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("ErrorCode: " + e.getErrorCode());
            showError(DUPLICATE_USERNAME_ERROR);
        } else {
            e.printStackTrace();
        }
    }

    public void userCreate(ActionEvent event) {
        if (addUserToDatabase(newusername_fld.getText(), password_fld.getText())) {
            closeAndShowLogin();
        }
    }

    public void onBack_btn(ActionEvent event) {
        closeAndShowLogin();
    }

    private void closeAndShowLogin() {
        Stage stage = (Stage) error_lbl.getScene().getWindow();
        sceneManager.closeStage(stage);
        sceneManager.showLogin();
    }
}