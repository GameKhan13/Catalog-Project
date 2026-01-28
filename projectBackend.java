import java.io.*;
import java.nio.file.*;
import java.util.*;

public class projectBackend{

    public void addEntry(String name, String description) {
        Path file = Paths.get("GroceryList.csv");

        try {
            int nextId = 1;

            if (Files.exists(file)) {
                List<String> lines = Files.readAllLines(file);

                // find last data line (skip header, skip blanks)
                for (int i = lines.size() - 1; i >= 1; i--) {
                    String line = lines.get(i).trim();
                    if (!line.isEmpty()) {
                        String[] parts = line.split(",", -1);
                        nextId = Integer.parseInt(parts[0].trim()) + 1;
                        break;
                    }
                }
            } else {
                // if file does not exist, write header first
                Files.writeString(file, "ID,Item,Describe\n");
            }

            try (BufferedWriter writer = Files.newBufferedWriter(
                    file, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                writer.write(nextId + ". " + name + ", " + description);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void editEntry(int id,String newName, String newDescription) {
        Path file = Paths.get("GroceryList.csv");
        try{
        List<String> lines = Files.readAllLines(file);
        String line = id + ". " + newName + ", " + newDescription;
        lines.set(id, line);
        Files.write(file, lines);

        }catch(IOException e){
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