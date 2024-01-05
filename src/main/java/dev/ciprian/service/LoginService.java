package dev.ciprian.service;

import dev.ciprian.models.domain.User;
import dev.ciprian.models.response.AccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;

import java.util.Map;
import java.util.logging.Logger;

import static dev.ciprian.constants.UserConstants.AUTH_PASSWORD;
import static dev.ciprian.constants.UserConstants.AUTH_USERNAME;

@Service
public class LoginService {

    private final Logger logger;
    private final CognitoIdentityProviderClient identityProviderClient;

    public LoginService(CognitoIdentityProviderClient identityProviderClient) {
        this.logger = Logger.getLogger(LoginService.class.getName());
        this.identityProviderClient = identityProviderClient;
    }

    public AccessResponse login(User user, String userPoolId, String clientId) {
        var response = new AccessResponse(true, HttpStatus.OK.value());

        try {
            var authParameters = Map.of(
                    AUTH_USERNAME, user.username(),
                    AUTH_PASSWORD, user.password()
            );

            var adminInitiateAuthRequest = AdminInitiateAuthRequest.builder()
                    .userPoolId(userPoolId)
                    .clientId(clientId)
                    .authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                    .authParameters(authParameters)
                    .build();

            var adminInitiateAuthResponse = identityProviderClient.adminInitiateAuth(adminInitiateAuthRequest);

            var authResult = adminInitiateAuthResponse.authenticationResult();
            response.setTokenType(authResult.tokenType());
            response.setExpiresInSeconds(authResult.expiresIn());
            response.setAccessToken(authResult.accessToken());
            response.setRefreshToken(authResult.refreshToken());
            response.setIdToken(authResult.idToken());
        } catch (SdkServiceException exception) {
            logger.warning(exception.getMessage());
            response.setValid(false);
            response.setStatusCode(exception.statusCode());
            response.setErrorMessage(exception.getMessage());
        }

        return response;
    }

}
