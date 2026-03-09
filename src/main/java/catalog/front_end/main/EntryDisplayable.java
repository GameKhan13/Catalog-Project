package catalog.front_end.main;

import catalog.back_end.Entry;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class EntryDisplayable extends GridPane {

    private final Entry entry;

    public EntryDisplayable(Entry entry) {
        this.entry = entry;

        setHgap(20);
        setVgap(10);

        Label nameLabel = new Label(entry.getName());
        Label albumLabel = new Label(entry.getAlbum());
        Label artistLabel = new Label(entry.getArtist());
        Label yearLabel = new Label(Integer.toString(entry.getYear()));
        Label genreLabel = new Label(entry.getGenre());

        add(nameLabel, 0, 0);
        add(albumLabel, 0, 1);
        add(artistLabel, 1, 0);
        add(yearLabel, 2, 0);
        add(genreLabel, 1, 1);
    }

    public Entry getEntry() {
        return this.entry;
    }
}
