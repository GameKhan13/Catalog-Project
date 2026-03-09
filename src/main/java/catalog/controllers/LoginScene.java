package catalog.controllers;

import java.io.IOException;
import java.util.ArrayList;

import catalog.back_end.User;
import catalog.back_end.UserService;
import catalog.front_end.login.LoginPage;
import catalog.front_end.login.SignupPage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class LoginScene extends Scene {

    private final LoginPage loginPage;
    private final SignupPage signupPage;

    public LoginScene(UserService userService) {
        super(new Pane(), 800, 500);

        loginPage = new LoginPage();

        loginPage.signupButton.setOnAction(e -> {
            switchToSignUp();
        });

        loginPage.loginButton.setOnAction(e -> {
            String username = loginPage.usernameField.getText().trim();
            String password = loginPage.passwordField.getText();

            try {
                boolean validLogin = userService.loginUser(username, password);

                if (validLogin) {
                    ((Stage) getWindow()).setScene(new MainScene(userService.getCurrentUser()));
                } else {
                    showPopup("Login Failed", "Incorrect username or password.", Alert.AlertType.ERROR);
                }
            } catch (IOException ex) {
                showPopup("Error", "There was a problem reading the users file.", Alert.AlertType.ERROR);
                ex.printStackTrace();
            }
        });

        signupPage = new SignupPage();

        signupPage.loginButton.setOnAction(e -> {
            switchToLogin();
        });

        signupPage.signupButton.setOnAction(e -> {
            String username = signupPage.usernameField.getText().trim();
            String password = signupPage.passwordField.getText();

            User newUser = new User(username, password, false, new ArrayList<>());

            try {
                boolean signUpSuccess = userService.signUpUser(newUser);

                if (signUpSuccess) {
                    userService.loginUser(username, password);
                    ((Stage) getWindow()).setScene(new MainScene(userService.getCurrentUser()));
                } else {
                    showPopup("Signup Failed", "Username already exists.", Alert.AlertType.ERROR);
                }
            } catch (IOException ex) {
                showPopup("Error", "There was a problem updating the users file.", Alert.AlertType.ERROR);
                ex.printStackTrace();
            }
        });

        switchToLogin();
    }

    protected final void switchToSignUp () {
        setRoot(signupPage);
        signupPage.requestFocus();
    }

    protected final void switchToLogin () {
        setRoot(loginPage);
        loginPage.requestFocus();
    }
    private void showPopup(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
