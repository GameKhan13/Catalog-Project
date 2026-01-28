import java.io.*;
import java.nio.file.*;
import java.util.*;

public class projectBackend{

    public void addEntry(String name, String description) {
        Path file = Paths.get("GroceryList.csv");

        try {
            if (!Files.exists(file)) {
                Files.writeString(file, "ID,Item,Describe\n");
            }

            List<String> lines = Files.readAllLines(file);

            int nextId = 1;
            for (int i = lines.size() - 1; i >= 1; i--) { // skip header
                String row = lines.get(i).trim();
                if (!row.isEmpty()) {
                    String[] parts = row.split(",", -1);
                    nextId = Integer.parseInt(parts[0].trim()) + 1;
                    break;
                }
            }

            try (BufferedWriter writer = Files.newBufferedWriter(
                    file, StandardOpenOption.APPEND)) {
                writer.write(nextId + "," + name + "," + description);
                writer.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void editEntry(int id, String newName, String newDescription) {
        Path file = Paths.get("GroceryList.csv");

        try {
            List<String> lines = Files.readAllLines(file);

            for (int i = 1; i < lines.size(); i++) { // skip header
                String[] parts = lines.get(i).split(",", -1);
                if (parts.length >= 3 && parts[0].trim().equals(String.valueOf(id))) {
                    lines.set(i, id + "," + newName + "," + newDescription);
                    break;
                }
            }

            Files.write(file, lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> viewEntries() {
        Path file = Paths.get("GroceryList.csv");
        List<String> entries = new ArrayList<>();

        try {
            if (Files.exists(file)) {
                entries = Files.readAllLines(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return entries;
    }
    public static void main(String[] args) {
        projectBackend backend = new projectBackend();
        CatalogView view = new CatalogView();
        List<String> entries = backend.viewEntries();
        view.listDisplay.setListData(entries.toArray());

        view.addButton.addActionListener(e -> {
            String name = view.nameField.getText();
            String description = view.descriptionField.getText();
            backend.addEntry(name, description);
            List<String> updatedEntries = backend.viewEntries();
            view.listDisplay.setListData(updatedEntries.toArray());
        });

        view.editButton.addActionListener(e -> {
            int selectedIndex = view.listDisplay.getSelectedIndex();
            if (selectedIndex != -1) {
                String newName = view.nameField.getText();
                String newDescription = view.descriptionField.getText();
                backend.editEntry(selectedIndex, newName, newDescription);
                List<String> updatedEntries = backend.viewEntries();
                view.listDisplay.setListData(updatedEntries.toArray());
            }
        });

        view.setVisible(true);
    }

}