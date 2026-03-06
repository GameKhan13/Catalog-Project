package catalog;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String password;
    private boolean isAdmin;
    private List<Integer> playlistIds;

    public User() {
        this.playlistIds = new ArrayList<>();
    }

    public User(String username, String password, boolean isAdmin, List<Integer> playlistIds) {
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
        this.playlistIds = playlistIds;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public List<Integer> getPlaylistIds() {
        return playlistIds;
    }

    public void setPlaylistIds(List<Integer> playlistIds) {
        this.playlistIds = playlistIds;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", isAdmin=" + isAdmin +
                ", playlistIds=" + playlistIds +
                '}';
    }
}
