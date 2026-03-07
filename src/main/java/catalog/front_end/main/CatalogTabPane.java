package catalog.front_end.main;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;

class CatalogTabPane extends TabPane {

    public CatalogTabPane(Pane... panels) {
        setStyle("-fx-background-color: #2b2b2b;");

        for (Pane pane : panels) {
            Tab tab = new Tab(pane.getClass().getSimpleName());
            tab.setContent(pane);
            getTabs().add(tab);
        }
    }
}