package main.java.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Post {
    private String title;
    private String body;
    private Set<String> tags;
    private List<String> responses;

    // Creates a post with a title, body, and chosen tags.
    // The tag set may be empty if the user selected no tags.
    public Post(String title, String body, Set<String> tags) {
        this.title = title;
        this.body = body;
        this.tags = new HashSet<>(tags);
        this.responses = new ArrayList<>();
    }

    // Returns the title of the post.
    public String getTitle() {
        return title;
    }

    // Returns the body of the post.
    public String getBody() {
        return body;
    }

    // Returns the tags attached to the post.
    public Set<String> getTags() {
        return tags;
    }

    // Returns the responses that have been added to the post.
    public List<String> getResponses() {
        return new ArrayList<>(responses);
    }

    // Effects: adds a new response to the post.
    public void addResponse(String response) {
        responses.add(response);
    }
}
