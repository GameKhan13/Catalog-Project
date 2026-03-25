package com.musiccatalog.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class for a playlist.
 *
 * A playlist can either be:
 * - global, meaning visible to everyone
 * - user-owned, meaning tied to a specific user
 *
 * The songIds list stores only song ids, not full Song objects.
 */
public class Playlist {
    /**
     * Unique playlist identifier.
     */
    private String id;

    /**
     * Display name of the playlist.
     */
    private String name;

    /**
     * Id of the user who owns this playlist.
     *
     * For global playlists this may still be present, but visibility is mainly
     * controlled by the global flag.
     */
    private String ownerUserId;

    /**
     * True when the playlist should be visible to all users.
     */
    private boolean global;

    /**
     * List of song ids included in the playlist.
     */
    private List<String> songIds = new ArrayList<>();

    /**
     * No-argument constructor for serializers and tools.
     */
    public Playlist() {}

    /**
     * Full constructor.
     */
    public Playlist(String id, String name, String ownerUserId, boolean global, List<String> songIds) {
        this.id = id;
        this.name = name;
        this.ownerUserId = ownerUserId;
        this.global = global;
        this.songIds = songIds;
    }

    /**
     * Returns the playlist id.
     */
    public String getId() {
        return id;
    }

    /**
     * Updates the playlist id.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the playlist name.
     */
    public String getName() {
        return name;
    }

    /**
     * Updates the playlist name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the owner user's id.
     */
    public String getOwnerUserId() {
        return ownerUserId;
    }

    /**
     * Updates the owner user's id.
     */
    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    /**
     * Returns whether the playlist is global.
     */
    public boolean isGlobal() {
        return global;
    }

    /**
     * Updates the global flag.
     */
    public void setGlobal(boolean global) {
        this.global = global;
    }

    /**
     * Returns the list of song ids currently in the playlist.
     */
    public List<String> getSongIds() {
        return songIds;
    }

    /**
     * Replaces the list of song ids stored in the playlist.
     */
    public void setSongIds(List<String> songIds) {
        this.songIds = songIds;
    }
}
