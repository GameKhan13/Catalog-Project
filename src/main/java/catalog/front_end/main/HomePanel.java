package catalog.front_end.main;

import javafx.scene.layout.GridPane;

public class HomePanel extends GridPane {

    public ResultsDisplayPanel results = new ResultsDisplayPanel();

    public HomePanel() {
        setStyle("-fx-background-color: #2b2b2b;");

        setUpResultsView();
    }

    protected final void setUpResultsView() {
        add(results, 0, 0);
    }
}