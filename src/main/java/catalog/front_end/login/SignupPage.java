package catalog.front_end.login;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class SignupPage extends GridPane {

    public final TextField usernameField = new TextField();
    public final PasswordField passwordField = new PasswordField();

    public final Button loginButton = new Button();
    public final Button signupButton = new Button();

    public SignupPage() {
        setStyle("-fx-background-color: #2b2b2b;");

        setAlignment(Pos.CENTER);
        setHgap(10);
        setVgap(10);
        setPadding(new Insets(20));


        defineColumns();
        createHeaderArea();
        createUsernameField();
        createPasswordField();
        createButtonsArea();

        Platform.runLater(this::requestFocus);
    }

    private void defineColumns () {
        ColumnConstraints col1 = new ColumnConstraints(250);
        ColumnConstraints col2 = new ColumnConstraints(250);

        getColumnConstraints().addAll(col1, col2);
    }

    private void createHeaderArea() {

        Label header = new Label("Music Catalog Sign Up");
        String headerStyle = 
            """
            -fx-font-size: 24px;
            -fx-text-fill: lightgrey;
            """;
        header.setStyle(headerStyle);

        add(header, 0, 0, 2, 1);
    }

    private void createUsernameField() {

        usernameField.setPromptText("Username");
        add(usernameField, 0, 1, 2, 1);
    }

    private void createPasswordField() {

        passwordField.setPromptText("Password");
        add(passwordField, 0, 2, 2, 1);
    }

    private void createButtonsArea() {

        loginButton.setText("Return To Login");
        loginButton.setPrefWidth(Double.MAX_VALUE);
        GridPane.setHgrow(loginButton, Priority.ALWAYS);
        add(loginButton, 0, 3);

        signupButton.setText("Sign Up");
        signupButton.setPrefWidth(Double.MAX_VALUE);
        GridPane.setHgrow(signupButton, Priority.ALWAYS);
        add(signupButton, 1, 3);
    }
}