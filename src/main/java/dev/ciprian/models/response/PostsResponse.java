package dev.ciprian.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.ciprian.models.domain.Post;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PostsResponse extends GenericResponse {

    private List<Post> posts;

    public PostsResponse(boolean valid, int statusCode) {
        super(valid, statusCode);
    }

    public PostsResponse(boolean valid, int statusCode, String errorMessage) {
        super(valid, statusCode, errorMessage);
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

}
