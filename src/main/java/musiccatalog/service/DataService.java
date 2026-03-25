package musiccatalog.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import musiccatalog.model.Song;
import musiccatalog.model.User;

public class DataService {
    public static final Path DATA_DIR = Paths.get("src\\main\\resources");
    public static final Path IMAGE_DIR = DATA_DIR.resolve("public\\images");
    public static final Path USERS_CSV = DATA_DIR.resolve("private\\users.csv");
    public static final Path SONGS_CSV = DATA_DIR.resolve("private\\songs.csv");
    public static final Path PLAYLISTS_CSV = DATA_DIR.resolve("private\\playlists.csv");

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

            if (Files.notExists(USERS_CSV)) {
                userService.writeHeader(USERS_CSV);
            }
            if (Files.notExists(SONGS_CSV)) {
                songService.writeHeader(SONGS_CSV);
            }
            if (Files.notExists(PLAYLISTS_CSV)) {
                playlistService.writeHeader(PLAYLISTS_CSV);
            }

            seedImages();
            seedDefaultAdmin();
            seedSampleSongs();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize data files", e);
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

    private void seedSampleSongs() throws IOException {
        List<Song> songs = songService.getAllSongs();
        if (!songs.isEmpty()) {
            return;
        }

        songService.createSong(
                "Blinding Lights",
                "The Weeknd",
                "After Hours",
                2020,
                "Synth-Pop",
                "weeknd.svg"
        );
        songService.createSong(
                "Levitating",
                "Dua Lipa",
                "Future Nostalgia",
                2020,
                "Pop",
                "dua.svg"
        );
        songService.createSong(
                "N95",
                "Kendrick Lamar",
                "Mr. Morale & the Big Steppers",
                2022,
                "Hip-Hop",
                "kendrick.svg"
        );
        songService.createSong(
                "Electric Feel",
                "MGMT",
                "Oracular Spectacular",
                2007,
                "Indie",
                "mgmt.svg"
        );
    }

    private void seedImages() throws IOException {
        writeImageIfMissing("weeknd.svg", svg("BW", "#3ddc84"));
        writeImageIfMissing("dua.svg", svg("DL", "#8b5cf6"));
        writeImageIfMissing("kendrick.svg", svg("KL", "#f59e0b"));
        writeImageIfMissing("mgmt.svg", svg("MG", "#06b6d4"));
        writeImageIfMissing("default.svg", svg("♪", "#1db954"));
    }

    private void writeImageIfMissing(String fileName, String content) throws IOException {
        Path file = IMAGE_DIR.resolve(fileName);
        if (Files.notExists(file)) {
            Files.writeString(file, content, StandardCharsets.UTF_8);
        }
    }

    private String svg(String text, String accent) {
        String safeText = escapeXml(text);
        String safeAccent = escapeXml(accent);

        return """
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 320 320">
                  <defs>
                    <linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
                      <stop offset="0%%" stop-color="#121212"/>
                      <stop offset="100%%" stop-color="ACCENT_COLOR"/>
                    </linearGradient>
                  </defs>
                  <rect width="320" height="320" rx="28" fill="url(#g)"/>
                  <circle cx="160" cy="160" r="96" fill="rgba(255,255,255,0.08)"/>
                  <text x="160" y="182" text-anchor="middle" fill="#ffffff" font-size="76" font-family="Arial, sans-serif" font-weight="700">TEXT_VALUE</text>
                </svg>
                """
                .replace("ACCENT_COLOR", safeAccent)
                .replace("TEXT_VALUE", safeText)
                .replace("%%", "%");
    }

    private String escapeXml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
