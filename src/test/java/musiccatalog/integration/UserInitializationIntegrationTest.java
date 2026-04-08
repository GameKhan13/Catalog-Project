package musiccatalog.integration;

import musiccatalog.model.User;
import musiccatalog.service.DataService;
import musiccatalog.service.PlaylistService;
import musiccatalog.service.SongService;
import musiccatalog.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserInitializationIntegrationTest {

    @Test
    @DisplayName("IT-01-TB: Initialization creates a default admin account when none exists")
    void initialize_withNoExistingAdmin_createsDefaultAdmin() throws Exception {

        // Arrange: create fresh service instances simulating a clean application startup.
        UserService userService = new UserService();
        SongService songService = new SongService();
        PlaylistService playlistService = new PlaylistService();

        DataService dataService =
                new DataService(userService, songService, playlistService);

        // Act: run the initialization process.
        dataService.initialize();

        // Assert: verify that at least one admin user now exists in the system.
        List<User> users = userService.getAllUsers();
        boolean adminExists = users.stream().anyMatch(User::isAdmin);

        assertTrue(adminExists, "Initialization should create a default admin account");
    }
}