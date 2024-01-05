package dev.ciprian.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import static org.springframework.util.StringUtils.hasLength;

@Configuration
@ConfigurationProperties(prefix = "dynamodb")
public class DynamoDbConfig {

    private String region;
    private String tableName;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Bean
    DynamoDbClient dynamoDbClient() {
        var region = hasLength(this.region) ? Region.of(this.region) : Region.EU_CENTRAL_1;
        return DynamoDbClient.builder()
                .region(region)
                .build();
    }

}
