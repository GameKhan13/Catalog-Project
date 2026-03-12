package catalog.front_end.main;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

public class TopBar extends GridPane{

    public final Button logoutButton = new Button();

    public TopBar() {
        setStyle("-fx-background-color: #2b2b2b;");
        setPrefHeight(50);
        setAlignment(Pos.CENTER_RIGHT);

        setUpLogoutButton();
    }
    
    protected void setUpLogoutButton() {
        logoutButton.setText("Logout");
        logoutButton.setPrefSize(100, 50);
        add(logoutButton, 0, 0);
    }
}
