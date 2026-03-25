package com.musiccatalog.service;

import com.musiccatalog.model.Song;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service responsible for reading and writing songs in songs.csv.
 *
 * Like the other file-based services in this project, the methods are
 * synchronized so two threads do not modify the same CSV file at once.
 */
public class SongService {
    /**
     * Loads every song from songs.csv.
     *
     * After reading the file, the method sorts songs by title so the UI
     * receives the catalog in a consistent order.
     *
     * @return list of songs sorted alphabetically by title
     * @throws IOException if the file cannot be read
     */
    public synchronized List<Song> getAllSongs() throws IOException {
        // This list will hold each song created from the CSV rows.
        List<Song> songs = new ArrayList<>();

        // Open the songs file and parse it using Apache Commons CSV.
        try (Reader reader = Files.newBufferedReader(DataBootstrapService.SONGS_CSV, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader)) {

            // Convert each CSV row into a Song object.
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

        // Sort the songs so the catalog order is predictable in the UI.
        songs.sort(Comparator.comparing(Song::getTitle, String.CASE_INSENSITIVE_ORDER));

        return songs;
    }

    /**
     * Finds a song by its unique id.
     *
     * @param songId id to search for
     * @return Optional containing the matching song when found
     * @throws IOException if the file cannot be read
     */
    public synchronized Optional<Song> findById(String songId) throws IOException {
        return getAllSongs().stream().filter(song -> song.getId().equals(songId)).findFirst();
    }

    /**
     * Adds a new song to the CSV file.
     *
     * @param song song object to add
     * @return the same song object after it has been saved
     * @throws IOException if the file cannot be read or written
     */
    public synchronized Song createSong(Song song) throws IOException {
        // Load the current catalog into memory.
        List<Song> songs = getAllSongs();

        // Append the new song to the end of the list.
        songs.add(song);

        // Rewrite the full CSV file with the new contents.
        writeAll(songs);

        return song;
    }

    /**
     * Replaces the stored data for one existing song.
     *
     * The method keeps the original id from the path parameter,
     * even if the provided updatedSong object contains a different id.
     *
     * @param songId id of the song to update
     * @param updatedSong replacement data
     * @return the updated song object after saving
     * @throws IOException if the file cannot be read or written
     */
    public synchronized Song updateSong(String songId, Song updatedSong) throws IOException {
        // Load all songs so the matching one can be replaced in memory.
        List<Song> songs = getAllSongs();

        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i).getId().equals(songId)) {
                // Force the replacement object to keep the original id.
                updatedSong.setId(songId);

                // Replace the old song in the list.
                songs.set(i, updatedSong);

                // Persist the updated list back to the CSV file.
                writeAll(songs);

                return updatedSong;
            }
        }

        // If no match was found, the requested song id does not exist.
        throw new IllegalArgumentException("Song not found");
    }

    /**
     * Deletes a song by removing it from the in-memory list
     * and then rewriting the CSV file.
     *
     * @param songId id of the song to remove
     * @throws IOException if the file cannot be read or written
     */
    public synchronized void deleteSong(String songId) throws IOException {
        // Read the current catalog.
        List<Song> songs = getAllSongs();

        // Remove any song whose id matches the requested id.
        songs.removeIf(song -> song.getId().equals(songId));

        // Save the remaining songs back to the CSV file.
        writeAll(songs);
    }

    /**
     * Writes the full list of songs back to songs.csv.
     *
     * @param songs all songs that should exist in storage after the write
     * @throws IOException if the file cannot be written
     */
    private synchronized void writeAll(List<Song> songs) throws IOException {
        // Open the songs CSV file for writing and include the header.
        try (Writer writer = Files.newBufferedWriter(DataBootstrapService.SONGS_CSV, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader("id", "title", "artist", "album", "year", "genre", "lyrics", "imageFile")
                     .build())) {

            // Write one CSV row for each Song object.
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

    /**
     * Safely converts the year text from the CSV file into an int.
     *
     * If the year is missing or invalid, the method returns 0
     * so the rest of the file can still be loaded.
     *
     * @param year raw year value from CSV
     * @return parsed year or 0 when parsing fails
     */
    private int parseYear(String year) {
        try {
            return Integer.parseInt(year.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
