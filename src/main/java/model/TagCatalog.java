package main.java.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class TagCatalog {
    private static final Map<String, Set<String>> TAGS_BY_CATEGORY = buildTagsByCategory();

    private TagCatalog() {
    }

    // Effects: returns all allowed tags across every display category.
    public static Set<String> getAllTags() {
        Set<String> allTags = new LinkedHashSet<>();

        for (Set<String> categoryTags : TAGS_BY_CATEGORY.values()) {
            allTags.addAll(categoryTags);
        }

        return allTags;
    }

    // Effects: returns the predefined tags grouped by category for use in the UI.
    public static Map<String, Set<String>> getTagsByCategory() {
        Map<String, Set<String>> tagsByCategory = new LinkedHashMap<>();

        for (Map.Entry<String, Set<String>> entry : TAGS_BY_CATEGORY.entrySet()) {
            tagsByCategory.put(entry.getKey(), new LinkedHashSet<>(entry.getValue()));
        }

        return tagsByCategory;
    }

    // Effects: builds the fixed tag categories used throughout the app.
    private static Map<String, Set<String>> buildTagsByCategory() {
        Map<String, Set<String>> tagsByCategory = new LinkedHashMap<>();

        tagsByCategory.put("Language", Set.of(
            "English",
            "Chinese (Simplified + Traditional)",
            "Punjabi",
            "Tagalog",
            "Hindi/Urdu",
            "Korean"
        ));

        tagsByCategory.put("Situation", Set.of(
            "Childcare",
            "Finances",
            "Pregnancy",
            "Mental Health",
            "Physical Health",
            "Relationships",
            "Housing",
            "Safety"
        ));

        tagsByCategory.put("Background", Set.of(
            "Indigenous",
            "LGBTQ2S+",
            "Immigrant"
        ));

        return tagsByCategory;
    }
}
