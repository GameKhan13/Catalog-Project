package com.musiccatalog.service;

import com.musiccatalog.model.User;
import com.musiccatalog.util.IdUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Service class responsible for reading and writing user data.
 *
 * This service stores users in a CSV file rather than in a database.
 * Most methods are synchronized so concurrent requests do not try to
 * read and write the same file at the exact same time.
 */
public class UserService {
    /**
     * Reads every user from users.csv and returns them as User objects.
     *
     * @return list of all users currently stored in the CSV file
     * @throws IOException if the file cannot be read
     */
    public synchronized List<User> getAllUsers() throws IOException {
        // This list will be filled with one User object per CSV row.
        List<User> users = new ArrayList<>();

        // Open the users CSV file for reading and let Apache Commons CSV parse it.
        try (Reader reader = Files.newBufferedReader(DataBootstrapService.USERS_CSV, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader)) {

            // Convert each parsed CSV row into a User object.
            for (CSVRecord record : parser) {
                users.add(new User(
                        record.get("id"),
                        record.get("username"),
                        record.get("passwordHash"),
                        Boolean.parseBoolean(record.get("isAdmin"))
                ));
            }
        }

        return users;
    }

    /**
     * Finds a user by username.
     *
     * The comparison ignores case and trims extra spaces so that
     * " Alice " and "alice" are treated as the same username.
     *
     * @param username username to search for
     * @return Optional containing the matching user when found
     * @throws IOException if the file cannot be read
     */
    public synchronized Optional<User> findByUsername(String username) throws IOException {
        // Normalize the search value to make matching more forgiving.
        String normalized = username.toLowerCase(Locale.ROOT).trim();

        // Search through the full user list and return the first match.
        return getAllUsers().stream()
                .filter(user -> user.getUsername().toLowerCase(Locale.ROOT).equals(normalized))
                .findFirst();
    }

    /**
     * Finds a user by their unique id.
     *
     * @param userId id to search for
     * @return Optional containing the matching user when found
     * @throws IOException if the file cannot be read
     */
    public synchronized Optional<User> findById(String userId) throws IOException {
        // Load every user, then filter down to the matching id.
        return getAllUsers().stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst();
    }

    /**
     * Creates a new user and appends it to storage.
     *
     * The passwordHash parameter is expected to already contain a hashed password.
     * This method does not hash it itself.
     *
     * @param username username for the new account
     * @param passwordHash already-hashed password value
     * @param admin whether the new user should be an administrator
     * @return the newly created User object
     * @throws IOException if the file cannot be read or written
     */
    public synchronized User createUser(String username, String passwordHash, boolean admin) throws IOException {
        // Build the new user object using a generated id and a trimmed username.
        User user = new User(IdUtil.newId(), username.trim(), passwordHash, admin);

        // Read the current file contents so the new user can be added to the list.
        List<User> users = getAllUsers();

        // Add the new user to the in-memory list.
        users.add(user);

        // Rewrite the full CSV file with the updated user list.
        writeAll(users);

        return user;
    }

    /**
     * Rewrites the complete users.csv file using the provided list.
     *
     * This is a private helper used after create operations.
     *
     * @param users full list of users to save
     * @throws IOException if the file cannot be written
     */
    private synchronized void writeAll(List<User> users) throws IOException {
        // Open the users CSV file for writing and include the header row.
        try (Writer writer = Files.newBufferedWriter(DataBootstrapService.USERS_CSV, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader("id", "username", "passwordHash", "isAdmin")
                     .build())) {

            // Write one CSV record per User object.
            for (User user : users) {
                printer.printRecord(user.getId(), user.getUsername(), user.getPasswordHash(), user.isAdmin());
            }
        }
    }
}
