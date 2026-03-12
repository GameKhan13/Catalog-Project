package catalog.controllers;

import java.io.IOException;

import catalog.back_end.EntryService;
import catalog.back_end.UserService;
import catalog.front_end.main.MainPage;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MainScene extends Scene {

    private final MainPage mainPage;
    private final AdminController adminController;

    private final EntryService entryService;
    private final UserService userService;

    public MainScene(UserService userService, EntryService entryService) {
        super(new Pane());

        mainPage = new MainPage(userService.getCurrentUser().isAdmin());
        setRoot(mainPage);

        this.entryService = entryService;
        this.userService = userService;

        refreshHomePanel();

        this.adminController = new AdminController(entryService);

        mainPage.topBar.logoutButton.setOnAction(e -> {
            userService.logout();
            ((Stage) getWindow()).setScene(new LoginScene(userService, entryService));
        });

        mainPage.tabPane.homeButton.setOnAction(e -> {
            refreshHomePanel();
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
            mainPage.adminPanel.results.setResults(adminController.getAllSongs());
        } catch (IOException e) {
            System.out.println("Could not load songs.");
        }
    }

    private void refreshHomePanel() {
        try {
            mainPage.homePanel.results.setResults(entryService.getAllEntries());
        } catch (IOException e) {
            System.out.println("Could not load songs.");
        }
    }
}