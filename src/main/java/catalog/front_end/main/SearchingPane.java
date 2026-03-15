package catalog.front_end.main;

import java.util.List;

import catalog.back_end.Entry;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;

public class SearchingPane extends GridPane {
    public ResultsDisplayPanel results = new ResultsDisplayPanel();
    public TextField searchBar = new TextField();
    public FilterPane filtersPane = new FilterPane();

    public SearchingPane() {

        setUpContent();
    }

    protected final void setUpContent() {
        TitledPane collapsablePane = new TitledPane("Filters", filtersPane);

        add(searchBar, 0, 0);
        add(collapsablePane, 0, 1);
        add(results, 0, 2);
    }

    public void setResults(List<Entry> entries) {
        results.setResults(entries);
    }

    public Entry getSelectedResult() {
        return results.getSelectedResult();
    }
}
