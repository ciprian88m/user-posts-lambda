package dev.ciprian.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ciprian.config.DynamoDbConfig;
import dev.ciprian.exceptions.CustomException;
import dev.ciprian.models.request.GenericRequest;
import dev.ciprian.models.request.PostRequest;
import dev.ciprian.models.response.GenericResponse;
import dev.ciprian.models.response.PostsResponse;
import dev.ciprian.service.PostsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.util.function.Function;
import java.util.logging.Logger;

import static dev.ciprian.constants.HeaderConstants.USER_ID_HEADER;
import static org.springframework.util.StringUtils.hasLength;

@Configuration
public class PostsConfig {

    private final Logger logger;
    private final DynamoDbConfig dynamoDbConfig;
    private final ObjectMapper objectMapper;
    private final PostsService postsService;

    public PostsConfig(DynamoDbConfig dynamoDbConfig, ObjectMapper objectMapper, PostsService postsService) {
        this.logger = Logger.getLogger(PostsConfig.class.getName());
        this.dynamoDbConfig = dynamoDbConfig;
        this.objectMapper = objectMapper;
        this.postsService = postsService;
    }

    @Bean
    public Function<String, PostsResponse> getPosts() {
        return request -> {
            try {
                var genericRequest = objectMapper.readValue(request, GenericRequest.class);
                var sub = genericRequest.getHeaders().get(USER_ID_HEADER);

                if (!hasLength(sub)) {
                    throw new CustomException(HttpStatus.FORBIDDEN.value() + " Invalid user id");
                }

                logger.info("Getting posts for sub: " + sub);
                var response = postsService.getPosts(dynamoDbConfig.getTableName(), sub);

                if (!response.isValid()) {
                    throw new CustomException(response.getStatusCode() + " " + response.getErrorMessage());
                }

                return response;
            } catch (JsonProcessingException exception) {
                logger.warning("Could not deserialize request: " + exception.getMessage());
                throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value() + " Could not deserialize request");
            }
        };
    }

    @Bean
    public Function<String, GenericResponse> savePost() {
        return request -> {
            try {
                var postRequest = objectMapper.readValue(request, PostRequest.class);
                var sub = postRequest.getHeaders().get(USER_ID_HEADER);

                validate(sub, postRequest);

                logger.info("Saving post: " + postRequest.getBody().postTitle());
                var response = postsService.save(dynamoDbConfig.getTableName(), sub, postRequest.getBody());

                if (!response.isValid()) {
                    throw new CustomException(response.getStatusCode() + " " + response.getErrorMessage());
                }

                return response;
            } catch (JsonProcessingException exception) {
                logger.warning("Could not deserialize request: " + exception.getMessage());
                throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value() + " Could not deserialize request");
            }
        };
    }

    private void validate(String sub, PostRequest postRequest) {
        if (!hasLength(sub)) {
            throw new CustomException(HttpStatus.FORBIDDEN.value() + " Invalid user id");
        }

        if (postRequest.getBody() == null || !hasLength(postRequest.getBody().postTitle())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value() + " Invalid data");
        }
    }

    @Bean
    public Function<String, GenericResponse> deletePost() {
        return request -> {
            try {
                var postRequest = objectMapper.readValue(request, PostRequest.class);
                var sub = postRequest.getHeaders().get(USER_ID_HEADER);

                validate(sub, postRequest);

                logger.info("Deleting post: " + postRequest.getBody().postTitle());
                var response = postsService.delete(dynamoDbConfig.getTableName(), sub, postRequest.getBody());

                if (!response.isValid()) {
                    throw new CustomException(response.getStatusCode() + " " + response.getErrorMessage());
                }

                return response;
            } catch (JsonProcessingException exception) {
                logger.warning("Could not deserialize request: " + exception.getMessage());
                throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value() + " Could not deserialize request");
            }
        };
    }

}
