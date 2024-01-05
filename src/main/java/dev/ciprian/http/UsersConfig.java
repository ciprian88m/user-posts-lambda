package dev.ciprian.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ciprian.config.CognitoConfig;
import dev.ciprian.exceptions.CustomException;
import dev.ciprian.models.domain.User;
import dev.ciprian.models.request.UserRequest;
import dev.ciprian.models.response.AccessResponse;
import dev.ciprian.models.response.GenericResponse;
import dev.ciprian.service.LoginService;
import dev.ciprian.service.RegisterService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

import java.util.function.Function;
import java.util.logging.Logger;

import static org.springframework.util.StringUtils.hasLength;

@Configuration
public class UsersConfig {

    private final Logger logger;
    private final CognitoConfig cognitoConfig;
    private final RegisterService registerService;
    private final LoginService loginService;
    private final ObjectMapper objectMapper;

    public UsersConfig(CognitoConfig cognitoConfig, RegisterService registerService, LoginService loginService, ObjectMapper objectMapper) {
        this.logger = Logger.getLogger(UsersConfig.class.getName());
        this.cognitoConfig = cognitoConfig;
        this.registerService = registerService;
        this.loginService = loginService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public Function<String, GenericResponse> registerUser() {
        return request -> {
            try {
                var userRequest = objectMapper.readValue(request, UserRequest.class);
                validate(userRequest.getBody());

                var response = registerService.register(userRequest.getBody(), cognitoConfig.getUserPoolId());

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
    public Function<String, AccessResponse> loginUser() {
        return request -> {
            try {
                var userRequest = objectMapper.readValue(request, UserRequest.class);
                validate(userRequest.getBody());

                var response = loginService.login(userRequest.getBody(), cognitoConfig.getUserPoolId(), cognitoConfig.getClientId());

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

    private void validate(@NonNull User user) {
        if (!hasLength(user.email()) || !hasLength(user.username()) || !hasLength(user.password())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value() + " Invalid data");
        }
    }

}
