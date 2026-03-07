package catalog.back_end;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private static final String HEADER = "username,password,isAdmin,playlist_ids";

    private final File usersFile;
    private User currentUser;

    public UserService(String filePath) throws IOException {
        this.usersFile = new File(filePath);
        ensureFileExists();
    }

    /**
     * Logs in a user if username/password match.
     * Returns true if valid and populates currentUser.
     * Returns false if invalid.
     */
    public boolean loginUser(String username, String password) throws IOException {
        List<User> users = readAllUsers();

        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                currentUser = user;
                return true;
            }
        }

        currentUser = null;
        return false;
    }

    /**
     * Signs up a new user by appending them to users.csv.
     * Assumes validation already happened elsewhere, but still prevents duplicate usernames.
     * Returns true if added, false if username already exists.
     */
    public boolean signUpUser(User user) throws IOException {
        if (user == null || user.getUsername() == null || user.getPassword() == null) {
            return false;
        }

        List<User> users = readAllUsers();
        for (User existingUser : users) {
            if (existingUser.getUsername().equalsIgnoreCase(user.getUsername())) {
                return false; // duplicate username
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(usersFile, true))) {
            writer.newLine();
            writer.write(toCsvRow(user));
        }

        return true;
    }

    /**
     * Deletes a user by username.
     * Admin only: currentUser must be logged in and must be admin.
     * Returns true if deleted, false if user not found.
     * Throws SecurityException if current user is not admin.
     */
    public boolean deleteUser(String username) throws IOException {
        if (currentUser == null || !currentUser.isAdmin()) {
            throw new SecurityException("Only admins can delete users.");
        }

        List<User> users = readAllUsers();
        List<User> updatedUsers = new ArrayList<>();

        boolean deleted = false;

        for (User user : users) {
            if (user.getUsername().equals(username)) {
                deleted = true;
            } else {
                updatedUsers.add(user);
            }
        }

        if (!deleted) {
            return false;
        }

        writeAllUsers(updatedUsers);

        // If deleted user was the logged-in user somehow, clear session
        if (currentUser.getUsername().equals(username)) {
            currentUser = null;
        }

        return true;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        currentUser = null;
    }

    private void ensureFileExists() throws IOException {
        if (!usersFile.exists()) {
            File parent = usersFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(usersFile))) {
                writer.write(HEADER);
            }
        }
    }

    private List<User> readAllUsers() throws IOException {
        List<User> users = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(usersFile))) {
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
                if (columns.size() < 4) {
                    continue;
                }

                String username = columns.get(0).trim();
                String password = columns.get(1).trim();
                boolean isAdmin = Boolean.parseBoolean(columns.get(2).trim());
                String playlistIdsRaw = columns.get(3).trim();

                User user = new User();
                user.setUsername(username);
                user.setPassword(password);
                user.setAdmin(isAdmin);
                user.setPlaylistIds(parsePlaylistIds(playlistIdsRaw));

                users.add(user);
            }
        }

        return users;
    }

    private void writeAllUsers(List<User> users) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(usersFile))) {
            writer.write(HEADER);

            for (User user : users) {
                writer.newLine();
                writer.write(toCsvRow(user));
            }
        }
    }

    private String toCsvRow(User user) {
        return escapeCsv(user.getUsername()) + "," +
               escapeCsv(user.getPassword()) + "," +
               user.isAdmin() + "," +
               "\"" + formatPlaylistIds(user.getPlaylistIds()) + "\"";
    }

    private String formatPlaylistIds(List<Integer> playlistIds) {
        if (playlistIds == null || playlistIds.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < playlistIds.size(); i++) {
            sb.append(playlistIds.get(i));
            if (i < playlistIds.size() - 1) {
                sb.append("|");
            }
        }
        return sb.toString();
    }

    private List<Integer> parsePlaylistIds(String raw) {
        List<Integer> ids = new ArrayList<>();

        raw = raw.replace("\"", "").trim();

        if (raw.isEmpty()) {
            return ids;
        }

        String[] parts = raw.split("\\|");
        for (String part : parts) {
            try {
                ids.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException e) {
                // ignore bad values
            }
        }

        return ids;
    }

    /**
     * Simple CSV parser that supports commas inside quoted fields.
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

    /**
     * Escapes values if they contain commas or quotes.
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