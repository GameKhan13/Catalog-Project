package catalog.front_end.main;

import java.util.ArrayList;
import java.util.List;

import catalog.back_end.Entry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;

public class ResultsDisplayPanel extends ScrollPane {

    private final ListView<EntryDisplayable> results = new ListView<>();

    public ResultsDisplayPanel() {
        setStyle("-fx-background-color: #2b2b2b;");
        setPrefSize(1000, 500);
        results.setPrefWidth(1000);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setContent(results);
    }

    public void setResults(List<Entry> entries) {
        List<EntryDisplayable> displayableEntries = new ArrayList<>();

        for (Entry entry : entries) {
            displayableEntries.add(new EntryDisplayable(entry));
        }

        ObservableList<EntryDisplayable> observableEntries = FXCollections.observableArrayList(displayableEntries);

        this.results.setItems(observableEntries);
    }

    public Entry getSelectedResult() {
        return results.getSelectionModel().getSelectedItem().getEntry();
    }
}