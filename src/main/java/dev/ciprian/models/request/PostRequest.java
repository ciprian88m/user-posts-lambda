package dev.ciprian.models.request;

import dev.ciprian.models.domain.Post;

public class PostRequest extends GenericRequest {

    private Post body;

    public Post getBody() {
        return body;
    }

    public void setBody(Post body) {
        this.body = body;
    }

}