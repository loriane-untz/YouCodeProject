package main.java.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import main.java.model.Post;
import main.java.model.PostRepo;
import main.java.model.TagCatalog;

public class PanelHome extends JPanel {
    private static final int NARROW_LAYOUT_WIDTH = 800;
    private static final double PANEL_MARGIN_INCHES = 1.0;
    private static final double POST_CARD_HEIGHT_INCHES = 1.0;
    private static final int FILTER_PANEL_GAP = 12;
    private static final int POST_CARD_GAP = 28;
    private static final int HEADER_TO_CONTENT_GAP = 34;
    private static final Color PAGE_BACKGROUND = Color.decode("#F4EAE0");
    private static final Color POST_BACKGROUND = Color.decode("#B0D4E3");
    private static final Color NEW_BUTTON_BACKGROUND = Color.decode("#7DB3C8");
    private static final Color FILTER_BUTTON_BACKGROUND = Color.decode("#A8C5A0");
    private static final Color FILTER_PANEL_BACKGROUND = Color.WHITE;
    private static final int CARD_CORNER_RADIUS = 18;
    private static final int BUTTON_CORNER_RADIUS = 18;
    private static final int LOGO_HEIGHT = 82;

    private PostRepo repo;
    private Runnable onNewPost;
    private Consumer<Post> onViewPost;
    private JPanel contentPanel;
    private JButton filterButton;
    private boolean usingNarrowLayout;
    private boolean filterPanelOpen;
    private Set<String> selectedTags;

