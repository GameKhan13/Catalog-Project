package catalog.front_end.main;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public class MainPage extends GridPane {

    public final HomePanel homePanel = new HomePanel();
    public final AdminPanel adminPanel = new AdminPanel();

    public final Pane displayPane = new Pane();
    public final CatalogTabPane tabPane = new CatalogTabPane();
    public final TopBar topBar = new TopBar();

    private Pane currentPanel;

    public MainPage(boolean isAdmin) {
        setStyle("-fx-background-color: #2b2b2b;");

        setUpTopBar();
        setUpPanels();
        setUpTabs(isAdmin);
    }

    protected final void setUpPanels() {
        //displayPane.setPrefHeight(Double.MAX_VALUE);
        add(displayPane, 0, 1);

        setTab(homePanel);
    }

    protected final void setUpTabs(boolean isAdmin) {
        if (!isAdmin) {
            tabPane.removeAdminButton();
        }

        add(tabPane, 0, 2);
    }

    protected final void setUpTopBar() {
        add(topBar, 0, 0);
    }

    public void setTab(Pane pane) {
        displayPane.getChildren().remove(currentPanel);
        currentPanel = pane;
        displayPane.getChildren().add(currentPanel);
    }
}