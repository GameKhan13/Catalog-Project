import java.util.Vector;

public class CatalogModel {

    private final String loadedFile;
    private final Vector<Entry> entries;

    private int current_id;

    public CatalogModel (String fileName) {
        // initialise `loadedFile` using `fileName`
        // initialise `entries` to an empty Vector
        // call `readFromFile`
    }

    private void addEntry (int id, String name, String description) {
        // create a new `Entry` object
        // add it to the end of the `entires` vector
    }

    public void addEntry (String name, String description) {
        // call the private version of `addEntry` with the `current_id`
        // incriment `current_id`
    }

    public void editEntry (int id, String newName, String newDescription) {
        // find the `Entry` object in the `entires` vector with `Entry.getId` = `id`
        // call `Entry.setName` with `newName`
        // call `Entry.setDescription` with `newDescription`
    }

    public Vector<Entry> getEntries () {
        // return the `entries` vector
    }

    private void readFromFile () {
        // line one contains the `current_id` only
        // every line after contains the following:
        //      `id` (int), `name` (String), `description` (String)

        // attempt to read from the file `loadedFile`
        // if no file is found then set `current_id` = 0 and return

        // update `current_id`
        // construct the list of `Entry` objects in `entries` (use the private version of `addEntry`)
    }

    public void writeToFile () {
        // line one contains the `current_id` only
        // every line after contains the following:
        //      `id` (int), `name` (String), `description` (String)

        // attempt to open the file `loadedFile`
        // if no file found create the file and open it

        // write the `current_id` variable to the first line
        // then every other line write the `id`, `name` and `description` to the 
        // same line separated by commas for each `Entry` in the `entries` variable
    }
}
