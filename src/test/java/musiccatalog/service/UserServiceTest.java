package musiccatalog.service;

import musiccatalog.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Test
    @DisplayName("UT-05-CB: UserService.findByUsername() finds the correct user with different case")
    void findByUsername_withDifferentCase_returnsCorrectUser() throws Exception {
        UserService userService = new UserService();
        SongService songService = new SongService();
        PlaylistService playlistService = new PlaylistService();
        DataService dataService = new DataService(userService, songService, playlistService);

        dataService.initialize();

        Optional<User> foundUser = userService.findByUsername("AdMiN");

        assertTrue(foundUser.isPresent(), "User should still be found even with different case");
        assertEquals("admin", foundUser.get().getUsername(), "The correct user should be returned");
    }
}