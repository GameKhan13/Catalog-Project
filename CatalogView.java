import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

public class CatalogView extends JFrame {
    JList listDisplay;
    JButton addButton;
    JButton editButton;

    JTextField nameField;
    JTextField descriptionField;

    public CatalogView() {
        this.setTitle("Catalog Manager");
        this.setSize(400, 300);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());

        listDisplay = new JList<>();
        listDisplay.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.add(new JScrollPane(listDisplay), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        this.add(bottomPanel, BorderLayout.PAGE_END);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.LINE_START);

        addButton = new JButton("Add");
        buttonPanel.add(addButton, BorderLayout.PAGE_START);

        editButton = new JButton("Edit");
        buttonPanel.add(editButton, BorderLayout.PAGE_END);

        JPanel fieldPanel = new JPanel(new BorderLayout());
        bottomPanel.add(fieldPanel, BorderLayout.LINE_END);

        nameField = new JTextField(40);
        fieldPanel.add(nameField, BorderLayout.PAGE_START);

        descriptionField = new JTextField(40);
        fieldPanel.add(descriptionField, BorderLayout.PAGE_END);
    }

    public static void main(String[] args) {
        CatalogView view = new CatalogView();
        view.setVisible(true);
    }
    
}
