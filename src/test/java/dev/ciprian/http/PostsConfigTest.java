package dev.ciprian.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ciprian.config.DynamoDbConfig;
import dev.ciprian.exceptions.CustomException;
import dev.ciprian.models.response.GenericResponse;
import dev.ciprian.models.response.PostsResponse;
import dev.ciprian.service.PostsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

import static dev.ciprian.constants.PostConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {PostsConfig.class, DynamoDbConfig.class, ObjectMapper.class, PostsService.class})
@SuppressWarnings({"unchecked"})
class PostsConfigTest {

    @MockBean
    DynamoDbClient dynamoDbClient;

    @Autowired
    Function<String, PostsResponse> getPosts;

    @Autowired
    @Qualifier("savePost")
    Function<String, GenericResponse> savePost;

    @Autowired
    @Qualifier("deletePost")
    Function<String, GenericResponse> deletePost;

    @Test
    @DisplayName("Context loads fine")
    void test_0() {
    }

    @Test
    @DisplayName("Should get posts if available")
    void test_1() throws IOException {
        // given
        var queryResponse = QueryResponse.builder()
                .items(Map.of(
                        USER_ID, AttributeValue.builder().s("e654ebca-38e0-487a-b609-0284923be582").build(),
                        POST_TITLE, AttributeValue.builder().s("Tests are important").build(),
                        POST_BODY, AttributeValue.builder().s("Lorem ipsum").build(),
                        POST_TAGS, AttributeValue.builder().s("testing,junit").build()
                ))
                .build();
        when(dynamoDbClient.query(any(QueryRequest.class))).thenReturn(queryResponse);
        var fileContent = new ClassPathResource("/requests/get-posts.json").getInputStream().readAllBytes();
        var request = new String(fileContent);

        // when
        var response = getPosts.apply(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getPosts().size()).isEqualTo(1);
        assertThat(response.getPosts().getFirst().postTitle()).isEqualTo("Tests are important");
        assertThat(response.getPosts().getFirst().postBody()).isEqualTo("Lorem ipsum");
        assertThat(response.getPosts().getFirst().postTags().size()).isEqualTo(2);
        assertThat(response.getPosts().getFirst().postTags().get(0)).isEqualTo("testing");
        assertThat(response.getPosts().getFirst().postTags().get(1)).isEqualTo("junit");
    }

    @Test
    @DisplayName("Exception is thrown if sub is missing for the get posts function")
    void test_2() throws IOException {
        // given
        var fileContent = new ClassPathResource("/requests/get-posts-no-sub.json").getInputStream().readAllBytes();
        var request = new String(fileContent);

        // when
        // then
        assertThatThrownBy(() -> getPosts.apply(request)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> getPosts.apply(request)).hasMessage("403 Invalid user id");
    }

    @Test
    @DisplayName("Exception is thrown if the AWS call fails for the get posts function")
    void test_3() throws IOException {
        // given
        var exception = SdkServiceException.builder().statusCode(500).message("Call failed").build();
        when(dynamoDbClient.query(any(QueryRequest.class))).thenThrow(exception);
        var fileContent = new ClassPathResource("/requests/get-posts.json").getInputStream().readAllBytes();
        var request = new String(fileContent);

        // when
        // then
        assertThatThrownBy(() -> getPosts.apply(request)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> getPosts.apply(request)).hasMessage("500 Call failed");
    }

    @Test
    @DisplayName("Should save new post")
    void test_4() throws IOException {
        // given
        when(dynamoDbClient.putItem(any(PutItemRequest.class))).thenReturn(PutItemResponse.builder().build());
        var fileContent = new ClassPathResource("/requests/save-post.json").getInputStream().readAllBytes();
        var request = new String(fileContent);

        // when
        var response = savePost.apply(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(201);
        assertThat(response.getErrorMessage()).isBlank();
    }

    @Test
    @DisplayName("Exception is thrown if sub is missing for the save post function")
    void test_5() throws IOException {
        // given
        var fileContent = new ClassPathResource("/requests/save-post-no-sub.json").getInputStream().readAllBytes();
        var request = new String(fileContent);

        // when
        // then
        assertThatThrownBy(() -> savePost.apply(request)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> savePost.apply(request)).hasMessage("403 Invalid user id");
    }

    @Test
    @DisplayName("Exception is thrown if body is empty for the save post function")
    void test_6() throws IOException {
        // given
        var fileContent = new ClassPathResource("/requests/save-post-empty-body.json").getInputStream().readAllBytes();
        var request = new String(fileContent);

        // when
        // then
        assertThatThrownBy(() -> savePost.apply(request)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> savePost.apply(request)).hasMessage("400 Invalid data");
    }

    @Test
    @DisplayName("Exception is thrown if post title is empty for the save post function")
    void test_7() throws IOException {
        // given
        var fileContent = new ClassPathResource("/requests/save-post-no-title.json").getInputStream().readAllBytes();
        var request = new String(fileContent);

        // when
        // then
        assertThatThrownBy(() -> savePost.apply(request)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> savePost.apply(request)).hasMessage("400 Invalid data");
    }

    @Test
    @DisplayName("Exception is thrown if the AWS call fails for the save post function")
    void test_8() throws IOException {
        // given
        var exception = SdkServiceException.builder().statusCode(500).message("Call failed").build();
        when(dynamoDbClient.putItem(any(PutItemRequest.class))).thenThrow(exception);
        var fileContent = new ClassPathResource("/requests/save-post.json").getInputStream().readAllBytes();
        var request = new String(fileContent);

        // when
        // then
        assertThatThrownBy(() -> savePost.apply(request)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> savePost.apply(request)).hasMessage("500 Call failed");
    }

    @Test
    @DisplayName("Should delete existing post")
    void test_9() throws IOException {
        // given
        when(dynamoDbClient.deleteItem(any(DeleteItemRequest.class))).thenReturn(DeleteItemResponse.builder().build());
        var fileContent = new ClassPathResource("/requests/delete-post.json").getInputStream().readAllBytes();
        var request = new String(fileContent);

        // when
        var response = deletePost.apply(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(204);
        assertThat(response.getErrorMessage()).isBlank();
    }

    @Test
    @DisplayName("Exception is thrown if sub is missing for the delete post function")
    void test_10() throws IOException {
        // given
        var fileContent = new ClassPathResource("/requests/delete-post-no-sub.json").getInputStream().readAllBytes();
        var request = new String(fileContent);

        // when
        // then
        assertThatThrownBy(() -> deletePost.apply(request)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> deletePost.apply(request)).hasMessage("403 Invalid user id");
    }

    @Test
    @DisplayName("Exception is thrown if body is empty for the delete post function")
    void test_11() throws IOException {
        // given
        var fileContent = new ClassPathResource("/requests/delete-post-empty-body.json").getInputStream().readAllBytes();
        var request = new String(fileContent);

        // when
        // then
        assertThatThrownBy(() -> deletePost.apply(request)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> deletePost.apply(request)).hasMessage("400 Invalid data");
    }

    @Test
    @DisplayName("Exception is thrown if the AWS call fails for the delete post function")
    void test_12() throws IOException {
        // given
        var exception = SdkServiceException.builder().statusCode(500).message("Call failed").build();
        when(dynamoDbClient.deleteItem(any(DeleteItemRequest.class))).thenThrow(exception);
        var fileContent = new ClassPathResource("/requests/delete-post.json").getInputStream().readAllBytes();
        var request = new String(fileContent);

        // when
        // then
        assertThatThrownBy(() -> deletePost.apply(request)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> deletePost.apply(request)).hasMessage("500 Call failed");
    }

}