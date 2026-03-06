package catalog.front_end;

import javafx.scene.Scene;
import javafx.scene.layout.GridPane;

public class LoginScene extends Scene {

    private final LoginPage loginPage;
    private final SignupPage signupPage;

    public LoginScene() {
        super(new GridPane(), 1500, 1000);

        loginPage = new LoginPage();

        loginPage.signupButton.setOnAction(e -> {
            switchToSignUp();
        });

        loginPage.loginButton.setOnAction(e -> {
            // Functionallity for clicking the login button
        });

        signupPage = new SignupPage();

        signupPage.loginButton.setOnAction(e -> {
            switchToLogin();
        });

        signupPage.signupButton.setOnAction(e -> {
            // Functionallity for clicking the signup button
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
}
