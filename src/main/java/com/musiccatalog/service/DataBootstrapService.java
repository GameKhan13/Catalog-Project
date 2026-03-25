package com.musiccatalog.service;

import com.musiccatalog.model.Song;
import com.musiccatalog.model.User;
import com.musiccatalog.util.IdUtil;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Service that prepares the application's file-based data storage.
 *
 * On startup, this class makes sure:
 * - the data directories exist
 * - the CSV files exist and contain headers
 * - default images are present
 * - a default admin account exists
 * - sample songs exist when the catalog is empty
 */
public class DataBootstrapService {
    /**
     * Root data directory used by the application.
     */
    public static final Path DATA_DIR = Paths.get("data");

    /**
     * Folder that stores the generated song image files.
     */
    public static final Path IMAGE_DIR = DATA_DIR.resolve("images");

    /**
     * File that stores user records.
     */
    public static final Path USERS_CSV = DATA_DIR.resolve("users.csv");

    /**
     * File that stores song records.
     */
    public static final Path SONGS_CSV = DATA_DIR.resolve("songs.csv");

    /**
     * File that stores playlist records.
     */
    public static final Path PLAYLISTS_CSV = DATA_DIR.resolve("playlists.csv");

    /**
     * Services used during bootstrapping.
     */
    private final UserService userService;
    private final SongService songService;
    private final PlaylistService playlistService;

    /**
     * Constructor that receives the services needed for seeding data.
     */
    public DataBootstrapService(UserService userService, SongService songService, PlaylistService playlistService) {
        this.userService = userService;
        this.songService = songService;
        this.playlistService = playlistService;
    }

    /**
     * Prepares directories, CSV files, images, and starter data.
     */
    public void initialize() {
        try {
            // Make sure the main data directories exist before any file operations happen.
            Files.createDirectories(DATA_DIR);
            Files.createDirectories(IMAGE_DIR);

            // Create CSV files with header rows if they do not already exist.
            if (Files.notExists(USERS_CSV)) {
                Files.writeString(USERS_CSV, "id,username,passwordHash,isAdmin\n", StandardCharsets.UTF_8);
            }
            if (Files.notExists(SONGS_CSV)) {
                Files.writeString(SONGS_CSV, "id,title,artist,album,year,genre,lyrics,imageFile\n", StandardCharsets.UTF_8);
            }
            if (Files.notExists(PLAYLISTS_CSV)) {
                Files.writeString(PLAYLISTS_CSV, "id,name,ownerUserId,isGlobal,songIds\n", StandardCharsets.UTF_8);
            }

            // Seed the different parts of the application data.
            seedImages();
            seedDefaultAdmin();
            seedSampleSongs();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize data files", e);
        }
    }

    /**
     * Ensures there is at least one administrator account.
     */
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

    /**
     * Seeds a few starter songs when the songs file is empty.
     */
    private void seedSampleSongs() throws IOException {
        List<Song> songs = songService.getAllSongs();

        // If songs already exist, do not seed duplicates.
        if (!songs.isEmpty()) {
            return;
        }

        songService.createSong(new Song(
                IdUtil.newId(),
                "Blinding Lights",
                "The Weeknd",
                "After Hours",
                2020,
                "Synth-Pop",
                "I said, ooh, I'm blinded by the lights...",
                "weeknd.svg"
        ));
        songService.createSong(new Song(
                IdUtil.newId(),
                "Levitating",
                "Dua Lipa",
                "Future Nostalgia",
                2020,
                "Pop",
                "You want me, I want you, baby...",
                "dua.svg"
        ));
        songService.createSong(new Song(
                IdUtil.newId(),
                "N95",
                "Kendrick Lamar",
                "Mr. Morale & the Big Steppers",
                2022,
                "Hip-Hop",
                "Hello, new world, all the boys and girls...",
                "kendrick.svg"
        ));
        songService.createSong(new Song(
                IdUtil.newId(),
                "Electric Feel",
                "MGMT",
                "Oracular Spectacular",
                2007,
                "Indie",
                "All along the western front...",
                "mgmt.svg"
        ));
    }

    /**
     * Creates small default SVG artwork files if they are missing.
     */
    private void seedImages() throws IOException {
        writeImageIfMissing("weeknd.svg", svg("BW", "#3ddc84"));
        writeImageIfMissing("dua.svg", svg("DL", "#8b5cf6"));
        writeImageIfMissing("kendrick.svg", svg("KL", "#f59e0b"));
        writeImageIfMissing("mgmt.svg", svg("MG", "#06b6d4"));
        writeImageIfMissing("default.svg", svg("♪", "#1db954"));
    }

    /**
     * Writes an image file only when it does not already exist.
     */
    private void writeImageIfMissing(String fileName, String content) throws IOException {
        Path file = IMAGE_DIR.resolve(fileName);

        if (Files.notExists(file)) {
            Files.writeString(file, content, StandardCharsets.UTF_8);
        }
    }

    /**
     * Generates a simple SVG cover image as a String.
     *
     * The double percent signs inside the text block are later converted
     * back to single percent signs because String replacement is used below.
     */
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

    /**
     * Escapes a few XML-sensitive characters so generated SVG stays valid.
     */
    private String escapeXml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
