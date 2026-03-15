package catalog.front_end.main;

import javafx.scene.layout.GridPane;

public class HomePanel extends GridPane {

    public SearchingPane searchingPane = new SearchingPane();

    public HomePanel() {
        setStyle("-fx-background-color: #2b2b2b;");

        setUpContent();
    }

    protected final void setUpContent() {
        add(searchingPane, 0, 0);
    }
}