package musiccatalog.service;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import musiccatalog.model.User;
import musiccatalog.util.IdUtil;

/**
 * Service to manage accounts, capable of finding and creating users
 */
public class UserService {

    private final String idHeader = "id";
    private final String usernameHeader = "username";
    private final String passwordHeader = "password";
    private final String permissionHeader = "permission";

    public void writeHeader(Path path) throws IOException {
        Files.writeString(path, String.join(",", idHeader, usernameHeader, passwordHeader, permissionHeader), StandardCharsets.UTF_8);
        Files.writeString(path, "\n");
    }

    /**
     * @return a {@code List<User>} that contains all users that exist
     * @throws IOException
     */
    public synchronized List<User> getAllUsers() throws IOException {
        List<User> users = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(DataService.USERS_CSV, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader)) {
            for (CSVRecord record : parser) {
                users.add(new User(
                        record.get(idHeader),
                        record.get(usernameHeader),
                        record.get(passwordHeader),
                        Boolean.parseBoolean(record.get(permissionHeader))
                ));
            }
        }
        return users;
    }

    /**
     * Attempts to find the specified username, check if the user was found by using {@code isPresent()}
     * @param username the username to search for
     * @return A {@code Optional} object that wraps the {@code User} object
     * @throws IOException
     */
    public synchronized Optional<User> findByUsername(String username) throws IOException {
        String normalized = username.toLowerCase(Locale.ROOT).trim();
        return getAllUsers().stream()
                .filter(user -> user.getUsername().toLowerCase(Locale.ROOT).equals(normalized))
                .findFirst();
    }

    /**
     * Attempts to find the specified user id, check if the user was found by using {@code isPresent()}
     * @param userId the user id to search for
     * @return A {@code Optional} object that wraps the {@code User} object
     * @throws IOException
     */
    public synchronized Optional<User> findById(String userId) throws IOException {
        return getAllUsers().stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst();
    }

    /**
     * Creates the user and updates the users file
     * @param username username for the user
     * @param passwordHash hashed password for the user (no hashing is done in the function)
     * @param admin the permissions of the user, if {@code true} then the user has admin permissions
     * @return the created user object for use
     * @throws IOException
     */
    public synchronized User createUser(String username, String passwordHash, boolean admin) throws IOException {
        User user = new User(IdUtil.newId(), username.trim(), passwordHash, admin);
        List<User> users = getAllUsers();
        users.add(user);
        writeAll(users);
        return user;
    }

    /**
     * writes user information to file
     * @param users the list of users to write
     * @throws IOException
     */
    private synchronized void writeAll(List<User> users) throws IOException {
        try (Writer writer = Files.newBufferedWriter(DataService.USERS_CSV, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader(idHeader, usernameHeader, passwordHeader, permissionHeader)
                     .build())) {
            for (User user : users) {
                printer.printRecord(user.getId(), user.getUsername(), user.getPasswordHash(), user.isAdmin());
            }
        }
    }
}
