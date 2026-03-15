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
    void addSong_shouldFail_whenArtistIsOnlySpecialCharacter() {
        AdminController controller = new AdminController(null);

        boolean result = controller.addSong("Song", "!", "Album", 2020, "Pop");

        assertFalse(result);
    }

    @Test
    void addSong_shouldFail_whenNameIsOnlySpecialCharacter() {
        AdminController controller = new AdminController(null);

        boolean result = controller.addSong("!", "Artist", "Album", 2020, "Pop");

        assertFalse(result);
    }

    @Test
    void addSong_shouldSucceed_whenInputIsValid() throws Exception {
        AdminController controller = createAdminController();

        boolean result = controller.addSong("Song", "Artist", "Album", 2020, "Pop");

        assertTrue(result);
        assertEquals(1, controller.getAllSongs().size());
        assertEquals("Song", controller.getAllSongs().get(0).getName());
    }

    @Test
    void editSong_shouldSucceed_whenSongExists() throws Exception {
        AdminController controller = createAdminController();
        controller.addSong("Old Song", "Old Artist", "Old Album", 2000, "Rock");

        int songId = controller.getAllSongs().get(0).getSongId();

        boolean result = controller.editSong(songId, "New Song", "New Artist", "New Album", 2024, "Pop");

        assertTrue(result);
        assertEquals("New Song", controller.getAllSongs().get(0).getName());
        assertEquals("New Artist", controller.getAllSongs().get(0).getArtist());
    }

    @Test
    void deleteSong_shouldSucceed_whenSongExists() throws Exception {
        AdminController controller = createAdminController();
        controller.addSong("Song", "Artist", "Album", 2020, "Pop");

        int songId = controller.getAllSongs().get(0).getSongId();

        boolean result = controller.deleteSong(songId);

        assertTrue(result);
        assertEquals(0, controller.getAllSongs().size());
    }
}