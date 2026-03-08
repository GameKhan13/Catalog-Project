package catalog.front_end.main;


import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.TilePane;

public class CatalogTabPane extends TilePane {

    public Button homeButton = new Button("Home");;
    public Button adminButton = new Button("Admin");

    public CatalogTabPane() {
        setStyle("-fx-background-color: #2b2b2b;");

        addTabButtons();
    }

    protected final void addTabButtons() {
        setAlignment(Pos.CENTER);

        homeButton.setPrefSize(150, 50);
        adminButton.setPrefSize(150, 50);

        add(homeButton);
        add(adminButton);
    }

    public void removeAdminButton() {
        getChildren().remove(adminButton);
    }

    protected final void add(Button button) {
        getChildren().add(button);
    }
}