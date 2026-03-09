package catalog.back_end;

/*
 * Represents one song entry from Songs.csv.
 *
 * CSV format:
 * name,artist,album,year,genre,song_id
 *
 * songId is treated as the unique identifier for each row.
 */
public class Entry {
    private String name;
    private String artist;
    private String album;
    private int year;
    private String genre;
    private int songId;

    /*
     * Empty constructor.
     * Useful when creating an Entry first and setting fields later.
     */
    public Entry() {
    }

    /*
     * Full constructor.
     * Use this when you already have all values for a song entry.
     */
    public Entry(String name, String artist, String album, int year, String genre, int songId) {
        this.name = name;
        this.artist = artist;
        this.album = album;
        this.year = year;
        this.genre = genre;
        this.songId = songId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    @Override
    public String toString() {
        return "Entry{" +
                "name='" + name + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", year=" + year +
                ", genre='" + genre + '\'' +
                ", songId=" + songId +
                '}';
    }
}