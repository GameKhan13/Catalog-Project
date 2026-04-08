package musiccatalog.service;

import musiccatalog.model.Playlist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlaylistServiceTest {

    @Test
    @DisplayName("UT-06-TB: PlaylistService.addSong() adds a song only once")
    void addSong_withValidPlaylistAndSong_addsSongOnlyOnce() throws Exception {
        UserService userService = new UserService();
        SongService songService = new SongService();
        PlaylistService playlistService = new PlaylistService();
        DataService dataService = new DataService(userService, songService, playlistService);

        dataService.initialize();

        Playlist playlist = playlistService.createPlaylist("Unit Test Playlist", "test-user", false);

        try {
            playlistService.addSong(playlist.getId(), "1");
            Playlist updatedPlaylist = playlistService.addSong(playlist.getId(), "1");

            assertEquals(1, updatedPlaylist.getSongIds().size(),
                    "The same song should only appear once in the playlist");
            assertEquals("1", updatedPlaylist.getSongIds().get(0),
                    "The expected song ID should be in the playlist");
        } finally {
            playlistService.deletePlaylist(playlist.getId());
        }
    }
}
