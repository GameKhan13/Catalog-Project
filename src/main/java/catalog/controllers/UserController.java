package catalog.controllers;

import java.io.IOException;
import java.util.ArrayList;

import catalog.back_end.User;
import catalog.back_end.UserService;
import catalog.front_end.LoginPage;
import catalog.front_end.LoginScene;
import catalog.front_end.SignupPage;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UserController {

    private final LoginScene loginScene;
    private final UserService userService;
    private final Stage stage;

    public UserController(LoginScene loginScene, UserService userService, Stage stage) {
        this.loginScene = loginScene;
        this.userService = userService;
        this.stage = stage;

        // attach functionality to whatever page is currently showing
        attachHandlersToCurrentRoot();

        // whenever LoginScene switches between LoginPage and SignupPage,
        // re-attach the correct handlers
        this.loginScene.rootProperty().addListener((obs, oldRoot, newRoot) -> {
            attachHandlersToCurrentRoot();
        });
    }

    private void attachHandlersToCurrentRoot() {
        if (loginScene.getRoot() instanceof LoginPage loginPage) {
            loginPage.loginButton.setOnAction(e -> handleLogin(loginPage));
            // do NOT touch signupButton here
            // LoginScene already uses it to switch to signup
        }

        if (loginScene.getRoot() instanceof SignupPage signupPage) {
            signupPage.signupButton.setOnAction(e -> handleSignup(signupPage));
            // do NOT touch loginButton here
            // LoginScene already uses it to switch back to login
        }
    }

    private void handleLogin(LoginPage loginPage) {
        String username = loginPage.usernameField.getText().trim();
        String password = loginPage.passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        try {
            boolean validLogin = userService.loginUser(username, password);

            if (validLogin) {
                showSongsPlaceholderScene(username);
            } else {
                showError("Incorrect username or password.");
                loginPage.passwordField.clear();
            }

        } catch (IOException e) {
            showError("There was a problem reading Users.csv.");
            e.printStackTrace();
        }
    }

    private void handleSignup(SignupPage signupPage) {
        String username = signupPage.usernameField.getText().trim();
        String password = signupPage.passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both a username and password.");
            return;
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setAdmin(false);
        newUser.setPlaylistIds(new ArrayList<>());

        try {
            boolean signUpSuccess = userService.signUpUser(newUser);

            if (signUpSuccess) {
                showInfo("Account created successfully. Please log in.");

                // use the existing frontend navigation
                signupPage.loginButton.fire();

                // after switching back to login, optionally prefill username
                if (loginScene.getRoot() instanceof LoginPage loginPage) {
                    loginPage.usernameField.setText(username);
                    loginPage.passwordField.clear();
                }

            } else {
                showError("That username already exists.");
            }

        } catch (IOException e) {
            showError("There was a problem writing to Users.csv.");
            e.printStackTrace();
        }
    }

    private void showSongsPlaceholderScene(String username) {
        Label welcomeLabel = new Label("Welcome, " + username + "!");
        Label infoLabel = new Label("Songs page placeholder");

        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            userService.logout();
            stage.setScene(loginScene);

            if (loginScene.getRoot() instanceof LoginPage loginPage) {
                loginPage.usernameField.clear();
                loginPage.passwordField.clear();
            }
        });

        VBox layout = new VBox(20, welcomeLabel, infoLabel, logoutButton);
        layout.setAlignment(Pos.CENTER);

        Scene songsScene = new Scene(layout, 1500, 1000);
        stage.setScene(songsScene);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}