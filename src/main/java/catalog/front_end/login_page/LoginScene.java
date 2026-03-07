package catalog.front_end.login_page;

import java.util.LinkedList;

import catalog.User;
import catalog.front_end.main.MainScene;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class LoginScene extends Scene {

    private final LoginPage loginPage;
    private final SignupPage signupPage;

    public LoginScene() {
        super(new Pane(), 800, 500);

        loginPage = new LoginPage();

        loginPage.signupButton.setOnAction(e -> {
            switchToSignUp();
        });

        loginPage.loginButton.setOnAction(e -> {
            ((Stage) getWindow()).setScene(new MainScene(new User("Test", "123", true, new LinkedList<>()))); // Functionality for clicking the login button (Currently no checking is done)
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
