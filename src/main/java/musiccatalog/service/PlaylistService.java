package musiccatalog.service;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

    private final String idHeader = "id";
    private final String nameHeader = "name";
    private final String ownerHeader = "ownerUserId";
    private final String scopeHeader = "scope";
    private final String songsHeader = "songs";

    public synchronized List<Playlist> getAllPlaylists() throws IOException {
        List<Playlist> playlists = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(DataService.PLAYLISTS_CSV, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader)) {
            for (CSVRecord record : parser) {
                playlists.add(new Playlist(
                        record.get(idHeader),
                        record.get(nameHeader),
                        record.get(ownerHeader),
                        Boolean.parseBoolean(record.get(scopeHeader)),
                        CsvUtil.splitIds(record.get(songsHeader))
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
                     .setHeader(idHeader, nameHeader, ownerHeader, scopeHeader, songsHeader)
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
}
