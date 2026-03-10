package catalog.controllers;

import catalog.back_end.Entry;
import catalog.back_end.EntryService;
import catalog.back_end.User;
import catalog.front_end.main.MainPage;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class MainScene extends Scene {

    private final MainPage mainPage;
    private final User user;
    private final AdminController adminController;

    public MainScene(User user, EntryService entryService) {
        super(new Pane());

        mainPage = new MainPage(user.isAdmin());
        setRoot(mainPage);
        this.user = user;
        this.adminController = new AdminController(entryService);

        mainPage.tabPane.homeButton.setOnAction(e -> {
            mainPage.setTab(mainPage.homePanel);
        });

        mainPage.tabPane.adminButton.setOnAction(e -> {
            refreshAdminPanel();
            mainPage.setTab(mainPage.adminPanel);
        });

        mainPage.adminPanel.addButton.setOnAction(e -> {
            // add functionality for the add button in the admin screen here
            handleAddSong();
        });

        mainPage.adminPanel.editButton.setOnAction(e -> {
            // add functionality for the edit button in the admin screen here
            handleEditSong();
        });

        mainPage.adminPanel.deleteButton.setOnAction(e -> {
            // add functionality for the delete button in the admin screen here
            handleDeleteSong();
        });
    }
    private void handleAddSong() {
        try {
            String name = mainPage.adminPanel.nameField.getText();
            String artist = mainPage.adminPanel.artistField.getText();
            String album = mainPage.adminPanel.albumField.getText();
            int year = Integer.parseInt(mainPage.adminPanel.yearField.getText());
            String genre = mainPage.adminPanel.genreField.getText();

            boolean success = adminController.addSong(name, artist, album, year, genre);

            if (success) {
                refreshAdminPanel();
                mainPage.adminPanel.clearFields();
            }
        } catch (NumberFormatException e) {
            System.out.println("Year must be a number.");
        }
    }

    private void handleEditSong() {
        try {
            int songId = mainPage.adminPanel.getSelectedSongId();

            String name = mainPage.adminPanel.nameField.getText();
            String artist = mainPage.adminPanel.artistField.getText();
            String album = mainPage.adminPanel.albumField.getText();
            int year = Integer.parseInt(mainPage.adminPanel.yearField.getText());
            String genre = mainPage.adminPanel.genreField.getText();

            boolean success = adminController.editSong(songId, name, artist, album, year, genre);

            if (success) {
                refreshAdminPanel();
                mainPage.adminPanel.clearFields();
            }
        } catch (NumberFormatException e) {
            System.out.println("Year must be a number.");
        }
    }

    private void handleDeleteSong() {
        int songId = mainPage.adminPanel.getSelectedSongId();
        boolean success = adminController.deleteSong(songId);

        if (success) {
            refreshAdminPanel();
            mainPage.adminPanel.clearFields();
        }
    }

    private void refreshAdminPanel() {
        try {
            mainPage.adminPanel.setResults(adminController.getAllSongs());
        } catch (IOException e) {
            System.out.println("Could not load songs.");
        }
    }
}