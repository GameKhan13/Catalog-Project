package com.musiccatalog.model;

/**
 * Model class for a song in the catalog.
 *
 * Each Song object mirrors one row in songs.csv.
 * The service layer reads and writes these objects when the catalog changes.
 */
public class Song {
    /**
     * Unique identifier for the song.
     */
    private String id;

    /**
     * Display title of the song.
     */
    private String title;

    /**
     * Name of the artist who performs the song.
     */
    private String artist;

    /**
     * Album name associated with the song.
     */
    private String album;

    /**
     * Release year.
     */
    private int year;

    /**
     * Genre label used for filtering and display.
     */
    private String genre;

    /**
     * Song lyrics text stored in the catalog.
     */
    private String lyrics;

    /**
     * File name of the cover image or artwork.
     */
    private String imageFile;

    /**
     * No-argument constructor for frameworks and serializers.
     */
    public Song() {}

    /**
     * Full constructor for creating a complete song object.
     */
    public Song(String id, String title, String artist, String album, int year, String genre, String lyrics, String imageFile) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.year = year;
        this.genre = genre;
        this.lyrics = lyrics;
        this.imageFile = imageFile;
    }

    /**
     * Returns the song id.
     */
    public String getId() {
        return id;
    }

    /**
     * Updates the song id.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the title shown to the user.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Updates the song title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the artist name.
     */
    public String getArtist() {
        return artist;
    }

    /**
     * Updates the artist name.
     */
    public void setArtist(String artist) {
        this.artist = artist;
    }

    /**
     * Returns the album name.
     */
    public String getAlbum() {
        return album;
    }

    /**
     * Updates the album field.
     */
    public void setAlbum(String album) {
        this.album = album;
    }

    /**
     * Returns the release year.
     */
    public int getYear() {
        return year;
    }

    /**
     * Updates the release year.
     */
    public void setYear(int year) {
        this.year = year;
    }

    /**
     * Returns the genre label.
     */
    public String getGenre() {
        return genre;
    }

    /**
     * Updates the genre label.
     */
    public void setGenre(String genre) {
        this.genre = genre;
    }

    /**
     * Returns the stored lyrics text.
     */
    public String getLyrics() {
        return lyrics;
    }

    /**
     * Updates the lyrics text.
     */
    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    /**
     * Returns the cover image file name.
     */
    public String getImageFile() {
        return imageFile;
    }

    /**
     * Updates the cover image file name.
     */
    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
    }
}