    // Effects: creates the home screen panel and rebuilds the layout when the screen
    // becomes wide or narrow enough to need a different arrangement.
    public PanelHome(PostRepo repo, Runnable onNewPost, Consumer<Post> onViewPost) {
        this.repo = repo;
        this.onNewPost = onNewPost;
        this.onViewPost = onViewPost;
        this.selectedTags = new LinkedHashSet<>();

        setLayout(new BorderLayout(0, HEADER_TO_CONTENT_GAP));
        setBackground(PAGE_BACKGROUND);
        setOpaque(true);
        int outerMargin = getOuterMarginPixels();
        setBorder(BorderFactory.createEmptyBorder(outerMargin, outerMargin, outerMargin, outerMargin));

        add(buildHeader(), BorderLayout.NORTH);

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(true);
        contentPanel.setBackground(PAGE_BACKGROUND);
        add(contentPanel, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                updateResponsiveLayout();
            }
        });

        updateResponsiveLayout();
    }

    // Effects: builds the top section of the home screen with the logo on the
    // left and the new-post / filter buttons grouped on the right.
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(true);
        header.setBackground(PAGE_BACKGROUND);

        JLabel logoLabel = buildLogoLabel();

        JButton newPostButton = buildActionButton("New +", NEW_BUTTON_BACKGROUND);
        newPostButton.addActionListener(event -> onNewPost.run());

        filterButton = buildActionButton("Filter", FILTER_BUTTON_BACKGROUND);
        filterButton.addActionListener(event -> toggleFilterPanel());

        JPanel actionButtons = new JPanel();
        actionButtons.setOpaque(false);
        actionButtons.setLayout(new BoxLayout(actionButtons, BoxLayout.X_AXIS));
        actionButtons.add(newPostButton);
        actionButtons.add(Box.createHorizontalStrut(14));
        actionButtons.add(filterButton);

        header.add(logoLabel, BorderLayout.WEST);
        header.add(actionButtons, BorderLayout.EAST);

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
            narrowContent.setOpaque(true);
            narrowContent.setBackground(PAGE_BACKGROUND);
            narrowContent.setLayout(new BorderLayout());
            narrowContent.add(postList, BorderLayout.CENTER);
            if (filterPanelOpen) {
                narrowContent.add(buildFilterPanel(), BorderLayout.EAST);
            }

            contentPanel.add(narrowContent, BorderLayout.CENTER);
        } else {
            contentPanel.add(postList, BorderLayout.CENTER);
            if (filterPanelOpen) {
                contentPanel.add(buildFilterPanel(), BorderLayout.EAST);
            }
        }

        revalidate();
        repaint();
    }

    // Effects: creates a scrollable list of post previews for the home page.
    private JScrollPane buildPostList() {
        JPanel postList = new JPanel();
        postList.setLayout(new BoxLayout(postList, BoxLayout.Y_AXIS));
        postList.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        postList.setOpaque(true);
        postList.setBackground(PAGE_BACKGROUND);

        for (Post post : repo.filterByTags(selectedTags)) {
            postList.add(buildPostCard(post));
        }

        postList.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(postList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(true);
        scrollPane.setBackground(PAGE_BACKGROUND);
        scrollPane.getViewport().setOpaque(true);
        scrollPane.getViewport().setBackground(PAGE_BACKGROUND);
        return scrollPane;
    }

    // Effects: creates a wide post card that shows only the title and tags, sized
    // to stay close to 1 inche tall on the current screen.
    private JPanel buildPostCard(Post post) {
        int cardHeight = getPostCardHeightPixels();

        JPanel card = new RoundedPanel(POST_BACKGROUND, CARD_CORNER_RADIUS);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel title = new JLabel(post.getTitle());
        title.setFont(new Font("SansSerif", Font.BOLD, 24));

        JLabel tags = new JLabel(formatTags(post.getTags()));
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);

        MouseAdapter openPostListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                onViewPost.accept(post);
            }
        };

        card.add(title, BorderLayout.NORTH);
        card.add(spacer, BorderLayout.CENTER);
        card.add(tags, BorderLayout.SOUTH);
        card.addMouseListener(openPostListener);
        title.addMouseListener(openPostListener);
        tags.addMouseListener(openPostListener);

        JPanel cardRow = new JPanel(new BorderLayout());
        cardRow.setOpaque(false);
        cardRow.setBorder(BorderFactory.createEmptyBorder(0, 0, POST_CARD_GAP, 0));
        cardRow.setPreferredSize(new Dimension(0, cardHeight));
        cardRow.setMinimumSize(new Dimension(0, cardHeight));
        cardRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, cardHeight));
        cardRow.setAlignmentX(LEFT_ALIGNMENT);
        cardRow.add(card, BorderLayout.CENTER);

        return cardRow;
    }

    // Effects: returns the pixel height that most closely matches a 1 inch card
    // on the current display using the screen DPI reported by the operating system.
    private int getPostCardHeightPixels() {
        int screenDpi = Toolkit.getDefaultToolkit().getScreenResolution();
        return (int) Math.round(screenDpi * POST_CARD_HEIGHT_INCHES);
    }

    // Effects: returns the pixel margin that most closely matches a 1 inch outer
    // border on the current display.
    private int getOuterMarginPixels() {
        int screenDpi = Toolkit.getDefaultToolkit().getScreenResolution();
        return (int) Math.round(screenDpi * PANEL_MARGIN_INCHES);
    }

    // Effects: shows or hides the right-side filter panel.
    private void toggleFilterPanel() {
        filterPanelOpen = !filterPanelOpen;
        rebuildContentLayout();
    }

    // Effects: creates a right-side filter panel containing visible tag categories
    // and multi-select checkboxes for filtering the home-page post list.
    private JPanel buildFilterPanel() {
        JPanel filterPanelWrapper = new JPanel(new BorderLayout());
        filterPanelWrapper.setOpaque(false);
        filterPanelWrapper.setBorder(BorderFactory.createEmptyBorder(0, FILTER_PANEL_GAP, 0, 0));

        JPanel filterPanel = new RoundedPanel(FILTER_PANEL_BACKGROUND, CARD_CORNER_RADIUS);
        filterPanel.setLayout(new BorderLayout(0, 12));
        filterPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        filterPanel.setPreferredSize(new Dimension(getFilterPanelWidth(), 0));

        JPanel filterContent = new JPanel();
        filterContent.setLayout(new BoxLayout(filterContent, BoxLayout.Y_AXIS));
        filterContent.setBackground(FILTER_PANEL_BACKGROUND);
        filterContent.setOpaque(true);
        filterContent.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        for (Map.Entry<String, Set<String>> entry : TagCatalog.getTagsByCategory().entrySet()) {
            filterContent.add(buildCategorySection(entry.getKey(), entry.getValue()));
        }

        JScrollPane scrollPane = new JScrollPane(filterContent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(FILTER_PANEL_BACKGROUND);
        scrollPane.setOpaque(true);
        scrollPane.getViewport().setBackground(FILTER_PANEL_BACKGROUND);
        scrollPane.getViewport().setOpaque(true);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);

        filterPanel.add(scrollPane, BorderLayout.CENTER);
        filterPanelWrapper.add(filterPanel, BorderLayout.CENTER);

        return filterPanelWrapper;
    }

    // Effects: creates one category section with a visible header and the full
    // list of checkboxes for the tags in that category.
    private JPanel buildCategorySection(String categoryName, Set<String> categoryTags) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(LEFT_ALIGNMENT);

        JLabel categoryHeader = new JLabel(categoryName);
        categoryHeader.setFont(new Font("SansSerif", Font.BOLD, 15));
        categoryHeader.setBorder(BorderFactory.createEmptyBorder(6, 4, 4, 4));
        categoryHeader.setAlignmentX(LEFT_ALIGNMENT);

        JPanel tagPanel = new JPanel();
        tagPanel.setLayout(new BoxLayout(tagPanel, BoxLayout.Y_AXIS));
        tagPanel.setOpaque(false);
        tagPanel.setAlignmentX(LEFT_ALIGNMENT);
        tagPanel.setBorder(BorderFactory.createEmptyBorder(0, 18, 6, 0));

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

        section.add(categoryHeader);
        section.add(tagPanel);

        return section;
    }

    // Effects: returns the preferred width for the side filter panel, targeting
    // about one quarter of the current home screen width.
    private int getFilterPanelWidth() {
        int currentWidth = Math.max(getWidth(), 900);
        return Math.max(220, currentWidth / 5);
    }

    // Effects: creates the rounded action buttons used in the home-page header.
    private JButton buildActionButton(String text, Color backgroundColor) {
        JButton button = new RoundedButton(text, backgroundColor, BUTTON_CORNER_RADIUS);
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setMargin(new Insets(10, 18, 10, 18));
        return button;
    }

    // Effects: loads and scales the home-page logo shown at the left side of the
    // header, falling back to text if the image cannot be loaded.
    private JLabel buildLogoLabel() {
        ImageIcon icon = new ImageIcon("src/main/resources/logo.png");

        if (icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) {
            return new JLabel("YouCode");
        }

        int scaledWidth = (int) Math.round((double) icon.getIconWidth() * LOGO_HEIGHT / icon.getIconHeight());
        Image scaledImage = getHighQualityScaledImage(icon.getImage(), scaledWidth, LOGO_HEIGHT);
        return new JLabel(new ImageIcon(scaledImage));
    }

    // Effects: scales the logo image with higher-quality interpolation so it stays
    // as crisp as possible in the header.
    private Image getHighQualityScaledImage(Image sourceImage, int targetWidth, int targetHeight) {
        BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaledImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(sourceImage, 0, 0, targetWidth, targetHeight, null);
        g2.dispose();
        return scaledImage;
    }

    // Effects: returns the post's tags as bracketed labels such as
    // [English] [Mental Health].
    private String formatTags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }

        java.util.List<String> orderedTags = new ArrayList<>(tags);
        Collections.sort(orderedTags);

        StringBuilder builder = new StringBuilder();

        for (String tag : orderedTags) {
            if (builder.length() > 0) {
                builder.append(' ');
            }

            builder.append('[').append(tag).append(']');
        }

        return builder.toString();
    }

    private static class RoundedPanel extends JPanel {
        private final Color fillColor;
        private final int cornerRadius;

        private RoundedPanel(Color fillColor, int cornerRadius) {
            this.fillColor = fillColor;
            this.cornerRadius = cornerRadius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fillColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class RoundedButton extends JButton {
        private final Color fillColor;
        private final int cornerRadius;

        private RoundedButton(String text, Color fillColor, int cornerRadius) {
            super(text);
            this.fillColor = fillColor;
            this.cornerRadius = cornerRadius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fillColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

}
