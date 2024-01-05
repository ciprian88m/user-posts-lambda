package dev.ciprian.models.domain;

import java.util.List;

public record Post(String postTitle, String postBody, List<String> postTags) {
}
