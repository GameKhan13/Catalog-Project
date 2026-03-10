package catalog.controllers;

import catalog.back_end.Entry;
import catalog.back_end.EntryService;

import java.io.IOException;
import java.util.List;

public class AdminController {

    private final EntryService entryService;

    public AdminController(EntryService entryService) {
        this.entryService = entryService;
    }

    public boolean addSong(String name, String artist, String album,
                           int year, String genre) {

        if (name == null || name.isBlank()) return false;
        if (artist == null || artist.isBlank()) return false;

        try {
            return entryService.addEntry(name, artist, album, year, genre);
        } catch (IOException | SecurityException e) {
            return false;
        }
    }

    public boolean editSong(int songId, String name, String artist,
                            String album, int year, String genre) {

        try {
            return entryService.editEntry(songId, name, artist, album, year, genre);
        } catch (IOException | SecurityException e) {
            return false;
        }
    }

    public boolean deleteSong(int songId) {

        try {
            return entryService.deleteEntry(songId);
        } catch (IOException | SecurityException e) {
            return false;
        }
    }

    public List<Entry> getAllSongs() throws IOException {
        return entryService.getAllEntries();
    }
}
