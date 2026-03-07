package catalog;

import catalog.back_end.UserService;
import catalog.controllers.UserController;
import catalog.front_end.LoginScene;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage mainStage) throws Exception {
        UserService userService = new UserService("src\\main\\resources\\Users.csv");
        LoginScene loginScene = new LoginScene();

        new UserController(loginScene, userService, mainStage);

        mainStage.setTitle("Music Catalog");
        mainStage.setScene(loginScene);
        mainStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}