package main.java.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import main.java.model.Post;
import main.java.model.PostRepo;

public class PanelHome extends JPanel {
    private static final int NARROW_LAYOUT_WIDTH = 800;
    private static final double POST_CARD_HEIGHT_INCHES = 1;

    private PostRepo repo;
    private Runnable onNewPost;
    private Consumer<Post> onViewPost;
    private JPanel contentPanel;
    private boolean usingNarrowLayout;

    // Effects: creates the home screen panel and rebuilds the layout when the screen
    // becomes wide or narrow enough to need a different arrangement.
    public PanelHome(PostRepo repo, Runnable onNewPost, Consumer<Post> onViewPost) {
        this.repo = repo;
        this.onNewPost = onNewPost;
        this.onViewPost = onViewPost;

        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        add(buildHeader(), BorderLayout.NORTH);

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        add(contentPanel, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                updateResponsiveLayout();
            }
        });

        updateResponsiveLayout();
    }

    // Effects: builds the top section of the home screen with a new-post button
    // on the left and a sort button on the right.
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JButton newPostButton = new JButton("New Post");
        newPostButton.addActionListener(event -> onNewPost.run());

        JButton sortButton = new JButton("Sort");
        sortButton.setEnabled(false);

        header.add(newPostButton, BorderLayout.WEST);
        header.add(sortButton, BorderLayout.EAST);

        return header;
    }

    // Effects: switches between a wide layout and a narrow layout based on the
    // current panel width, then refreshes the visible components if needed.
    private void updateResponsiveLayout() {
        int currentWidth = getWidth();
        boolean shouldUseNarrowLayout = currentWidth > 0 && currentWidth < NARROW_LAYOUT_WIDTH;

        if (contentPanel.getComponentCount() == 0 || shouldUseNarrowLayout != usingNarrowLayout) {
            usingNarrowLayout = shouldUseNarrowLayout;
            rebuildContentLayout();
        }
    }

    // Effects: rebuilds the main content area to keep the post list responsive
    // across wider and narrower screen sizes.
    private void rebuildContentLayout() {
        contentPanel.removeAll();

        JScrollPane postList = buildPostList();

        if (usingNarrowLayout) {
            JPanel narrowContent = new JPanel();
            narrowContent.setOpaque(false);
            narrowContent.setLayout(new BorderLayout());
            narrowContent.add(postList, BorderLayout.CENTER);

            contentPanel.add(narrowContent, BorderLayout.CENTER);
        } else {
            contentPanel.add(postList, BorderLayout.CENTER);
        }

        revalidate();
        repaint();
    }

    // Effects: creates a scrollable list of post previews for the home page.
    private JScrollPane buildPostList() {
        JPanel postList = new JPanel();
        postList.setLayout(new BoxLayout(postList, BoxLayout.Y_AXIS));
        postList.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        for (Post post : repo.getAllPosts()) {
            postList.add(buildPostCard(post));
        }

        JScrollPane scrollPane = new JScrollPane(postList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        return scrollPane;
    }

    // Effects: creates a wide post card that shows only the title and tags, sized
    // to stay close to 1 inche tall on the current screen.
    private JPanel buildPostCard(Post post) {
        int cardHeight = getPostCardHeightPixels();

        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 0, 16, 0),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
            )
        ));
        card.setPreferredSize(new Dimension(0, cardHeight));
        card.setMinimumSize(new Dimension(0, cardHeight));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, cardHeight));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JLabel title = new JLabel(post.getTitle());
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel tags = new JLabel("Tags: " + post.getTags());

        MouseAdapter openPostListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                onViewPost.accept(post);
            }
        };

        card.add(title, BorderLayout.NORTH);
        card.add(tags, BorderLayout.SOUTH);
        card.addMouseListener(openPostListener);
        title.addMouseListener(openPostListener);
        tags.addMouseListener(openPostListener);

        return card;
    }

    // Effects: returns the pixel height that most closely matches a 1 inch card
    // on the current display using the screen DPI reported by the operating system.
    private int getPostCardHeightPixels() {
        int screenDpi = Toolkit.getDefaultToolkit().getScreenResolution();
        return (int) Math.round(screenDpi * POST_CARD_HEIGHT_INCHES);
    }
}
