package main.java.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import main.java.model.Post;

public class PanelPostThread extends JPanel {
    private static final double PANEL_MARGIN_INCHES = 1.0;
    private static final double RESPONSE_BOX_HEIGHT_INCHES = 0.5;
    private static final double DIVIDER_OFFSET_INCHES = 1.0;
    private static final double ACTIVE_COMPOSER_THREAD_GAP_INCHES = 0.5;

    // Effects: creates a view-post screen showing the selected post and a button
    // that returns the user to the home page.
    public PanelPostThread(Post post, Runnable onBack) {
        setLayout(new BorderLayout(0, 16));
        int outerMargin = getOuterMarginPixels();
        setBorder(BorderFactory.createEmptyBorder(outerMargin, outerMargin, outerMargin, outerMargin));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel(post.getTitle());
        title.setFont(new Font("SansSerif", Font.BOLD, 30));

        JButton backButton = new JButton("X");
        backButton.addActionListener(event -> onBack.run());
        header.add(title, BorderLayout.WEST);
        header.add(backButton, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel postContent = new JPanel();
        postContent.setLayout(new BoxLayout(postContent, BoxLayout.Y_AXIS));
        postContent.setOpaque(false);
        postContent.setAlignmentX(LEFT_ALIGNMENT);

        JLabel tags = new JLabel(formatTags(post.getTags()));
        JLabel body = new JLabel("<html>" + post.getBody() + "</html>");
        tags.setAlignmentX(LEFT_ALIGNMENT);
        body.setAlignmentX(LEFT_ALIGNMENT);

        postContent.add(javax.swing.Box.createVerticalStrut(12));
        postContent.add(tags);
        postContent.add(javax.swing.Box.createVerticalStrut(24));
        postContent.add(body);
        postContent.add(Box.createVerticalStrut(getDividerOffsetPixels()));

        JPanel divider = new JPanel();
        divider.setBackground(Color.LIGHT_GRAY);
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        divider.setPreferredSize(new Dimension(0, 2));
        divider.setMinimumSize(new Dimension(0, 2));
        divider.setAlignmentX(LEFT_ALIGNMENT);
        postContent.add(divider);

        JPanel bottomSection = new JPanel();
        bottomSection.setLayout(new BorderLayout());
        bottomSection.setOpaque(false);

        JPanel responseComposer = new JPanel();
        responseComposer.setLayout(new BoxLayout(responseComposer, BoxLayout.Y_AXIS));
        responseComposer.setOpaque(false);

        JButton respondButton = new JButton("Respond?");
        respondButton.setAlignmentX(LEFT_ALIGNMENT);

        JPanel responseList = new JPanel();
        responseList.setLayout(new BoxLayout(responseList, BoxLayout.Y_AXIS));
        responseList.setOpaque(false);
        responseList.setAlignmentX(LEFT_ALIGNMENT);

        JScrollPane responseListScrollPane = new JScrollPane(responseList);
        responseListScrollPane.setAlignmentX(LEFT_ALIGNMENT);
        responseListScrollPane.setBorder(BorderFactory.createEmptyBorder());
        responseListScrollPane.setOpaque(false);
        responseListScrollPane.getViewport().setOpaque(false);
        responseListScrollPane.setPreferredSize(new Dimension(0, 180));
        responseListScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        responseListScrollPane.getVerticalScrollBar().setUnitIncrement(12);
        responseListScrollPane.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                refreshResponseList(post, responseList, responseListScrollPane.getViewport().getWidth());
            }
        });

        JTextArea responseArea = new JTextArea(7, 40);
        responseArea.setLineWrap(true);
        responseArea.setWrapStyleWord(true);

        int responseBoxHeight = getResponseBoxHeightPixels();
        JScrollPane responseScrollPane = new JScrollPane(responseArea);
        responseScrollPane.setAlignmentX(LEFT_ALIGNMENT);
        responseScrollPane.setPreferredSize(new Dimension(0, responseBoxHeight));
        responseScrollPane.setMinimumSize(new Dimension(0, responseBoxHeight));
        responseScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, responseBoxHeight));
        responseScrollPane.setVisible(false);

        JButton closeResponseButton = new JButton("X");
        JButton postResponseButton = new JButton("✓");

        JPanel responseButtonRow = new JPanel(new BorderLayout());
        responseButtonRow.setOpaque(false);
        responseButtonRow.setAlignmentX(LEFT_ALIGNMENT);
        responseButtonRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, postResponseButton.getPreferredSize().height));
        responseButtonRow.add(closeResponseButton, BorderLayout.WEST);
        responseButtonRow.add(postResponseButton, BorderLayout.EAST);
        responseButtonRow.setVisible(false);

        JPanel threadGapSpacer = new JPanel();
        threadGapSpacer.setOpaque(false);
        threadGapSpacer.setPreferredSize(new Dimension(0, getActiveComposerThreadGapPixels()));
        threadGapSpacer.setMinimumSize(new Dimension(0, getActiveComposerThreadGapPixels()));
        threadGapSpacer.setMaximumSize(new Dimension(Integer.MAX_VALUE, getActiveComposerThreadGapPixels()));
        threadGapSpacer.setVisible(false);

        respondButton.addActionListener(event -> {
            responseScrollPane.setVisible(true);
            responseButtonRow.setVisible(true);
            threadGapSpacer.setVisible(!post.getResponses().isEmpty());
            bottomSection.revalidate();
            bottomSection.repaint();
        });

        closeResponseButton.addActionListener(event -> {
            responseArea.setText("");
            responseScrollPane.setVisible(false);
            responseButtonRow.setVisible(false);
            threadGapSpacer.setVisible(false);
            bottomSection.revalidate();
            bottomSection.repaint();
        });

        postResponseButton.addActionListener(event -> {
            String responseText = responseArea.getText().trim();

            if (responseText.isBlank()) {
                return;
            }

            post.addResponse(responseText);
            responseArea.setText("");
            responseScrollPane.setVisible(false);
            responseButtonRow.setVisible(false);
            threadGapSpacer.setVisible(false);
            refreshResponseList(post, responseList, responseListScrollPane.getViewport().getWidth());
            bottomSection.revalidate();
            bottomSection.repaint();
        });

        responseComposer.add(Box.createVerticalStrut(24));
        responseComposer.add(respondButton);
        responseComposer.add(Box.createVerticalStrut(16));
        responseComposer.add(responseScrollPane);
        responseComposer.add(Box.createVerticalStrut(12));
        responseComposer.add(responseButtonRow);
        responseComposer.add(Box.createVerticalStrut(2));

        JPanel responseThreadArea = new JPanel();
        responseThreadArea.setLayout(new BoxLayout(responseThreadArea, BoxLayout.Y_AXIS));
        responseThreadArea.setOpaque(false);
        responseThreadArea.add(threadGapSpacer);
        responseThreadArea.add(responseListScrollPane);

        bottomSection.add(responseComposer, BorderLayout.NORTH);
        bottomSection.add(responseThreadArea, BorderLayout.CENTER);

        JPanel contentArea = new JPanel(new BorderLayout());
        contentArea.setOpaque(false);
        contentArea.add(postContent, BorderLayout.NORTH);
        contentArea.add(bottomSection, BorderLayout.CENTER);

        add(contentArea, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() ->
            refreshResponseList(post, responseList, responseListScrollPane.getViewport().getWidth())
        );
    }

    // Effects: rebuilds the visible list of saved responses for the current post.
    private void refreshResponseList(Post post, JPanel responseList, int availableWidth) {
        responseList.removeAll();

        int textWidth = Math.max(180, availableWidth - 40);
        java.util.List<String> responses = post.getResponses();

        for (int i = responses.size() - 1; i >= 0; i--) {
            responseList.add(buildResponseRow(responses.get(i), textWidth));
        }

        responseList.revalidate();
        responseList.repaint();
    }

    // Effects: creates a full-width row for one response so the card aligns with
    // the shared content column rather than sizing itself independently.
    private JPanel buildResponseRow(String response, int textWidth) {
        JPanel responseRow = new JPanel(new BorderLayout());
        responseRow.setOpaque(false);
        responseRow.setAlignmentX(LEFT_ALIGNMENT);
        responseRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        responseRow.add(buildResponseCard(response, textWidth), BorderLayout.CENTER);

        int preferredHeight = responseRow.getPreferredSize().height;
        responseRow.setPreferredSize(new Dimension(0, preferredHeight));
        responseRow.setMinimumSize(new Dimension(0, preferredHeight));
        responseRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferredHeight));

        return responseRow;
    }

    // Effects: creates a rectangular response item containing one response's text.
    private JPanel buildResponseCard(String response, int textWidth) {
        JPanel responseCard = new JPanel(new BorderLayout());
        responseCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JTextArea responseText = new JTextArea(response);
        responseText.setLineWrap(true);
        responseText.setWrapStyleWord(true);
        responseText.setEditable(false);
        responseText.setFocusable(false);
        responseText.setOpaque(false);
        responseText.setBorder(null);
        responseText.setFont(new JLabel().getFont());
        responseText.setSize(new Dimension(textWidth, Short.MAX_VALUE));
        responseCard.add(responseText, BorderLayout.CENTER);

        int preferredHeight = responseCard.getPreferredSize().height;
        responseCard.setPreferredSize(new Dimension(0, preferredHeight));
        responseCard.setMinimumSize(new Dimension(0, preferredHeight));
        responseCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferredHeight));

        return responseCard;
    }

    // Effects: returns the pixel height that most closely matches a 1 inch response
    // box on the current display using the screen DPI reported by the operating system.
    private int getResponseBoxHeightPixels() {
        int screenDpi = Toolkit.getDefaultToolkit().getScreenResolution();
        return (int) Math.round(screenDpi * RESPONSE_BOX_HEIGHT_INCHES);
    }

    // Effects: returns the pixel distance that most closely matches a 1 inch gap
    // between the post body and the divider on the current display.
    private int getDividerOffsetPixels() {
        int screenDpi = Toolkit.getDefaultToolkit().getScreenResolution();
        return (int) Math.round(screenDpi * DIVIDER_OFFSET_INCHES);
    }

    // Effects: returns the pixel distance that most closely matches a 1 inch gap
    // between the active response composer buttons and the existing response thread.
    private int getActiveComposerThreadGapPixels() {
        int screenDpi = Toolkit.getDefaultToolkit().getScreenResolution();
        return (int) Math.round(screenDpi * ACTIVE_COMPOSER_THREAD_GAP_INCHES);
    }

    // Effects: returns the pixel margin that most closely matches a 1 inch outer
    // border on the current display.
    private int getOuterMarginPixels() {
        int screenDpi = Toolkit.getDefaultToolkit().getScreenResolution();
        return (int) Math.round(screenDpi * PANEL_MARGIN_INCHES);
    }

    // Effects: returns the post's tags as bracketed labels such as
    // [English] [Finances].
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

}
