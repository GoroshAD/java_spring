package ru.netology.repository;

import ru.netology.exception.NotFoundException;
import ru.netology.model.Post;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class PostRepository {
    private final ConcurrentHashMap<Long, Post> posts = new ConcurrentHashMap<>();
    private final AtomicLong currentId = new AtomicLong(1);

    public List<Post> all() {
        return Collections.unmodifiableList(new ArrayList<>(posts.values()));
    }

    public Optional<Post> getById(long id) {
        return Optional.ofNullable(posts.get(id));
    }

    public Post save(Post post) {
        if (post.getId() == 0) {
            // Create new post
            long newId = currentId.getAndIncrement();
            Post newPost = new Post(newId, post.getContent());
            posts.put(newId, newPost);
            return newPost;
        } else {
            // Update existing post
            return posts.compute(post.getId(), (id, existingPost) -> {
                if (existingPost == null) {
                    // Post not found - create new one with specified ID
                    // Alternatively, you could throw NotFoundException here
                    return post;
                }
                // Update existing post content
                existingPost.setContent(post.getContent());
                return existingPost;
            });
        }
    }

    public void removeById(long id) {
        Post removedPost = posts.remove(id);
        if (removedPost == null) {
            throw new NotFoundException("Post with id " + id + " not found");
        }
    }
}