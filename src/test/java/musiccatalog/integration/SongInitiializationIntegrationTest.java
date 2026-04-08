package musiccatalog.integration;

import musiccatalog.model.Song;
import musiccatalog.service.DataService;
import musiccatalog.service.PlaylistService;
import musiccatalog.service.SongService;
import musiccatalog.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SongInitiializationIntegrationTest {

    @Test
    @DisplayName("IT-02-TB: Initialization inserts sample songs when the song list is empty")
    void initialize_withEmptySongs_insertsSampleSongs() throws Exception {

        // Arrange: create fresh service instances simulating a clean application startup.
        UserService userService = new UserService();
        SongService songService = new SongService();
        PlaylistService playlistService = new PlaylistService();

        DataService dataService =
                new DataService(userService, songService, playlistService);

        // Act: run the initialization process.
        dataService.initialize();

        // Assert: verify that the song list is no longer empty after initialization.
        List<Song> songs = songService.getAllSongs();

        assertNotNull(songs);
        assertFalse(songs.isEmpty(), "Initialization should insert sample songs when no songs exist");
    }
}