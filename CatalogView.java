import java.awt.*;
import javax.swing.*;

public class CatalogView extends JFrame {
    JList<Entry> listDisplay;  // for displaying the catalog, also allows selections
    JButton addButton;  // used to add entries to the model
    JButton editButton; //used to edit entries in the model

    JTextField nameField;           // input field for the name
    JTextField descriptionField;    // input field for the description

    public CatalogView() {
        // set JFrame parameters
        this.setTitle("Catalog Manager");
        this.setSize(400, 300);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());

        // set up JList component
        listDisplay = new JList<>();
        listDisplay.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.add(new JScrollPane(listDisplay), BorderLayout.CENTER);

        // set up JButton and JTextField panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        this.add(bottomPanel, BorderLayout.PAGE_END);

        // set up JButton panel
        JPanel buttonPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.LINE_START);

        // set up 'add' button
        addButton = new JButton("Add");
        buttonPanel.add(addButton, BorderLayout.PAGE_START);

        // set up 'edit' button
        editButton = new JButton("Edit");
        buttonPanel.add(editButton, BorderLayout.PAGE_END);

        // set up JTextField panel
        JPanel fieldPanel = new JPanel(new BorderLayout());
        bottomPanel.add(fieldPanel, BorderLayout.CENTER);

        // set up 'name' field
        nameField = new JTextField(40);
        fieldPanel.add(nameField, BorderLayout.PAGE_START);

        // set up 'description' field
        descriptionField = new JTextField(40);
        fieldPanel.add(descriptionField, BorderLayout.PAGE_END);
    }
}
