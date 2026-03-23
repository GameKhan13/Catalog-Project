package musiccatalog.service;

import musiccatalog.model.User;
import musiccatalog.util.IdUtil;
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

public class UserService {
    public synchronized List<User> getAllUsers() throws IOException {
        List<User> users = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(DataBootstrapService.USERS_CSV, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader)) {
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
        try (Writer writer = Files.newBufferedWriter(DataBootstrapService.USERS_CSV, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader("id", "username", "passwordHash", "isAdmin")
                     .build())) {
            for (User user : users) {
                printer.printRecord(user.getId(), user.getUsername(), user.getPasswordHash(), user.isAdmin());
            }
        }
    }
}
