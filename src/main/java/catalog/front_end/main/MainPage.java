package catalog.front_end.main;

import javafx.scene.layout.GridPane;

public class MainPage extends GridPane {

    private CatalogTabPane tabPane;
    private HomePanel homePanel;
    private AdminPanel adminPanel;


    public MainPage(boolean isAdmin) {
        setStyle("-fx-background-color: #2b2b2b;");

        homePanel = new HomePanel();
        adminPanel = new AdminPanel();

        if (isAdmin) {
            tabPane = new CatalogTabPane(homePanel, adminPanel);
        } else {
            tabPane = new CatalogTabPane(homePanel);
        }

        add(tabPane, 0, 0);
    }
}