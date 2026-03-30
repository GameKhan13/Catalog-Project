package musiccatalog.service;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import musiccatalog.model.Playlist;
import musiccatalog.util.CsvUtil;
import musiccatalog.util.IdUtil;

public class PlaylistService {

    private static final String[] STANDARD_HEADERS = {"id", "name", "ownerUserId", "scope", "songs"};

    public String headerLine() {
        return String.join(",", STANDARD_HEADERS);
    }

    public void writeHeader(Path path) throws IOException {
        Files.writeString(path, headerLine() + System.lineSeparator(), StandardCharsets.UTF_8);
    }

    public synchronized List<Playlist> getAllPlaylists() throws IOException {
        List<Playlist> playlists = new ArrayList<>();
        if (Files.notExists(DataService.PLAYLISTS_CSV) || Files.size(DataService.PLAYLISTS_CSV) == 0L) {
            return playlists;
        }

        try (Reader reader = Files.newBufferedReader(DataService.PLAYLISTS_CSV, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader)) {
            for (CSVRecord record : parser) {
                playlists.add(new Playlist(
                        get(record, "id"),
                        get(record, "name"),
                        get(record, "ownerUserId"),
                        Boolean.parseBoolean(firstPresent(record, "scope", "isGlobal")),
                        CsvUtil.splitIds(firstPresent(record, "songs", "songIds"))
                ));
            }
        }
        return playlists;
    }

    public synchronized List<Playlist> getVisiblePlaylistsForUser(String userId) throws IOException {
        return getAllPlaylists().stream()
                .filter(playlist -> playlist.isGlobal() || playlist.getOwnerUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public synchronized List<Playlist> getPlaylistsOwnedByUser(String userId) throws IOException {
        return getAllPlaylists().stream()
                .filter(playlist -> !playlist.isGlobal() && playlist.getOwnerUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public synchronized Optional<Playlist> findById(String playlistId) throws IOException {
        return getAllPlaylists().stream()
                .filter(playlist -> playlist.getId().equals(playlistId))
                .findFirst();
    }

    public synchronized Playlist createPlaylist(String name, String ownerUserId, boolean global) throws IOException {
        Playlist playlist = new Playlist(IdUtil.newId(), name.trim(), ownerUserId, global, new ArrayList<>());
        List<Playlist> playlists = getAllPlaylists();
        playlists.add(playlist);
        writeAll(playlists);
        return playlist;
    }

    public synchronized Playlist renamePlaylist(String playlistId, String name) throws IOException {
        List<Playlist> playlists = getAllPlaylists();
        for (Playlist playlist : playlists) {
            if (playlist.getId().equals(playlistId)) {
                playlist.setName(name.trim());
                writeAll(playlists);
                return playlist;
            }
        }
        throw new IllegalArgumentException("Playlist not found");
    }

    public synchronized Playlist addSong(String playlistId, String songId) throws IOException {
        List<Playlist> playlists = getAllPlaylists();
        for (Playlist playlist : playlists) {
            if (playlist.getId().equals(playlistId)) {
                if (!playlist.getSongIds().contains(songId)) {
                    playlist.getSongIds().add(songId);
                }
                writeAll(playlists);
                return playlist;
            }
        }
        throw new IllegalArgumentException("Playlist not found");
    }

    public synchronized Playlist removeSong(String playlistId, String songId) throws IOException {
        List<Playlist> playlists = getAllPlaylists();
        for (Playlist playlist : playlists) {
            if (playlist.getId().equals(playlistId)) {
                playlist.getSongIds().removeIf(id -> id.equals(songId));
                writeAll(playlists);
                return playlist;
            }
        }
        throw new IllegalArgumentException("Playlist not found");
    }

    public synchronized void removeSongFromAllPlaylists(String songId) throws IOException {
        List<Playlist> playlists = getAllPlaylists();
        boolean changed = false;
        for (Playlist playlist : playlists) {
            if (playlist.getSongIds().removeIf(id -> id.equals(songId))) {
                changed = true;
            }
        }
        if (changed) {
            writeAll(playlists);
        }
    }

    public synchronized void deletePlaylist(String playlistId) throws IOException {
        List<Playlist> playlists = getAllPlaylists();
        playlists.removeIf(playlist -> playlist.getId().equals(playlistId));
        writeAll(playlists);
    }

    private synchronized void writeAll(List<Playlist> playlists) throws IOException {
        try (Writer writer = Files.newBufferedWriter(DataService.PLAYLISTS_CSV, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader(STANDARD_HEADERS)
                     .build())) {
            for (Playlist playlist : playlists) {
                printer.printRecord(
                        playlist.getId(),
                        playlist.getName(),
                        playlist.getOwnerUserId(),
                        playlist.isGlobal(),
                        CsvUtil.joinIds(playlist.getSongIds())
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
}
