package catalog.front_end.main;

import java.util.ArrayList;
import java.util.List;

import catalog.back_end.Entry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;

public class AdminPanel extends GridPane {

    private final ListView<EntryDisplayable> results = new ListView<>();

    public final TextField nameField = new TextField();
    public final TextField albumField = new TextField();
    public final TextField artistField = new TextField();
    public final TextField yearField = new TextField();
    public final TextField genreField = new TextField();

    public final Button addButton = new Button();
    public final Button editButton = new Button();
    public final Button deleteButton = new Button();

    public AdminPanel() {
        setStyle("-fx-background-color: #2b2b2b;");

        setHgap(50);

        setUpResultsView();
        setUpInputFields();
        setUpButtons();
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
        results.setPrefSize(600, 500);
        setRowSpan(results, REMAINING);
        add(results, 0, 0);
    }

    protected final void setUpInputFields() {
        GridPane fieldsPane = new GridPane();

        Label nameLabel = new Label("Name: ");
        nameLabel.setTextFill(Color.WHITE);

        fieldsPane.add(nameLabel, 0, 0);
        fieldsPane.add(nameField, 1, 0);

        Label albumLabel = new Label("Album: ");
        albumLabel.setTextFill(Color.WHITE);

        fieldsPane.add(albumLabel, 0, 1);
        fieldsPane.add(albumField, 1, 1);

        Label artistLabel = new Label("Artist: ");
        artistLabel.setTextFill(Color.WHITE);

        fieldsPane.add(artistLabel, 0, 2);
        fieldsPane.add(artistField, 1, 2);

        Label yearLabel = new Label("Year: ");
        yearLabel.setTextFill(Color.WHITE);

        fieldsPane.add(yearLabel, 0, 3);
        fieldsPane.add(yearField, 1, 3);

        Label genreLabel = new Label("Genre: ");
        genreLabel.setTextFill(Color.WHITE);

        fieldsPane.add(genreLabel, 0, 4);
        fieldsPane.add(genreField, 1, 4);

        add(fieldsPane, 1, 0);
    }

    protected final void setUpButtons() {
        TilePane buttonPane = new TilePane();

        addButton.setText("Add");
        editButton.setText("Edit");
        deleteButton.setText("Delete");

        buttonPane.getChildren().add(addButton);
        buttonPane.getChildren().add(editButton);
        buttonPane.getChildren().add(deleteButton);

        add(buttonPane, 1, 1);
    }
    public void clearFields() {
        nameField.clear();
        artistField.clear();
        albumField.clear();
        yearField.clear();
        genreField.clear();
    }

    public int getSelectedSongId() {
        EntryDisplayable selected = results.getSelectionModel().getSelectedItem();
        return selected.getEntry().getSongId();
    }
}