package musiccatalog.model;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private String id;
    private String name;
    private String ownerUserId;
    private boolean global;
    private List<String> songIds = new ArrayList<>();

    public Playlist() {}

    public Playlist(String id, String name, String ownerUserId, boolean global, List<String> songIds) {
        this.id = id;
        this.name = name;
        this.ownerUserId = ownerUserId;
        this.global = global;
        this.songIds = songIds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public List<String> getSongIds() {
        return songIds;
    }

    public void setSongIds(List<String> songIds) {
        this.songIds = songIds;
    }
}
