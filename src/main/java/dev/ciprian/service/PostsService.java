package dev.ciprian.service;

import dev.ciprian.models.domain.Post;
import dev.ciprian.models.response.GenericResponse;
import dev.ciprian.models.response.PostsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

import java.util.*;
import java.util.logging.Logger;

import static dev.ciprian.constants.PostConstants.*;

@Service
public class PostsService {

    private final Logger logger;
    private final DynamoDbClient dynamoDbClient;

    public PostsService(DynamoDbClient dynamoDbClient) {
        this.logger = Logger.getLogger(PostsService.class.getName());
        this.dynamoDbClient = dynamoDbClient;
    }

    public PostsResponse getPosts(String tableName, String sub) {
        var queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("UserId = :sub")
                .expressionAttributeValues(Map.of(":sub", AttributeValue.builder().s(sub).build()))
                .build();

        var response = new PostsResponse(true, HttpStatus.OK.value());

        try {
            var queryResponse = dynamoDbClient.query(queryRequest);
            var posts = mapPosts(queryResponse.items());
            response.setPosts(posts);
        } catch (SdkServiceException exception) {
            logger.warning(exception.getMessage());
            response.setValid(false);
            response.setStatusCode(exception.statusCode());
            response.setErrorMessage(exception.getMessage());
        }

        return response;
    }

    private List<Post> mapPosts(List<Map<String, AttributeValue>> items) {
        return items.stream()
                .map(attributeMap -> {
                    var postTitle = attributeMap.get(POST_TITLE).s();
                    var postBody = attributeMap.get(POST_BODY).s();
                    var allTags = attributeMap.get(POST_TAGS).s();
                    var postTags = (allTags == null || allTags.isBlank()) ? Collections.<String>emptyList() : Arrays.stream(allTags.split(",")).toList();
                    return new Post(postTitle, postBody, postTags);
                })
                .toList();
    }

    public GenericResponse save(String tableName, String userId, Post post) {
        var values = new HashMap<String, AttributeValue>();
        values.put(USER_ID, AttributeValue.builder().s(userId).build());
        values.put(POST_TITLE, AttributeValue.builder().s(post.postTitle()).build());

        var postBody = post.postBody() == null ? "" : post.postBody();
        values.put(POST_BODY, AttributeValue.builder().s(postBody).build());

        var tags = post.postTags() == null ? new ArrayList<String>() : post.postTags();
        var postTags = String.join(",", tags);
        values.put(POST_TAGS, AttributeValue.builder().s(postTags).build());

        var putItemRequest = PutItemRequest.builder()
                .tableName(tableName)
                .item(values)
                .build();

        try {
            dynamoDbClient.putItem(putItemRequest);
            return new GenericResponse(true, HttpStatus.CREATED.value());
        } catch (SdkServiceException exception) {
            logger.warning(exception.getMessage());
            return new GenericResponse(false, exception.statusCode(), exception.getMessage());
        }
    }

    public GenericResponse delete(String tableName, String userId, Post post) {
        var key = Map.of(
                USER_ID, AttributeValue.builder().s(userId).build(),
                POST_TITLE, AttributeValue.builder().s(post.postTitle()).build()
        );

        var deleteItemRequest = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

        try {
            dynamoDbClient.deleteItem(deleteItemRequest);
            return new GenericResponse(true, HttpStatus.NO_CONTENT.value());
        } catch (SdkServiceException exception) {
            logger.warning(exception.getMessage());
            return new GenericResponse(false, exception.statusCode(), exception.getMessage());
        }
    }

}
