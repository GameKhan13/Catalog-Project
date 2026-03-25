package com.musiccatalog.service;

import com.musiccatalog.model.Playlist;
import com.musiccatalog.util.CsvUtil;
import com.musiccatalog.util.IdUtil;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for playlist storage and manipulation.
 *
 * Playlists are stored in playlists.csv and the song membership is stored
 * as pipe-separated song ids inside one CSV column.
 */
public class PlaylistService {
    /**
     * Reads every playlist from playlists.csv.
     *
     * @return list of all playlists in storage
     * @throws IOException if the file cannot be read
     */
    public synchronized List<Playlist> getAllPlaylists() throws IOException {
        List<Playlist> playlists = new ArrayList<>();

        // Open the playlists CSV file and parse each record.
        try (Reader reader = Files.newBufferedReader(DataBootstrapService.PLAYLISTS_CSV, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader)) {

            // Convert each CSV row into a Playlist object.
            for (CSVRecord record : parser) {
                playlists.add(new Playlist(
                        record.get("id"),
                        record.get("name"),
                        record.get("ownerUserId"),
                        Boolean.parseBoolean(record.get("isGlobal")),
                        CsvUtil.splitIds(record.get("songIds"))
                ));
            }
        }

        return playlists;
    }

    /**
     * Returns playlists visible to a specific user.
     *
     * A playlist is visible when it is:
     * - global, or
     * - owned by that user
     */
    public synchronized List<Playlist> getVisiblePlaylistsForUser(String userId) throws IOException {
        return getAllPlaylists().stream()
                .filter(playlist -> playlist.isGlobal() || playlist.getOwnerUserId().equals(userId))
                .collect(Collectors.toList());
    }

    /**
     * Returns only non-global playlists owned by a user.
     */
    public synchronized List<Playlist> getPlaylistsOwnedByUser(String userId) throws IOException {
        return getAllPlaylists().stream()
                .filter(playlist -> !playlist.isGlobal() && playlist.getOwnerUserId().equals(userId))
                .collect(Collectors.toList());
    }

    /**
     * Finds a playlist by id.
     */
    public synchronized Optional<Playlist> findById(String playlistId) throws IOException {
        return getAllPlaylists().stream()
                .filter(playlist -> playlist.getId().equals(playlistId))
                .findFirst();
    }

    /**
     * Creates a new playlist and saves it.
     */
    public synchronized Playlist createPlaylist(String name, String ownerUserId, boolean global) throws IOException {
        // Build the new playlist with a generated id and an empty song list.
        Playlist playlist = new Playlist(IdUtil.newId(), name.trim(), ownerUserId, global, new ArrayList<>());

        // Append it to the existing playlists.
        List<Playlist> playlists = getAllPlaylists();
        playlists.add(playlist);

        // Rewrite the full CSV file with the new playlist included.
        writeAll(playlists);

        return playlist;
    }

    /**
     * Renames an existing playlist.
     */
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

    /**
     * Adds a song id to a playlist if it is not already present.
     */
    public synchronized Playlist addSong(String playlistId, String songId) throws IOException {
        List<Playlist> playlists = getAllPlaylists();

        for (Playlist playlist : playlists) {
            if (playlist.getId().equals(playlistId)) {
                // Prevent duplicate song ids in the same playlist.
                if (!playlist.getSongIds().contains(songId)) {
                    playlist.getSongIds().add(songId);
                }
                writeAll(playlists);
                return playlist;
            }
        }

        throw new IllegalArgumentException("Playlist not found");
    }

    /**
     * Removes a song id from a playlist.
     */
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

    /**
     * Removes a song from every playlist that currently contains it.
     *
     * This is useful when a song is deleted from the catalog.
     */
    public synchronized void removeSongFromAllPlaylists(String songId) throws IOException {
        List<Playlist> playlists = getAllPlaylists();
        boolean changed = false;

        for (Playlist playlist : playlists) {
            if (playlist.getSongIds().removeIf(id -> id.equals(songId))) {
                changed = true;
            }
        }

        // Only rewrite the file if at least one playlist actually changed.
        if (changed) {
            writeAll(playlists);
        }
    }

    /**
     * Deletes a playlist by id.
     */
    public synchronized void deletePlaylist(String playlistId) throws IOException {
        List<Playlist> playlists = getAllPlaylists();
        playlists.removeIf(playlist -> playlist.getId().equals(playlistId));
        writeAll(playlists);
    }

    /**
     * Writes every playlist back to playlists.csv.
     */
    private synchronized void writeAll(List<Playlist> playlists) throws IOException {
        try (Writer writer = Files.newBufferedWriter(DataBootstrapService.PLAYLISTS_CSV, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader("id", "name", "ownerUserId", "isGlobal", "songIds")
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
