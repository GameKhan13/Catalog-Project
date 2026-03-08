package catalog.front_end.main;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public class MainPage extends GridPane {

    public final HomePanel homePanel = new HomePanel();
    public final AdminPanel adminPanel = new AdminPanel();

    public final Pane displayPane = new Pane();
    public final CatalogTabPane tabPane = new CatalogTabPane();

    private Pane currentPanel;

    public MainPage(boolean isAdmin) {
        setStyle("-fx-background-color: #2b2b2b;");

        setUpPanels();
        setUpTabs(isAdmin);
    }

    protected final void setUpPanels() {
        displayPane.setPrefSize(1500, 900);
        add(displayPane, 0, 0);

        setTab(homePanel);
    }

    protected final void setUpTabs(boolean isAdmin) {
        if (!isAdmin) {
            tabPane.removeAdminButton();
        }

        add(tabPane, 0, 1);
    }

    public void setTab(Pane pane) {
        displayPane.getChildren().remove(currentPanel);
        currentPanel = pane;
        displayPane.getChildren().add(currentPanel);
    }
}