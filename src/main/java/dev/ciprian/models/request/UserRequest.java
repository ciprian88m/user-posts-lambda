package dev.ciprian.models.request;

import dev.ciprian.models.domain.User;

public class UserRequest extends GenericRequest {

    private User body;

    public User getBody() {
        return body;
    }

    public void setBody(User body) {
        this.body = body;
    }

}
