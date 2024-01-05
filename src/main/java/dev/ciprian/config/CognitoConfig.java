package dev.ciprian.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

import static org.springframework.util.StringUtils.hasLength;

@Configuration
@ConfigurationProperties(prefix = "cognito")
public class CognitoConfig {

    private String region;
    private String userPoolId;
    private String clientId;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getUserPoolId() {
        return userPoolId;
    }

    public void setUserPoolId(String userPoolId) {
        this.userPoolId = userPoolId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Bean
    CognitoIdentityProviderClient cognitoIdentityProviderClient() {
        var region = hasLength(this.region) ? Region.of(this.region) : Region.EU_CENTRAL_1;
        return CognitoIdentityProviderClient.builder()
                .region(region)
                .build();
    }

}
