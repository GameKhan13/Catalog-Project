package catalog;

import catalog.back_end.EntryService;
import catalog.back_end.UserService;
import catalog.controllers.LoginScene;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage mainStage) throws Exception {
        UserService userService = new UserService("src\\main\\resources\\Users.csv");
        EntryService entryService = new EntryService("src\\main\\resources\\Songs.csv", userService);
        LoginScene loginScene = new LoginScene(userService, entryService);

        mainStage.setTitle("Music Catalog");
        mainStage.setScene(loginScene);
        mainStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
