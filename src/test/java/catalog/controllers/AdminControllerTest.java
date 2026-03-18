package catalog.controllers;

import catalog.back_end.EntryService;
import catalog.back_end.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class AdminControllerTest {

    @TempDir
    Path tempDir;

    private AdminController createAdminController() throws Exception {
        Path usersFile = tempDir.resolve("Users.csv");
        Path songsFile = tempDir.resolve("Songs.csv");

        Files.writeString(
                usersFile,
                "username,password,isAdmin,playlist_ids\n" +
                        "admin,pass,true,\"\""
        );

        UserService userService = new UserService(usersFile.toString());
        assertTrue(userService.loginUser("admin", "pass"));

        EntryService entryService = new EntryService(songsFile.toString(), userService);
        return new AdminController(entryService);
    }

    @Test
    void addSong_shouldFail_whenNameIsBlank() {
        AdminController controller = new AdminController(null);

        boolean result = controller.addSong("", "Artist", "Album", 2020, "Pop");

        assertFalse(result);
    }

    @Test
    void addSong_shouldFail_whenArtistIsBlank() {
        AdminController controller = new AdminController(null);

        boolean result = controller.addSong("Song", "", "Album", 2020, "Pop");

        assertFalse(result);
    }

    @Test
    void addSong_shouldFail_whenNameIsOnlySpaces() {
        AdminController controller = new AdminController(null);

        boolean result = controller.addSong("   ", "Artist", "Album", 2020, "Pop");

        assertFalse(result);
    }

    @Test
    void addSong_shouldFail_whenArtistIsOnlySpaces() {
        AdminController controller = new AdminController(null);

        boolean result = controller.addSong("Song", "   ", "Album", 2020, "Pop");

        assertFalse(result);
    }
    @Test
    void addSong_shouldSucceed_whenInputIsValid() throws Exception {
        AdminController controller = createAdminController();

        boolean result = controller.addSong("Song", "Artist", "Album", 2020, "Pop");

        assertTrue(result);
        assertEquals(1, controller.getAllSongs().size());
        assertEquals("Song", controller.getAllSongs().getFirst().getName());
    }

    @Test
    void editSong_shouldSucceed_whenSongExists() throws Exception {
        AdminController controller = createAdminController();
        controller.addSong("Old Song", "Old Artist", "Old Album", 2000, "Rock");

        int songId = controller.getAllSongs().getFirst().getSongId();

        boolean result = controller.editSong(songId, "New Song", "New Artist", "New Album", 2024, "Pop");

        assertTrue(result);
        assertEquals("New Song", controller.getAllSongs().getFirst().getName());
        assertEquals("New Artist", controller.getAllSongs().getFirst().getArtist());
    }

    @Test
    void deleteSong_shouldSucceed_whenSongExists() throws Exception {
        AdminController controller = createAdminController();
        controller.addSong("Song", "Artist", "Album", 2020, "Pop");

        int songId = controller.getAllSongs().getFirst().getSongId();

        boolean result = controller.deleteSong(songId);

        assertTrue(result);
        assertEquals(0, controller.getAllSongs().size());
    }

    @Test
    void deleteSong_shouldFail_whenSongDoesNotExist() throws Exception {
        AdminController controller = createAdminController();
        boolean result = controller.deleteSong(-1);
        assertFalse(result);
    }
    @Test
    void editSong_shouldFail_whenSongDoesNotExist() throws Exception {
        AdminController controller = createAdminController();

        boolean result = controller.editSong(999, "New Song", "New Artist", "New Album", 2024, "Pop");

        assertFalse(result);
    }

}