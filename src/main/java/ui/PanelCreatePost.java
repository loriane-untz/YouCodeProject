package main.java.ui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

public class PanelCreatePost extends JPanel {
    // Effects: creates the create-post screen with a button that returns the user
    // to the home page when clicked.
    public PanelCreatePost(Runnable onBack) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton backButton = new JButton("Back to Home");
        backButton.addActionListener(event -> onBack.run());

        add(backButton, BorderLayout.NORTH);
    }
}
