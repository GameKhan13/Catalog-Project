package musiccatalog.service;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import musiccatalog.model.Song;

public class SongService {
    public synchronized List<Song> getAllSongs() throws IOException {
        List<Song> songs = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(DataBootstrapService.SONGS_CSV, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader)) {
            for (CSVRecord record : parser) {
                songs.add(new Song(
                        record.get("id"),
                        record.get("title"),
                        record.get("artist"),
                        record.get("album"),
                        parseYear(record.get("year")),
                        record.get("genre"),
                        record.get("lyrics"),
                        record.get("imageFile")
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
        List<Song> songs = getAllSongs();
        songs.add(song);
        writeAll(songs);
        return song;
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

    public synchronized void deleteSong(String songId) throws IOException {
        List<Song> songs = getAllSongs();
        songs.removeIf(song -> song.getId().equals(songId));
        writeAll(songs);
    }

    private synchronized void writeAll(List<Song> songs) throws IOException {
        try (Writer writer = Files.newBufferedWriter(DataBootstrapService.SONGS_CSV, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader("id", "title", "artist", "album", "year", "genre", "lyrics", "imageFile")
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

    private int parseYear(String year) {
        try {
            return Integer.parseInt(year.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
