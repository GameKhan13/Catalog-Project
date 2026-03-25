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
import musiccatalog.util.IdUtil;

/**
 * Service to manage songs, capable of finding, creating, editing and deleting songs
 */
public class SongService {

    private final String idHeader = "id";
    private final String titleHeader = "title";
    private final String artistHeader = "artist";
    private final String albumHeader = "album";
    private final String yearHeader = "year";
    private final String genreHeader = "genre";
    private final String imageHeader = "imageName";

    /**
     * @return a {@code List<Song>} that contains all songs that exist
     * @throws IOException
     */
    public synchronized List<Song> getAllSongs() throws IOException {
        List<Song> songs = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(DataService.SONGS_CSV, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader)) {
            for (CSVRecord record : parser) {
                songs.add(new Song(
                        record.get(idHeader),
                        record.get(titleHeader),
                        record.get(artistHeader),
                        record.get(albumHeader),
                        parseYear(record.get(yearHeader)),
                        record.get(genreHeader),
                        record.get(imageHeader)
                ));
            }
        }
        songs.sort(Comparator.comparing(Song::getTitle, String.CASE_INSENSITIVE_ORDER));
        return songs;
    }

    /**
     * Attempts to find the specified song id, check if the song was found by using {@code isPresent()}
     * @param songId the song id to search for
     * @return A {@code Optional} object that wraps the {@code Song} object
     * @throws IOException
     */
    public synchronized Optional<Song> findById(String songId) throws IOException {
        return getAllSongs().stream().filter(song -> song.getId().equals(songId)).findFirst();
    }

    /**
     * Creates a song and updates the file
     * @param song 
     * @return
     * @throws IOException
     */
    public synchronized Song createSong(String title, String artist, String album, int year, String genre, String imageName) throws IOException {
        List<Song> songs = getAllSongs();
        Song song = new Song(
            IdUtil.newId(), 
            title, 
            artist, 
            album, 
            year, 
            genre, 
            imageName
        );
        songs.add(song);
        writeAll(songs);
        return song;
    }

    /**
     * Updates the song with matching id to the new data then updates the files
     * @param songId the id of the song to update
     * @param newTitle the new title for the song
     * @param newArtist the new artist for the song
     * @param newAlbum the new album for the song
     * @param newYear the new year for the song
     * @param newGenre the new genre for the song
     * @param newImageName the new image for the song
     * @return the newly updated song
     * @throws IOException
     */
    public synchronized Song updateSong(String songId, String newTitle, String newArtist, String newAlbum, int newYear, String newGenre, String newImageName) throws IOException {
        List<Song> songs = getAllSongs();
        Song newSong = new Song(songId, newTitle, newArtist, newAlbum, newYear, newGenre, newImageName);
        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i).getId().equals(songId)) {
                newSong.setId(songId);
                songs.set(i, newSong);
                writeAll(songs);
                return newSong;
            }
        }
        throw new IllegalArgumentException("Song not found");
    }

    /**
     * Deletes the song with matching id
     * @param songId the id of the song to delete
     * @throws IOException
     */
    public synchronized void deleteSong(String songId) throws IOException {
        List<Song> songs = getAllSongs();
        songs.removeIf(song -> song.getId().equals(songId));
        writeAll(songs);
    }

    /**
     * Updates the songs file to be the list of songs
     * @param songs the list of songs to write
     * @throws IOException
     */
    private synchronized void writeAll(List<Song> songs) throws IOException {
        try (Writer writer = Files.newBufferedWriter(DataService.SONGS_CSV, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader(idHeader, titleHeader, artistHeader, albumHeader, yearHeader, genreHeader, imageHeader)
                     .build())) {
            for (Song song : songs) {
                printer.printRecord(
                        song.getId(),
                        song.getTitle(),
                        song.getArtist(),
                        song.getAlbum(),
                        song.getYear(),
                        song.getGenre(),
                        song.getImageFile()
                );
            }
        }
    }

    /**
     * Helper function for parsing the year data
     * @param year the year obtained (as {@code String})
     * @return the year as {@code int}
     */
    private int parseYear(String year) {
        try {
            return Integer.parseInt(year.trim());
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }
}
