package musiccatalog.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import musiccatalog.model.User;

public class DataService {
    public static final Path DATA_DIR = Paths.get("data");
    public static final Path IMAGE_DIR = DATA_DIR.resolve("images");
    public static final Path USERS_CSV = DATA_DIR.resolve("users.csv");
    public static final Path SONGS_CSV = DATA_DIR.resolve("songs.csv");
    public static final Path PLAYLISTS_CSV = DATA_DIR.resolve("playlists.csv");

    private final UserService userService;
    private final SongService songService;
    private final PlaylistService playlistService;

    public DataService(UserService userService, SongService songService, PlaylistService playlistService) {
        this.userService = userService;
        this.songService = songService;
        this.playlistService = playlistService;
    }

    public void initialize() {
        try {
            Files.createDirectories(DATA_DIR);
            Files.createDirectories(IMAGE_DIR);

            ensureFileHasHeader(USERS_CSV, userService.headerLine());
            ensureFileHasHeader(SONGS_CSV, songService.headerLine());
            ensureFileHasHeader(PLAYLISTS_CSV, playlistService.headerLine());

            seedDefaultAdmin();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize data files", e);
        }
    }

    private void ensureFileHasHeader(Path file, String headerLine) throws IOException {
        if (Files.notExists(file) || Files.size(file) == 0L) {
            Files.writeString(file, headerLine + System.lineSeparator(), StandardCharsets.UTF_8);
        }
    }

    private void seedDefaultAdmin() throws IOException {
        List<User> users = userService.getAllUsers();
        boolean hasAdmin = users.stream().anyMatch(User::isAdmin);
        if (!hasAdmin) {
            userService.createUser(
                    "admin",
                    BCrypt.hashpw("admin123", BCrypt.gensalt()),
                    true
            );
        }
    }
}
