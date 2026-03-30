package musiccatalog.service;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import musiccatalog.model.Song;
import musiccatalog.util.IdUtil;

/**
 * Service to manage songs, capable of finding, creating, editing, and deleting songs.
 */
public class SongService {

    private static final String[] STANDARD_HEADERS = {"id", "title", "artist", "album", "year", "genre", "lyrics", "imageFile"};

    public String headerLine() {
        return String.join(",", STANDARD_HEADERS);
    }

    public void writeHeader(Path path) throws IOException {
        Files.writeString(path, headerLine() + System.lineSeparator(), StandardCharsets.UTF_8);
    }

    public synchronized List<Song> getAllSongs() throws IOException {
        List<Song> songs = new ArrayList<>();
        if (Files.notExists(DataService.SONGS_CSV) || Files.size(DataService.SONGS_CSV) == 0L) {
            return songs;
        }

        try (Reader reader = Files.newBufferedReader(DataService.SONGS_CSV, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader)) {
            for (CSVRecord record : parser) {
                songs.add(new Song(
                        get(record, "id"),
                        get(record, "title"),
                        get(record, "artist"),
                        get(record, "album"),
                        parseYear(get(record, "year")),
                        get(record, "genre"),
                        get(record, "lyrics"),
                        firstPresent(record, "imageFile", "imageName")
                ));
            }
        }

        songs.sort(Comparator.comparing(Song::getTitle, String.CASE_INSENSITIVE_ORDER));
        return songs;
    }

    public synchronized Optional<Song> findById(String songId) throws IOException {
        return getAllSongs().stream().filter(song -> song.getId().equals(songId)).findFirst();
    }

    public synchronized Song createSong(Song song) throws IOException {
        if (song.getId() == null || song.getId().isBlank()) {
            song.setId(IdUtil.newId());
        }
        List<Song> songs = getAllSongs();
        songs.add(song);
        writeAll(songs);
        return song;
    }

    public synchronized Song createSong(String title, String artist, String album, int year, String genre, String lyrics, String imageFile) throws IOException {
        return createSong(new Song(null, title, artist, album, year, genre, lyrics, imageFile));
    }

    public synchronized Song updateSong(String songId, Song updatedSong) throws IOException {
        List<Song> songs = getAllSongs();
        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i).getId().equals(songId)) {
                updatedSong.setId(songId);
                songs.set(i, updatedSong);
                writeAll(songs);
                return updatedSong;
            }
        }
        throw new IllegalArgumentException("Song not found");
    }

    public synchronized Song updateSong(String songId, String title, String artist, String album, int year, String genre, String lyrics, String imageFile) throws IOException {
        return updateSong(songId, new Song(songId, title, artist, album, year, genre, lyrics, imageFile));
    }

    public synchronized void deleteSong(String songId) throws IOException {
        List<Song> songs = getAllSongs();
        songs.removeIf(song -> song.getId().equals(songId));
        writeAll(songs);
    }

    private synchronized void writeAll(List<Song> songs) throws IOException {
        try (Writer writer = Files.newBufferedWriter(DataService.SONGS_CSV, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader(STANDARD_HEADERS)
                     .build())) {
            for (Song song : songs) {
                printer.printRecord(
                        song.getId(),
                        song.getTitle(),
                        song.getArtist(),
                        song.getAlbum(),
                        song.getYear(),
                        song.getGenre(),
                        song.getLyrics(),
                        song.getImageFile()
                );
            }
        }
    }

    private String firstPresent(CSVRecord record, String... headers) {
        for (String header : headers) {
            if (record.isMapped(header)) {
                return get(record, header);
            }
        }
        return "";
    }

    private String get(CSVRecord record, String header) {
        return record.isMapped(header) ? record.get(header) : "";
    }

    private int parseYear(String year) {
        try {
            return Integer.parseInt(year.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
