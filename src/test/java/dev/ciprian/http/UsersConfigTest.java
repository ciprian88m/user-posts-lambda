package dev.ciprian.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ciprian.config.CognitoConfig;
import dev.ciprian.exceptions.CustomException;
import dev.ciprian.models.response.AccessResponse;
import dev.ciprian.models.response.GenericResponse;
import dev.ciprian.service.LoginService;
import dev.ciprian.service.RegisterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.io.IOException;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {UsersConfig.class, CognitoConfig.class, RegisterService.class, LoginService.class, ObjectMapper.class})
class UsersConfigTest {

    @MockBean
    CognitoIdentityProviderClient cognitoIdentityProviderClient;

    @Autowired
    Function<String, GenericResponse> registerUser;

    @Autowired
    Function<String, AccessResponse> loginUser;

    @Test
    @DisplayName("Context loads fine")
    void test_0() {
    }

    @Test
    @DisplayName("User should be able to register")
    void test_1() throws IOException {
        // given
        when(cognitoIdentityProviderClient.adminCreateUser(any(AdminCreateUserRequest.class))).thenReturn(AdminCreateUserResponse.builder().build());
        when(cognitoIdentityProviderClient.adminSetUserPassword(any(AdminSetUserPasswordRequest.class))).thenReturn(AdminSetUserPasswordResponse.builder().build());
        var fileContent = new ClassPathResource("/requests/register-user.json").getInputStream().readAllBytes();
        var request = new String(fileContent);

        // when
        var response = registerUser.apply(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(201);
        assertThat(response.getErrorMessage()).isBlank();
    }

    @Test
    @DisplayName("Exception is thrown if the AWS call fails for the register user function [1]")
    void test_2() throws IOException {
        // given
        var exception = SdkServiceException.builder().statusCode(500).message("Call failed").build();
        when(cognitoIdentityProviderClient.adminCreateUser(any(AdminCreateUserRequest.class))).thenThrow(exception);
        var fileContent = new ClassPathResource("/requests/register-user.json").getInputStream().readAllBytes();
        var request = new String(fileContent);

        // when
        // then
        assertThatThrownBy(() -> registerUser.apply(request)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> registerUser.apply(request)).hasMessage("500 Call failed");
    }

    @Test
    @DisplayName("Exception is thrown if the AWS call fails for the register user function [2]")
    void test_3() throws IOException {
        // given
        var exception = SdkServiceException.builder().statusCode(500).message("Call failed").build();
        when(cognitoIdentityProviderClient.adminCreateUser(any(AdminCreateUserRequest.class))).thenReturn(AdminCreateUserResponse.builder().build());
        when(cognitoIdentityProviderClient.adminSetUserPassword(any(AdminSetUserPasswordRequest.class))).thenThrow(exception);
        var fileContent = new ClassPathResource("/requests/register-user.json").getInputStream().readAllBytes();
        var request = new String(fileContent);

        // when
        // then
        assertThatThrownBy(() -> registerUser.apply(request)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> registerUser.apply(request)).hasMessage("500 Call failed");
    }

    @ParameterizedTest
    @ValueSource(strings = {"register-user-no-email", "register-user-no-username", "register-user-no-password"})
    @DisplayName("Exception is thrown if the registration request is invalid")
    void test_4(String filename) throws IOException {
        // given
        var fileContent = new ClassPathResource("/requests/%s.json".formatted(filename)).getInputStream().readAllBytes();
        var request = new String(fileContent);

        // when
        // then
        assertThatThrownBy(() -> registerUser.apply(request)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> registerUser.apply(request)).hasMessage("400 Invalid data");
    }

    @Test
    @DisplayName("User should be able to login")
    void test_5() throws IOException {
        // given
        var resultType = AuthenticationResultType.builder().tokenType("Bearer").expiresIn(3600).accessToken("token").refreshToken("token").idToken("token").build();
        var authResponse = AdminInitiateAuthResponse.builder().authenticationResult(resultType).build();
        when(cognitoIdentityProviderClient.adminInitiateAuth(any(AdminInitiateAuthRequest.class))).thenReturn(authResponse);
        var fileContent = new ClassPathResource("/requests/login-user.json").getInputStream().readAllBytes();
        var request = new String(fileContent);

        // when
        var response = loginUser.apply(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresInSeconds()).isEqualTo(3600);
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isNotBlank();
        assertThat(response.getIdToken()).isNotBlank();
        assertThat(response.getErrorMessage()).isBlank();
    }

    @Test
    @DisplayName("Exception is thrown if the AWS call fails for the login user function")
    void test_6() throws IOException {
        // given
        var exception = SdkServiceException.builder().statusCode(500).message("Call failed").build();
        when(cognitoIdentityProviderClient.adminInitiateAuth(any(AdminInitiateAuthRequest.class))).thenThrow(exception);
        var fileContent = new ClassPathResource("/requests/login-user.json").getInputStream().readAllBytes();
        var request = new String(fileContent);

        // when
        // then
        assertThatThrownBy(() -> loginUser.apply(request)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> loginUser.apply(request)).hasMessage("500 Call failed");
    }

    @ParameterizedTest
    @ValueSource(strings = {"login-user-no-email", "login-user-no-username", "login-user-no-password"})
    @DisplayName("Exception is thrown if the login request is invalid")
    void test_7(String filename) throws IOException {
        // given
        var fileContent = new ClassPathResource("/requests/%s.json".formatted(filename)).getInputStream().readAllBytes();
        var request = new String(fileContent);

        // when
        // then
        assertThatThrownBy(() -> loginUser.apply(request)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> loginUser.apply(request)).hasMessage("400 Invalid data");
    }

}