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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import main.java.model.Post;
import main.java.model.PostRepo;
import main.java.model.TagCatalog;

public class PanelHome extends JPanel {
    private static final int NARROW_LAYOUT_WIDTH = 800;
    private static final double POST_CARD_HEIGHT_INCHES = 1;

    private PostRepo repo;
    private Runnable onNewPost;
    private Consumer<Post> onViewPost;
    private JPanel contentPanel;
    private JButton filterButton;
    private JPopupMenu filterMenu;
    private boolean usingNarrowLayout;
    private Set<String> selectedTags;
    private Set<String> expandedCategories;

    // Effects: creates the home screen panel and rebuilds the layout when the screen
    // becomes wide or narrow enough to need a different arrangement.
    public PanelHome(PostRepo repo, Runnable onNewPost, Consumer<Post> onViewPost) {
        this.repo = repo;
        this.onNewPost = onNewPost;
        this.onViewPost = onViewPost;
        this.selectedTags = new LinkedHashSet<>();
        this.expandedCategories = new LinkedHashSet<>();

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

        filterButton = new JButton("Filter");
        filterButton.addActionListener(event -> toggleFilterMenu());

        header.add(newPostButton, BorderLayout.WEST);
        header.add(filterButton, BorderLayout.EAST);

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

        for (Post post : repo.filterByTags(selectedTags)) {
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

    // Effects: shows or hides the filter dropdown menu below the filter button.
    private void toggleFilterMenu() {
        if (filterMenu != null && filterMenu.isVisible()) {
            filterMenu.setVisible(false);
            return;
        }

        filterMenu = buildFilterMenu();
        filterMenu.show(filterButton, 0, filterButton.getHeight());
    }

    // Effects: creates a scrollable popup menu containing expandable tag categories
    // and multi-select checkboxes for filtering the home-page post list.
    private JPopupMenu buildFilterMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        JPanel filterContent = new JPanel();
        filterContent.setLayout(new BoxLayout(filterContent, BoxLayout.Y_AXIS));
        filterContent.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        for (Map.Entry<String, Set<String>> entry : TagCatalog.getTagsByCategory().entrySet()) {
            filterContent.add(buildCategorySection(entry.getKey(), entry.getValue()));
        }

        JScrollPane scrollPane = new JScrollPane(filterContent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setPreferredSize(new Dimension(280, 260));
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);

        popupMenu.add(scrollPane);
        return popupMenu;
    }

    // Effects: creates one category section with a toggle button and an expandable
    // list of checkboxes for the tags in that category.
    private JPanel buildCategorySection(String categoryName, Set<String> categoryTags) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(LEFT_ALIGNMENT);

        JButton categoryButton = new JButton(getCategoryButtonLabel(categoryName));
        categoryButton.setHorizontalAlignment(JButton.LEFT);
        categoryButton.setFocusPainted(false);
        categoryButton.setContentAreaFilled(false);
        categoryButton.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        categoryButton.setAlignmentX(LEFT_ALIGNMENT);

        JPanel tagPanel = new JPanel();
        tagPanel.setLayout(new BoxLayout(tagPanel, BoxLayout.Y_AXIS));
        tagPanel.setAlignmentX(LEFT_ALIGNMENT);
        tagPanel.setBorder(BorderFactory.createEmptyBorder(0, 18, 6, 0));
        tagPanel.setVisible(expandedCategories.contains(categoryName));

        for (String tag : categoryTags) {
            JCheckBox tagCheckBox = new JCheckBox(tag);
            tagCheckBox.setOpaque(false);
            tagCheckBox.setSelected(selectedTags.contains(tag));
            tagCheckBox.setAlignmentX(LEFT_ALIGNMENT);
            tagCheckBox.addActionListener(event -> {
                if (tagCheckBox.isSelected()) {
                    selectedTags.add(tag);
                } else {
                    selectedTags.remove(tag);
                }

                rebuildContentLayout();
            });

            tagPanel.add(tagCheckBox);
        }

        categoryButton.addActionListener(event -> {
            if (expandedCategories.contains(categoryName)) {
                expandedCategories.remove(categoryName);
            } else {
                expandedCategories.add(categoryName);
            }

            filterMenu.setVisible(false);
            toggleFilterMenu();
        });

        section.add(categoryButton);
        section.add(tagPanel);

        return section;
    }

    // Effects: returns the label for a category button using a dropdown arrow that
    // reflects whether the category is currently expanded.
    private String getCategoryButtonLabel(String categoryName) {
        if (expandedCategories.contains(categoryName)) {
            return "▼ " + categoryName;
        }

        return "▶ " + categoryName;
    }
}
