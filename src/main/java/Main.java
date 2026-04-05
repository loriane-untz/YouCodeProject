package main.java;

import java.util.Set;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import main.java.model.Post;
import main.java.model.PostRepo;
import main.java.model.TagCatalog;
import main.java.ui.PanelCreatePost;
import main.java.ui.PanelHome;
import main.java.ui.PanelPostThread;

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
        showHomePanel(frame, repo);
        return frame;
    }

    // Effects: replaces the current screen with the home panel.
    private static void showHomePanel(JFrame frame, PostRepo repo) {
        PanelHome homePanel = new PanelHome(
            repo,
            () -> showCreatePostPanel(frame, repo),
            post -> showViewPostPanel(frame, repo, post)
        );
        frame.setContentPane(homePanel);
        frame.revalidate();
        frame.repaint();
    }

    // Effects: replaces the current screen with the create-post panel.
    private static void showCreatePostPanel(JFrame frame, PostRepo repo) {
        PanelCreatePost createPostPanel = new PanelCreatePost(
            () -> showHomePanel(frame, repo),
            post -> {
                repo.addPost(post);
                showHomePanel(frame, repo);
            }
        );
        frame.setContentPane(createPostPanel);
        frame.revalidate();
        frame.repaint();
    }

    // Effects: replaces the current screen with the selected post's thread view.
    private static void showViewPostPanel(JFrame frame, PostRepo repo, Post post) {
        PanelPostThread postThreadPanel = new PanelPostThread(post, () -> showHomePanel(frame, repo));
        frame.setContentPane(postThreadPanel);
        frame.revalidate();
        frame.repaint();
    }

    // Effects: creates a repository with the predefined tags and a few sample posts
    // so the home screen has data to display when the app opens.
    private static PostRepo buildStarterRepo() {
        PostRepo repo = new PostRepo(TagCatalog.getAllTags());

        repo.addPost(new Post(
            "Looking for food support options",
            "I am trying to find food banks or meal programs nearby that I can access this week.",
            Set.of("English", "Finances")
        ));

        return repo;
    }
}
