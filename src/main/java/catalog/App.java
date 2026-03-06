package catalog;

import catalog.front_end.LoginScene;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    

    @Override
    public void start(Stage mainStage) throws Exception {
        LoginScene loginScene = new LoginScene();

        mainStage.setTitle("Music Catalog");
        mainStage.setScene(loginScene);
        mainStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
