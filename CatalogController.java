import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class CatalogController {

    public CatalogController (CatalogModel model, CatalogView view) {
        view.addButton.addActionListener(e -> {
            String name = view.nameField.getText();
            String description = view.descriptionField.getText();
            model.addEntry(name, description);
            Vector<Entry> updatedEntries = model.getEntries();
            view.listDisplay.setListData(updatedEntries);
        });

        view.editButton.addActionListener(e -> {
            Entry selectedEntry = view.listDisplay.getSelectedValue();
            if (selectedEntry != null) {
                String newName = view.nameField.getText();
                String newDescription = view.descriptionField.getText();
                model.editEntry(selectedEntry.getId(), newName, newDescription);
                Vector<Entry> updatedEntries = model.getEntries();
                view.listDisplay.setListData(updatedEntries);
            }
        });

        view.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // overide the already defined defualt close opperation to allow for code execution at the end
        view.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                model.writeToFile(); // save the data to the csv file
                view.dispose();
            }
        });

        view.setVisible(true);
    }

    
    
    public static void main(String[] args) {
        CatalogModel model = new CatalogModel("GroceryList.csv");
        CatalogView view = new CatalogView();
        CatalogController controller = new CatalogController(model, view);
    }

}