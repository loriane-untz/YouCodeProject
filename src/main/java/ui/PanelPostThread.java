package main.java.ui;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import main.java.model.Post;

public class PanelPostThread extends JPanel {
    // Effects: creates a view-post screen showing the selected post and a button
    // that returns the user to the home page.
    public PanelPostThread(Post post, Runnable onBack) {
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(36, 36, 36, 36));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel(post.getTitle());
        title.setFont(new Font("SansSerif", Font.BOLD, 30));

        JButton backButton = new JButton("←");
        backButton.addActionListener(event -> onBack.run());
        header.add(title, BorderLayout.WEST);
        header.add(backButton, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel postContent = new JPanel();
        postContent.setLayout(new BoxLayout(postContent, BoxLayout.Y_AXIS));
        postContent.setOpaque(false);

        JLabel tags = new JLabel("Tags: " + post.getTags());
        JLabel body = new JLabel("<html>" + post.getBody() + "</html>");

        postContent.add(javax.swing.Box.createVerticalStrut(12));
        postContent.add(tags);
        postContent.add(javax.swing.Box.createVerticalStrut(24));
        postContent.add(body);

        add(postContent, BorderLayout.CENTER);
    }
}
