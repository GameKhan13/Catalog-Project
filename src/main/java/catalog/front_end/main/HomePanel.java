package catalog.front_end.main;

import java.util.ArrayList;
import java.util.List;

import catalog.back_end.Entry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;

public class HomePanel extends GridPane {

    private final ListView<EntryDisplayable> results = new ListView<>();

    public HomePanel() {
        setStyle("-fx-background-color: #2b2b2b;");

        setUpResultsView();
    }

    public void setResults(List<Entry> entries) {
        List<EntryDisplayable> displayableEntries = new ArrayList<>();

        for (Entry entry : entries) {
            displayableEntries.add(new EntryDisplayable(entry));
        }

        ObservableList<EntryDisplayable> observableEntries = FXCollections.observableArrayList(displayableEntries);

        this.results.setItems(observableEntries);
    }

    protected final void setUpResultsView() {
        results.setPrefSize(1000, 500);

        add(results, 0, 0);
    }
}