package musiccatalog.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mindrot.jbcrypt.BCrypt;

import musiccatalog.App;
import musiccatalog.service.DataService;
import musiccatalog.service.UserService;
import musiccatalog.util.JsonUtil;

import static spark.Spark.awaitInitialization;
import static spark.Spark.awaitStop;
import static spark.Spark.stop;

/**
 * Scenario tests for the assignment's login and playlist workflows.
 *
 * These tests exercise the real HTTP API instead of only calling service
 * methods directly. That makes them stronger evidence that the application
 * behaves correctly from the user's point of view.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthAndPlaylistScenarioTest {

    private static final String BASE_URL = "http://localhost:4570";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final UserService userService = new UserService();

    private String originalUsersCsv;
    private String originalPlaylistsCsv;
    private String originalSongsCsv;

    @BeforeAll
    void startApplicationAndBackupDataFiles() throws Exception {
        // Keep a copy of the current CSV files so the tests can safely restore them.
        originalUsersCsv = Files.readString(DataService.USERS_CSV, StandardCharsets.UTF_8);
        originalPlaylistsCsv = Files.readString(DataService.PLAYLISTS_CSV, StandardCharsets.UTF_8);
        originalSongsCsv = Files.readString(DataService.SONGS_CSV, StandardCharsets.UTF_8);

        // Start the real Spark application once for all scenario tests.
        App.main(new String[0]);
        awaitInitialization();
    }

    @AfterAll
    void stopApplicationAndRestoreDataFiles() throws Exception {
        // Shut down the server so the test run leaves no background process behind.
        stop();
        awaitStop();

        // Restore the original CSV content so the project data is unchanged after testing.
        Files.writeString(DataService.USERS_CSV, originalUsersCsv, StandardCharsets.UTF_8);
        Files.writeString(DataService.PLAYLISTS_CSV, originalPlaylistsCsv, StandardCharsets.UTF_8);
        Files.writeString(DataService.SONGS_CSV, originalSongsCsv, StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("ST-02-OB: Existing user logs in successfully and receives a token")
    void existingUserLogsIn_loginSuccessfulAndTokenReturned() throws Exception {
        // Arrange: create a real account in the CSV so the API can authenticate it.
        String username = uniqueUsername("login_user");
        String password = "Password123";
        userService.createUser(username, BCrypt.hashpw(password, BCrypt.gensalt()), false);

        // Act: send the same kind of login request the front end would send.
        HttpResponse<String> response = postJson(
                "/api/auth/login",
                "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}",
                null
        );

        // Assert: login succeeds and the response includes a non-empty token.
        assertEquals(200, response.statusCode());
        Map<String, Object> payload = readJsonObject(response.body());
        assertNotNull(payload.get("token"));
        assertFalse(payload.get("token").toString().isBlank());

        @SuppressWarnings("unchecked")
        Map<String, Object> userPayload = (Map<String, Object>) payload.get("user");
        assertEquals(username, userPayload.get("username"));
    }

    @Test
    @DisplayName("ST-03-OB: Logged-in user creates a personal playlist and it appears for that user")
    void userCreatesPersonalPlaylist_playlistAppearsForUser() throws Exception {
        // Arrange: create a user, log in through the API, and capture the returned token.
        String username = uniqueUsername("playlist_user");
        String password = "Password123";
        userService.createUser(username, BCrypt.hashpw(password, BCrypt.gensalt()), false);

        HttpResponse<String> loginResponse = postJson(
                "/api/auth/login",
                "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}",
                null
        );
        assertEquals(200, loginResponse.statusCode());
        String token = readJsonObject(loginResponse.body()).get("token").toString();

        String playlistName = "Road Trip Mix " + UUID.randomUUID();

        // Act: create a non-global playlist, then request the user's visible playlists.
        HttpResponse<String> createResponse = postJson(
                "/api/playlists",
                "{\"name\":\"" + playlistName + "\",\"isGlobal\":false}",
                token
        );
        HttpResponse<String> listResponse = getJson("/api/playlists", token);

        // Assert: creation succeeds and the new playlist appears in the user's list.
        assertEquals(200, createResponse.statusCode());
        Map<String, Object> createdPlaylist = readJsonObject(createResponse.body());
        assertEquals(playlistName, createdPlaylist.get("name"));
        assertEquals(false, createdPlaylist.get("global"));

        assertEquals(200, listResponse.statusCode());
        List<Map<String, Object>> playlists = readJsonArray(listResponse.body());
        boolean playlistFound = playlists.stream()
                .anyMatch(playlist -> createdPlaylist.get("id").equals(playlist.get("id"))
                        && playlistName.equals(playlist.get("name")));
        assertTrue(playlistFound);
    }

    @Test
    @DisplayName("ST-01-OB: New user signs up and is logged in")
    void newUserSignsUp_accountCreatedAndUserLoggedIn() throws Exception {
        String username = uniqueUsername("signup_user");
        String password = "Password123";

        HttpResponse<String> response = postJson(
                "/api/auth/signup",
                "{\"username\":\"" + username + "\",\"password\":\"" + password + "\",\"isAdmin\":false}",
                null
        );

        assertEquals(200, response.statusCode());

        Map<String, Object> payload = readJsonObject(response.body());
        assertNotNull(payload.get("token"));
        assertFalse(payload.get("token").toString().isBlank());

        @SuppressWarnings("unchecked")
        Map<String, Object> userPayload = (Map<String, Object>) payload.get("user");
        assertEquals(username, userPayload.get("username"));
        assertEquals(false, userPayload.get("isAdmin"));

        assertTrue(userService.findByUsername(username).isPresent());
    }

    @Test
    @DisplayName("ST-05-OB: User renames playlist and updated name is saved")
    void userRenamesPlaylist_updatedNameIsSaved() throws Exception {
        String username = uniqueUsername("rename_user");
        String password = "Password123";
        userService.createUser(username, BCrypt.hashpw(password, BCrypt.gensalt()), false);

        HttpResponse<String> loginResponse = postJson(
                "/api/auth/login",
                "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}",
                null
        );
        assertEquals(200, loginResponse.statusCode());
        String token = readJsonObject(loginResponse.body()).get("token").toString();

        String oldName = "Old Playlist " + UUID.randomUUID();
        HttpResponse<String> createResponse = postJson(
                "/api/playlists",
                "{\"name\":\"" + oldName + "\",\"isGlobal\":false}",
                token
        );
        assertEquals(200, createResponse.statusCode());

        Map<String, Object> createdPlaylist = readJsonObject(createResponse.body());
        String playlistId = createdPlaylist.get("id").toString();

        String newName = "New Playlist " + UUID.randomUUID();
        HttpResponse<String> renameResponse = putJson(
                "/api/playlists/" + playlistId,
                "{\"name\":\"" + newName + "\"}",
                token
        );

        assertEquals(200, renameResponse.statusCode());
        Map<String, Object> renamedPlaylist = readJsonObject(renameResponse.body());
        assertEquals(newName, renamedPlaylist.get("name"));

        HttpResponse<String> listResponse = getJson("/api/playlists", token);
        assertEquals(200, listResponse.statusCode());

        List<Map<String, Object>> playlists = readJsonArray(listResponse.body());
        boolean renamedPlaylistFound = playlists.stream()
                .anyMatch(playlist -> playlistId.equals(playlist.get("id"))
                        && newName.equals(playlist.get("name")));

        assertTrue(renamedPlaylistFound);
    }

    @Test
    @DisplayName("IT-06-TB: Logged-in user adds a valid song to a playlist")
    void loggedInUserAddsValidSongToPlaylist_playlistUpdatedCorrectly() throws Exception {
        String username = uniqueUsername("playlist_add_user");
        String password = "Password123";
        userService.createUser(username, BCrypt.hashpw(password, BCrypt.gensalt()), false);

        HttpResponse<String> userLoginResponse = postJson(
                "/api/auth/login",
                "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}",
                null
        );
        assertEquals(200, userLoginResponse.statusCode());
        String userToken = readJsonObject(userLoginResponse.body()).get("token").toString();

        HttpResponse<String> adminLoginResponse = postJson(
                "/api/auth/login",
                "{\"username\":\"admin\",\"password\":\"admin123\"}",
                null
        );
        assertEquals(200, adminLoginResponse.statusCode());
        String adminToken = readJsonObject(adminLoginResponse.body()).get("token").toString();

        HttpResponse<String> playlistResponse = postJson(
                "/api/playlists",
                "{\"name\":\"My Playlist\",\"isGlobal\":false}",
                userToken
        );
        assertEquals(200, playlistResponse.statusCode());
        String playlistId = readJsonObject(playlistResponse.body()).get("id").toString();

        HttpResponse<String> createSongResponse = postJson(
                "/api/admin/songs",
                "{\"title\":\"Test Song\",\"artist\":\"Test Artist\",\"album\":\"Test Album\",\"year\":2024,\"genre\":\"Pop\",\"lyrics\":\"lyrics\",\"imageFile\":\"default.svg\"}",
                adminToken
        );
        assertEquals(200, createSongResponse.statusCode());
        String songId = readJsonObject(createSongResponse.body()).get("id").toString();

        HttpResponse<String> addSongResponse = postJson(
                "/api/playlists/" + playlistId + "/songs",
                "{\"songId\":\"" + songId + "\"}",
                userToken
        );

        assertEquals(200, addSongResponse.statusCode());
        Map<String, Object> updatedPlaylist = readJsonObject(addSongResponse.body());

        @SuppressWarnings("unchecked")
        List<String> songIds = (List<String>) updatedPlaylist.get("songIds");

        assertTrue(songIds.contains(songId));
    }

    @Test
    @DisplayName("IT-07-TB: Admin deletes song and it is removed from songs and playlists")
    void adminDeletesSong_songRemovedFromSongsAndPlaylists() throws Exception {
        String username = uniqueUsername("playlist_delete_user");
        String password = "Password123";
        userService.createUser(username, BCrypt.hashpw(password, BCrypt.gensalt()), false);

        HttpResponse<String> userLoginResponse = postJson(
                "/api/auth/login",
                "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}",
                null
        );
        assertEquals(200, userLoginResponse.statusCode());
        String userToken = readJsonObject(userLoginResponse.body()).get("token").toString();

        HttpResponse<String> adminLoginResponse = postJson(
                "/api/auth/login",
                "{\"username\":\"admin\",\"password\":\"admin123\"}",
                null
        );
        assertEquals(200, adminLoginResponse.statusCode());
        String adminToken = readJsonObject(adminLoginResponse.body()).get("token").toString();

        HttpResponse<String> playlistResponse = postJson(
                "/api/playlists",
                "{\"name\":\"Delete Test Playlist\",\"isGlobal\":false}",
                userToken
        );
        assertEquals(200, playlistResponse.statusCode());
        String playlistId = readJsonObject(playlistResponse.body()).get("id").toString();

        HttpResponse<String> createSongResponse = postJson(
                "/api/admin/songs",
                "{\"title\":\"Delete Song\",\"artist\":\"Delete Artist\",\"album\":\"Delete Album\",\"year\":2024,\"genre\":\"Pop\",\"lyrics\":\"lyrics\",\"imageFile\":\"default.svg\"}",
                adminToken
        );
        assertEquals(200, createSongResponse.statusCode());
        String songId = readJsonObject(createSongResponse.body()).get("id").toString();

        HttpResponse<String> addSongResponse = postJson(
                "/api/playlists/" + playlistId + "/songs",
                "{\"songId\":\"" + songId + "\"}",
                userToken
        );
        assertEquals(200, addSongResponse.statusCode());

        HttpResponse<String> deleteSongResponse = deleteJson(
                "/api/admin/songs/" + songId,
                adminToken
        );
        assertEquals(200, deleteSongResponse.statusCode());

        HttpResponse<String> songsResponse = getJson("/api/songs", null);
        assertEquals(200, songsResponse.statusCode());
        List<Map<String, Object>> songs = readJsonArray(songsResponse.body());

        boolean songStillExists = songs.stream()
                .anyMatch(song -> songId.equals(song.get("id")));
        assertFalse(songStillExists);

        HttpResponse<String> playlistsResponse = getJson("/api/playlists", userToken);
        assertEquals(200, playlistsResponse.statusCode());
        List<Map<String, Object>> playlists = readJsonArray(playlistsResponse.body());

        Map<String, Object> updatedPlaylist = playlists.stream()
                .filter(playlist -> playlistId.equals(playlist.get("id")))
                .findFirst()
                .orElseThrow();

        @SuppressWarnings("unchecked")
        List<String> songIds = (List<String>) updatedPlaylist.get("songIds");

        assertFalse(songIds.contains(songId));
    }

    private HttpResponse<String> postJson(String path, String jsonBody, String bearerToken)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

        if (bearerToken != null && !bearerToken.isBlank()) {
            builder.header("Authorization", "Bearer " + bearerToken);
        }

        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> getJson(String path, String bearerToken)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .GET();

        if (bearerToken != null && !bearerToken.isBlank()) {
            builder.header("Authorization", "Bearer " + bearerToken);
        }

        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> putJson(String path, String jsonBody, String bearerToken)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody));

        if (bearerToken != null && !bearerToken.isBlank()) {
            builder.header("Authorization", "Bearer " + bearerToken);
        }

        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> deleteJson(String path, String bearerToken)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .DELETE();

        if (bearerToken != null && !bearerToken.isBlank()) {
            builder.header("Authorization", "Bearer " + bearerToken);
        }

        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readJsonObject(String json) throws IOException {
        return JsonUtil.MAPPER.readValue(json, Map.class);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> readJsonArray(String json) throws IOException {
        return JsonUtil.MAPPER.readValue(json, List.class);
    }

    private String uniqueUsername(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }
}
