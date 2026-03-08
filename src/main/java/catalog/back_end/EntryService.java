package catalog.back_end;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * Handles all CSV operations for song entries.
 *
 * Responsibilities:
 * - Read all songs from Songs.csv
 * - Write all songs back to Songs.csv
 * - Add new songs
 * - Edit existing songs
 * - Delete songs
 *
 * Important:
 * getAllEntries() can be used by anyone.
 * add/edit/delete are ADMIN ONLY.
 */
public class EntryService {

    // Header that should exist at the top of Songs.csv
    private static final String HEADER = "name,artist,album,year,genre,song_id";

    // The CSV file storing all songs
    private final File entriesFile;

    // Used to check who is logged in, so we can enforce admin-only edits
    private final UserService userService;

    /*
     * Constructor
     *
     * filePath -> location of Songs.csv
     * userService -> lets us check currentUser and whether they are admin
     */
    public EntryService(String filePath, UserService userService) throws IOException {
        this.entriesFile = new File(filePath);
        this.userService = userService;
        ensureFileExists();
    }

    /*
     * Returns every song entry from the CSV.
     *
     * This is NOT admin-only because regular users should still be able to
     * view the catalog.
     */
    public List<Entry> getAllEntries() throws IOException {
        return readAllEntries();
    }

    /*
     * Adds a new song entry to the CSV.
     *
     * Admin only.
     *
     * Returns true after the song is added.
     */
    public boolean addEntry(String name, String artist, String album, int year, String genre) throws IOException {
        requireAdmin();

        List<Entry> entries = readAllEntries();
        int newSongId = getNextSongId(entries);

        Entry newEntry = new Entry(name, artist, album, year, genre, newSongId);
        entries.add(newEntry);

        writeAllEntries(entries);
        return true;
    }

    /*
     * Edits an existing song by songId.
     *
     * Admin only.
     *
     * Returns:
     * true  -> if the song was found and updated
     * false -> if no song with that songId exists
     */
    public boolean editEntry(int songId, String newName, String newArtist,
                             String newAlbum, int newYear, String newGenre) throws IOException {
        requireAdmin();

        List<Entry> entries = readAllEntries();
        boolean updated = false;

        for (Entry entry : entries) {
            if (entry.getSongId() == songId) {
                entry.setName(newName);
                entry.setArtist(newArtist);
                entry.setAlbum(newAlbum);
                entry.setYear(newYear);
                entry.setGenre(newGenre);
                updated = true;
                break;
            }
        }

        if (!updated) {
            return false;
        }

        writeAllEntries(entries);
        return true;
    }

    /*
     * Deletes a song by songId.
     *
     * Admin only.
     *
     * Returns:
     * true  -> if the song was found and removed
     * false -> if no song with that songId exists
     */
    public boolean deleteEntry(int songId) throws IOException {
        requireAdmin();

        List<Entry> entries = readAllEntries();
        List<Entry> updatedEntries = new ArrayList<>();
        boolean deleted = false;

        for (Entry entry : entries) {
            if (entry.getSongId() == songId) {
                deleted = true;
            } else {
                updatedEntries.add(entry);
            }
        }

        if (!deleted) {
            return false;
        }

        writeAllEntries(updatedEntries);
        return true;
    }

    /*
     * Checks whether the current logged-in user is an admin.
     *
     * Throws SecurityException if:
     * - no user is logged in
     * - logged-in user is not an admin
     */
    private void requireAdmin() {
        User currentUser = userService.getCurrentUser();

        if (currentUser == null || !currentUser.isAdmin()) {
            throw new SecurityException("Only admins can modify song entries.");
        }
    }

    /*
     * Creates the CSV file if it does not exist.
     * Also writes the correct header line.
     */
    private void ensureFileExists() throws IOException {
        if (!entriesFile.exists()) {
            File parent = entriesFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(entriesFile))) {
                writer.write(HEADER);
            }
        }
    }

    /*
     * Reads every row from Songs.csv and converts each row into an Entry object.
     *
     * Notes:
     * - skips the header line
     * - skips empty lines
     * - skips malformed rows
     */
    private List<Entry> readAllEntries() throws IOException {
        List<Entry> entries = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(entriesFile))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // skip header
                }

                List<String> columns = parseCsvLine(line);

                // Expected format:
                // name,artist,album,year,genre,song_id
                if (columns.size() < 6) {
                    continue;
                }

                try {
                    String name = columns.get(0).trim();
                    String artist = columns.get(1).trim();
                    String album = columns.get(2).trim();
                    int year = Integer.parseInt(columns.get(3).trim());
                    String genre = columns.get(4).trim();
                    int songId = Integer.parseInt(columns.get(5).trim());

                    Entry entry = new Entry(name, artist, album, year, genre, songId);
                    entries.add(entry);
                } catch (NumberFormatException e) {
                    // Skip bad rows where year or songId are not valid integers
                }
            }
        }

        return entries;
    }

    /*
     * Rewrites the full CSV file using the list provided.
     *
     * This is the normal CSV workflow:
     * 1. read everything
     * 2. update data in memory
     * 3. rewrite the file
     */
    private void writeAllEntries(List<Entry> entries) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(entriesFile))) {
            writer.write(HEADER);

            for (Entry entry : entries) {
                writer.newLine();
                writer.write(toCsvRow(entry));
            }
        }
    }

    /*
     * Converts one Entry object into one CSV row.
     */
    private String toCsvRow(Entry entry) {
        return escapeCsv(entry.getName()) + "," +
                escapeCsv(entry.getArtist()) + "," +
                escapeCsv(entry.getAlbum()) + "," +
                entry.getYear() + "," +
                escapeCsv(entry.getGenre()) + "," +
                entry.getSongId();
    }

    /*
     * Finds the next available song ID.
     *
     * Example:
     * if existing IDs are 1, 2, 5
     * next ID becomes 6
     */
    private int getNextSongId(List<Entry> entries) {
        int maxId = 0;

        for (Entry entry : entries) {
            if (entry.getSongId() > maxId) {
                maxId = entry.getSongId();
            }
        }

        return maxId + 1;
    }

    /*
     * Simple CSV parser that supports commas inside quoted fields.
     *
     * Example:
     * "Song, Part 2",Artist,Album,2020,Pop,9
     */
    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        result.add(current.toString());
        return result;
    }

    /*
     * Escapes a value before saving it into CSV.
     *
     * If the text contains commas or quotes, wrap it in quotes.
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains(",") || value.contains("\"")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }

        return value;
    }
}