package main.java;

import java.util.Set;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import main.java.model.Post;
import main.java.model.PostRepo;
import main.java.ui.PanelHome;

public class Main {
    // Effects: starts the Swing application and opens the main window.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = buildWindow();
            frame.setVisible(true);
        });
    }

    // Effects: creates the main application window, prepares starter data,
    // and sets the home panel as the visible screen.
    private static JFrame buildWindow() {
        JFrame frame = new JFrame("SideBySide");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);

        PostRepo repo = buildStarterRepo();
        PanelHome homePanel = new PanelHome(repo);

        frame.setContentPane(homePanel);
        return frame;
    }

    // Effects: creates a repository with the predefined tags and a few sample posts
    // so the home screen has data to display when the app opens.
    private static PostRepo buildStarterRepo() {
        Set<String> allowedTags = Set.of(
            "English",
            "Childcare",
            "Finances",
            "Pregnancy",
            "Mental Health",
            "Physical health",
            "Relationships",
            "Housing",
            "Safety"
        );

        PostRepo repo = new PostRepo(allowedTags);

        repo.addPost(new Post(
            "Looking for food support options",
            "I am trying to find food banks or meal programs nearby that I can access this week.",
            Set.of("English", "Finances")
        ));

        return repo;
    }
}
