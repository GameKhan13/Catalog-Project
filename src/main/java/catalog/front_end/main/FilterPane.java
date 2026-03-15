package catalog.front_end.main;

import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class FilterPane extends GridPane {

    public TextField songNameField = new TextField();
    public TextField albumNameField = new TextField();
    public TextField artistNameField = new TextField();
    public TextField genreField = new TextField();

    public DatePicker beforeDatePicker = new DatePicker();
    public DatePicker afterDatePicker = new DatePicker();

    public FilterPane() {

        setUpContent();
    }   

    protected final void setUpContent() {
        add(songNameField, 0, 0);
        add(albumNameField, 1, 0);
        add(artistNameField, 0, 1);
        add(genreField, 1, 1);
        add(beforeDatePicker, 2, 0);
        add(afterDatePicker, 2, 1);
    }
}
