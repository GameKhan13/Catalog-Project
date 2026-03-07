package catalog.controllers;

import catalog.back_end.User;
import catalog.front_end.main.MainPage;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

public class MainScene extends Scene {

    private MainPage mainPanel;

    public MainScene(User user) {
        super(new Pane(), 1500, 1000);

        mainPanel = new MainPage(user.isAdmin());

        
        

    }
}