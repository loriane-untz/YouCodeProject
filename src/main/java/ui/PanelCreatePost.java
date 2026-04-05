package main.java.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import main.java.model.Post;
import main.java.model.TagCatalog;

public class PanelCreatePost extends JPanel {
    private static final double PANEL_MARGIN_INCHES = 1.0;
    private static final int FORM_WIDTH = 560;
    private static final int TAG_PANEL_WIDTH = 190;
    private static final int TAG_PANEL_HEIGHT = 450;
    private static final int TAG_PANEL_TOP_OFFSET = 2;
    private static final Color PAGE_BACKGROUND = Color.decode("#F4EAE0");
    private static final Color TAG_PANEL_BACKGROUND = Color.decode("#B0D4E3");
    private static final Color BACK_BUTTON_COLOR = new Color(211, 102, 97);
    private static final Color DONE_BUTTON_COLOR = Color.decode("#A8C5A0");
    private static final Color PLACEHOLDER_COLOR = new Color(150, 150, 150);
    private static final Color INPUT_COLOR = new Color(150, 150, 150);
    private static final double TITLE_GAP_INCHES = 0.5;
    private static final int PANEL_CORNER_RADIUS = 18;
    private static final int BUTTON_CORNER_RADIUS = 14;

    // Effects: creates the create-post screen with a button that returns the user
    // to the home page when clicked, plus text inputs for the post title and body.
    public PanelCreatePost(Runnable onBack, Consumer<Post> onDone) {
        setLayout(new BorderLayout(0, 24));
        setBackground(PAGE_BACKGROUND);
        setOpaque(true);
        int outerMargin = getOuterMarginPixels();
        setBorder(BorderFactory.createEmptyBorder(outerMargin, outerMargin, outerMargin, outerMargin));

        JButton backButton = buildActionButton("X", BACK_BUTTON_COLOR);
        backButton.addActionListener(event -> onBack.run());

        JButton doneButton = buildActionButton("✓", DONE_BUTTON_COLOR);
        Set<String> selectedTags = new LinkedHashSet<>();

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setAlignmentY(TOP_ALIGNMENT);
        formPanel.setMaximumSize(new Dimension(FORM_WIDTH, Integer.MAX_VALUE));
        formPanel.setPreferredSize(new Dimension(FORM_WIDTH, 0));

        JLabel promptLabel = new JLabel("Whats happening?");
        promptLabel.setFont(new Font("Serif", Font.ITALIC, 42));
        promptLabel.setAlignmentX(LEFT_ALIGNMENT);

        JTextField titleField = new JTextField();
        titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        titleField.setPreferredSize(new Dimension(0, 48));
        titleField.setAlignmentX(LEFT_ALIGNMENT);
        titleField.setFont(new Font("SansSerif", Font.PLAIN, 22));
        titleField.setForeground(PLACEHOLDER_COLOR);
        titleField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(90, 90, 90)),
            BorderFactory.createEmptyBorder(6, 16, 6, 16)
        ));
        installPlaceholder(titleField, "Title");

        JTextArea bodyArea = new JTextArea(12, 40);
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        bodyArea.setFont(new Font("SansSerif", Font.PLAIN, 22));
        bodyArea.setForeground(PLACEHOLDER_COLOR);
        bodyArea.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        installPlaceholder(bodyArea, "Body");

        doneButton.addActionListener(event -> {
            String title = getSubmittedText(titleField, "Title");
            String body = getSubmittedText(bodyArea, "Body");

            if (title.isBlank() || body.isBlank()) {
                return;
            }

            onDone.accept(new Post(title, body, selectedTags));
        });

        JScrollPane bodyScrollPane = new JScrollPane(bodyArea);
        bodyScrollPane.setPreferredSize(new Dimension(0, 260));
        bodyScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        bodyScrollPane.setAlignmentX(LEFT_ALIGNMENT);
        bodyScrollPane.setBorder(BorderFactory.createLineBorder(new Color(90, 90, 90)));

        JPanel buttonRow = new JPanel(new BorderLayout());
        buttonRow.setOpaque(false);
        buttonRow.setAlignmentX(LEFT_ALIGNMENT);
        buttonRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, backButton.getPreferredSize().height));
        buttonRow.add(backButton, BorderLayout.WEST);
        buttonRow.add(doneButton, BorderLayout.EAST);

        formPanel.add(Box.createVerticalStrut(18));
        formPanel.add(promptLabel);
        formPanel.add(Box.createVerticalStrut(getTitleGapPixels()));
        formPanel.add(titleField);
        formPanel.add(Box.createVerticalStrut(34));
        formPanel.add(bodyScrollPane);
        formPanel.add(Box.createVerticalStrut(28));
        formPanel.add(buttonRow);
        
        JPanel tagSelectorPanel = buildTagSelector(selectedTags, promptLabel.getPreferredSize().height);

        JPanel formAndTagsGroup = new JPanel();
        formAndTagsGroup.setOpaque(false);
        formAndTagsGroup.setLayout(new BoxLayout(formAndTagsGroup, BoxLayout.X_AXIS));
        formAndTagsGroup.add(formPanel);
        formAndTagsGroup.add(Box.createHorizontalStrut(32));
        formAndTagsGroup.add(tagSelectorPanel);

        JPanel centeredContent = new JPanel();
        centeredContent.setOpaque(true);
        centeredContent.setBackground(PAGE_BACKGROUND);
        centeredContent.setLayout(new java.awt.GridBagLayout());

        java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = java.awt.GridBagConstraints.CENTER;
        centeredContent.add(formAndTagsGroup, constraints);

        add(centeredContent, BorderLayout.CENTER);
    }

    // Effects: creates the tag-selection column shown to the right of the post form.
    private JPanel buildTagSelector(Set<String> selectedTags, int titleHeight) {
        JPanel tagSelectorColumn = new JPanel();
        tagSelectorColumn.setLayout(new BoxLayout(tagSelectorColumn, BoxLayout.Y_AXIS));
        tagSelectorColumn.setOpaque(false);
        tagSelectorColumn.setAlignmentY(TOP_ALIGNMENT);
        tagSelectorColumn.add(Box.createVerticalStrut(18 + titleHeight + getTitleGapPixels() + TAG_PANEL_TOP_OFFSET));

        JPanel tagPanel = new RoundedPanel(TAG_PANEL_BACKGROUND, PANEL_CORNER_RADIUS);
        tagPanel.setLayout(new BoxLayout(tagPanel, BoxLayout.Y_AXIS));
        tagPanel.setAlignmentX(LEFT_ALIGNMENT);
        tagPanel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        tagPanel.setMaximumSize(new Dimension(TAG_PANEL_WIDTH, Integer.MAX_VALUE));

        for (Map.Entry<String, Set<String>> entry : TagCatalog.getTagsByCategory().entrySet()) {
            tagPanel.add(buildTagCategory(entry.getKey(), entry.getValue(), selectedTags));
        }

        JScrollPane tagScrollPane = new JScrollPane(tagPanel);
        tagScrollPane.setPreferredSize(new Dimension(TAG_PANEL_WIDTH, TAG_PANEL_HEIGHT));
        tagScrollPane.setMaximumSize(new Dimension(TAG_PANEL_WIDTH, TAG_PANEL_HEIGHT));
        tagScrollPane.setMinimumSize(new Dimension(TAG_PANEL_WIDTH, TAG_PANEL_HEIGHT));
        tagScrollPane.setBorder(BorderFactory.createEmptyBorder());
        tagScrollPane.getVerticalScrollBar().setUnitIncrement(12);
        tagScrollPane.getViewport().setOpaque(false);
        tagScrollPane.setOpaque(false);
        tagScrollPane.setBackground(PAGE_BACKGROUND);

        tagSelectorColumn.add(tagScrollPane);
        return tagSelectorColumn;
    }

    // Effects: creates one visible tag category section with checkboxes that update
    // the set of tags selected for the post being created.
    private JPanel buildTagCategory(String categoryName, Set<String> categoryTags, Set<String> selectedTags) {
        JPanel categoryPanel = new JPanel();
        categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));
        categoryPanel.setOpaque(false);
        categoryPanel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel categoryLabel = new JLabel(categoryName);
        categoryLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        categoryLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 10, 0));
        categoryLabel.setAlignmentX(LEFT_ALIGNMENT);
        categoryPanel.add(categoryLabel);

        for (String tag : categoryTags) {
            JCheckBox tagCheckBox = new JCheckBox(tag);
            tagCheckBox.setOpaque(false);
            tagCheckBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
            tagCheckBox.setAlignmentX(LEFT_ALIGNMENT);
            tagCheckBox.addActionListener(event -> {
                if (tagCheckBox.isSelected()) {
                    selectedTags.add(tag);
                } else {
                    selectedTags.remove(tag);
                }
            });

            categoryPanel.add(tagCheckBox);
        }

        categoryPanel.add(Box.createVerticalStrut(10));
        return categoryPanel;
    }

    // Effects: displays light placeholder text in the title field until the user
    // focuses the field and starts entering their own text.
    private void installPlaceholder(JTextField field, String placeholderText) {
        field.setText(placeholderText);
        field.setForeground(PLACEHOLDER_COLOR);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                if (field.getText().equals(placeholderText) && field.getForeground().equals(PLACEHOLDER_COLOR)) {
                    field.setText("");
                    field.setForeground(INPUT_COLOR);
                }
            }

            @Override
            public void focusLost(FocusEvent event) {
                if (field.getText().isBlank()) {
                    field.setText(placeholderText);
                    field.setForeground(PLACEHOLDER_COLOR);
                }
            }
        });
    }

    // Effects: displays light placeholder text in the body field until the user
    // focuses the field and starts entering their own text.
    private void installPlaceholder(JTextArea area, String placeholderText) {
        area.setText(placeholderText);
        area.setForeground(PLACEHOLDER_COLOR);

        area.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                if (area.getText().equals(placeholderText) && area.getForeground().equals(PLACEHOLDER_COLOR)) {
                    area.setText("");
                    area.setForeground(INPUT_COLOR);
                }
            }

            @Override
            public void focusLost(FocusEvent event) {
                if (area.getText().isBlank()) {
                    area.setText(placeholderText);
                    area.setForeground(PLACEHOLDER_COLOR);
                }
            }
        });
    }

    // Effects: returns the user's input text unless the field still only contains
    // its placeholder value, in which case an empty string is returned.
    private String getSubmittedText(JTextField field, String placeholderText) {
        if (field.getForeground().equals(PLACEHOLDER_COLOR) && field.getText().equals(placeholderText)) {
            return "";
        }

        return field.getText().trim();
    }

    // Effects: returns the user's input text unless the field still only contains
    // its placeholder value, in which case an empty string is returned.
    private String getSubmittedText(JTextArea area, String placeholderText) {
        if (area.getForeground().equals(PLACEHOLDER_COLOR) && area.getText().equals(placeholderText)) {
            return "";
        }

        return area.getText().trim();
    }

    // Effects: returns the pixel spacing that most closely matches a 1 inch gap
    // below the create-post title on the current display.
    private int getTitleGapPixels() {
        int screenDpi = Toolkit.getDefaultToolkit().getScreenResolution();
        return (int) Math.round(screenDpi * TITLE_GAP_INCHES);
    }

    // Effects: returns the pixel margin that most closely matches a 1 inch outer
    // border on the current display.
    private int getOuterMarginPixels() {
        int screenDpi = Toolkit.getDefaultToolkit().getScreenResolution();
        return (int) Math.round(screenDpi * PANEL_MARGIN_INCHES);
    }

    // Effects: creates one rounded colored action button used below the form.
    private JButton buildActionButton(String text, Color backgroundColor) {
        JButton button = new RoundedButton(text, backgroundColor, BUTTON_CORNER_RADIUS);
        button.setFont(new Font("SansSerif", Font.BOLD, 22));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setForeground(Color.BLACK);
        button.setMargin(new Insets(8, 18, 8, 18));
        return button;
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
