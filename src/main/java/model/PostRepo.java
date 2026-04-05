package main.java.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PostRepo {
    private List<Post> posts;
    private Set<String> allowedTags;

    // Creates a repository with the fixed set of tags users are allowed to choose from.
    public PostRepo(Set<String> allowedTags) {
        this.posts = new ArrayList<>();
        this.allowedTags = new HashSet<>(allowedTags);
    }

    // Adds a post to the repository if all of its tags are valid.
    public void addPost(Post post) {
        if (!allowedTags.containsAll(post.getTags())) {
            throw new IllegalArgumentException("Post contains a tag that is not allowed.");
        }

        posts.add(post);
    }

    // Returns all posts currently stored in the repository.
    public List<Post> getAllPosts() {
        return new ArrayList<>(posts);
    }

    // Returns the fixed set of tags that can be used when creating filters or posts.
    public Set<String> getAllowedTags() {
        return new HashSet<>(allowedTags);
    }

    // Returns every post that contains all of the selected tags.
    // If no tags are selected, the full post list is returned.
    public List<Post> filterByTags(Set<String> selectedTags) {
        if (selectedTags == null || selectedTags.isEmpty()) {
            return getAllPosts();
        }

        List<Post> filteredPosts = new ArrayList<>();

        for (Post post : posts) {
            if (post.getTags().containsAll(selectedTags)) {
                filteredPosts.add(post);
            }
        }

        return filteredPosts;
    }
}
