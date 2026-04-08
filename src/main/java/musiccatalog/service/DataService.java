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

            seedImages();
            seedDefaultAdmin();
            seedSampleSongs();
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

    private void seedSampleSongs() throws IOException {
        List<Song> songs = songService.getAllSongs();
        if (!songs.isEmpty()) {
            return;
        }

        songService.createSong(new Song(
                null,
                "Blinding Lights",
                "The Weeknd",
                "After Hours",
                2020,
                "Synth-Pop",
                "I said, ooh, I am blinded by the lights...",
                "weeknd.svg"
        ));
        songService.createSong(new Song(
                null,
                "Levitating",
                "Dua Lipa",
                "Future Nostalgia",
                2020,
                "Pop",
                "You want me, I want you, baby...",
                "dua.svg"
        ));
        songService.createSong(new Song(
                null,
                "N95",
                "Kendrick Lamar",
                "Mr. Morale & the Big Steppers",
                2022,
                "Hip-Hop",
                "Hello, new world, all the boys and girls...",
                "kendrick.svg"
        ));
        songService.createSong(new Song(
                null,
                "Electric Feel",
                "MGMT",
                "Oracular Spectacular",
                2007,
                "Indie",
                "All along the western front...",
                "mgmt.svg"
        ));
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
