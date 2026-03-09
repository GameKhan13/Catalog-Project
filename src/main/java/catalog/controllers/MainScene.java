package catalog.controllers;

import catalog.back_end.User;
import catalog.front_end.main.MainPage;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

public class MainScene extends Scene {

    private final MainPage mainPage;
    private final User user;

    public MainScene(User user) {
        super(new Pane());

        mainPage = new MainPage(user.isAdmin());
        setRoot(mainPage);
        this.user = user;

        mainPage.tabPane.homeButton.setOnAction(e -> {
            mainPage.setTab(mainPage.homePanel);
        });

        mainPage.tabPane.adminButton.setOnAction(e -> {
            mainPage.setTab(mainPage.adminPanel);
        });

        mainPage.adminPanel.addButton.setOnAction(e -> {
            // add functionality for the add button in the admin screen here
        });

        mainPage.adminPanel.editButton.setOnAction(e -> {
            // add functionality for the edit button in the admin screen here
        });

        mainPage.adminPanel.deleteButton.setOnAction(e -> {
            // add functionality for the delete button in the admin screen here
        });
    }
}