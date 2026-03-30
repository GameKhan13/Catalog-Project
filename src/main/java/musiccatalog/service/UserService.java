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
 * Service to manage accounts, capable of finding and creating users.
 */
public class UserService {

    private static final String[] STANDARD_HEADERS = {"id", "username", "passwordHash", "isAdmin"};

    public String headerLine() {
        return String.join(",", STANDARD_HEADERS);
    }

    public void writeHeader(Path path) throws IOException {
        Files.writeString(path, headerLine() + System.lineSeparator(), StandardCharsets.UTF_8);
    }

    public synchronized List<User> getAllUsers() throws IOException {
        List<User> users = new ArrayList<>();
        if (Files.notExists(DataService.USERS_CSV) || Files.size(DataService.USERS_CSV) == 0L) {
            return users;
        }

        try (Reader reader = Files.newBufferedReader(DataService.USERS_CSV, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader)) {
            for (CSVRecord record : parser) {
                users.add(new User(
                        get(record, "id"),
                        get(record, "username"),
                        firstPresent(record, "passwordHash", "password"),
                        Boolean.parseBoolean(firstPresent(record, "isAdmin", "permission"))
                ));
            }
        }
        return users;
    }

    public synchronized Optional<User> findByUsername(String username) throws IOException {
        String normalized = username.toLowerCase(Locale.ROOT).trim();
        return getAllUsers().stream()
                .filter(user -> user.getUsername().toLowerCase(Locale.ROOT).equals(normalized))
                .findFirst();
    }

    public synchronized Optional<User> findById(String userId) throws IOException {
        return getAllUsers().stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst();
    }

    public synchronized User createUser(String username, String passwordHash, boolean admin) throws IOException {
        User user = new User(IdUtil.newId(), username.trim(), passwordHash, admin);
        List<User> users = getAllUsers();
        users.add(user);
        writeAll(users);
        return user;
    }

    private synchronized void writeAll(List<User> users) throws IOException {
        try (Writer writer = Files.newBufferedWriter(DataService.USERS_CSV, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader(STANDARD_HEADERS)
                     .build())) {
            for (User user : users) {
                printer.printRecord(user.getId(), user.getUsername(), user.getPasswordHash(), user.isAdmin());
            }
        }
    }

    private String firstPresent(CSVRecord record, String... headers) {
        for (String header : headers) {
            if (record.isMapped(header)) {
                return get(record, header);
            }
        }
        return "";
    }

    private String get(CSVRecord record, String header) {
        return record.isMapped(header) ? record.get(header) : "";
    }
}
