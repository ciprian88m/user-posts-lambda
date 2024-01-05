package dev.ciprian.service;

import dev.ciprian.models.domain.User;
import dev.ciprian.models.response.GenericResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminSetUserPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.MessageActionType;

import java.util.logging.Logger;

import static dev.ciprian.constants.UserConstants.EMAIL;

@Service
public class RegisterService {

    private final Logger logger;
    private final CognitoIdentityProviderClient identityProviderClient;

    public RegisterService(CognitoIdentityProviderClient identityProviderClient) {
        this.logger = Logger.getLogger(RegisterService.class.getName());
        this.identityProviderClient = identityProviderClient;
    }

    public GenericResponse register(User user, String userPoolId) {
        var response = createUser(user, userPoolId);

        if (!response.isValid()) {
            return response;
        }

        var setUserPasswordResponse = setUserPassword(user, userPoolId);

        if (!setUserPasswordResponse.isValid()) {
            return setUserPasswordResponse;
        }

        return response;
    }

    private GenericResponse createUser(User user, String userPoolId) {
        try {
            var attributeType = AttributeType.builder()
                    .name(EMAIL)
                    .value(user.email())
                    .build();

            var adminCreateUserRequest = AdminCreateUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(user.username())
                    .userAttributes(attributeType)
                    .messageAction(MessageActionType.SUPPRESS)
                    .build();

            identityProviderClient.adminCreateUser(adminCreateUserRequest);
            return new GenericResponse(true, HttpStatus.CREATED.value());
        } catch (SdkServiceException exception) {
            logger.warning(exception.getMessage());
            return new GenericResponse(false, exception.statusCode(), exception.getMessage());
        }
    }

    private GenericResponse setUserPassword(User user, String userPoolId) {
        try {
            var adminSetUserPasswordResponse = AdminSetUserPasswordRequest.builder()
                    .userPoolId(userPoolId)
                    .username(user.username())
                    .password(user.password())
                    .permanent(true)
                    .build();

            identityProviderClient.adminSetUserPassword(adminSetUserPasswordResponse);
            return new GenericResponse(true, HttpStatus.OK.value());
        } catch (SdkServiceException exception) {
            logger.warning(exception.getMessage());
            return new GenericResponse(false, exception.statusCode(), exception.getMessage());
        }
    }

}
